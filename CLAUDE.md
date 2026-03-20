# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**The Nothing System** is a Java/Kotlin desktop IDE that reimplements the concepts of Niklaus Wirth's Oberon System. The core idea: "Everything is a text" — users write, compile, and execute Java code directly within the environment using a text-command interface. Commands take the form `ClassName.methodName [args...]`.

## Build & Run

```bash
./gradlew run          # Run the application
./gradlew build        # Build the project
./gradlew packageDmg   # macOS distribution
./gradlew packageMsi   # Windows distribution
./gradlew packageDeb   # Linux distribution
```

**Requirements:** JDK 17+. Main class: `com.excelsior.nothing.MainKt`.

There are no automated tests in this project.

## Architecture

The system has two source trees:
- `src/main/kotlin/` — active Kotlin/Compose rewrite (the live codebase)
- `src/com/` — legacy Java implementation (kept for reference/gradual migration)

### Core Engine (`src/main/kotlin/com/excelsior/nothing/`)

| File | Role |
|------|------|
| `Main.kt` | Entry point; initializes Output window, loads `commands.txt`, starts Compose app |
| `AppState.kt` | Central state (Compose `State`); owns all open text/frame windows and a registry map |
| `Kernel.kt` | Command execution engine; parses `ClassName.method [args]` and dispatches via reflection |
| `MethodHandle.kt` | Reflection layer; dynamic class loading via `OneClassClassLoader`, string-to-type argument coercion |
| `Sys.kt` | File I/O, `javac` compilation, script execution |
| `Editor.kt` | Rich text formatting (bold/italic/color) and document serialization |
| `GUIBuilder.kt` | Dynamic panel creation (buttons, text fields) with proxy wrappers |
| `Calc.kt` | Expression evaluator (JavaScript engine with arithmetic fallback) |

### UI Layer (`src/main/kotlin/com/excelsior/nothing/ui/`)

Built with Jetbrains Compose for Desktop + Material3. Layout: 80% MDI user pane (left) + 20% system panes (right).

| File | Role |
|------|------|
| `NothingDesktop.kt` | Root layout composable |
| `MdiPane.kt` | Hosts all open windows in a key-based container |
| `InternalWindow.kt` | Draggable, resizable window wrapper |
| `Stylepad.kt` | Text editor with styling support |
| `DynamicPanel.kt` | Renders dynamically-built GUI panels |

### Persistence (`src/com/excelsior/nothing/persistance/`)

Custom Java serialization (`PersistentObjectOutputStream`/`PersistentObjectInputStream`) designed to be independent of JDK micro-version — this is an active area of work (see recent commits on `claude-experiments` branch).

## Command Execution Flow

1. User types a command (e.g., `Sys.open myfile.txt`) in any text window
2. `Kernel.executeCommand()` parses it using `QuotedStringTokenizer`
3. `MethodHandle.getMethodHandle()` locates the method via reflection (with caching)
4. Arguments are coerced from strings to target types and the method is invoked
5. Results appear in the Output window (stdout/stderr are redirected at startup)

Commands can target: static methods, instance methods on the `AppState` registry, or chained calls like `ClassName.staticField.method`.

## Key Design Patterns

- **Everything via text commands** — no menus, just clickable text links in documents
- **Dynamic class loading** — `Sys.compile` + `OneClassClassLoader` enables live code execution
- **`AppState` as single source of truth** — all window state flows through here; UI observes it via Compose state
- **Dual source trees** — when editing serialization or controls, check both `src/main/kotlin/` and `src/com/` to understand intent
