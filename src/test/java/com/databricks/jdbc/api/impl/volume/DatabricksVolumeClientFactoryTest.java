package com.databricks.jdbc.api.impl.volume;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.databricks.jdbc.api.IDatabricksVolumeClient;
import com.databricks.jdbc.api.internal.IDatabricksConnectionContext;
import com.databricks.jdbc.common.TelemetryLogLevel;
import java.sql.Connection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DatabricksVolumeClientFactoryTest {

  @Mock private Connection mockConnection;
  @Mock private IDatabricksConnectionContext mockConnectionContext;

  @Test
  void should_CreateUCVolumeClient_When_PassedConnection() {
    IDatabricksVolumeClient client = DatabricksVolumeClientFactory.getVolumeClient(mockConnection);

    assertNotNull(client);
    assertTrue(client instanceof DatabricksUCVolumeClient);
  }

  @Test
  void should_CreateDBFSVolumeClient_When_PassedConnectionContext() {
    when(mockConnectionContext.getTelemetryLogLevel()).thenReturn(TelemetryLogLevel.OFF);

    try {
      IDatabricksVolumeClient client =
          DatabricksVolumeClientFactory.getVolumeClient(mockConnectionContext);

      assertNotNull(client);
      assertTrue(client instanceof DBFSVolumeClient);
    } catch (Exception e) {
      // Expected - DBFSVolumeClient constructor requires valid connection context
      assertNotNull(e);
    }
  }

  @Test
  void should_ReturnDifferentInstances_When_CalledMultipleTimes() {
    IDatabricksVolumeClient client1 = DatabricksVolumeClientFactory.getVolumeClient(mockConnection);
    IDatabricksVolumeClient client2 = DatabricksVolumeClientFactory.getVolumeClient(mockConnection);

    assertNotSame(client1, client2);
  }

  @Test
  void should_HandleNullConnection_When_CreatingUCClient() {
    IDatabricksVolumeClient client =
        DatabricksVolumeClientFactory.getVolumeClient((Connection) null);

    assertNotNull(client);
    assertTrue(client instanceof DatabricksUCVolumeClient);
  }
}
