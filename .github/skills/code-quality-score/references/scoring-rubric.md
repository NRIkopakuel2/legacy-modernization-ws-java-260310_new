# Scoring Rubric — Code Quality Anti-Pattern Assessment

## Overview
This document describes the detailed scoring methodology for each of the 15 dimensions.
All metrics are normalized per 1,000 lines of code (KLOC) to enable fair comparison across
codebases of different sizes.

## General Formula
Each dimension starts at its maximum points and deducts based on anti-pattern density:

```
dimension_score = max(0, max_points - deduction)
deduction = normalized_metric * deduction_rate
normalized_metric = raw_count / KLOC
```

---

## Dimension 1: Security Vulnerabilities (15 points)

**Why it matters:** Security flaws are the highest-impact code quality issue. A single SQL
injection can compromise an entire database.

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| SQL injection vectors | String concatenation in SQL queries (`"SELECT..." +`) | ×1 |
| Hardcoded credentials | Patterns like `password=`, `legacy_pass` in source | ×1 |
| JDBC in view layer | JSP files containing `DriverManager`/`getConnection` | ×5 |
| JDBC in wrong layer | Model/form/action files with `java.sql.*` | ×3 |

**Deduction rate:** 0.3 per normalized point

### Score Interpretation
| Score | Meaning |
|-------|---------|
| 13-15 | No or minimal security issues |
| 9-12 | Some hardcoded values, minor exposure |
| 5-8 | SQL injection present, credentials in source |
| 0-4 | Critical: widespread injection, credentials everywhere |

---

## Dimension 2: Error Handling (12 points)

**Why it matters:** Poor error handling leads to silent failures, data corruption, and
impossible debugging in production.

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Generic `catch(Exception)` | Grep for catch(Exception | ×1 |
| `catch(Throwable)` | Catches everything including Error | ×3 |
| `catch(NullPointerException)` | Masking null bugs | ×3 |
| Empty catch blocks | `catch(...) { }` with no handling | ×2 |
| `printStackTrace()` | Not using a logging framework | ×1 |

**Deduction rate:** 0.2 per normalized point

---

## Dimension 3: God Classes (10 points)

**Why it matters:** God classes are the #1 indicator of architectural decay. They resist
refactoring, testing, and team collaboration.

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Files > 500 LOC | `wc -l` per Java file | ×0.5 per file |
| Files > 1000 LOC | Additional penalty for extreme size | ×1.5 per file |
| Code concentration | Largest file LOC / total LOC | ×0.2 per % |

---

## Dimension 4: Memory & Resource Leaks (6 points)

**Why it matters:** Memory leaks cause OutOfMemoryError in production. Resource leaks
exhaust connection pools and file handles.

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Static collections | `static List/Map/Set/ArrayList/HashMap` (mutable) | ×1 |
| Missing finally blocks | Files with getConnection/openSession but no finally | ×2 |

---

## Dimension 5: Copy-Paste / Duplication (8 points)

**Why it matters:** Duplicated code means bugs must be fixed in multiple places. Missed
copies become divergent behavior.

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| JDBC URL copies | `jdbc:mysql://` pattern count | ×2 |
| getConnection calls | Spread of raw connection acquisition | ×1 |
| System.out.println | Console output instead of logging | ×1 |
| System.err usage | Error console output | ×1 |

---

## Dimension 6: Global Mutable State (7 points)

**Why it matters:** Mutable static state creates hidden coupling between requests,
race conditions, and unpredictable behavior.

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Mutable static fields | `static` (non-final) field declarations | ×1 |
| Thread-unsafe DateFormat | Static SimpleDateFormat without ThreadLocal | ×5 |

---

## Dimension 7: Naming & Readability (5 points)

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Wildcard imports | `import.*\.\*;` | ×1 |
| Single-char variables | `private .* [a-z];` | ×2 |
| Mixed getter naming | `get[a-z][a-z]` (lowercase after get) | ×2 |

---

## Dimension 8: Concurrency Safety (5 points)

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Thread.sleep in handlers | `Thread.sleep` in source | ×3 |
| String == comparison | `== "` in Java files | ×1 |
| Broken DCL patterns | synchronized + null check without volatile | ×5 |

---

## Dimension 9: Magic Numbers & Strings (5 points)

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Hardcoded status strings | "ACTIVE", "PENDING", "DRAFT", etc. | ×1 |
| Numeric return codes | `return N;` where N > 1 or N < 0 | ×2 |

---

## Dimension 10: Logging Consistency (5 points)

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Number of logging frameworks | Count of distinct frameworks used | ×1.5 per extra |
| System.out file count | Files using System.out.println | ×0.3/KLOC |

**Framework detection:** System.out, java.util.logging, commons-logging, log4j, slf4j

---

## Dimension 11: Configuration Quality (5 points)

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Commented-out XML | `<!--` count in XML files | ×1 |
| Config param density | context-param, init-param, filter-mapping count | ×1 |

---

## Dimension 12: Dead Code (5 points)

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| TODO/FIXME/HACK comments | Pattern count in source | ×1 |
| Commented-out code | Lines starting with `//` followed by code keywords | ×1 |
| Feature flags | LEGACY_MODE, USE_NEW_, ENABLE_...=false patterns | ×2 |

---

## Dimension 13: Method Complexity (5 points)

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Average method size | LOC / method_count | Based on ratio |
| Deep nesting | Lines with 24+ spaces of indentation | ×0.1/KLOC |

---

## Dimension 14: Dependency Structure (4 points)

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Circular dependencies | Manager/service files that import each other | ×2 |
| Tight coupling | Non-factory files using `new SomethingImpl()` | ×0.5/KLOC |

---

## Dimension 15: JSP / View Layer (3 points)

### Metrics
| Metric | How Measured | Weight |
|--------|-------------|--------|
| Scriptlet blocks | `<%` (non-directive) in JSP files | ×0.1/KLOC |
| JDBC in JSPs | JSP files with Connection/DriverManager | ×0.5 each |
| Reflection in JSPs | getMethod/invoke in JSP files | ×0.3 each |

---

## Interpreting Results

### What Each Grade Means for a Team

| Grade | Developer Experience | Risk Level | Recommended Action |
|-------|---------------------|------------|-------------------|
| A (90+) | Productive, confident | Low | Continue current practices |
| B (80-89) | Mostly smooth | Low-Medium | Address in sprint planning |
| C (70-79) | Friction on some areas | Medium | Allocate 20% time to tech debt |
| D (50-69) | Frustrating, slow | High | Dedicated refactoring effort |
| E (30-49) | Painful, risky changes | Very High | Architectural intervention |
| F (0-29) | Dangerous, unpredictable | Critical | Consider rewrite/replace |

### Tracking Progress
Run this assessment regularly (e.g., monthly) and track the score over time.
A healthy codebase trends upward; a decaying one trends downward.
