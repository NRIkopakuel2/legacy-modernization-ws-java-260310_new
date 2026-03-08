# Scoring Rubric — Mathematical Methodology

## Overview
This document describes the mathematical formulas and normalization methodology used
by the assessment script. For the definitive assessment criteria (what each dimension
measures, with examples), see [SKILL.md](../SKILL.md).

## General Formula

Each dimension starts at its maximum points and deducts based on anti-pattern density:

```
dimension_score = max(0, max_points - deduction)
deduction = normalized_weighted_count × deduction_rate
normalized_weighted_count = weighted_raw_count / KLOC
KLOC = max(1.0, total_LOC / 1000)
total_LOC = Java_LOC + JSP_LOC
```

Normalization per 1,000 LOC (KLOC) enables fair comparison across codebases of
different sizes. A 5K-LOC project and a 50K-LOC project are scored on the same scale.

---

## Dimension Formulas

### D1: Security Vulnerabilities (max 15)
```
raw = SQL_injection_count + hardcoded_creds + (JDBC_in_views × 5) + (JDBC_in_wrong_layer × 3)
normalized = raw / KLOC
deduction = normalized × 0.3 (capped at 15)
```

### D2: Error Handling (max 12)
```
raw = catch_Exception + (catch_Throwable × 3) + (catch_NPE × 3) + (empty_catch × 2) + printStackTrace
normalized = raw / KLOC
deduction = normalized × 0.2 (capped at 12)
```

### D3: God Classes (max 10)
```
deduction = (files_over_500 × 0.5) + (files_over_1000 × 1.5) + (concentration_pct × 0.2)
concentration_pct = (largest_file_LOC / total_Java_LOC) × 100
```
*Not normalized by KLOC — absolute file counts matter.*

### D4: Memory & Resource Leaks (max 6)
```
raw = mutable_static_collections + (files_without_finally × 2)
normalized = raw / KLOC
deduction = normalized × 1.0 (capped at 6)
```

### D5: Copy-Paste / Duplication (max 8)
```
raw = (JDBC_URLs × 2) + getConnection_count + System_out_count + System_err_count
normalized = raw / KLOC
deduction = normalized × 0.15 (capped at 8)
```

### D6: Global Mutable State (max 7)
```
raw = mutable_static_fields + (thread_unsafe_DateFormat × 5)
normalized = raw / KLOC
deduction = normalized × 0.5 (capped at 7)
```

### D7: Naming & Readability (max 5)
```
raw = wildcard_imports + (single_char_vars × 2) + (mixed_getters × 2)
normalized = raw / KLOC
deduction = normalized × 0.3 (capped at 5)
```

### D8: Concurrency Safety (max 5)
```
raw = (Thread_sleep × 3) + String_identity_comparisons + (broken_DCL × 5)
normalized = raw / KLOC
deduction = normalized × 0.8 (capped at 5)
```

### D9: Magic Numbers & Strings (max 5)
```
raw = hardcoded_status_strings + (numeric_return_codes × 2)
normalized = raw / KLOC
deduction = normalized × 0.5 (capped at 5)
```

### D10: Logging Consistency (max 5)
```
framework_penalty = max(0, (distinct_frameworks - 1)) × 1.5
spread_penalty = (sysout_file_count / KLOC) × 0.3
deduction = framework_penalty + spread_penalty (capped at 5)
```

### D11: Configuration Quality (max 5)
```
raw = commented_XML_blocks + config_param_count
normalized = raw / KLOC
deduction = normalized × 0.3 (capped at 5)
```

### D12: Dead Code (max 5)
```
raw = TODO_FIXME_count + commented_code_lines + (feature_flags × 2)
normalized = raw / KLOC
deduction = normalized × 0.3 (capped at 5)
```

### D13: Method Complexity (max 5)
```
avg_method_size = Java_LOC / method_count
size_penalty = max(0, (avg_method_size - 30)) × 0.1
nesting_penalty = (deep_nesting_lines / KLOC) × 0.1
deduction = size_penalty + nesting_penalty (capped at 5)
```

### D14: Dependency Structure (max 4)
```
deduction = (circular_dep_pairs × 2) + (tight_coupling_count / KLOC × 0.5)
(capped at 4)
```

### D15: JSP / View Layer (max 3)
```
deduction = (scriptlet_count / KLOC × 0.1) + (JDBC_in_JSP_count × 0.5) + (reflection_in_JSP × 0.3)
(capped at 3)
```
*If no JSP files exist, score = 3/3 (full points).*

---

## Final Score

```
total = D1 + D2 + D3 + D4 + D5 + D6 + D7 + D8 + D9 + D10 + D11 + D12 + D13 + D14 + D15
```

| Total Score | Grade | Interpretation |
|-------------|-------|----------------|
| 90-100 | A (Excellent) | Clean, well-structured, production-ready |
| 80-89 | B (Good) | Minor issues, maintainable with small effort |
| 70-79 | C (Acceptable) | Notable tech debt, needs planned refactoring |
| 50-69 | D (Poor) | Significant anti-patterns, hard to maintain |
| 30-49 | E (Very Poor) | Critical issues, high risk of bugs/outages |
| 0-29 | F (Unmaintainable) | Rewrite recommended, active liability |

---

## Tracking Progress

Run the assessment at regular intervals and record the score:

```
Date        Score  Grade  Notes
2024-01-15  42.3   E      Baseline before refactoring
2024-02-15  58.7   D      After security fixes
2024-03-15  71.2   C      After god class decomposition
2024-04-15  83.1   B      After error handling overhaul
```

A healthy codebase trends upward; a decaying one trends downward.
A 5-point improvement per sprint is a realistic target for dedicated refactoring work.

