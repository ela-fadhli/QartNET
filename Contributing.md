# Contributing to QartNET

This document defines the development workflow, coding standards, and collaboration rules for the QartNET team. Everyone on the team is expected to follow these guidelines to keep the codebase clean, consistent, and reviewable.

---

## Table of Contents

- [Workflow Overview](#workflow-overview)
- [Branch Strategy](#branch-strategy)
- [Commit Messages](#commit-messages)
- [Pull Request Rules](#pull-request-rules)
- [Code Style](#code-style)
- [Definition of Done](#definition-of-done)
- [What NOT to Commit](#what-not-to-commit)
- [References & Documentation](#references--documentation)

---

## Workflow Overview

QartNET uses a **feature-branch workflow** inspired by Git Flow.

```
main
 └── develop              ← integration branch (always deployable)
      ├── feature/...     ← new features
      ├── fix/...         ← bug fixes
      └── chore/...       ← config, tooling, refactoring
```

The rules:
- **`main`** — production-ready code only. Never push directly.
- **`develop`** — the shared integration branch. All features merge here via PR.
- **`feature/*`** — one branch per feature or task. Short-lived, focused.
- **No one force-pushes to `develop` or `main`.** Ever.

---

## Branch Strategy

### Naming Convention

```
<type>/<module>-<short-description>
```

| Type | When to use |
|---|---|
| `feature` | New functionality |
| `fix` | Bug fix |
| `chore` | Refactoring, config, tooling, no logic change |
| `docs` | Documentation only |
| `test` | Adding or fixing tests |

**Examples:**

```bash
feature/auth-email-verification
feature/git-repo-creation
feature/forum-tag-filter
fix/auth-jwt-expiry
fix/messaging-websocket-reconnect
chore/backend-package-restructure
docs/api-endpoints
```

### Creating a Branch

Always branch off `develop`:

```bash
git checkout develop
git pull origin develop
git checkout -b feature/your-module-description
```

### Keeping Your Branch Up to Date

Rebase frequently to avoid painful merge conflicts:

```bash
git fetch origin
git rebase origin/develop
```

---

## Commit Messages

Follow the **Conventional Commits** standard. Every commit message must be structured as:

```
<type>(<scope>): <short description>

[optional body]

[optional footer]
```

### Types

| Type | Meaning |
|---|---|
| `feat` | A new user-facing feature |
| `fix` | A bug fix |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `perf` | A performance improvement (query optimization, caching, indexing) |
| `test` | Adding or updating tests |
| `docs` | Documentation only changes |
| `chore` | Foundational work: entities, repositories, DTOs, dependencies, tooling |
| `style` | Formatting, missing semicolons, no logic change |
| `revert` | Reverts a previous commit |
| `wip` | Work in progress: incomplete, not ready for review |

### Scopes

`auth` · `user` · `profile` · `repo` · `review` · `project` · `phase` · `task` · `forum` · `group` · `messaging` · `notification` · `search` · `admin` · `shared` · `ws` · `db` · `config` · `frontend`

### Examples

```
feat(auth): add email confirmation on registration
fix(git): resolve push failure when repo is empty
refactor(forum): extract tag filtering into dedicated service
test(auth): add unit tests for JWT token validation
docs(readme): add backend setup instructions
chore(config): configure CORS for local development
```

### Rules

- Use the **imperative mood**: "add feature" not "added feature"
- Keep the subject line under **72 characters**
- No period at the end of the subject line
- Reference issues/tasks in the footer when applicable: `Closes #12`

---

## Pull Request Rules

### Before Opening a PR

- [ ] Your branch is rebased on the latest `develop`
- [ ] The code compiles without errors
- [ ] You have tested your changes manually
- [ ] No debug logs, commented-out code, or `TODO` comments left in
- [ ] No secrets, credentials, or local config files are included

### PR Title

Follow the same format as commit messages:

```
feat(forum): add category-based thread filtering
```

### PR Description Template

```markdown
## What does this PR do?
<!-- Briefly describe the change -->

## How to test
<!-- Steps to verify the feature/fix works -->

## Related
<!-- Link to task, issue, or Cahier des Charges section -->

## Checklist
- [ ] Code compiles
- [ ] Manually tested
- [ ] No secrets committed
- [ ] Follows code style guidelines
```

### Review Rules

- **At least 1 approval** is required before merging into `develop`
- The PR author **cannot approve their own PR**
- Reviewers should respond within **24 hours** (we're a small team, keep it moving)
- Address all review comments before merging — "Resolve" only when actually resolved
- **Squash and merge** into `develop` to keep history clean

---

## Code Style

### Backend (Java / Spring Boot)

- Follow standard **Java naming conventions** (camelCase methods, PascalCase classes, UPPER_SNAKE_CASE constants)
- One class per file
- Use **constructor injection** — never `@Autowired` on fields
- DTOs go in `shared/dto` or the module's own `dto/` sub-package
- All endpoints must return consistent response wrappers `ResponseEntity<ApiResponse<T>>` (a shared `ApiResponse<T>` class in `shared/dto`)
- Never expose entity classes directly in API responses — always use DTOs
- Write at least one unit test per service method

## Exception Handling

QartNET uses a centralized exception handling strategy. All exceptions bubble up
to `GlobalExceptionHandler`, which converts them into consistent `ApiResponse`
error bodies. Controllers must never catch exceptions just to return an error
response manually — let the handler do it.

### Rules

**Throw domain exceptions from the service layer, never from controllers.**
Controllers only handle the happy path. If something is wrong, the service throws,
the handler catches, and the client gets a clean `ApiResponse.error(...)`.

**Never swallow exceptions silently.**
Do not write empty `catch` blocks or catch an exception just to return `null`.
If you catch something, either handle it meaningfully or rethrow it.

**Never expose raw exception messages to the client.**
`e.getMessage()` from a JPA or JGit exception may contain internal details
(table names, file paths, stack traces). Always wrap in a controlled message.

**Use the existing exceptions before creating new ones.**
Check `shared/exception/` first. Only create a new exception class if none of
the existing ones fit semantically.

### Existing exceptions

| Class | HTTP status | When to use |
|---|---|---|
| `ResourceNotFoundException` | 404 | Entity not found by id or slug |
| `BadCredentialsException` (Spring) | 401 | Wrong email or password |
| `AccessDeniedException` (Spring) | 403 | User lacks permission for this action |
| `MethodArgumentNotValidException` (Spring) | 400 | `@Valid` fails on a request body |

### Adding a new exception

When you need a new exception type, create it in `shared/exception/`, extend
`RuntimeException`, and register a handler for it in `GlobalExceptionHandler`.
Never create exception classes inside a feature package.
```java
// shared/exception/ConflictException.java
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}

// then in GlobalExceptionHandler:
@ExceptionHandler(ConflictException.class)
public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage()));
}
```

### Frontend (Angular / TypeScript)

- Use **strict TypeScript**
- Follow the Angular style guide: one component/service/module per file
- All components, directives, and pipes must be declared with `standalone: true`
- Use **reactive forms** (not template-driven) for any form with validation
- Use modern control flow syntax in templates (`@if`, `@for`, `@switch`)
- Use Angular's `HttpClient` only inside services, never in components
- Keep components **dumb** (display + events only); push logic into services
- Name components with the feature prefix: `ForumThreadListComponent`, not just `ThreadListComponent`
- Use **Tailwind** for layout, spacing, and responsive breakpoints & use **PrimeNG** for all UI components. The two are independent and complementary; never use one to replicate what the other already provides

### General

- Delete dead code — don't comment it out
- Don't mix refactoring with feature changes in the same commit
- If you touch a file and notice an unrelated bug, fix it in a **separate commit**

---


## Definition of Done

A feature is **Done** when:

1. The feature works as described in the Cahier des Charges
2. The PR has been approved by at least one teammate
3. The code is merged into `develop`
4. No regressions on the features that existed before
5. At minimum a basic unit test exists for backend logic
6. The feature can be demonstrated end-to-end (backend + frontend connected)

---

## What NOT to Commit

The following must **never** be committed and should be covered in `.gitignore`:

| File / Pattern | Reason |
|---|---|
| `application-local.properties` | Contains DB credentials |
| `.env` files | Contains secrets |
| `environment.prod.ts` `environment.development.ts` `environment.local.ts`| Contains sensitive data |
| `*.class`, `target/` | Compiled Java artifacts |
| `node_modules/` | Frontend dependencies |
| `.idea/`, `.vscode/` | IDE-specific config |
| Any file with real passwords, JWT secrets, API keys | Security |

If you accidentally commit a secret:
1. **Do not push** if you haven't yet
2. If pushed, notify the team immediately, **the secret must be rotated**
3. Use `git rebase -i` or `git reset` to remove it from history before the next push

---

## References & Documentation

### General
- [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
- [GitHub: Removing sensitive data from a repository](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)

### Backend
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Spring: Dependency injection & constructor injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html)
- [Spring: `@RestControllerAdvice`](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-advice.html)
- [Spring Boot: Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
### Frontend
- [Angular Style Guide](https://angular.dev/style-guide)
- [Angular: Reactive Forms](https://angular.dev/guide/forms/reactive-forms)
- [Angular: HttpClient](https://angular.dev/guide/http)
- [Angular: Signals](https://angular.dev/guide/signals)
- [Angular: Control flow syntax (`@if`, `@for`)](https://angular.dev/guide/templates/control-flow)
- [PrimeNG: Installation & theming](https://primeng.org/installation)
- [PrimeNG: Design tokens & `definePreset`](https://primeng.org/theming/styled#definePreset)
- [PrimeNG: Tailwind CSS integration overview](https://primeng.org/tailwind)
- [Tailwind CSS v4: What's new](https://tailwindcss.com/blog/tailwindcss-v4)
- [TypeScript: Strict mode](https://www.typescriptlang.org/tsconfig/#strict)