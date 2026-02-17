# JaCoCo Code Coverage Report - Databricks JDBC Driver

## Executive Summary

**Report Generated:** February 17, 2026

**Total Packages Analyzed:** 21

**Total Classes:** 201

### Overall Coverage Metrics

| Metric | Coverage | Missed | Covered | Total |
|--------|----------|--------|---------|-------|
| **Instructions** | **84.93%** | 9,149 | 51,574 | 60,723 |
| **Branches** | **76.22%** | 1,140 | 3,653 | 4,793 |
| **Lines** | **85.28%** | 2,160 | 12,516 | 14,676 |
| **Methods** | **90.04%** | 287 | 2,595 | 2,882 |

---

## Package Rankings by Coverage

### Top 10 Packages by Instruction Coverage

| Rank | Package | Instruction Coverage | Branch Coverage | Line Coverage | Classes |
|------|---------|---------------------|-----------------|---------------|----------|
| 1 | `com.databricks.jdbc.api` | 100.00% | 100.00% | 100.00% | 1 |
| 2 | `com.databricks.jdbc.api.impl.arrow.incubator` | 95.99% | 87.50% | 95.24% | 10 |
| 3 | `com.databricks.jdbc.common.safe` | 93.89% | 80.77% | 91.82% | 5 |
| 4 | `com.databricks.jdbc.api.impl.batch` | 93.73% | 93.75% | 95.16% | 1 |
| 5 | `com.databricks.jdbc.api.impl.converters` | 92.10% | 83.67% | 91.96% | 21 |
| 6 | `com.databricks.jdbc.common` | 89.50% | 67.44% | 86.02% | 14 |
| 7 | `com.databricks.client.jdbc` | 89.27% | 50.00% | 92.31% | 1 |
| 8 | `com.databricks.jdbc.common.util` | 88.11% | 84.60% | 88.68% | 26 |
| 9 | `com.databricks.jdbc.dbclient.impl.common` | 87.51% | 78.10% | 86.65% | 12 |
| 10 | `com.databricks.jdbc.dbclient.impl.thrift` | 87.21% | 77.73% | 87.50% | 4 |

### Bottom 10 Packages by Instruction Coverage (Need Improvement)

| Rank | Package | Instruction Coverage | Branch Coverage | Line Coverage | Classes |
|------|---------|---------------------|-----------------|---------------|----------|
| 1 | `com.databricks.jdbc.dbclient.impl.sqlexec` | 69.81% | 62.56% | 68.48% | 4 |
| 2 | `com.databricks.jdbc.auth` | 76.50% | 65.38% | 77.55% | 9 |
| 3 | `com.databricks.jdbc.telemetry.latency` | 79.10% | 61.02% | 80.62% | 3 |
| 4 | `com.databricks.jdbc.api.impl.volume` | 79.82% | 77.54% | 80.60% | 11 |
| 5 | `com.databricks.jdbc.pooling` | 83.01% | 62.82% | 82.64% | 3 |
| 6 | `com.databricks.jdbc.telemetry` | 83.60% | 75.40% | 84.41% | 11 |
| 7 | `com.databricks.jdbc.exception` | 83.78% | 71.43% | 87.50% | 1 |
| 8 | `com.databricks.jdbc.dbclient.impl.http` | 84.72% | 79.02% | 84.84% | 7 |
| 9 | `com.databricks.jdbc.api.impl` | 84.77% | 73.42% | 85.81% | 32 |
| 10 | `com.databricks.jdbc.api.impl.arrow` | 85.29% | 69.68% | 84.68% | 19 |

---

## Detailed Package Analysis

Complete breakdown of all packages sorted by instruction coverage:

| Package | Instruction Cov | Branch Cov | Line Cov | Method Cov | Classes | Methods | Lines |
|---------|-----------------|------------|----------|------------|---------|---------|-------|
| `com.databricks.jdbc.api` | 100.00% | 100.00% | 100.00% | 100.00% | 1 | 1 | 7 |
| `com.databricks.jdbc.api.impl.arrow.incubator` | 95.99% | 87.50% | 95.24% | 97.96% | 10 | 49 | 210 |
| `com.databricks.jdbc.common.safe` | 93.89% | 80.77% | 91.82% | 100.00% | 5 | 22 | 110 |
| `com.databricks.jdbc.api.impl.batch` | 93.73% | 93.75% | 95.16% | 100.00% | 1 | 7 | 62 |
| `com.databricks.jdbc.api.impl.converters` | 92.10% | 83.67% | 91.96% | 94.74% | 21 | 247 | 1356 |
| `com.databricks.jdbc.common` | 89.50% | 67.44% | 86.02% | 87.50% | 14 | 48 | 186 |
| `com.databricks.client.jdbc` | 89.27% | 50.00% | 92.31% | 92.59% | 1 | 27 | 65 |
| `com.databricks.jdbc.common.util` | 88.11% | 84.60% | 88.68% | 89.47% | 26 | 190 | 1025 |
| `com.databricks.jdbc.dbclient.impl.common` | 87.51% | 78.10% | 86.65% | 94.89% | 12 | 137 | 1101 |
| `com.databricks.jdbc.dbclient.impl.thrift` | 87.21% | 77.73% | 87.50% | 84.21% | 4 | 95 | 832 |
| `com.databricks.jdbc.log` | 86.68% | 68.42% | 83.93% | 84.62% | 6 | 52 | 168 |
| `com.databricks.jdbc.api.impl.arrow` | 85.29% | 69.68% | 84.68% | 90.66% | 19 | 182 | 1260 |
| `com.databricks.jdbc.api.impl` | 84.77% | 73.42% | 85.81% | 90.56% | 32 | 1314 | 4807 |
| `com.databricks.jdbc.dbclient.impl.http` | 84.72% | 79.02% | 84.84% | 91.07% | 7 | 56 | 376 |
| `com.databricks.jdbc.exception` | 83.78% | 71.43% | 87.50% | 87.50% | 1 | 8 | 32 |
| `com.databricks.jdbc.telemetry` | 83.60% | 75.40% | 84.41% | 92.50% | 11 | 80 | 513 |
| `com.databricks.jdbc.pooling` | 83.01% | 62.82% | 82.64% | 100.00% | 3 | 18 | 144 |
| `com.databricks.jdbc.api.impl.volume` | 79.82% | 77.54% | 80.60% | 79.11% | 11 | 158 | 1201 |
| `com.databricks.jdbc.telemetry.latency` | 79.10% | 61.02% | 80.62% | 91.18% | 3 | 34 | 129 |
| `com.databricks.jdbc.auth` | 76.50% | 65.38% | 77.55% | 89.33% | 9 | 75 | 432 |
| `com.databricks.jdbc.dbclient.impl.sqlexec` | 69.81% | 62.56% | 68.48% | 78.05% | 4 | 82 | 660 |

---

## Coverage Analysis by Module

### Module-Level Coverage Summary

| Rank | Module | Instruction Cov | Branch Cov | Line Cov | Classes | Packages |
|------|--------|-----------------|------------|----------|---------|----------|
| 1 | `com.databricks.client.jdbc` | 89.27% | 50.00% | 92.31% | 1 | 1 |
| 2 | `com.databricks.jdbc.common` | 88.78% | 83.40% | 88.57% | 45 | 3 |
| 3 | `com.databricks.jdbc.log` | 86.68% | 68.42% | 83.93% | 6 | 1 |
| 4 | `com.databricks.jdbc.api` | 85.74% | 75.93% | 86.18% | 95 | 7 |
| 5 | `com.databricks.jdbc.exception` | 83.78% | 71.43% | 87.50% | 1 | 1 |
| 6 | `com.databricks.jdbc.pooling` | 83.01% | 62.82% | 82.64% | 3 | 1 |
| 7 | `com.databricks.jdbc.telemetry` | 82.67% | 70.81% | 83.64% | 14 | 2 |
| 8 | `com.databricks.jdbc.dbclient` | 82.38% | 75.30% | 82.62% | 27 | 4 |
| 9 | `com.databricks.jdbc.auth` | 76.50% | 65.38% | 77.55% | 9 | 1 |

---

## Recommendations

### Packages Requiring Immediate Attention (< 50% Coverage)

Excellent! All packages have at least 50% instruction coverage.

### Priority Areas for Test Improvement

Based on the analysis, focus testing efforts on:

1. **Volume Operations** - The `com.databricks.jdbc.api.impl.volume` package shows opportunities for improvement
2. **Arrow Streaming** - Complex streaming logic in `com.databricks.jdbc.api.impl.arrow` needs more edge case coverage
3. **Authentication Flows** - Auth modules have good coverage but edge cases and error paths could be improved
4. **Thrift Protocol** - Thrift client implementations have moderate coverage with room for improvement

### Strengths

Areas with excellent test coverage:

- **com.databricks.jdbc.api**: 100.00% coverage - Well tested
- **com.databricks.jdbc.api.impl.arrow.incubator**: 95.99% coverage - Well tested
- **com.databricks.jdbc.common.safe**: 93.89% coverage - Well tested
- **com.databricks.jdbc.api.impl.batch**: 93.73% coverage - Well tested
- **com.databricks.jdbc.api.impl.converters**: 92.10% coverage - Well tested

---

## Additional Information

### Coverage Report Location

The full interactive HTML report is available at:
```
target/site/jacoco/index.html
```

### How to Generate This Report

To regenerate the coverage report:

```bash
# Run tests with coverage
mvn clean test

# Generate JaCoCo report
mvn jacoco:report

# Analyze coverage data
python3 analyze_coverage.py
```

### JaCoCo Configuration

JaCoCo is configured in `pom.xml` with the following exclusions:
- Constants classes
- Exception classes
- Generated model classes
- Thrift generated code
- Driver entry point

---

*Report generated using JaCoCo Maven Plugin version 0.8.11*
