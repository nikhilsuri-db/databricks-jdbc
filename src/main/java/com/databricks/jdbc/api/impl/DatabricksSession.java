package com.databricks.jdbc.api.impl;

import static com.databricks.jdbc.common.DatabricksJdbcConstants.INVALID_SESSION_STATE_MSG;
import static com.databricks.jdbc.common.DatabricksJdbcConstants.REDACTED_TOKEN;

import com.databricks.jdbc.api.internal.IDatabricksConnectionContext;
import com.databricks.jdbc.api.internal.IDatabricksSession;
import com.databricks.jdbc.common.CompressionCodec;
import com.databricks.jdbc.common.DatabricksClientType;
import com.databricks.jdbc.common.DatabricksJdbcUrlParams;
import com.databricks.jdbc.common.IDatabricksComputeResource;
import com.databricks.jdbc.dbclient.IDatabricksClient;
import com.databricks.jdbc.dbclient.IDatabricksMetadataClient;
import com.databricks.jdbc.dbclient.impl.sqlexec.DatabricksEmptyMetadataClient;
import com.databricks.jdbc.dbclient.impl.sqlexec.DatabricksMetadataSdkClient;
import com.databricks.jdbc.dbclient.impl.sqlexec.DatabricksSdkClient;
import com.databricks.jdbc.dbclient.impl.thrift.DatabricksThriftServiceClient;
import com.databricks.jdbc.exception.DatabricksHttpException;
import com.databricks.jdbc.exception.DatabricksSQLException;
import com.databricks.jdbc.exception.DatabricksTemporaryRedirectException;
import com.databricks.jdbc.log.JdbcLogger;
import com.databricks.jdbc.log.JdbcLoggerFactory;
import com.databricks.jdbc.telemetry.TelemetryHelper;
import com.databricks.jdbc.telemetry.latency.DatabricksMetricsTimedProcessor;
import com.databricks.sdk.support.ToStringer;
import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Implementation of {@link IDatabricksSession}, which maintains an underlying session in SQL
 * Gateway.
 */
public class DatabricksSession implements IDatabricksSession {

  private static final JdbcLogger LOGGER = JdbcLoggerFactory.getLogger(DatabricksSession.class);
  private IDatabricksClient databricksClient;
  private IDatabricksMetadataClient databricksMetadataClient;
  private final IDatabricksComputeResource computeResource;
  private boolean isSessionOpen;
  private ImmutableSessionInfo sessionInfo;

  /** For context based commands */
  private String catalog;

  private String schema;
  private final Map<String, String> sessionConfigs;
  private final Map<String, String> clientInfoProperties;
  private final CompressionCodec compressionCodec;
  private final IDatabricksConnectionContext connectionContext;

  /**
   * Creates an instance of Databricks session for given connection context
   *
   * @param connectionContext underlying connection context
   */
  public DatabricksSession(IDatabricksConnectionContext connectionContext)
      throws DatabricksSQLException {
    if (connectionContext.getClientType() == DatabricksClientType.THRIFT) {
      this.databricksClient =
          DatabricksMetricsTimedProcessor.createProxy(
              new DatabricksThriftServiceClient(connectionContext));
    } else {
      this.databricksClient =
          DatabricksMetricsTimedProcessor.createProxy(new DatabricksSdkClient(connectionContext));
      this.databricksMetadataClient =
          DatabricksMetricsTimedProcessor.createProxy(
              new DatabricksMetadataSdkClient(databricksClient));
    }
    this.isSessionOpen = false;
    this.sessionInfo = null;
    this.computeResource = connectionContext.getComputeResource();
    this.catalog = connectionContext.getCatalog();
    this.schema = connectionContext.getSchema();
    this.sessionConfigs = connectionContext.getSessionConfigs();
    this.clientInfoProperties = connectionContext.getClientInfoProperties();
    this.compressionCodec = connectionContext.getCompressionCodec();
    this.connectionContext = connectionContext;
  }

  /** Constructor method to be used for mocking in a test case. */
  @VisibleForTesting
  public DatabricksSession(
      IDatabricksConnectionContext connectionContext, IDatabricksClient testDatabricksClient) {
    this.databricksClient = testDatabricksClient;
    if (databricksClient instanceof DatabricksSdkClient) {
      this.databricksMetadataClient = new DatabricksMetadataSdkClient(databricksClient);
    }
    this.isSessionOpen = false;
    this.sessionInfo = null;
    this.computeResource = connectionContext.getComputeResource();
    this.catalog = connectionContext.getCatalog();
    this.schema = connectionContext.getSchema();
    this.sessionConfigs = connectionContext.getSessionConfigs();
    this.clientInfoProperties = connectionContext.getClientInfoProperties();
    this.compressionCodec = connectionContext.getCompressionCodec();
    this.connectionContext = connectionContext;
  }

  @Nullable
  @Override
  public String getSessionId() {
    LOGGER.debug("public String getSessionId()");
    return (isSessionOpen) ? sessionInfo.sessionId() : null;
  }

  @Override
  @Nullable
  public ImmutableSessionInfo getSessionInfo() {
    LOGGER.debug("public String getSessionInfo()");
    return sessionInfo;
  }

  @Override
  public IDatabricksComputeResource getComputeResource() {
    LOGGER.debug("public String getComputeResource()");
    return this.computeResource;
  }

  @Override
  public CompressionCodec getCompressionCodec() {
    LOGGER.debug("public String getCompressionType()");
    return compressionCodec;
  }

  @Override
  public boolean isOpen() {
    LOGGER.debug("public boolean isOpen()");
    // TODO (PECO-1949): Check for expired sessions
    return isSessionOpen;
  }

  @Override
  public void open() throws DatabricksSQLException {
    LOGGER.debug("public void open()");
    synchronized (this) {
      if (!isSessionOpen) {
        try {
          this.sessionInfo =
              databricksClient.createSession(
                  this.computeResource, this.catalog, this.schema, this.sessionConfigs);
        } catch (DatabricksTemporaryRedirectException e) {
          this.connectionContext.setClientType(DatabricksClientType.THRIFT);
          this.databricksClient =
              DatabricksMetricsTimedProcessor.createProxy(
                  new DatabricksThriftServiceClient(connectionContext));
          this.sessionInfo =
              this.databricksClient.createSession(
                  this.computeResource, this.catalog, this.schema, this.sessionConfigs);
        }
        this.isSessionOpen = true;
      }
    }
  }

  @Override
  public void close() throws DatabricksSQLException {
    LOGGER.debug("public void close()");
    synchronized (this) {
      if (isSessionOpen) {
        try {
          databricksClient.deleteSession(sessionInfo);
        } catch (DatabricksHttpException e) {
          if (e.getMessage() != null
              && e.getMessage().toLowerCase().contains(INVALID_SESSION_STATE_MSG)) {
            LOGGER.warn(
                "Session [{}] already expired/invalid on server – ignoring during close()",
                sessionInfo.sessionId());
          } else {
            throw e;
          }
        } finally {
          // Always clean up local state
          this.sessionInfo = null;
          this.isSessionOpen = false;
        }
      }
    }
  }

  @Override
  public IDatabricksClient getDatabricksClient() {
    LOGGER.debug("public IDatabricksClient getDatabricksClient()");
    return databricksClient;
  }

  @Override
  public IDatabricksMetadataClient getDatabricksMetadataClient() {
    LOGGER.debug("public IDatabricksClient getDatabricksMetadataClient()");
    if (this.connectionContext.getClientType() == DatabricksClientType.THRIFT) {
      return (IDatabricksMetadataClient) databricksClient;
    }
    return databricksMetadataClient;
  }

  @Override
  public String getCatalog() {
    LOGGER.debug("public String getCatalog()");
    return catalog;
  }

  @Override
  public void setCatalog(String catalog) {
    LOGGER.debug("public void setCatalog(String catalog = {})", catalog);
    this.catalog = catalog;
  }

  @Override
  public String getSchema() {
    LOGGER.debug("public String getSchema()");
    return schema;
  }

  @Override
  public void setSchema(String schema) {
    LOGGER.debug("public void setSchema(String schema = {})", schema);
    this.schema = schema;
  }

  @Override
  public String toString() {
    return (new ToStringer(DatabricksSession.class))
        .add("compute", this.computeResource.toString())
        .add("catalog", this.catalog)
        .add("schema", this.schema)
        .add("sessionID", this.getSessionId())
        .toString();
  }

  @Override
  public Map<String, String> getSessionConfigs() {
    LOGGER.debug("public Map<String, String> getSessionConfigs()");
    return sessionConfigs;
  }

  @Override
  public void setSessionConfig(String name, String value) {
    LOGGER.debug("public void setSessionConfig(String name = {}, String value = {})", name, value);
    sessionConfigs.put(name, value);
  }

  @Override
  public Map<String, String> getClientInfoProperties() {
    LOGGER.debug("public Map<String, String> getClientInfoProperties()");
    return clientInfoProperties;
  }

  @Override
  public String getConfigValue(String name) {
    LOGGER.debug("public String getConfigValue(String name = {})", name);
    return sessionConfigs.getOrDefault(
        name.toLowerCase(), clientInfoProperties.getOrDefault(name.toLowerCase(), null));
  }

  @Override
  public void setClientInfoProperty(String name, String value) {
    LOGGER.debug(
        String.format(
            "public void setClientInfoProperty(String name = {%s}, String value = {%s})",
            name, value));
    if (name.equalsIgnoreCase(DatabricksJdbcUrlParams.AUTH_ACCESS_TOKEN.getParamName())) {
      // refresh the access token if provided a new value in client info
      this.databricksClient.resetAccessToken(value);
      value = REDACTED_TOKEN; // mask access token
    }

    // If application name is being set, update both telemetry and user agent
    if (name.equalsIgnoreCase(DatabricksJdbcUrlParams.APPLICATION_NAME.getParamName())) {
      TelemetryHelper.updateTelemetryAppName(connectionContext, value);
    }

    clientInfoProperties.put(name, value);
  }

  @Override
  public IDatabricksConnectionContext getConnectionContext() {
    return this.connectionContext;
  }

  @Override
  public void setEmptyMetadataClient() {
    databricksMetadataClient = new DatabricksEmptyMetadataClient(connectionContext);
  }

  @Override
  public void forceClose() {
    try {
      this.close();
    } catch (DatabricksSQLException e) {
      LOGGER.error("Error closing session resources, but marking the session as closed.");
    } finally {
      this.isSessionOpen = false;
    }
  }
}
