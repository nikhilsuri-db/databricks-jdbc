package com.databricks.jdbc.api.impl.volume;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.databricks.jdbc.api.impl.VolumeOperationStatus;
import com.databricks.jdbc.api.impl.volume.DBFSVolumeClient.UploadRequest;
import com.databricks.jdbc.api.internal.IDatabricksConnectionContext;
import com.databricks.jdbc.common.TelemetryLogLevel;
import com.databricks.jdbc.common.util.VolumeRetryUtil;
import com.databricks.jdbc.dbclient.IDatabricksHttpClient;
import com.databricks.jdbc.model.client.filesystem.VolumePutResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VolumeUploadCallbackTest {

  @Mock private IDatabricksHttpClient mockHttpClient;
  @Mock private VolumeUploadCallback.UrlGenerator mockUrlGenerator;
  @Mock private Function<Integer, Long> mockRetryDelayCalculator;
  @Mock private IDatabricksConnectionContext mockConnectionContext;
  @Mock private SimpleHttpResponse mockResponse;

  private CompletableFuture<VolumePutResult> uploadFuture;
  private Semaphore semaphore;
  private UploadRequest uploadRequest;
  @TempDir Path tempDir;

  @BeforeEach
  void setup() throws IOException {
    when(mockConnectionContext.getTelemetryLogLevel()).thenReturn(TelemetryLogLevel.OFF);

    uploadFuture = new CompletableFuture<>();
    semaphore = new Semaphore(1);

    Path testFile = tempDir.resolve("test.txt");
    Files.write(testFile, "test content".getBytes());
    uploadRequest = new UploadRequest();
    uploadRequest.ucVolumePath = "ucVolumePath";
    uploadRequest.objectPath = "objectPath";
    uploadRequest.file = testFile;
  }

  private VolumeUploadCallback createCallback() {
    return new VolumeUploadCallback(
        mockHttpClient,
        uploadFuture,
        uploadRequest,
        semaphore,
        mockUrlGenerator,
        mockRetryDelayCalculator,
        mockConnectionContext);
  }

  @ParameterizedTest
  @CsvSource({"200", "201", "204", "299"})
  void should_CompleteSuccessfully_When_HttpStatusIsSuccess(int statusCode) {
    when(mockResponse.getCode()).thenReturn(statusCode);

    VolumeUploadCallback callback = createCallback();

    callback.completed(mockResponse);

    assertTrue(uploadFuture.isDone());
    VolumePutResult result = uploadFuture.getNow(null);
    assertNotNull(result);
    assertEquals(statusCode, result.getStatusCode());
    assertEquals(VolumeOperationStatus.SUCCEEDED, result.getStatus());
    assertNull(result.getMessage());
  }

  @ParameterizedTest
  @CsvSource({"400", "401", "403", "404"})
  void should_FailPermanently_When_HttpStatusIsNonRetryable(int statusCode) {
    when(mockResponse.getCode()).thenReturn(statusCode);
    when(mockResponse.getReasonPhrase()).thenReturn("Error");

    try (MockedStatic<VolumeRetryUtil> mockedStatic = mockStatic(VolumeRetryUtil.class)) {
      mockedStatic
          .when(() -> VolumeRetryUtil.isRetryableHttpCode(eq(statusCode), any()))
          .thenReturn(false);

      VolumeUploadCallback callback = createCallback();

      callback.completed(mockResponse);

      assertTrue(uploadFuture.isDone());
      VolumePutResult result = uploadFuture.getNow(null);
      assertNotNull(result);
      assertEquals(statusCode, result.getStatusCode());
      assertEquals(VolumeOperationStatus.FAILED, result.getStatus());
      assertNotNull(result.getMessage());
    }
  }

  @Test
  void should_FailPermanently_When_MaxRetriesExceeded() {
    when(mockResponse.getCode()).thenReturn(503);
    when(mockResponse.getReasonPhrase()).thenReturn("Service Unavailable");

    try (MockedStatic<VolumeRetryUtil> mockedStatic = mockStatic(VolumeRetryUtil.class)) {
      mockedStatic.when(() -> VolumeRetryUtil.isRetryableHttpCode(eq(503), any())).thenReturn(true);
      mockedStatic
          .when(() -> VolumeRetryUtil.shouldRetry(anyInt(), anyLong(), any()))
          .thenReturn(false);

      VolumeUploadCallback callback = createCallback();

      callback.completed(mockResponse);

      assertTrue(uploadFuture.isDone());
      VolumePutResult result = uploadFuture.getNow(null);
      assertEquals(VolumeOperationStatus.FAILED, result.getStatus());
    }
  }

  @Test
  void should_FailPermanently_When_ExceptionAndMaxRetriesExceeded() {
    Exception exception = new RuntimeException("Network error");

    try (MockedStatic<VolumeRetryUtil> mockedStatic = mockStatic(VolumeRetryUtil.class)) {
      mockedStatic
          .when(() -> VolumeRetryUtil.shouldRetry(anyInt(), anyLong(), any()))
          .thenReturn(false);

      VolumeUploadCallback callback = createCallback();

      callback.failed(exception);

      assertTrue(uploadFuture.isDone());
      VolumePutResult result = uploadFuture.getNow(null);
      assertEquals(500, result.getStatusCode());
      assertEquals(VolumeOperationStatus.FAILED, result.getStatus());
      assertTrue(result.getMessage().contains("Network error"));
    }
  }

  @Test
  void should_CompleteAsAborted_When_Cancelled() {
    VolumeUploadCallback callback = createCallback();

    callback.cancelled();

    assertTrue(uploadFuture.isDone());
    VolumePutResult result = uploadFuture.getNow(null);
    assertEquals(499, result.getStatusCode());
    assertEquals(VolumeOperationStatus.ABORTED, result.getStatus());
    assertTrue(result.getMessage().contains("cancelled"));
  }

  @Test
  void should_HandleFileUpload_When_RequestIsFile() {
    assertNotNull(uploadRequest.file);
    assertTrue(uploadRequest.isFile());
  }

  @Test
  void should_HandleStreamUpload_When_RequestIsStream() {
    byte[] testData = "test data".getBytes();
    InputStream inputStream = new ByteArrayInputStream(testData);
    UploadRequest streamRequest = new UploadRequest();
    streamRequest.ucVolumePath = "ucVolumePath";
    streamRequest.objectPath = "objectPath";
    streamRequest.inputStream = inputStream;
    streamRequest.contentLength = (long) testData.length;

    assertFalse(streamRequest.isFile());
    assertNotNull(streamRequest.inputStream);
  }
}
