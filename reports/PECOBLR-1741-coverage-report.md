# JaCoCo Code Coverage Report - databricks-jdbc

**JIRA Ticket**: PECOBLR-1741
**Repository**: https://github.com/databricks/databricks-jdbc
**Generated**: 2026-02-17
**JaCoCo Version**: 0.8.11

## Overall Project Coverage

- **Instruction Coverage**: 51595/60723 (84.97%)
- **Branch Coverage**: 3653/4793 (76.22%)
- **Line Coverage**: 12525/14676 (85.34%)
- **Method Coverage**: 2598/2882 (90.15%)

## Package Rankings by Line Coverage

| Rank | Package | Line Coverage | Branch Coverage | Instruction Coverage | Methods |
|------|---------|---------------|-----------------|---------------------|---------|
| 1 | `com.databricks.jdbc.api` | 100.00% (7/7) | 0.00% (0/0) | 100.00% (64/64) | 100.00% (1/1) |
| 2 | `com.databricks.jdbc.api.impl.arrow.incubator` | 95.24% (200/210) | 87.50% (14/16) | 95.99% (813/847) | 97.96% (48/49) |
| 3 | `com.databricks.jdbc.api.impl.batch` | 95.16% (59/62) | 93.75% (15/16) | 93.73% (284/303) | 100.00% (7/7) |
| 4 | `com.databricks.client.jdbc` | 92.31% (60/65) | 50.00% (5/10) | 89.27% (208/233) | 92.59% (25/27) |
| 5 | `com.databricks.jdbc.api.impl.converters` | 91.96% (1247/1356) | 83.67% (502/600) | 92.10% (5763/6257) | 94.74% (234/247) |
| 6 | `com.databricks.jdbc.common.safe` | 91.82% (101/110) | 80.77% (21/26) | 93.89% (384/409) | 100.00% (22/22) |
| 7 | `com.databricks.jdbc.common.util` | 88.68% (909/1025) | 84.60% (533/630) | 88.11% (3662/4156) | 89.47% (170/190) |
| 8 | `com.databricks.jdbc.dbclient.impl.thrift` | 87.50% (728/832) | 77.73% (171/220) | 87.21% (3074/3525) | 84.21% (80/95) |
| 9 | `com.databricks.jdbc.exception` | 87.50% (28/32) | 71.43% (10/14) | 83.78% (124/148) | 87.50% (7/8) |
| 10 | `com.databricks.jdbc.dbclient.impl.common` | 86.65% (954/1101) | 78.10% (453/580) | 87.51% (3775/4314) | 94.89% (130/137) |
| 11 | `com.databricks.jdbc.common` | 86.02% (160/186) | 67.44% (29/43) | 89.50% (835/933) | 87.50% (42/48) |
| 12 | `com.databricks.jdbc.api.impl` | 86.00% (4134/4807) | 73.42% (939/1279) | 84.88% (16004/18854) | 90.79% (1193/1314) |
| 13 | `com.databricks.jdbc.dbclient.impl.http` | 84.84% (319/376) | 79.02% (113/143) | 84.72% (1120/1322) | 91.07% (51/56) |
| 14 | `com.databricks.jdbc.api.impl.arrow` | 84.68% (1067/1260) | 69.68% (262/376) | 85.29% (4672/5478) | 90.66% (165/182) |
| 15 | `com.databricks.jdbc.telemetry` | 84.41% (433/513) | 75.40% (95/126) | 83.60% (1494/1787) | 92.50% (74/80) |
| 16 | `com.databricks.jdbc.log` | 83.93% (141/168) | 68.42% (26/38) | 86.68% (566/653) | 84.62% (44/52) |
| 17 | `com.databricks.jdbc.pooling` | 82.64% (119/144) | 62.82% (49/78) | 83.01% (508/612) | 100.00% (18/18) |
| 18 | `com.databricks.jdbc.telemetry.latency` | 80.62% (104/129) | 61.02% (36/59) | 79.10% (371/469) | 91.18% (31/34) |
| 19 | `com.databricks.jdbc.api.impl.volume` | 80.60% (968/1201) | 77.54% (214/276) | 79.82% (4184/5242) | 79.11% (125/158) |
| 20 | `com.databricks.jdbc.auth` | 77.55% (335/432) | 65.38% (34/52) | 76.50% (1348/1762) | 89.33% (67/75) |
| 21 | `com.databricks.jdbc.dbclient.impl.sqlexec` | 68.48% (452/660) | 62.56% (132/211) | 69.81% (2342/3355) | 78.05% (64/82) |

## Packages with High Coverage (>= 80.0%)

| Package | Line Coverage | Lines Covered/Total |
|---------|---------------|---------------------|
| `com.databricks.jdbc.api` | 100.00% | 7/7 |
| `com.databricks.jdbc.api.impl.arrow.incubator` | 95.24% | 200/210 |
| `com.databricks.jdbc.api.impl.batch` | 95.16% | 59/62 |
| `com.databricks.client.jdbc` | 92.31% | 60/65 |
| `com.databricks.jdbc.api.impl.converters` | 91.96% | 1247/1356 |
| `com.databricks.jdbc.common.safe` | 91.82% | 101/110 |
| `com.databricks.jdbc.common.util` | 88.68% | 909/1025 |
| `com.databricks.jdbc.dbclient.impl.thrift` | 87.50% | 728/832 |
| `com.databricks.jdbc.exception` | 87.50% | 28/32 |
| `com.databricks.jdbc.dbclient.impl.common` | 86.65% | 954/1101 |

... and 9 more packages

## Packages with Low Coverage (< 50.0%)

✅ No packages found with coverage below 50.0%

## Key Findings

### Strengths
- **Excellent Overall Coverage**: The project has strong overall coverage with 85.34% line coverage and 84.97% instruction coverage
- **High Method Coverage**: 90.15% method coverage indicates comprehensive testing of public APIs
- **No Critical Gaps**: All packages have at least 68% coverage - no critical low-coverage areas
- **Well-tested Core Components**: API implementation, converters, and common utilities all have > 88% coverage

### Areas for Improvement
- **Branch Coverage Gap**: Overall branch coverage (76.22%) is about 9% lower than line coverage, indicating some conditional logic paths may not be fully tested
- **SQL Execution Client**: The `com.databricks.jdbc.dbclient.impl.sqlexec` package has the lowest coverage at 68.48%, suggesting this is a good target for additional testing
- **Authentication Module**: At 77.55% coverage, the auth package could benefit from more edge case testing given its security-critical nature
- **Volume Operations**: The volume package (80.60%) handles file I/O operations and could benefit from more error handling tests

## Recommendations

### Immediate Actions
1. **Improve SQL Execution Testing**: Add tests for `com.databricks.jdbc.dbclient.impl.sqlexec` package (currently 68.48%)
   - Focus on error handling and edge cases
   - Test various SQL command types and scenarios
   - Add integration tests for metadata operations

2. **Enhance Branch Coverage**: Target packages with branch coverage < 70%
   - `com.databricks.client.jdbc`: 50% branch coverage (focus on connection pooling logic)
   - `com.databricks.jdbc.pooling`: 62.82% branch coverage (test connection lifecycle edge cases)
   - `com.databricks.telemetry.latency`: 61.02% branch coverage (test timing edge cases)

3. **Security-Critical Components**: Improve coverage in authentication module
   - Add more tests for credential validation
   - Test token refresh and expiry scenarios
   - Test error handling for invalid credentials

### Long-term Strategy
1. **Maintain High Coverage**: Continue testing practices for packages with > 80% coverage (19 of 21 packages)
2. **Monitor Trends**: Track coverage metrics over time to ensure improvement
3. **Code Review Focus**: Require tests for new code, especially in lower-coverage packages
4. **Performance Testing**: Consider adding performance benchmarks alongside coverage tests

## How to View the Full Report

The JaCoCo HTML report provides detailed coverage information for every class and method:

```bash
# Generate the latest report
mvn clean test jacoco:report

# View the HTML report
open target/site/jacoco/index.html
# Or on Linux:
xdg-open target/site/jacoco/index.html
```

## Report Files

- **HTML Report**: `target/site/jacoco/index.html` - Interactive browsable report
- **CSV Data**: `target/site/jacoco/jacoco.csv` - Machine-readable summary data
- **XML Data**: `target/site/jacoco/jacoco.xml` - Detailed XML format for CI/CD integration
- **Execution Data**: `target/jacoco.exec` - Raw coverage execution data

## CI/CD Integration

To integrate JaCoCo into your CI/CD pipeline:

```bash
# Run tests and generate coverage
mvn clean verify

# The build is configured to generate reports during the prepare-package phase
# Coverage data is automatically collected during test execution
```

The `pom.xml` already includes JaCoCo configuration with:
- Automatic instrumentation during test execution
- Report generation during the `prepare-package` phase
- Exclusions for generated code, constants, and exception classes

## Appendix: Coverage Metrics Explained

- **Instruction Coverage**: Percentage of Java bytecode instructions executed during tests
- **Branch Coverage**: Percentage of conditional branches (if/else, switch) executed
- **Line Coverage**: Percentage of source code lines executed
- **Method Coverage**: Percentage of methods invoked during tests
- **Complexity Coverage**: Based on cyclomatic complexity, measures coverage of independent code paths

