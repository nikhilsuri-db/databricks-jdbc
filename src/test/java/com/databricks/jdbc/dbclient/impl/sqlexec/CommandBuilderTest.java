package com.databricks.jdbc.dbclient.impl.sqlexec;

import static com.databricks.jdbc.dbclient.impl.common.CommandConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.databricks.jdbc.api.internal.IDatabricksSession;
import com.databricks.jdbc.common.util.WildcardUtil;
import com.databricks.jdbc.exception.DatabricksValidationException;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommandBuilderTest {

  @Mock private IDatabricksSession mockSession;

  private static final String TEST_CATALOG = "test_catalog";
  private static final String TEST_SCHEMA = "test_schema";
  private static final String TEST_TABLE = "test_table";
  private static final String TEST_SESSION_CONTEXT = "test_session_context";

  @BeforeEach
  void setUp() {
    when(mockSession.toString()).thenReturn(TEST_SESSION_CONTEXT);
  }

  @Nested
  @DisplayName("Tests for LIST_PRIMARY_KEYS command")
  class ListPrimaryKeysTests {

    @Test
    @DisplayName("Should generate correct SQL for fetching primary keys")
    void shouldGenerateCorrectSqlForPrimaryKeys() throws SQLException {
      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession).setSchema(TEST_SCHEMA).setTable(TEST_TABLE);

      String sql = builder.getSQLString(CommandName.LIST_PRIMARY_KEYS);

      String expectedSql =
          String.format(SHOW_PRIMARY_KEYS_SQL, TEST_CATALOG, TEST_SCHEMA, TEST_TABLE);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should throw SQLException when catalog is null for primary keys")
    void shouldThrowExceptionWhenCatalogIsNullForPrimaryKeys() {
      CommandBuilder builder =
          new CommandBuilder(null, mockSession).setSchema(TEST_SCHEMA).setTable(TEST_TABLE);

      assertThrows(SQLException.class, () -> builder.getSQLString(CommandName.LIST_PRIMARY_KEYS));
    }

    @Test
    @DisplayName("Should throw SQLException when schema is null for primary keys")
    void shouldThrowExceptionWhenSchemaIsNullForPrimaryKeys() {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession).setTable(TEST_TABLE);

      assertThrows(SQLException.class, () -> builder.getSQLString(CommandName.LIST_PRIMARY_KEYS));
    }

    @Test
    @DisplayName("Should throw SQLException when table is null for primary keys")
    void shouldThrowExceptionWhenTableIsNullForPrimaryKeys() {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession).setSchema(TEST_SCHEMA);

      assertThrows(SQLException.class, () -> builder.getSQLString(CommandName.LIST_PRIMARY_KEYS));
    }
  }

  @Nested
  @DisplayName("Tests for LIST_TABLES command")
  class ListTablesTests {

    @Test
    @DisplayName("Should generate correct SQL for fetching tables with catalog")
    void shouldGenerateCorrectSqlForTablesWithCatalog() throws SQLException {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession);

      String sql = builder.getSQLString(CommandName.LIST_TABLES);

      String expectedSql = String.format(SHOW_TABLES_SQL, TEST_CATALOG);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching tables with catalog and schema pattern")
    void shouldGenerateCorrectSqlForTablesWithCatalogAndSchemaPattern() throws SQLException {
      String schemaPattern = "test_schema%";
      String hiveSchemaPattern = WildcardUtil.jdbcPatternToHive(schemaPattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession).setSchemaPattern(schemaPattern);

      String sql = builder.getSQLString(CommandName.LIST_TABLES);

      String expectedSql =
          String.format(SHOW_TABLES_SQL.concat(SCHEMA_LIKE_SQL), TEST_CATALOG, hiveSchemaPattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName(
        "Should generate correct SQL for fetching tables with catalog, schema pattern, and table pattern")
    void shouldGenerateCorrectSqlForTablesWithCatalogSchemaAndTablePattern() throws SQLException {
      String schemaPattern = "test_schema%";
      String tablePattern = "test_table%";
      String hiveSchemaPattern = WildcardUtil.jdbcPatternToHive(schemaPattern);
      String hiveTablePattern = WildcardUtil.jdbcPatternToHive(tablePattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession)
              .setSchemaPattern(schemaPattern)
              .setTablePattern(tablePattern);

      String sql = builder.getSQLString(CommandName.LIST_TABLES);

      String expectedSql =
          String.format(
              SHOW_TABLES_SQL.concat(SCHEMA_LIKE_SQL).concat(LIKE_SQL),
              TEST_CATALOG,
              hiveSchemaPattern,
              hiveTablePattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching tables from all catalogs")
    void shouldGenerateCorrectSqlForTablesFromAllCatalogs() throws SQLException {
      CommandBuilder builder = new CommandBuilder(null, mockSession);

      String sql = builder.getSQLString(CommandName.LIST_TABLES);

      assertEquals(SHOW_TABLES_IN_ALL_CATALOGS_SQL, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching tables with wildcard catalog")
    void shouldGenerateCorrectSqlForTablesWithWildcardCatalog() throws SQLException {
      // Test with '*' wildcard
      CommandBuilder builder1 = new CommandBuilder("*", mockSession);
      String sql1 = builder1.getSQLString(CommandName.LIST_TABLES);
      assertEquals(SHOW_TABLES_IN_ALL_CATALOGS_SQL, sql1);
    }

    @Test
    @DisplayName("Should generate SCHEMA LIKE clause for empty string schema pattern")
    void shouldGenerateSchemaLikeClauseForEmptyStringSchemaPattern() throws SQLException {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession).setSchemaPattern("");

      String sql = builder.getSQLString(CommandName.LIST_TABLES);

      String expectedSql = String.format(SHOW_TABLES_SQL.concat(SCHEMA_LIKE_SQL), TEST_CATALOG, "");
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should NOT generate SCHEMA LIKE clause for null schema pattern")
    void shouldNotGenerateSchemaLikeClauseForNullSchemaPattern() throws SQLException {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession).setSchemaPattern(null);

      String sql = builder.getSQLString(CommandName.LIST_TABLES);

      String expectedSql = String.format(SHOW_TABLES_SQL, TEST_CATALOG);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate LIKE clause for empty string table pattern")
    void shouldGenerateLikeClauseForEmptyStringTablePattern() throws SQLException {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession).setTablePattern("");

      String sql = builder.getSQLString(CommandName.LIST_TABLES);

      String expectedSql = String.format(SHOW_TABLES_SQL.concat(LIKE_SQL), TEST_CATALOG, "");
      assertEquals(expectedSql, sql);
    }
  }

  @Nested
  @DisplayName("Tests for LIST_FOREIGN_KEYS command")
  class ListForeignKeysTests {

    @Test
    @DisplayName("Should generate correct SQL for fetching foreign keys")
    void shouldGenerateCorrectSqlForForeignKeys() throws SQLException {
      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession).setSchema(TEST_SCHEMA).setTable(TEST_TABLE);

      String sql = builder.getSQLString(CommandName.LIST_FOREIGN_KEYS);

      String expectedSql =
          String.format(SHOW_FOREIGN_KEYS_SQL, TEST_CATALOG, TEST_SCHEMA, TEST_TABLE);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should throw SQLException when catalog is null for foreign keys")
    void shouldThrowExceptionWhenCatalogIsNullForForeignKeys() {
      CommandBuilder builder =
          new CommandBuilder(null, mockSession).setSchema(TEST_SCHEMA).setTable(TEST_TABLE);

      assertThrows(SQLException.class, () -> builder.getSQLString(CommandName.LIST_FOREIGN_KEYS));
    }

    @Test
    @DisplayName("Should throw SQLException when schema is null for foreign keys")
    void shouldThrowExceptionWhenSchemaIsNullForForeignKeys() {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession).setTable(TEST_TABLE);

      assertThrows(SQLException.class, () -> builder.getSQLString(CommandName.LIST_FOREIGN_KEYS));
    }

    @Test
    @DisplayName("Should throw SQLException when table is null for foreign keys")
    void shouldThrowExceptionWhenTableIsNullForForeignKeys() {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession).setSchema(TEST_SCHEMA);

      assertThrows(SQLException.class, () -> builder.getSQLString(CommandName.LIST_FOREIGN_KEYS));
    }
  }

  @Test
  @DisplayName("Should throw exception for unsupported command")
  void shouldThrowExceptionForUnsupportedCommand() {
    CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession);

    CommandName mockCommand = mock(CommandName.class);

    assertThrows(DatabricksValidationException.class, () -> builder.getSQLString(mockCommand));
  }

  @Nested
  @DisplayName("Tests for LIST_COLUMNS command")
  class ListColumnsTests {

    @Test
    @DisplayName("Should generate correct SQL for fetching columns")
    void shouldGenerateCorrectSqlForColumns() throws SQLException {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession);

      String sql = builder.getSQLString(CommandName.LIST_COLUMNS);

      String expectedSql = String.format(SHOW_COLUMNS_SQL, TEST_CATALOG);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching columns with schema pattern")
    void shouldGenerateCorrectSqlForColumnsWithSchemaPattern() throws SQLException {
      String schemaPattern = "test_schema%";
      String hiveSchemaPattern = WildcardUtil.jdbcPatternToHive(schemaPattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession).setSchemaPattern(schemaPattern);

      String sql = builder.getSQLString(CommandName.LIST_COLUMNS);

      String expectedSql =
          String.format(SHOW_COLUMNS_SQL.concat(SCHEMA_LIKE_SQL), TEST_CATALOG, hiveSchemaPattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching columns with table pattern")
    void shouldGenerateCorrectSqlForColumnsWithTablePattern() throws SQLException {
      String tablePattern = "test_table%";
      String hiveTablePattern = WildcardUtil.jdbcPatternToHive(tablePattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession).setTablePattern(tablePattern);

      String sql = builder.getSQLString(CommandName.LIST_COLUMNS);

      String expectedSql =
          String.format(SHOW_COLUMNS_SQL.concat(TABLE_LIKE_SQL), TEST_CATALOG, hiveTablePattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching columns with column pattern")
    void shouldGenerateCorrectSqlForColumnsWithColumnPattern() throws SQLException {
      String columnPattern = "test_column%";
      String hiveColumnPattern = WildcardUtil.jdbcPatternToHive(columnPattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession).setColumnPattern(columnPattern);

      String sql = builder.getSQLString(CommandName.LIST_COLUMNS);

      String expectedSql =
          String.format(SHOW_COLUMNS_SQL.concat(LIKE_SQL), TEST_CATALOG, hiveColumnPattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching columns with all patterns")
    void shouldGenerateCorrectSqlForColumnsWithAllPatterns() throws SQLException {
      String schemaPattern = "test_schema%";
      String tablePattern = "test_table%";
      String columnPattern = "test_column%";
      String hiveSchemaPattern = WildcardUtil.jdbcPatternToHive(schemaPattern);
      String hiveTablePattern = WildcardUtil.jdbcPatternToHive(tablePattern);
      String hiveColumnPattern = WildcardUtil.jdbcPatternToHive(columnPattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession)
              .setSchemaPattern(schemaPattern)
              .setTablePattern(tablePattern)
              .setColumnPattern(columnPattern);

      String sql = builder.getSQLString(CommandName.LIST_COLUMNS);

      String expectedSql =
          String.format(
              SHOW_COLUMNS_SQL.concat(SCHEMA_LIKE_SQL).concat(TABLE_LIKE_SQL).concat(LIKE_SQL),
              TEST_CATALOG,
              hiveSchemaPattern,
              hiveTablePattern,
              hiveColumnPattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should throw SQLException when catalog is null for columns")
    void shouldThrowExceptionWhenCatalogIsNullForColumns() {
      CommandBuilder builder = new CommandBuilder(null, mockSession);

      assertThrows(SQLException.class, () -> builder.getSQLString(CommandName.LIST_COLUMNS));
    }
  }

  @Nested
  @DisplayName("Tests for LIST_FUNCTIONS command")
  class ListFunctionsTests {

    @Test
    @DisplayName("Should generate correct SQL for fetching functions")
    void shouldGenerateCorrectSqlForFunctions() throws SQLException {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession);

      String sql = builder.getSQLString(CommandName.LIST_FUNCTIONS);

      String expectedSql = String.format(SHOW_FUNCTIONS_SQL, TEST_CATALOG);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching functions with schema pattern")
    void shouldGenerateCorrectSqlForFunctionsWithSchemaPattern() throws SQLException {
      String schemaPattern = "test_schema%";
      String hiveSchemaPattern = WildcardUtil.jdbcPatternToHive(schemaPattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession).setSchemaPattern(schemaPattern);

      String sql = builder.getSQLString(CommandName.LIST_FUNCTIONS);

      String expectedSql =
          String.format(
              SHOW_FUNCTIONS_SQL.concat(SCHEMA_LIKE_SQL), TEST_CATALOG, hiveSchemaPattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching functions with function pattern")
    void shouldGenerateCorrectSqlForFunctionsWithFunctionPattern() throws SQLException {
      String functionPattern = "test_func%";
      String hiveFunctionPattern = WildcardUtil.jdbcPatternToHive(functionPattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession).setFunctionPattern(functionPattern);

      String sql = builder.getSQLString(CommandName.LIST_FUNCTIONS);

      String expectedSql =
          String.format(SHOW_FUNCTIONS_SQL.concat(LIKE_SQL), TEST_CATALOG, hiveFunctionPattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching functions with all patterns")
    void shouldGenerateCorrectSqlForFunctionsWithAllPatterns() throws SQLException {
      String schemaPattern = "test_schema%";
      String functionPattern = "test_func%";
      String hiveSchemaPattern = WildcardUtil.jdbcPatternToHive(schemaPattern);
      String hiveFunctionPattern = WildcardUtil.jdbcPatternToHive(functionPattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession)
              .setSchemaPattern(schemaPattern)
              .setFunctionPattern(functionPattern);

      String sql = builder.getSQLString(CommandName.LIST_FUNCTIONS);

      String expectedSql =
          String.format(
              SHOW_FUNCTIONS_SQL.concat(SCHEMA_LIKE_SQL).concat(LIKE_SQL),
              TEST_CATALOG,
              hiveSchemaPattern,
              hiveFunctionPattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should throw SQLException when catalog is null for functions")
    void shouldThrowExceptionWhenCatalogIsNullForFunctions() {
      CommandBuilder builder = new CommandBuilder(null, mockSession);

      assertThrows(SQLException.class, () -> builder.getSQLString(CommandName.LIST_FUNCTIONS));
    }
  }

  @Nested
  @DisplayName("Tests for LIST_SCHEMAS command")
  class ListSchemasTests {

    @Test
    @DisplayName("Should generate correct SQL for fetching schemas")
    void shouldGenerateCorrectSqlForSchemas() throws SQLException {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession);

      String sql = builder.getSQLString(CommandName.LIST_SCHEMAS);

      String expectedSql = String.format(SHOW_SCHEMAS_IN_CATALOG_SQL, TEST_CATALOG);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching schemas with pattern")
    void shouldGenerateCorrectSqlForSchemasWithPattern() throws SQLException {
      String schemaPattern = "test_schema%";
      String hiveSchemaPattern = WildcardUtil.jdbcPatternToHive(schemaPattern);

      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession).setSchemaPattern(schemaPattern);

      String sql = builder.getSQLString(CommandName.LIST_SCHEMAS);

      String expectedSql =
          String.format(
              SHOW_SCHEMAS_IN_CATALOG_SQL.concat(LIKE_SQL), TEST_CATALOG, hiveSchemaPattern);
      assertEquals(expectedSql, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching schemas from all catalogs")
    void shouldGenerateCorrectSqlForSchemasFromAllCatalogs() throws SQLException {
      CommandBuilder builder = new CommandBuilder(null, mockSession);

      String sql = builder.getSQLString(CommandName.LIST_SCHEMAS);

      assertEquals(SHOW_SCHEMAS_IN_ALL_CATALOGS_SQL, sql);
    }

    @Test
    @DisplayName("Should generate correct SQL for fetching schemas with wildcard catalog")
    void shouldGenerateCorrectSqlForSchemasWithWildcardCatalog() throws SQLException {
      CommandBuilder builder1 = new CommandBuilder("*", mockSession);
      String sql1 = builder1.getSQLString(CommandName.LIST_SCHEMAS);
      assertEquals(SHOW_SCHEMAS_IN_ALL_CATALOGS_SQL, sql1);

      CommandBuilder builder2 = new CommandBuilder("%", mockSession);
      String sql2 = builder2.getSQLString(CommandName.LIST_SCHEMAS);
      assertEquals(SHOW_SCHEMAS_IN_ALL_CATALOGS_SQL, sql2);
    }
  }

  @Nested
  @DisplayName("Tests for LIST_CATALOGS command")
  class ListCatalogsTests {

    @Test
    @DisplayName("Should generate correct SQL for fetching catalogs")
    void shouldGenerateCorrectSqlForCatalogs() throws SQLException {
      CommandBuilder builder = new CommandBuilder(mockSession);

      String sql = builder.getSQLString(CommandName.LIST_CATALOGS);

      assertEquals(SHOW_CATALOGS_SQL, sql);
    }
  }

  @Nested
  @DisplayName("Tests for LIST_TABLE_TYPES command")
  class ListTableTypesTests {

    @Test
    @DisplayName("Should generate correct SQL for fetching table types")
    void shouldGenerateCorrectSqlForTableTypes() throws SQLException {
      CommandBuilder builder = new CommandBuilder(TEST_CATALOG, mockSession);

      String sql = builder.getSQLString(CommandName.LIST_TABLE_TYPES);

      assertEquals(SHOW_TABLE_TYPES_SQL, sql);
    }
  }

  @Nested
  @DisplayName("Tests for pattern conversion")
  class PatternConversionTests {

    @Test
    @DisplayName("Should correctly convert JDBC wildcard patterns to Hive patterns")
    void shouldCorrectlyConvertJdbcPatternsToHivePatterns() throws SQLException {
      // Test underscore conversion
      CommandBuilder builder1 =
          new CommandBuilder(TEST_CATALOG, mockSession).setSchemaPattern("test_schema");
      String sql1 = builder1.getSQLString(CommandName.LIST_TABLES);
      assertTrue(sql1.contains(WildcardUtil.jdbcPatternToHive("test_schema")));

      // Test percent conversion
      CommandBuilder builder2 =
          new CommandBuilder(TEST_CATALOG, mockSession).setTablePattern("test%");
      String sql2 = builder2.getSQLString(CommandName.LIST_TABLES);
      assertTrue(sql2.contains(WildcardUtil.jdbcPatternToHive("test%")));
    }

    @Test
    @DisplayName("Should handle null patterns correctly")
    void shouldHandleNullPatternsCorrectly() throws SQLException {
      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession)
              .setSchemaPattern(null)
              .setTablePattern(null)
              .setColumnPattern(null)
              .setFunctionPattern(null);

      String sql = builder.getSQLString(CommandName.LIST_TABLES);

      // Should not contain any LIKE clauses
      assertFalse(sql.contains("LIKE"));
    }

    @Test
    @DisplayName("Should handle empty string patterns")
    void shouldHandleEmptyStringPatterns() throws SQLException {
      CommandBuilder builder =
          new CommandBuilder(TEST_CATALOG, mockSession)
              .setSchemaPattern("")
              .setTablePattern("")
              .setColumnPattern("");

      String sql = builder.getSQLString(CommandName.LIST_TABLES);

      // Should contain LIKE clauses with empty patterns
      assertTrue(sql.contains("SCHEMA LIKE ''"));
      assertTrue(sql.contains("LIKE ''"));
    }
  }
}
