package com.databricks.jdbc.api.impl.volume;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.databricks.jdbc.api.impl.VolumeOperationStatus;
import com.databricks.jdbc.api.impl.volume.DBFSVolumeClient.UploadRequest;
import com.databricks.jdbc.api.internal.IDatabricksConnectionContext;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VolumeUploadCallbackTest {

  @Mock private IDatabricksHttpClient mockHttpClient;
  @Mock private IDatabricksConnectionContext mockConnectionContext;
  @Mock private VolumeUploadCallback.UrlGenerator mockUrlGenerator;

  private CompletableFuture<VolumePutResult> uploadFuture;
  private Semaphore semaphore;
  private Function<Integer, Long> retryDelayCalculator;

  @TempDir Path tempDir;

  @BeforeEach
  void setup() {
    uploadFuture = new CompletableFuture<>();
    semaphore = new Semaphore(1);
    retryDelayCalculator = attempt -> 100L; // Short delay for tests
  }

  @ParameterizedTest
  @ValueSource(ints = {200, 201, 299})
  void should_CompleteSuccessfully_When_UploadReturnsSuccessStatusCode(int statusCode) {
    // Arrange
    UploadRequest request = createStreamUploadRequest("test.txt");
    SimpleHttpResponse response = mock(SimpleHttpResponse.class);
    when(response.getCode()).thenReturn(statusCode);

    VolumeUploadCallback callback = createCallback(request);

    // Act
    callback.completed(response);

    // Assert
    assertTrue(uploadFuture.isDone());
    VolumePutResult result = uploadFuture.join();
    assertEquals(statusCode, result.getStatusCode());
    assertEquals(VolumeOperationStatus.SUCCEEDED, result.getStatus());
    assertNull(result.getMessage());
  }

  @Test
  void should_CompleteWithFailure_When_ExceptionIsThrown() {
    // Arrange
    UploadRequest request = createStreamUploadRequest("test.txt");

    VolumeUploadCallback callback = createCallback(request);
    Exception exception = new IOException("Network error");

    // Act
    callback.failed(exception);

    // Assert
    assertTrue(uploadFuture.isDone());
    VolumePutResult result = uploadFuture.join();
    assertEquals(500, result.getStatusCode());
    assertEquals(VolumeOperationStatus.FAILED, result.getStatus());
    assertEquals("Network error", result.getMessage());
  }

  @Test
  void should_CompleteWithAborted_When_UploadIsCancelled() {
    // Arrange
    UploadRequest request = createStreamUploadRequest("test.txt");
    VolumeUploadCallback callback = createCallback(request);

    // Act
    callback.cancelled();

    // Assert
    assertTrue(uploadFuture.isDone());
    VolumePutResult result = uploadFuture.join();
    assertEquals(499, result.getStatusCode());
    assertEquals(VolumeOperationStatus.ABORTED, result.getStatus());
    assertEquals("Upload cancelled", result.getMessage());
  }

  @Test
  void should_HandleFileUpload_When_FilePathProvided() {
    // Arrange - exercises file-path code path (distinct from stream)
    Path testFile = tempDir.resolve("test.txt");
    UploadRequest request = createFileUploadRequest(testFile, "test.txt");
    SimpleHttpResponse response = mock(SimpleHttpResponse.class);
    when(response.getCode()).thenReturn(200);
    VolumeUploadCallback callback = createCallback(request);

    // Act
    callback.completed(response);

    // Assert
    assertTrue(uploadFuture.isDone());
    assertEquals(VolumeOperationStatus.SUCCEEDED, uploadFuture.join().getStatus());
  }

  // Helper methods
  private VolumeUploadCallback createCallback(UploadRequest request) {
    return new VolumeUploadCallback(
        mockHttpClient,
        uploadFuture,
        request,
        semaphore,
        mockUrlGenerator,
        retryDelayCalculator,
        mockConnectionContext);
  }

  private UploadRequest createFileUploadRequest(Path filePath, String objectPath) {
    try {
      Files.writeString(filePath, "test content");
      UploadRequest request = new UploadRequest();
      request.file = filePath;
      request.objectPath = objectPath;
      request.ucVolumePath = "/Volumes/catalog/schema/volume";
      return request;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private UploadRequest createStreamUploadRequest(String objectPath) {
    return createStreamUploadRequest(objectPath, new ByteArrayInputStream("test".getBytes()));
  }

  private UploadRequest createStreamUploadRequest(String objectPath, InputStream stream) {
    UploadRequest request = new UploadRequest();
    request.inputStream = stream;
    request.objectPath = objectPath;
    request.contentLength = 4L;
    request.ucVolumePath = "/Volumes/catalog/schema/volume";
    return request;
  }
}
