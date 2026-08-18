# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Scittle exposes the Small Clojure Interpreter (SCI) for use in browser `<script>` tags. Users write ClojureScript in `<script type="application/x-scittle">` tags and Scittle evaluates it in the browser without a compilation step.

## Build Commands

Requires [Babashka](https://babashka.org/) (`bb`) and Node.js/npm.

- **`bb dev`** — Start development (shadow-cljs watch + HTTP server on port 1341 + browser nREPL)
- **`bb prod`** — Production build (cleans first, outputs optimized JS to `resources/public/js/` and dev build to `resources/public/js/dev/`)
- **`bb clean`** — Remove build artifacts (js output, .cpcache, .shadow-cljs)

There is no automated test suite. Testing is done manually via HTML pages in `resources/public/` (e.g., `index.html`, `tictactoe.html`, `wordle.html`).

## Architecture

### Core (`src/scittle/core.cljs`)

Entry point. Initializes the SCI context with base namespaces (`clojure.core` extras, `goog.object`, `goog.string`, `sci.core`). On `DOMContentLoaded`, auto-evaluates all `<script type="application/x-scittle">` tags. Exports `eval-string` and `eval-script-tags` for programmatic use. The SCI context lives in `sci.ctx-store`.

### Plugin System

Each plugin registers additional SCI namespaces via `scittle.core/register-plugin!`, which calls `sci/merge-opts` on the shared context.

**Built-in plugins** (`src/scittle/*.cljs`): reagent, re-frame, promesa, pprint, js-interop, cljs-ajax, replicant, nrepl, cljs-devtools. Each is a separate shadow-cljs `:module` with `:depends-on` relationships (defined in `shadow-cljs.edn`).

**Plugin pattern** — a plugin file:
1. Imports the library and its `sci.configs` namespace mappings
2. Calls `scittle/register-plugin!` with a map of `{namespace-symbol var-map}`

Example (`src/scittle/reagent.cljs`):
```clojure
(scittle/register-plugin!
 ::reagent
 {:namespaces {'reagent.core reagent-namespace
               'reagent.dom reagent-dom-namespace}})
```

### External Plugin System (`build/src/scittle/build.clj`)

External projects can create plugins by placing a `scittle_plugin.edn` on the classpath. The build system auto-discovers these files and merges their shadow-cljs module configs. See `plugins/hoplon/` for an example.

### Key Dependencies

- **SCI** (`org.babashka/sci`) — the interpreter core
- **sci.configs** (`io.github.babashka/sci.configs`) — pre-built SCI namespace configs for popular libraries
- **Shadow CLJS** — ClojureScript compiler producing browser-targeted JS modules
- **Babashka** — task runner (`bb.edn`) and build orchestration

## Contributing Conventions

- Create an issue before writing code; keep changes small
- Do not use `git push --force` on PR branches

# Clojure REPL Evaluation

The command `clj-nrepl-eval` is installed on your path for evaluating Clojure code via nREPL.

**Discover nREPL servers:**

`clj-nrepl-eval --discover-ports`

**Evaluate code:**

`clj-nrepl-eval -p <port> "<clojure-code>"`

With timeout (milliseconds)

`clj-nrepl-eval -p <port> --timeout 5000 "<clojure-code>"`

The REPL session persists between evaluations - namespaces and state are maintained.
Always use `:reload` when requiring namespaces to pick up changes.
