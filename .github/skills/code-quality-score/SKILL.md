---
name: code-quality-score
description: >
  Assess the code quality of a Java/JSP codebase by measuring anti-patterns across 15 dimensions
  and producing a score out of 100. Use this skill when asked to evaluate code quality, assess
  maintainability, measure technical debt, check for anti-patterns, or score a codebase.
  Works on any Java project: Spring Boot, Jakarta EE, Struts, plain Java, etc.
argument-hint: 'Optional: path to source directory (default: src/)'
user-invocable: true
disable-model-invocation: false
---

# Code Quality Anti-Pattern Assessment

Measures how difficult a Java codebase is to maintain and how low its reliability is.
Produces a quality score from 0 (unmaintainable) to 100 (excellent).

---

## Usage Instructions

### Step 1: Run the Assessment
```bash
cd <project-root>
bash .github/skills/code-quality-score/scripts/assess-quality.sh [source-directory]

# With verbose output (shows matched files per dimension):
bash .github/skills/code-quality-score/scripts/assess-quality.sh --verbose [source-directory]
```
Default source directory: `src/`. Uses only standard Unix tools (grep, wc, find, awk).

### Step 2: Present the Results
The script outputs: Codebase Overview → Total Score with Grade → 15-Dimension Breakdown.

### Step 3: Analyze and Advise
After presenting raw results:
1. Highlight the **3 worst dimensions** (lowest score relative to max)
2. For each, explain what anti-patterns were found and their real-world impact
3. Suggest **top 3 improvements** that would most improve the score
4. If comparing before/after, show the delta per dimension

---

## Assessment Criteria

Each dimension below is measured with **deterministic, verifiable rules**. Every pattern
can be independently verified by running the documented grep command.

### Grade Scale
| Score | Grade | Meaning |
|-------|-------|---------|
| 90-100 | A | Excellent — clean, well-structured, production-ready |
| 80-89 | B | Good — minor issues, maintainable with small effort |
| 70-79 | C | Acceptable — notable tech debt, needs planned refactoring |
| 50-69 | D | Poor — significant anti-patterns, hard to maintain |
| 30-49 | E | Very Poor — critical issues, high risk of bugs/outages |
| 0-29 | F | Unmaintainable — rewrite recommended, active liability |

### Normalization
All raw counts are normalized per 1,000 lines of code (KLOC) to enable fair comparison
across codebases of different sizes. See [scoring-rubric.md](./references/scoring-rubric.md) for formulas.

---

### D1: Security Vulnerabilities (15 points)

**Definition:** Code patterns that directly enable security exploits — injection attacks,
exposed credentials, and database access from presentation layers.

#### Metrics

**M1.1: SQL Injection Vectors**
- **Pattern:** SQL keyword string literals (`"SELECT`, `"INSERT`, `"UPDATE`, `"DELETE`) on
  lines that also contain string concatenation (`+`)
- **Matches:** `"SELECT * FROM users WHERE id = " + userId`
- **Does NOT match:** `session.createQuery("FROM Book WHERE id = :id")` (parameterized)
- **Does NOT match:** `String SQL_TEMPLATE = "SELECT * FROM books";` (no concatenation)

**M1.2: Hardcoded Credentials**
- **Pattern:** Common credential keywords (`password`, `passwd`, `pwd`, `secret`, `api_key`,
  `apikey`, `token`, `credential`) appearing near string literal assignments
- **Matches:** `String password = "admin123";` or `pwd_hash = "abc"`
- **Does NOT match:** `request.getParameter("password")` (reading, not hardcoding)
- **Does NOT match:** `// password validation logic` (comment only)

**M1.3: JDBC in View Layer**
- **Pattern:** JSP files (`.jsp`) containing `DriverManager` or `getConnection`
- **Matches:** Any JSP with direct database access
- **Weight:** ×5 (severe — database logic in views)

**M1.4: JDBC in Wrong Layer**
- **Pattern:** Files in `/model/`, `/form/`, or `/action/` directories containing
  `DriverManager` or `java.sql.*` import
- **Matches:** Model classes directly querying the database
- **Weight:** ×3 (architectural violation)

#### Thresholds (per KLOC, after weighting)
| Density | Points Lost | Rating |
|---------|-------------|--------|
| 0.0 | 0 | Excellent |
| 0.1-1.0 | 1-3 | Minor issues |
| 1.1-5.0 | 3-9 | Significant |
| 5.1+ | 10-15 | Critical |

---

### D2: Error Handling (12 points)

**Definition:** Patterns that suppress, hide, or mishandle errors, leading to silent
failures and impossible debugging.

#### Metrics

**M2.1: Generic Exception Catches**
- **Pattern:** `catch (Exception` or `catch(Exception` in Java files
- **Matches:** `catch (Exception e) { log(e); }` — catches everything indiscriminately
- **Does NOT match:** `catch (IOException e)` — specific, targeted catch

**M2.2: Catch Throwable**
- **Pattern:** `catch (Throwable` or `catch(Throwable`
- **Matches:** Catches even `Error` (OutOfMemoryError, StackOverflowError)
- **Weight:** ×3

**M2.3: Catch NullPointerException**
- **Pattern:** `catch (NullPointerException` or `catch(NullPointerException`
- **Matches:** Masking null-safety bugs instead of fixing them
- **Weight:** ×3

**M2.4: Empty Catch Blocks**
- **Pattern:** `catch` followed by `{ }` with no code between braces
- **Matches:** `catch (Exception e) { }` — silently swallows errors
- **Does NOT match:** `catch (Exception e) { log.error(e); }` — has handling
- **Weight:** ×2

**M2.5: printStackTrace Usage**
- **Pattern:** `printStackTrace` anywhere in source
- **Matches:** Using stderr instead of a logging framework
- **Weight:** ×1

#### Thresholds (per KLOC, after weighting)
| Density | Points Lost | Rating |
|---------|-------------|--------|
| 0-2 | 0-1 | Acceptable |
| 3-10 | 2-4 | Notable |
| 11-30 | 5-8 | Poor |
| 31+ | 9-12 | Critical |

---

### D3: God Classes (10 points)

**Definition:** Files that have grown excessively large, indicating too many
responsibilities and resistance to refactoring.

#### Metrics

**M3.1: Files Over 500 LOC**
- **Detection:** `wc -l` on each `.java` file, count those > 500
- **Weight:** ×0.5 per file

**M3.2: Files Over 1000 LOC**
- **Detection:** Same scan, additional penalty for extreme size
- **Weight:** ×1.5 per file (on top of M3.1)

**M3.3: Code Concentration**
- **Detection:** Largest file LOC ÷ total Java LOC × 100
- **Weight:** ×0.2 per percentage point
- **Example:** If largest file is 2,740 of 27,868 LOC = 9.8%, deduction = 9.8 × 0.2 = 2.0

#### Thresholds
| Files > 500 LOC | Points Lost | Rating |
|-----------------|-------------|--------|
| 0 | 0 | Excellent |
| 1-2 | 1-3 | Acceptable |
| 3-5 | 4-6 | Poor |
| 6+ | 7-10 | Critical |

---

### D4: Memory & Resource Leaks (6 points)

**Definition:** Patterns that cause memory consumption to grow unbounded or database
connections/file handles to be exhausted.

#### Metrics

**M4.1: Mutable Static Collections**
- **Pattern:** `static` (non-final) field declarations of `List`, `Map`, `Set`, `ArrayList`,
  `HashMap`, `HashSet` in Java files
- **Matches:** `private static Map cache = new HashMap();` — grows forever
- **Does NOT match:** `private static final Map CONSTANTS = Collections.unmodifiableMap(...)`
- **Weight:** ×1

**M4.2: Missing Finally Blocks**
- **Detection:** Files containing `getConnection` or `openSession` but no `finally` block
- **Matches:** DAO that opens connections but never guarantees cleanup
- **Weight:** ×2

---

### D5: Copy-Paste / Duplication (8 points)

**Definition:** Repeated code patterns that indicate copy-paste programming, making
maintenance error-prone.

#### Metrics

**M5.1: JDBC URL Copies**
- **Pattern:** `jdbc:` in source files — each occurrence is a separate hardcoded URL
- **Matches:** `"jdbc:mysql://host:3306/db"` in multiple files
- **Weight:** ×2

**M5.2: Connection Acquisition Spread**
- **Pattern:** `getConnection` or `DriverManager` in source files
- **Matches:** Raw connection creation instead of centralized pooling
- **Weight:** ×1

**M5.3: System.out.println**
- **Pattern:** `System.out.println` or `System.out.print(` in source
- **Matches:** Console output instead of logging framework
- **Weight:** ×1

**M5.4: System.err**
- **Pattern:** `System.err` in source files
- **Weight:** ×1

---

### D6: Global Mutable State (7 points)

**Definition:** Mutable static fields that create hidden coupling between requests,
race conditions, and unpredictable behavior.

#### Metrics

**M6.1: Mutable Static Fields**
- **Pattern:** `static` field declarations in Java files, excluding `static final`,
  `static void`, `static class`, and method signatures
- **Matches:** `private static int counter = 0;` — shared mutable state
- **Does NOT match:** `private static final String NAME = "app";` — immutable constant

**M6.2: Thread-Unsafe DateFormat**
- **Pattern:** `static` declarations containing `SimpleDateFormat` or `DateFormat`,
  excluding those wrapped in `ThreadLocal`
- **Matches:** `private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");`
- **Does NOT match:** `private static ThreadLocal<SimpleDateFormat> sdf = ...`
- **Weight:** ×5

---

### D7: Naming & Readability (5 points)

**Definition:** Naming conventions that hinder code comprehension and violate
standard Java practices.

#### Metrics

**M7.1: Wildcard Imports**
- **Pattern:** `import` statements ending with `.*;`
- **Matches:** `import java.util.*;` — hides actual dependencies
- **Does NOT match:** `import java.util.List;` — explicit import

**M7.2: Single-Character Field Names**
- **Pattern:** `private` field declarations with single-letter names
- **Weight:** ×2

**M7.3: Non-Standard Getter Naming**
- **Pattern:** Methods named `get` followed by a lowercase letter (e.g., `getbookId`)
  instead of standard `getBookId`
- **Weight:** ×2

---

### D8: Concurrency Safety (5 points)

**Definition:** Patterns that cause race conditions, deadlocks, or incorrect behavior
under concurrent access.

#### Metrics

**M8.1: Thread.sleep in Business Logic**
- **Pattern:** `Thread.sleep` in source files
- **Matches:** Blocking request-handling threads
- **Weight:** ×3

**M8.2: String Identity Comparison**
- **Pattern:** `== "` in Java files (comparing strings with `==` instead of `.equals()`)
- **Matches:** `if (status == "ACTIVE")` — works with interned literals, breaks with variables
- **Does NOT match:** `if ("ACTIVE".equals(status))` — correct comparison

**M8.3: Broken Locking Patterns**
- **Pattern:** `synchronized` combined with null/boolean checks (broken double-checked locking)
- **Weight:** ×5

---

### D9: Magic Numbers & Strings (5 points)

**Definition:** Hardcoded literal values instead of named constants, making code
intent unclear and changes error-prone.

#### Metrics

**M9.1: Hardcoded Status Strings**
- **Pattern:** String literals `"ACTIVE"`, `"PENDING"`, `"DRAFT"`, `"COMPLETED"`,
  `"CANCELLED"`, `"DELETED"`, `"INACTIVE"`, `"ERROR"`, `"SUCCESS"`, `"FAILED"` in Java files
- **Matches:** `if (status.equals("ACTIVE"))` — should be a constant

**M9.2: Numeric Return Codes**
- **Pattern:** `return N;` where N is 2-99 or negative — arbitrary exit codes
- **Matches:** `return 9;` — undocumented error code
- **Does NOT match:** `return 0;` or `return 1;` (standard success/failure)
- **Weight:** ×2

---

### D10: Logging Consistency (5 points)

**Definition:** Mixing multiple logging approaches indicates accumulated technical
debt from multiple developers/eras with no standardization.

#### Metrics

**M10.1: Logging Framework Count**
- **Detection:** Count distinct frameworks found in Java source:
  - `System.out.println` / `System.out.print(`
  - `java.util.logging`
  - `org.apache.commons.logging`
  - `org.apache.log4j`
  - `org.slf4j`
- **1 framework:** No deduction. **Each additional:** ×1.5 deduction

**M10.2: System.out File Spread**
- **Detection:** Number of Java files containing `System.out.println`
- **Weight:** ×0.3 per KLOC

---

### D11: Configuration Quality (5 points)

**Definition:** Configuration files that have accumulated dead entries, duplicates,
and contradictions over time.

#### Metrics

**M11.1: Commented-Out XML**
- **Pattern:** `<!--` occurrences across XML files in source directory
- **Matches:** Dead configuration entries

**M11.2: Configuration Density**
- **Pattern:** `context-param`, `init-param`, `filter-mapping` counts in XML
- Excessive configuration suggests over-complexity

---

### D12: Dead Code (5 points)

**Definition:** Code that is unreachable, disabled, or abandoned but never removed,
creating confusion and maintenance burden.

#### Metrics

**M12.1: TODO/FIXME/HACK Comments**
- **Pattern:** `// TODO`, `// FIXME`, `// HACK`, `// XXX`, `// BUG` in source
- **Matches:** Acknowledged but unresolved issues

**M12.2: Commented-Out Code**
- **Pattern:** Lines starting with `//` followed by code keywords (`return`, `if (`, `for (`)
- **Matches:** `// return oldValue;` — dead code left in place
- **Does NOT match:** `// This method handles authentication` — documentation comment

**M12.3: Feature Flags**
- **Pattern:** `boolean` field assignments to `false` followed by comments containing
  `TODO`, `FIXME`, `disabled`, `deprecated`, `never`, `not yet`, `rolled back`
- **Also matches:** Variable names containing `_V2`, `_OLD`, `_DEPRECATED`, `_DISABLED`
- **Weight:** ×2

---

### D13: Method Complexity (5 points)

**Definition:** Methods that are too long or too deeply nested, indicating logic
that should be decomposed.

#### Metrics

**M13.1: Average Method Size**
- **Detection:** Total Java LOC ÷ method signature count
- Larger ratio = fewer, longer methods

**M13.2: Deep Nesting**
- **Pattern:** Lines indented 24+ spaces (6+ nesting levels)
- **Matches:** Deeply nested if/for/while/try blocks

---

### D14: Dependency Structure (4 points)

**Definition:** Coupling patterns that resist modular decomposition and testing.

#### Metrics

**M14.1: Circular Dependencies**
- **Detection:** Manager/service Java files where class A imports B and B imports A
- **Weight:** ×2 per circular pair

**M14.2: Tight Coupling**
- **Pattern:** `new SomethingImpl(` or `new SomethingManager(` or `new SomethingHelper(`
  in files that are NOT themselves Impl/Manager/Helper/Factory classes
- **Matches:** Hardcoded instantiation instead of dependency injection
- **Weight:** ×0.5 per KLOC

---

### D15: JSP / View Layer (3 points)

**Definition:** Server-side Java code embedded in presentation templates, violating
MVC separation.

*Note: If no JSP files exist, this dimension scores full points (3/3).*

#### Metrics

**M15.1: Scriptlet Blocks**
- **Pattern:** `<%` in JSP files (excluding `<%@` directives and `<%--` comments)
- **Matches:** `<% if (user != null) { %>` — Java logic in view

**M15.2: JDBC in JSPs**
- **Pattern:** JSP files containing `Connection`, `DriverManager`, or `ResultSet`
- **Weight:** ×0.5 per file

**M15.3: Reflection in JSPs**
- **Pattern:** `getMethod` or `invoke(` in JSP files
- **Weight:** ×0.3 per occurrence

---

## Reference
See [Scoring Rubric](./references/scoring-rubric.md) for mathematical formulas and
normalization methodology.

