package com.databricks.jdbc.api.impl.volume;

import static com.databricks.jdbc.common.DatabricksJdbcConstants.VOLUME_OPERATION_STATUS_COLUMN_NAME;
import static com.databricks.jdbc.common.DatabricksJdbcConstants.VOLUME_OPERATION_STATUS_SUCCEEDED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import com.databricks.jdbc.api.internal.IDatabricksResultSetInternal;
import com.databricks.jdbc.api.internal.IDatabricksStatementInternal;
import com.databricks.jdbc.exception.DatabricksSQLFeatureNotSupportedException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import org.apache.http.entity.InputStreamEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabricksUCVolumeClientTest {

  @Mock private Connection mockConnection;
  @Mock private Statement mockStatement;
  @Mock private ResultSet mockResultSet;
  @Mock private IDatabricksStatementInternal mockDatabricksStatement;
  @Mock private IDatabricksResultSetInternal mockDatabricksResultSet;

  private DatabricksUCVolumeClient client;

  @BeforeEach
  void setup() throws SQLException {
    client = new DatabricksUCVolumeClient(mockConnection);
    lenient().when(mockConnection.createStatement()).thenReturn(mockStatement);
  }

  @Test
  void should_ReturnFalse_When_PrefixIsEmpty() throws SQLException {
    boolean result = client.prefixExists("catalog", "schema", "volume", "");

    assertFalse(result);
    verify(mockConnection, never()).createStatement();
  }

  @ParameterizedTest
  @CsvSource({
    "file, file123.txt, true, true",
    "FILE, file123.txt, false, true",
    "file, other.txt, true, false",
    "test, testfile.txt, true, true"
  })
  void should_CheckPrefixMatch_When_CaseSensitivityVaries(
      String prefix, String fileName, boolean caseSensitive, boolean expected) throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getString("name")).thenReturn(fileName);

    boolean result = client.prefixExists("catalog", "schema", "volume", prefix, caseSensitive);

    assertEquals(expected, result);
  }

  @Test
  void should_ReturnTrue_When_PrefixExists() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getString("name")).thenReturn("testfile.txt");

    boolean result = client.prefixExists("catalog", "schema", "volume", "test", true);

    assertTrue(result);
    verify(mockStatement).executeQuery(argThat(query -> query.contains("LIST")));
  }

  @Test
  void should_PropagateException_When_PrefixExistsQueryFails() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Query failed"));

    SQLException ex =
        assertThrows(
            SQLException.class,
            () -> client.prefixExists("catalog", "schema", "volume", "test", true));
    assertTrue(ex.getMessage().contains("Query failed"));
  }

  @Test
  void should_ReturnFalse_When_ObjectPathIsEmpty() throws SQLException {
    boolean result = client.objectExists("catalog", "schema", "volume", "");

    assertFalse(result);
    verify(mockConnection, never()).createStatement();
  }

  @ParameterizedTest
  @CsvSource({
    "dir/file.txt, file.txt, true, true",
    "dir/FILE.txt, file.txt, false, true",
    "dir/file.txt, other.txt, true, false"
  })
  void should_CheckObjectExists_When_CaseSensitivityVaries(
      String objectPath, String fileName, boolean caseSensitive, boolean expected)
      throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getString("name")).thenReturn(fileName);

    boolean result = client.objectExists("catalog", "schema", "volume", objectPath, caseSensitive);

    assertEquals(expected, result);
  }

  @Test
  void should_ReturnTrue_When_ObjectExists() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getString("name")).thenReturn("file.txt");

    boolean result = client.objectExists("catalog", "schema", "volume", "file.txt");

    assertTrue(result);
  }

  @Test
  void should_ReturnFalse_When_VolumeNameIsEmpty() throws SQLException {
    boolean result = client.volumeExists("catalog", "schema", "");

    assertFalse(result);
    verify(mockConnection, never()).createStatement();
  }

  @ParameterizedTest
  @CsvSource({
    "myvolume, myvolume, true, true",
    "MYVOLUME, myvolume, false, true",
    "myvolume, other, true, false"
  })
  void should_CheckVolumeExists_When_CaseSensitivityVaries(
      String volumeName, String resultVolume, boolean caseSensitive, boolean expected)
      throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getString("volume_name")).thenReturn(resultVolume);

    boolean result = client.volumeExists("catalog", "schema", volumeName, caseSensitive);

    assertEquals(expected, result);
  }

  @Test
  void should_ReturnTrue_When_VolumeExists() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getString("volume_name")).thenReturn("myvolume");

    boolean result = client.volumeExists("catalog", "schema", "myvolume");

    assertTrue(result);
    verify(mockStatement).executeQuery(argThat(query -> query.contains("SHOW VOLUMES")));
  }

  @Test
  void should_ReturnFileList_When_ListObjectsWithPrefix() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, true, true, false);
    when(mockResultSet.getString("name")).thenReturn("file1.txt", "file2.txt", "other.txt");

    List<String> result = client.listObjects("catalog", "schema", "volume", "file", true);

    assertEquals(2, result.size());
    assertTrue(result.contains("file1.txt"));
    assertTrue(result.contains("file2.txt"));
  }

  @Test
  void should_ReturnEmptyList_When_NoObjectsMatch() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, false);
    when(mockResultSet.getString("name")).thenReturn("other.txt");

    List<String> result = client.listObjects("catalog", "schema", "volume", "file");

    assertTrue(result.isEmpty());
  }

  @Test
  void should_ReturnAllObjects_When_PrefixIsEmpty() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true, true, false);
    when(mockResultSet.getString("name")).thenReturn("file1.txt", "file2.txt");

    List<String> result = client.listObjects("catalog", "schema", "volume", "");

    assertEquals(2, result.size());
  }

  @Test
  void should_ReturnTrue_When_GetObjectSucceeds() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getString(VOLUME_OPERATION_STATUS_COLUMN_NAME))
        .thenReturn(VOLUME_OPERATION_STATUS_SUCCEEDED);

    boolean result = client.getObject("catalog", "schema", "volume", "file.txt", "/tmp/file.txt");

    assertTrue(result);
    verify(mockStatement).executeQuery(argThat(query -> query.contains("GET")));
  }

  @Test
  void should_ReturnFalse_When_GetObjectFails() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getString(VOLUME_OPERATION_STATUS_COLUMN_NAME)).thenReturn("FAILED");

    boolean result = client.getObject("catalog", "schema", "volume", "file.txt", "/tmp/file.txt");

    assertFalse(result);
  }

  @Test
  void should_PropagateException_When_GetObjectQueryFails() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("GET failed"));

    SQLException ex =
        assertThrows(
            SQLException.class,
            () -> client.getObject("catalog", "schema", "volume", "file.txt", "/tmp/file.txt"));
    assertTrue(ex.getMessage().contains("GET failed"));
  }

  @Test
  void should_ReturnInputStream_When_GetObjectForInputStream() throws SQLException {
    InputStreamEntity mockEntity = mock(InputStreamEntity.class);
    when(mockStatement.unwrap(IDatabricksStatementInternal.class))
        .thenReturn(mockDatabricksStatement);
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.unwrap(IDatabricksResultSetInternal.class))
        .thenReturn(mockDatabricksResultSet);
    when(mockDatabricksResultSet.getVolumeOperationInputStream()).thenReturn(mockEntity);

    InputStreamEntity result = client.getObject("catalog", "schema", "volume", "file.txt");

    assertNotNull(result);
    assertSame(mockEntity, result);
    verify(mockDatabricksStatement).allowInputStreamForVolumeOperation(true);
  }

  @Test
  void should_ReturnNull_When_GetObjectForInputStreamHasNoResults() throws SQLException {
    when(mockStatement.unwrap(IDatabricksStatementInternal.class))
        .thenReturn(mockDatabricksStatement);
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(false);

    InputStreamEntity result = client.getObject("catalog", "schema", "volume", "file.txt");

    assertNull(result);
  }

  @Test
  void should_ReturnTrue_When_PutObjectSucceeds() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getString(VOLUME_OPERATION_STATUS_COLUMN_NAME))
        .thenReturn(VOLUME_OPERATION_STATUS_SUCCEEDED);

    boolean result =
        client.putObject("catalog", "schema", "volume", "file.txt", "/tmp/file.txt", true);

    assertTrue(result);
    verify(mockStatement)
        .executeQuery(argThat(query -> query.contains("PUT") && query.contains("OVERWRITE")));
  }

  @Test
  void should_NotIncludeOverwrite_When_PutObjectWithoutOverwrite() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getString(VOLUME_OPERATION_STATUS_COLUMN_NAME))
        .thenReturn(VOLUME_OPERATION_STATUS_SUCCEEDED);

    boolean result =
        client.putObject("catalog", "schema", "volume", "file.txt", "/tmp/file.txt", false);

    assertTrue(result);
    verify(mockStatement).executeQuery(argThat(query -> !query.contains("OVERWRITE")));
  }

  @Test
  void should_ReturnTrue_When_PutObjectWithInputStreamSucceeds() throws SQLException {
    InputStream inputStream = new ByteArrayInputStream("test data".getBytes());
    when(mockStatement.unwrap(IDatabricksStatementInternal.class))
        .thenReturn(mockDatabricksStatement);
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getString(VOLUME_OPERATION_STATUS_COLUMN_NAME))
        .thenReturn(VOLUME_OPERATION_STATUS_SUCCEEDED);

    boolean result =
        client.putObject("catalog", "schema", "volume", "file.txt", inputStream, 100L, true);

    assertTrue(result);
    verify(mockDatabricksStatement).allowInputStreamForVolumeOperation(true);
    verify(mockDatabricksStatement).setInputStreamForUCVolume(any(InputStreamEntity.class));
  }

  @Test
  void should_PropagateException_When_PutObjectQueryFails() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("PUT failed"));

    SQLException ex =
        assertThrows(
            SQLException.class,
            () ->
                client.putObject("catalog", "schema", "volume", "file.txt", "/tmp/file.txt", true));
    assertTrue(ex.getMessage().contains("PUT failed"));
  }

  @Test
  void should_ReturnTrue_When_DeleteObjectSucceeds() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getString(VOLUME_OPERATION_STATUS_COLUMN_NAME))
        .thenReturn(VOLUME_OPERATION_STATUS_SUCCEEDED);

    boolean result = client.deleteObject("catalog", "schema", "volume", "file.txt");

    assertTrue(result);
    verify(mockStatement).executeQuery(argThat(query -> query.contains("REMOVE")));
  }

  @Test
  void should_ReturnFalse_When_DeleteObjectFails() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getString(VOLUME_OPERATION_STATUS_COLUMN_NAME)).thenReturn("FAILED");

    boolean result = client.deleteObject("catalog", "schema", "volume", "file.txt");

    assertFalse(result);
  }

  @Test
  void should_PropagateException_When_DeleteObjectQueryFails() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("REMOVE failed"));

    SQLException ex =
        assertThrows(
            SQLException.class,
            () -> client.deleteObject("catalog", "schema", "volume", "file.txt"));
    assertTrue(ex.getMessage().contains("REMOVE failed"));
  }

  @Test
  void should_ThrowException_When_PutFilesWithInputStreamsCalled() {
    List<String> objectPaths = Arrays.asList("file1.txt");
    List<InputStream> inputStreams = Arrays.asList(new ByteArrayInputStream(new byte[0]));
    List<Long> contentLengths = Arrays.asList(0L);

    DatabricksSQLFeatureNotSupportedException ex =
        assertThrows(
            DatabricksSQLFeatureNotSupportedException.class,
            () ->
                client.putFiles(
                    "catalog",
                    "schema",
                    "volume",
                    objectPaths,
                    inputStreams,
                    contentLengths,
                    true));
    assertTrue(ex.getMessage().contains("putFiles(...) is not supported"));
  }

  @Test
  void should_ThrowException_When_PutFilesWithLocalPathsCalled() {
    List<String> objectPaths = Arrays.asList("file1.txt");
    List<String> localPaths = Arrays.asList("/tmp/file1.txt");

    DatabricksSQLFeatureNotSupportedException ex =
        assertThrows(
            DatabricksSQLFeatureNotSupportedException.class,
            () -> client.putFiles("catalog", "schema", "volume", objectPaths, localPaths, true));
    assertTrue(ex.getMessage().contains("putFiles(...) is not supported"));
  }

  @Test
  void should_GenerateCorrectPath_When_GetObjectFullPath() {
    String result =
        DatabricksUCVolumeClient.getObjectFullPath("catalog", "schema", "volume", "file.txt");

    assertTrue(result.contains("catalog"));
    assertTrue(result.contains("schema"));
    assertTrue(result.contains("volume"));
    assertTrue(result.contains("file.txt"));
  }

  @Test
  void should_CloseResources_When_ExceptionOccurs() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockResultSet.next()).thenThrow(new SQLException("Unexpected error"));

    assertThrows(
        SQLException.class, () -> client.listObjects("catalog", "schema", "volume", "test"));

    verify(mockResultSet).close();
    verify(mockStatement).close();
  }
}
