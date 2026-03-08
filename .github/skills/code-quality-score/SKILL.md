---
name: code-quality-score
description: >
  Assess the code quality of a Java/JSP codebase by measuring anti-patterns across 15 dimensions
  and producing a score out of 100. Use this skill when asked to evaluate code quality, assess
  maintainability, measure technical debt, check for anti-patterns, or score a codebase.
  Covers: security vulnerabilities, error handling, god classes, memory leaks, duplication,
  global state, naming, concurrency, magic numbers, logging, config, dead code, complexity,
  dependencies, and JSP view layer issues.
argument-hint: 'Optional: path to source directory (default: src/)'
user-invocable: true
disable-model-invocation: false
---

# Code Quality Anti-Pattern Assessment

## Purpose
Measure how difficult a Java codebase is to maintain and how low its reliability is by
counting anti-patterns across 15 dimensions. Produces a quality score from 0-100.

## When to Use
- User asks to "assess code quality" or "evaluate the codebase"
- User asks about "technical debt", "maintainability", or "code smells"
- User asks to "score" or "rate" the code
- User asks about "anti-patterns" or "spaghetti code"
- User wants to compare before/after a refactoring effort

## How to Run

### Step 1: Execute the Assessment Script
Run the assessment script on the target source directory:

```bash
cd <project-root>
bash .github/skills/code-quality-score/scripts/assess-quality.sh [source-directory]
```

- Default source directory: `src/`
- The script uses only standard Unix tools (grep, wc, find, awk)
- It takes 5-30 seconds depending on codebase size

### Step 2: Read and Present the Results
The script outputs:
1. **Codebase Overview** — file counts and LOC
2. **Total Score** — out of 100 with letter grade (A-F)
3. **Dimension Breakdown** — 15 dimensions with individual scores and key metrics
4. **Score Interpretation** — what the grade means

### Step 3: Provide Analysis
After presenting the raw results, add your analysis:
- Highlight the **3 worst dimensions** (lowest scores relative to max)
- For each, explain the specific anti-patterns found and their impact
- Suggest the **top 3 actionable improvements** that would most improve the score
- If comparing before/after, show the score delta per dimension

## Scoring System

### Dimensions (15 total, 100 points)

| Tier | Dimension | Max Points | What It Measures |
|------|-----------|-----------|------------------|
| Critical | Security Vulnerabilities | 15 | SQL injection, hardcoded credentials, JDBC in views |
| Critical | Error Handling | 12 | Generic catches, empty catches, printStackTrace |
| Critical | God Classes | 10 | Files >500 LOC, code concentration |
| Critical | Memory & Resource Leaks | 6 | Static collections, unclosed connections |
| High | Copy-Paste / Duplication | 8 | JDBC URLs, getConnection spread, System.out |
| High | Global Mutable State | 7 | Mutable static fields, thread-unsafe formatters |
| High | Naming & Readability | 5 | Wildcard imports, mixed getter conventions |
| High | Concurrency Safety | 5 | Thread.sleep, String ==, broken locking |
| High | Magic Numbers & Strings | 5 | Hardcoded status strings, return codes |
| Moderate | Logging Consistency | 5 | Mixed frameworks, System.out file count |
| Moderate | Configuration Quality | 5 | Commented-out XML, config duplication |
| Moderate | Dead Code | 5 | TODO/FIXME, feature flags, commented code |
| Moderate | Method Complexity | 5 | Average method size, deep nesting |
| Moderate | Dependency Structure | 4 | Circular dependencies, tight coupling |
| Moderate | JSP / View Layer | 3 | Scriptlets, inline JDBC, reflection |

### Grade Scale
- **90-100 (A):** Excellent — clean, well-structured, production-ready
- **80-89 (B):** Good — minor issues, maintainable with small effort
- **70-79 (C):** Acceptable — notable tech debt, needs planned refactoring
- **50-69 (D):** Poor — significant anti-patterns, hard to maintain
- **30-49 (E):** Very Poor — critical issues, high risk of bugs/outages
- **0-29 (F):** Unmaintainable — rewrite recommended, active liability

## Output Format
Present results in this structure:
1. Score headline with emoji indicator
2. Dimension breakdown table
3. Top 3 problem areas with explanations
4. Top 3 recommended improvements

## Reference
See [Scoring Rubric](./references/scoring-rubric.md) for detailed methodology.
