#!/usr/bin/env python3
"""
JaCoCo Coverage Analysis Script
Analyzes the JaCoCo CSV report and generates insights by package/module.
"""

import csv
from collections import defaultdict
from typing import Dict, List, Tuple

def calculate_coverage_percentage(missed: int, covered: int) -> float:
    """Calculate coverage percentage."""
    total = missed + covered
    if total == 0:
        return 100.0
    return (covered / total) * 100.0

def analyze_jacoco_csv(csv_path: str) -> Dict[str, Dict]:
    """Analyze JaCoCo CSV and compute package-level statistics."""

    package_stats = defaultdict(lambda: {
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
        'class_count': 0
    })

    with open(csv_path, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            package = row['PACKAGE']

            # Skip header row if present
            if package == 'PACKAGE':
                continue

            stats = package_stats[package]
            stats['instruction_missed'] += int(row['INSTRUCTION_MISSED'])
            stats['instruction_covered'] += int(row['INSTRUCTION_COVERED'])
            stats['branch_missed'] += int(row['BRANCH_MISSED'])
            stats['branch_covered'] += int(row['BRANCH_COVERED'])
            stats['line_missed'] += int(row['LINE_MISSED'])
            stats['line_covered'] += int(row['LINE_COVERED'])
            stats['complexity_missed'] += int(row['COMPLEXITY_MISSED'])
            stats['complexity_covered'] += int(row['COMPLEXITY_COVERED'])
            stats['method_missed'] += int(row['METHOD_MISSED'])
            stats['method_covered'] += int(row['METHOD_COVERED'])
            stats['class_count'] += 1

    # Calculate percentages
    for package, stats in package_stats.items():
        stats['instruction_coverage'] = calculate_coverage_percentage(
            stats['instruction_missed'], stats['instruction_covered']
        )
        stats['branch_coverage'] = calculate_coverage_percentage(
            stats['branch_missed'], stats['branch_covered']
        )
        stats['line_coverage'] = calculate_coverage_percentage(
            stats['line_missed'], stats['line_covered']
        )
        stats['method_coverage'] = calculate_coverage_percentage(
            stats['method_missed'], stats['method_covered']
        )
        stats['complexity_coverage'] = calculate_coverage_percentage(
            stats['complexity_missed'], stats['complexity_covered']
        )

    return dict(package_stats)

def rank_packages(package_stats: Dict[str, Dict], metric: str = 'instruction_coverage') -> List[Tuple[str, float]]:
    """Rank packages by a given coverage metric."""
    return sorted(
        [(pkg, stats[metric]) for pkg, stats in package_stats.items()],
        key=lambda x: x[1],
        reverse=True
    )

def generate_markdown_report(package_stats: Dict[str, Dict], output_path: str):
    """Generate a comprehensive markdown report."""

    # Calculate overall statistics
    total_instructions_missed = sum(s['instruction_missed'] for s in package_stats.values())
    total_instructions_covered = sum(s['instruction_covered'] for s in package_stats.values())
    total_branches_missed = sum(s['branch_missed'] for s in package_stats.values())
    total_branches_covered = sum(s['branch_covered'] for s in package_stats.values())
    total_lines_missed = sum(s['line_missed'] for s in package_stats.values())
    total_lines_covered = sum(s['line_covered'] for s in package_stats.values())
    total_methods_missed = sum(s['method_missed'] for s in package_stats.values())
    total_methods_covered = sum(s['method_covered'] for s in package_stats.values())
    total_classes = sum(s['class_count'] for s in package_stats.values())

    overall_instruction_coverage = calculate_coverage_percentage(
        total_instructions_missed, total_instructions_covered
    )
    overall_branch_coverage = calculate_coverage_percentage(
        total_branches_missed, total_branches_covered
    )
    overall_line_coverage = calculate_coverage_percentage(
        total_lines_missed, total_lines_covered
    )
    overall_method_coverage = calculate_coverage_percentage(
        total_methods_missed, total_methods_covered
    )

    # Rank packages
    instruction_ranking = rank_packages(package_stats, 'instruction_coverage')
    branch_ranking = rank_packages(package_stats, 'branch_coverage')
    line_ranking = rank_packages(package_stats, 'line_coverage')

    with open(output_path, 'w') as f:
        f.write("# JaCoCo Code Coverage Report - Databricks JDBC Driver\n\n")
        f.write("## Executive Summary\n\n")
        f.write(f"**Report Generated:** February 17, 2026\n\n")
        f.write(f"**Total Packages Analyzed:** {len(package_stats)}\n\n")
        f.write(f"**Total Classes:** {total_classes}\n\n")

        f.write("### Overall Coverage Metrics\n\n")
        f.write("| Metric | Coverage | Missed | Covered | Total |\n")
        f.write("|--------|----------|--------|---------|-------|\n")
        f.write(f"| **Instructions** | **{overall_instruction_coverage:.2f}%** | {total_instructions_missed:,} | {total_instructions_covered:,} | {total_instructions_missed + total_instructions_covered:,} |\n")
        f.write(f"| **Branches** | **{overall_branch_coverage:.2f}%** | {total_branches_missed:,} | {total_branches_covered:,} | {total_branches_missed + total_branches_covered:,} |\n")
        f.write(f"| **Lines** | **{overall_line_coverage:.2f}%** | {total_lines_missed:,} | {total_lines_covered:,} | {total_lines_missed + total_lines_covered:,} |\n")
        f.write(f"| **Methods** | **{overall_method_coverage:.2f}%** | {total_methods_missed:,} | {total_methods_covered:,} | {total_methods_missed + total_methods_covered:,} |\n\n")

        f.write("---\n\n")

        f.write("## Package Rankings by Coverage\n\n")

        f.write("### Top 10 Packages by Instruction Coverage\n\n")
        f.write("| Rank | Package | Instruction Coverage | Branch Coverage | Line Coverage | Classes |\n")
        f.write("|------|---------|---------------------|-----------------|---------------|----------|\n")
        for i, (pkg, coverage) in enumerate(instruction_ranking[:10], 1):
            stats = package_stats[pkg]
            f.write(f"| {i} | `{pkg}` | {coverage:.2f}% | {stats['branch_coverage']:.2f}% | {stats['line_coverage']:.2f}% | {stats['class_count']} |\n")

        f.write("\n### Bottom 10 Packages by Instruction Coverage (Need Improvement)\n\n")
        f.write("| Rank | Package | Instruction Coverage | Branch Coverage | Line Coverage | Classes |\n")
        f.write("|------|---------|---------------------|-----------------|---------------|----------|\n")
        for i, (pkg, coverage) in enumerate(reversed(instruction_ranking[-10:]), 1):
            stats = package_stats[pkg]
            f.write(f"| {i} | `{pkg}` | {coverage:.2f}% | {stats['branch_coverage']:.2f}% | {stats['line_coverage']:.2f}% | {stats['class_count']} |\n")

        f.write("\n---\n\n")

        f.write("## Detailed Package Analysis\n\n")
        f.write("Complete breakdown of all packages sorted by instruction coverage:\n\n")
        f.write("| Package | Instruction Cov | Branch Cov | Line Cov | Method Cov | Classes | Methods | Lines |\n")
        f.write("|---------|-----------------|------------|----------|------------|---------|---------|-------|\n")

        for pkg, coverage in instruction_ranking:
            stats = package_stats[pkg]
            total_methods = stats['method_missed'] + stats['method_covered']
            total_lines = stats['line_missed'] + stats['line_covered']
            f.write(f"| `{pkg}` | {stats['instruction_coverage']:.2f}% | {stats['branch_coverage']:.2f}% | "
                   f"{stats['line_coverage']:.2f}% | {stats['method_coverage']:.2f}% | "
                   f"{stats['class_count']} | {total_methods} | {total_lines} |\n")

        f.write("\n---\n\n")

        f.write("## Coverage Analysis by Module\n\n")

        # Group packages by module (top-level package)
        module_stats = defaultdict(lambda: {
            'packages': [],
            'instruction_missed': 0,
            'instruction_covered': 0,
            'branch_missed': 0,
            'branch_covered': 0,
            'line_missed': 0,
            'line_covered': 0,
            'class_count': 0
        })

        for pkg, stats in package_stats.items():
            # Extract module (e.g., com.databricks.jdbc.api from com.databricks.jdbc.api.impl)
            parts = pkg.split('.')
            if len(parts) >= 4:
                module = '.'.join(parts[:4])
            else:
                module = pkg

            module_stats[module]['packages'].append(pkg)
            module_stats[module]['instruction_missed'] += stats['instruction_missed']
            module_stats[module]['instruction_covered'] += stats['instruction_covered']
            module_stats[module]['branch_missed'] += stats['branch_missed']
            module_stats[module]['branch_covered'] += stats['branch_covered']
            module_stats[module]['line_missed'] += stats['line_missed']
            module_stats[module]['line_covered'] += stats['line_covered']
            module_stats[module]['class_count'] += stats['class_count']

        # Calculate module coverage percentages
        for module, stats in module_stats.items():
            stats['instruction_coverage'] = calculate_coverage_percentage(
                stats['instruction_missed'], stats['instruction_covered']
            )
            stats['branch_coverage'] = calculate_coverage_percentage(
                stats['branch_missed'], stats['branch_covered']
            )
            stats['line_coverage'] = calculate_coverage_percentage(
                stats['line_missed'], stats['line_covered']
            )

        # Rank modules
        module_ranking = sorted(
            [(mod, stats['instruction_coverage']) for mod, stats in module_stats.items()],
            key=lambda x: x[1],
            reverse=True
        )

        f.write("### Module-Level Coverage Summary\n\n")
        f.write("| Rank | Module | Instruction Cov | Branch Cov | Line Cov | Classes | Packages |\n")
        f.write("|------|--------|-----------------|------------|----------|---------|----------|\n")

        for i, (module, coverage) in enumerate(module_ranking, 1):
            stats = module_stats[module]
            f.write(f"| {i} | `{module}` | {stats['instruction_coverage']:.2f}% | "
                   f"{stats['branch_coverage']:.2f}% | {stats['line_coverage']:.2f}% | "
                   f"{stats['class_count']} | {len(stats['packages'])} |\n")

        f.write("\n---\n\n")

        f.write("## Recommendations\n\n")

        # Find packages with low coverage
        low_coverage_packages = [(pkg, cov) for pkg, cov in instruction_ranking if cov < 50.0]

        f.write(f"### Packages Requiring Immediate Attention (< 50% Coverage)\n\n")
        if low_coverage_packages:
            f.write(f"Found {len(low_coverage_packages)} packages with less than 50% instruction coverage:\n\n")
            for pkg, cov in reversed(low_coverage_packages[-10:]):
                stats = package_stats[pkg]
                f.write(f"- **{pkg}**: {cov:.2f}% instruction coverage, "
                       f"{stats['branch_coverage']:.2f}% branch coverage\n")
        else:
            f.write("Excellent! All packages have at least 50% instruction coverage.\n")

        f.write("\n### Priority Areas for Test Improvement\n\n")
        f.write("Based on the analysis, focus testing efforts on:\n\n")
        f.write("1. **Volume Operations** - The `com.databricks.jdbc.api.impl.volume` package shows opportunities for improvement\n")
        f.write("2. **Arrow Streaming** - Complex streaming logic in `com.databricks.jdbc.api.impl.arrow` needs more edge case coverage\n")
        f.write("3. **Authentication Flows** - Auth modules have good coverage but edge cases and error paths could be improved\n")
        f.write("4. **Thrift Protocol** - Thrift client implementations have moderate coverage with room for improvement\n")

        f.write("\n### Strengths\n\n")
        f.write("Areas with excellent test coverage:\n\n")
        for pkg, cov in instruction_ranking[:5]:
            f.write(f"- **{pkg}**: {cov:.2f}% coverage - Well tested\n")

        f.write("\n---\n\n")
        f.write("## Additional Information\n\n")
        f.write("### Coverage Report Location\n\n")
        f.write("The full interactive HTML report is available at:\n")
        f.write("```\ntarget/site/jacoco/index.html\n```\n\n")
        f.write("### How to Generate This Report\n\n")
        f.write("To regenerate the coverage report:\n\n")
        f.write("```bash\n")
        f.write("# Run tests with coverage\n")
        f.write("mvn clean test\n\n")
        f.write("# Generate JaCoCo report\n")
        f.write("mvn jacoco:report\n\n")
        f.write("# Analyze coverage data\n")
        f.write("python3 analyze_coverage.py\n")
        f.write("```\n\n")
        f.write("### JaCoCo Configuration\n\n")
        f.write("JaCoCo is configured in `pom.xml` with the following exclusions:\n")
        f.write("- Constants classes\n")
        f.write("- Exception classes\n")
        f.write("- Generated model classes\n")
        f.write("- Thrift generated code\n")
        f.write("- Driver entry point\n\n")

        f.write("---\n\n")
        f.write("*Report generated using JaCoCo Maven Plugin version 0.8.11*\n")

if __name__ == '__main__':
    csv_path = 'target/site/jacoco/jacoco.csv'
    output_path = 'reports/PECOBLR-1741-jacoco-coverage-report.md'

    print("Analyzing JaCoCo coverage data...")
    package_stats = analyze_jacoco_csv(csv_path)
    print(f"Found {len(package_stats)} packages")

    print("Generating markdown report...")
    generate_markdown_report(package_stats, output_path)
    print(f"Report saved to: {output_path}")
