package com.databricks.jdbc.api.impl.volume;

import static org.junit.jupiter.api.Assertions.*;

import com.databricks.jdbc.api.IDatabricksVolumeClient;
import java.sql.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabricksVolumeClientFactoryTest {

  @Mock private Connection mockConnection;

  @Test
  void should_ReturnDatabricksUCVolumeClient_When_CalledWithConnection() {
    // Act
    IDatabricksVolumeClient client = DatabricksVolumeClientFactory.getVolumeClient(mockConnection);

    // Assert
    assertNotNull(client);
    assertTrue(client instanceof DatabricksUCVolumeClient);
  }

  @Test
  void should_CreateNewInstance_When_CalledMultipleTimesWithConnection() {
    // Act
    IDatabricksVolumeClient client1 = DatabricksVolumeClientFactory.getVolumeClient(mockConnection);
    IDatabricksVolumeClient client2 = DatabricksVolumeClientFactory.getVolumeClient(mockConnection);

    // Assert
    assertNotNull(client1);
    assertNotNull(client2);
    assertNotSame(client2, client1);
  }

  @Test
  void should_ThrowException_When_ConnectionIsNull() {
    // Act & Assert - Null connection is invalid; factory must reject it
    assertThrows(
        IllegalArgumentException.class,
        () -> DatabricksVolumeClientFactory.getVolumeClient((Connection) null));
  }
}
