package com.databricks.jdbc.api.impl.volume;

import static com.databricks.jdbc.TestConstants.TEST_CATALOG;
import static com.databricks.jdbc.TestConstants.TEST_SCHEMA;
import static com.databricks.jdbc.common.DatabricksJdbcConstants.VOLUME_OPERATION_STATUS_COLUMN_NAME;
import static com.databricks.jdbc.common.DatabricksJdbcConstants.VOLUME_OPERATION_STATUS_SUCCEEDED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabricksUCVolumeClientTest {

  @Mock(lenient = true)
  private Connection mockConnection;

  @Mock(lenient = true)
  private Statement mockStatement;

  @Mock(lenient = true)
  private ResultSet mockResultSet;

  @Mock(lenient = true)
  private IDatabricksStatementInternal mockDatabricksStatement;

  @Mock(lenient = true)
  private IDatabricksResultSetInternal mockDatabricksResultSet;

  private DatabricksUCVolumeClient client;

  private static final String VOLUME = "test_volume";

  @BeforeEach
  void setup() throws SQLException {
    client = new DatabricksUCVolumeClient(mockConnection);

    // Default mock behavior
    when(mockConnection.createStatement()).thenReturn(mockStatement);
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    when(mockStatement.unwrap(IDatabricksStatementInternal.class))
        .thenReturn(mockDatabricksStatement);
    when(mockResultSet.unwrap(IDatabricksResultSetInternal.class))
        .thenReturn(mockDatabricksResultSet);
  }

  /** Mocks result set with one row and given volume operation status (e.g. SUCCEEDED, FAILED). */
  private void givenVolumeOperationResult(String status) throws SQLException {
    when(mockResultSet.next()).thenReturn(true);
    when(mockResultSet.getString(VOLUME_OPERATION_STATUS_COLUMN_NAME)).thenReturn(status);
  }

  // ========== prefixExists Tests ==========

  @Test
  void should_ReturnFalse_When_PrefixIsEmpty() throws SQLException {
    // Act & Assert
    assertFalse(client.prefixExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, ""));
    verify(mockConnection, never()).createStatement();
  }

  @Test
  void should_ReturnTrue_When_PrefixMatchesFile() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("name")).thenReturn("test_file.txt");

    // Act
    boolean exists = client.prefixExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, "test", true);

    // Assert
    assertTrue(exists);
    verify(mockStatement).executeQuery(contains("LIST"));
  }

  @Test
  void should_ReturnFalse_When_PrefixDoesNotMatch() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("name")).thenReturn("other_file.txt");

    // Act
    boolean exists = client.prefixExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, "test", true);

    // Assert
    assertFalse(exists);
  }

  @ParameterizedTest
  @CsvSource({"TEST,test_file.txt,true", "test,TEST_FILE.TXT,true", "TEST,test,true"})
  void should_MatchCaseInsensitively_When_CaseSensitiveIsFalse(
      String prefix, String fileName, boolean shouldMatch) throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("name")).thenReturn(fileName);

    // Act
    boolean exists = client.prefixExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, prefix, false);

    // Assert
    assertEquals(shouldMatch, exists);
  }

  @Test
  void should_ThrowSQLException_When_QueryFails() throws SQLException {
    // Arrange
    when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Query failed"));

    // Act & Assert
    assertThrows(
        SQLException.class,
        () -> client.prefixExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, "test", true));
  }

  // ========== objectExists Tests ==========

  @Test
  void should_ReturnFalse_When_ObjectPathIsEmpty() throws SQLException {
    // Act & Assert
    assertFalse(client.objectExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, ""));
    verify(mockConnection, never()).createStatement();
  }

  @Test
  void should_ReturnTrue_When_ObjectExists() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("name")).thenReturn("file.txt");

    // Act
    boolean exists = client.objectExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt", true);

    // Assert
    assertTrue(exists);
  }

  @Test
  void should_ReturnFalse_When_ObjectDoesNotExist() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(false);

    // Act
    boolean exists = client.objectExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt", true);

    // Assert
    assertFalse(exists);
  }

  @Test
  void should_HandleFolderPath_When_CheckingObjectExists() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("name")).thenReturn("file.txt");

    // Act
    boolean exists =
        client.objectExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, "folder/file.txt", true);

    // Assert
    assertTrue(exists);
    verify(mockStatement).executeQuery(contains("folder"));
  }

  // ========== volumeExists Tests ==========

  @Test
  void should_ReturnFalse_When_VolumeNameIsEmpty() throws SQLException {
    // Act & Assert
    assertFalse(client.volumeExists(TEST_CATALOG, TEST_SCHEMA, ""));
    verify(mockConnection, never()).createStatement();
  }

  @Test
  void should_ReturnTrue_When_VolumeExists() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("volume_name")).thenReturn(VOLUME);

    // Act
    boolean exists = client.volumeExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, true);

    // Assert
    assertTrue(exists);
    verify(mockStatement).executeQuery(contains("SHOW VOLUMES"));
  }

  @Test
  void should_ReturnFalse_When_VolumeDoesNotExist() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("volume_name")).thenReturn("other_volume");

    // Act
    boolean exists = client.volumeExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, true);

    // Assert
    assertFalse(exists);
  }

  @Test
  void should_MatchCaseInsensitively_When_CheckingVolumeExists() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("volume_name")).thenReturn("TEST_VOLUME");

    // Act
    boolean exists = client.volumeExists(TEST_CATALOG, TEST_SCHEMA, "test_volume", false);

    // Assert
    assertTrue(exists);
  }

  // ========== listObjects Tests ==========

  @Test
  void should_ReturnEmptyList_When_NoObjectsMatch() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(false);

    // Act
    List<String> objects = client.listObjects(TEST_CATALOG, TEST_SCHEMA, VOLUME, "test", true);

    // Assert
    assertTrue(objects.isEmpty());
  }

  @Test
  void should_ReturnMatchingObjects_When_PrefixMatches() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("name")).thenReturn("test_file1.txt", "test_file2.txt");

    // Act
    List<String> objects = client.listObjects(TEST_CATALOG, TEST_SCHEMA, VOLUME, "test", true);

    // Assert
    assertEquals(2, objects.size());
    assertEquals(Arrays.asList("test_file1.txt", "test_file2.txt"), objects);
  }

  @Test
  void should_FilterNonMatchingObjects_When_ListingWithPrefix() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("name")).thenReturn("test_file.txt", "other_file.txt");

    // Act
    List<String> objects = client.listObjects(TEST_CATALOG, TEST_SCHEMA, VOLUME, "test", true);

    // Assert
    assertEquals(1, objects.size());
    assertEquals("test_file.txt", objects.get(0));
  }

  @Test
  void should_HandleFolderPrefix_When_Listing() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("name")).thenReturn("file.txt");

    // Act
    List<String> objects = client.listObjects(TEST_CATALOG, TEST_SCHEMA, VOLUME, "folder/", true);

    // Assert
    assertEquals(1, objects.size());
    verify(mockStatement).executeQuery(contains("folder"));
  }

  // ========== getObject (file) Tests ==========

  @ParameterizedTest
  @CsvSource({"SUCCEEDED,true", "FAILED,false"})
  void should_ReturnExpectedResult_When_GetObjectReturnsStatus(
      String status, boolean expectedSuccess) throws SQLException {
    // Arrange
    givenVolumeOperationResult(status);

    // Act
    boolean success =
        client.getObject(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt", "/tmp/local.txt");

    // Assert
    assertEquals(expectedSuccess, success);
    if (expectedSuccess) {
      verify(mockStatement).executeQuery(contains("GET"));
    }
  }

  @Test
  void should_ThrowSQLException_When_GetObjectQueryFails() throws SQLException {
    when(mockStatement.executeQuery(anyString())).thenThrow(new SQLException("Query failed"));
    assertThrows(
        SQLException.class,
        () -> client.getObject(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt", "/tmp/local.txt"));
  }

  // ========== getObject (InputStream) Tests ==========

  @Test
  void should_ReturnInputStream_When_GetObjectWithStreamSucceeds() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true);
    InputStreamEntity expectedEntity =
        new InputStreamEntity(new ByteArrayInputStream("test".getBytes()), 4);
    when(mockDatabricksResultSet.getVolumeOperationInputStream()).thenReturn(expectedEntity);

    // Act
    InputStreamEntity result = client.getObject(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt");

    // Assert
    assertNotNull(result);
    verify(mockDatabricksStatement).allowInputStreamForVolumeOperation(true);
  }

  @Test
  void should_ReturnNull_When_NoResultForInputStream() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(false);

    // Act
    InputStreamEntity result = client.getObject(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt");

    // Assert
    assertNull(result);
  }

  // ========== putObject (file) Tests ==========

  @ParameterizedTest
  @CsvSource({"SUCCEEDED,true", "FAILED,false"})
  void should_ReturnExpectedResult_When_PutObjectFileReturnsStatus(
      String status, boolean expectedSuccess) throws SQLException {
    givenVolumeOperationResult(status);
    boolean success =
        client.putObject(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt", "/tmp/local.txt", false);
    assertEquals(expectedSuccess, success);
    if (expectedSuccess) {
      verify(mockStatement)
          .executeQuery(argThat(query -> query.contains("PUT") && !query.contains("OVERWRITE")));
    }
  }

  @Test
  void should_IncludeOverwrite_When_ToOverwriteIsTrue() throws SQLException {
    givenVolumeOperationResult(VOLUME_OPERATION_STATUS_SUCCEEDED);
    boolean success =
        client.putObject(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt", "/tmp/local.txt", true);
    assertTrue(success);
    verify(mockStatement).executeQuery(contains("OVERWRITE"));
  }

  // ========== putObject (InputStream) Tests ==========

  @Test
  void should_ReturnTrue_When_PutObjectWithStreamSucceeds() throws SQLException {
    givenVolumeOperationResult(VOLUME_OPERATION_STATUS_SUCCEEDED);
    InputStream stream = new ByteArrayInputStream("test".getBytes());
    boolean success =
        client.putObject(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt", stream, 4L, false);
    assertTrue(success);
    verify(mockDatabricksStatement).allowInputStreamForVolumeOperation(true);
    verify(mockDatabricksStatement).setInputStreamForUCVolume(any(InputStreamEntity.class));
  }

  @Test
  void should_SetCorrectContentLength_When_PuttingStream() throws SQLException {
    givenVolumeOperationResult(VOLUME_OPERATION_STATUS_SUCCEEDED);
    InputStream stream = new ByteArrayInputStream("test data".getBytes());
    long contentLength = 9L;
    client.putObject(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt", stream, contentLength, false);
    verify(mockDatabricksStatement)
        .setInputStreamForUCVolume(argThat(entity -> entity.getContentLength() == contentLength));
  }

  // ========== deleteObject Tests ==========

  @ParameterizedTest
  @CsvSource({"SUCCEEDED, true", "FAILED, false"})
  void should_ReturnExpectedResult_When_DeleteObjectByStatus(String status, boolean expectedSuccess)
      throws SQLException {
    // Arrange
    givenVolumeOperationResult(status);

    // Act
    boolean success = client.deleteObject(TEST_CATALOG, TEST_SCHEMA, VOLUME, "file.txt");

    // Assert
    assertEquals(expectedSuccess, success);
    if (expectedSuccess) {
      verify(mockStatement).executeQuery(contains("REMOVE"));
    }
  }

  // ========== putFiles Tests ==========

  @Test
  void should_ThrowUnsupportedException_When_PutFilesWithStreamsIsCalled() {
    // Arrange
    List<String> objectPaths = Arrays.asList("file1.txt", "file2.txt");
    List<InputStream> streams =
        Arrays.asList(
            new ByteArrayInputStream("test1".getBytes()),
            new ByteArrayInputStream("test2".getBytes()));
    List<Long> contentLengths = Arrays.asList(5L, 5L);

    // Act & Assert
    assertThrows(
        DatabricksSQLFeatureNotSupportedException.class,
        () ->
            client.putFiles(
                TEST_CATALOG, TEST_SCHEMA, VOLUME, objectPaths, streams, contentLengths, false));
  }

  @Test
  void should_ThrowUnsupportedException_When_PutFilesWithLocalPathsIsCalled() {
    // Arrange
    List<String> objectPaths = Arrays.asList("file1.txt", "file2.txt");
    List<String> localPaths = Arrays.asList("/tmp/file1.txt", "/tmp/file2.txt");

    // Act & Assert
    assertThrows(
        DatabricksSQLFeatureNotSupportedException.class,
        () -> client.putFiles(TEST_CATALOG, TEST_SCHEMA, VOLUME, objectPaths, localPaths, false));
  }

  // ========== Helper method Tests ==========

  @Test
  void should_EscapeStringLiterals_When_BuildingQueries() throws SQLException {
    // Arrange - Test SQL injection prevention: single quote in literal must become ''
    String maliciousVolume = "test'; DROP TABLE users--";
    when(mockResultSet.next()).thenReturn(false);

    // Act
    client.listObjects(TEST_CATALOG, TEST_SCHEMA, maliciousVolume, "test", true);

    // Assert - Verify the query contains the escaped literal ('' not ') so injection is prevented
    ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
    verify(mockStatement).executeQuery(queryCaptor.capture());
    String executedQuery = queryCaptor.getValue();
    assertTrue(
        executedQuery.contains("test''"),
        "Query should contain escaped single quote (''): " + executedQuery);
    assertFalse(
        executedQuery.contains("test';"),
        "Query must not contain unescaped quote that could allow SQL injection: " + executedQuery);
  }

  @Test
  void should_UseCaseSensitiveDefault_When_CaseSensitivityNotSpecified() throws SQLException {
    // Arrange
    when(mockResultSet.next()).thenReturn(true).thenReturn(false);
    when(mockResultSet.getString("name")).thenReturn("Test.txt");

    // Act
    boolean exists = client.prefixExists(TEST_CATALOG, TEST_SCHEMA, VOLUME, "test");

    // Assert - Should not match with different case (case-sensitive by default)
    assertFalse(exists);
  }
}
