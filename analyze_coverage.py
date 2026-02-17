#!/usr/bin/env python3
"""Analyze JaCoCo coverage report and generate module rankings."""

import csv
import sys
from collections import defaultdict

class PackageCoverage:
    def __init__(self, package, instruction_missed, instruction_covered,
                 branch_missed, branch_covered, line_missed, line_covered,
                 complexity_missed, complexity_covered, method_missed, method_covered):
        self.package = package
        self.instruction_missed = instruction_missed
        self.instruction_covered = instruction_covered
        self.branch_missed = branch_missed
        self.branch_covered = branch_covered
        self.line_missed = line_missed
        self.line_covered = line_covered
        self.complexity_missed = complexity_missed
        self.complexity_covered = complexity_covered
        self.method_missed = method_missed
        self.method_covered = method_covered

    @property
    def instruction_coverage_pct(self):
        total = self.instruction_covered + self.instruction_missed
        return (self.instruction_covered / total * 100) if total > 0 else 0.0

    @property
    def branch_coverage_pct(self):
        total = self.branch_covered + self.branch_missed
        return (self.branch_covered / total * 100) if total > 0 else 0.0

    @property
    def line_coverage_pct(self):
        total = self.line_covered + self.line_missed
        return (self.line_covered / total * 100) if total > 0 else 0.0

    @property
    def method_coverage_pct(self):
        total = self.method_covered + self.method_missed
        return (self.method_covered / total * 100) if total > 0 else 0.0


def aggregate_by_package(csv_file):
    """Aggregate coverage data by package."""
    package_data = defaultdict(lambda: {
        'instruction_missed': 0,
        'instruction_covered': 0,
        'branch_missed': 0,
        'branch_covered': 0,
        'line_missed': 0,
        'line_covered': 0,
        'complexity_missed': 0,
        'complexity_covered': 0,
        'method_missed': 0,
        'method_covered': 0,
    })

    with open(csv_file, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            package = row['PACKAGE']
            if not package:
                continue

            package_data[package]['instruction_missed'] += int(row['INSTRUCTION_MISSED'])
            package_data[package]['instruction_covered'] += int(row['INSTRUCTION_COVERED'])
            package_data[package]['branch_missed'] += int(row['BRANCH_MISSED'])
            package_data[package]['branch_covered'] += int(row['BRANCH_COVERED'])
            package_data[package]['line_missed'] += int(row['LINE_MISSED'])
            package_data[package]['line_covered'] += int(row['LINE_COVERED'])
            package_data[package]['complexity_missed'] += int(row['COMPLEXITY_MISSED'])
            package_data[package]['complexity_covered'] += int(row['COMPLEXITY_COVERED'])
            package_data[package]['method_missed'] += int(row['METHOD_MISSED'])
            package_data[package]['method_covered'] += int(row['METHOD_COVERED'])

    # Convert to PackageCoverage objects
    result = {}
    for package, data in package_data.items():
        result[package] = PackageCoverage(
            package=package,
            instruction_missed=data['instruction_missed'],
            instruction_covered=data['instruction_covered'],
            branch_missed=data['branch_missed'],
            branch_covered=data['branch_covered'],
            line_missed=data['line_missed'],
            line_covered=data['line_covered'],
            complexity_missed=data['complexity_missed'],
            complexity_covered=data['complexity_covered'],
            method_missed=data['method_missed'],
            method_covered=data['method_covered']
        )

    return result


def print_summary(packages):
    """Print overall summary."""
    total_instruction_missed = sum(p.instruction_missed for p in packages.values())
    total_instruction_covered = sum(p.instruction_covered for p in packages.values())
    total_branch_missed = sum(p.branch_missed for p in packages.values())
    total_branch_covered = sum(p.branch_covered for p in packages.values())
    total_line_missed = sum(p.line_missed for p in packages.values())
    total_line_covered = sum(p.line_covered for p in packages.values())
    total_method_missed = sum(p.method_missed for p in packages.values())
    total_method_covered = sum(p.method_covered for p in packages.values())

    total_instructions = total_instruction_covered + total_instruction_missed
    total_branches = total_branch_covered + total_branch_missed
    total_lines = total_line_covered + total_line_missed
    total_methods = total_method_covered + total_method_missed

    print("# JaCoCo Code Coverage Report - databricks-jdbc")
    print()
    print("## Overall Project Coverage")
    print()
    print(f"- **Instruction Coverage**: {total_instruction_covered}/{total_instructions} ({total_instruction_covered/total_instructions*100:.2f}%)")
    print(f"- **Branch Coverage**: {total_branch_covered}/{total_branches} ({total_branch_covered/total_branches*100:.2f}%)")
    print(f"- **Line Coverage**: {total_line_covered}/{total_lines} ({total_line_covered/total_lines*100:.2f}%)")
    print(f"- **Method Coverage**: {total_method_covered}/{total_methods} ({total_method_covered/total_methods*100:.2f}%)")
    print()


def print_rankings(packages):
    """Print package rankings by coverage."""
    # Sort by line coverage percentage
    sorted_packages = sorted(packages.values(),
                            key=lambda p: p.line_coverage_pct,
                            reverse=True)

    print("## Package Rankings by Line Coverage")
    print()
    print("| Rank | Package | Line Coverage | Branch Coverage | Instruction Coverage | Methods |")
    print("|------|---------|---------------|-----------------|---------------------|---------|")

    for i, pkg in enumerate(sorted_packages, 1):
        print(f"| {i} | `{pkg.package}` | {pkg.line_coverage_pct:.2f}% ({pkg.line_covered}/{pkg.line_covered + pkg.line_missed}) | "
              f"{pkg.branch_coverage_pct:.2f}% ({pkg.branch_covered}/{pkg.branch_covered + pkg.branch_missed}) | "
              f"{pkg.instruction_coverage_pct:.2f}% ({pkg.instruction_covered}/{pkg.instruction_covered + pkg.instruction_missed}) | "
              f"{pkg.method_coverage_pct:.2f}% ({pkg.method_covered}/{pkg.method_covered + pkg.method_missed}) |")

    print()


def print_low_coverage_packages(packages, threshold=50.0):
    """Print packages with low coverage."""
    low_coverage = [p for p in packages.values() if p.line_coverage_pct < threshold]
    low_coverage.sort(key=lambda p: p.line_coverage_pct)

    print(f"## Packages with Low Coverage (< {threshold}%)")
    print()
    if not low_coverage:
        print(f"✅ No packages found with coverage below {threshold}%")
    else:
        print("| Package | Line Coverage | Lines Covered/Total | Recommendation |")
        print("|---------|---------------|---------------------|----------------|")

        for pkg in low_coverage:
            total_lines = pkg.line_covered + pkg.line_missed
            if pkg.line_coverage_pct < 30:
                recommendation = "🔴 Critical - Needs immediate attention"
            elif pkg.line_coverage_pct < 50:
                recommendation = "🟡 Moderate - Should improve coverage"
            else:
                recommendation = "🟢 Acceptable"

            print(f"| `{pkg.package}` | {pkg.line_coverage_pct:.2f}% | {pkg.line_covered}/{total_lines} | {recommendation} |")

    print()


def print_high_coverage_packages(packages, threshold=80.0):
    """Print packages with high coverage."""
    high_coverage = [p for p in packages.values() if p.line_coverage_pct >= threshold]
    high_coverage.sort(key=lambda p: p.line_coverage_pct, reverse=True)

    print(f"## Packages with High Coverage (>= {threshold}%)")
    print()
    if not high_coverage:
        print(f"⚠️  No packages found with coverage at or above {threshold}%")
    else:
        print("| Package | Line Coverage | Lines Covered/Total |")
        print("|---------|---------------|---------------------|")

        for pkg in high_coverage[:10]:  # Show top 10
            total_lines = pkg.line_covered + pkg.line_missed
            print(f"| `{pkg.package}` | {pkg.line_coverage_pct:.2f}% | {pkg.line_covered}/{total_lines} |")

        if len(high_coverage) > 10:
            print(f"\n... and {len(high_coverage) - 10} more packages")

    print()


def main():
    csv_file = 'target/site/jacoco/jacoco.csv'

    print("Analyzing JaCoCo coverage report...")
    print()

    packages = aggregate_by_package(csv_file)

    print_summary(packages)
    print_rankings(packages)
    print_high_coverage_packages(packages)
    print_low_coverage_packages(packages)

    print("## Recommendations")
    print()
    print("1. **Focus on Critical Packages**: Prioritize adding tests to packages with < 30% coverage")
    print("2. **Improve Branch Coverage**: Many packages have lower branch coverage than line coverage")
    print("3. **Maintain High Coverage**: Continue testing practices for packages with > 80% coverage")
    print("4. **Monitor Trends**: Track coverage metrics over time to ensure improvement")
    print()
    print("## Report Details")
    print()
    print("- **Generated**: Using JaCoCo Maven Plugin v0.8.11")
    print("- **Report Location**: `target/site/jacoco/index.html`")
    print("- **CSV Data**: `target/site/jacoco/jacoco.csv`")
    print("- **XML Data**: `target/site/jacoco/jacoco.xml`")
    print()


if __name__ == '__main__':
    main()
