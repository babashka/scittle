# Demo

A demo project of a custom scittle build.

This demo project uses the `scittle.javelin` and `scittle.hoplon` plugins which aren't part of the normal scittle distribution.

To produce release `.js` files, run: `bb release`.

See:

- `bb.edn` with
  - `:deps` which includes:
    - a dependency on the `scittle.build` project to build scittle + custom features
    - zero or more plugin dependencies
    - helpers like static file server
  - development `:tasks`. Run `bb dev` for development and `bb release` to produce release artifacts.
- `deps.edn`: this only contains a dependency on scittle itself

Available plugins are in the `plugins` directory inside the top level directory of this repo.

Writing a plugin involves writing

- SCI configuration (this can be shared via the [sci.configs](https://github.com/babashka/sci.configs) project too)
- Adding a `scittle_plugin.edn` file on the plugin's classpath (e.g. in the `src` directory). This EDN file contains:
  - `:name`, name of the plugin
  - `:namespaces`: the namespaces exposed to SCI
  - `:js`: the name of the produced `.js` module file
  - `:shadow-config`: the shadow-cljs configuration specific to this plugin
- A `.cljs` file with an `init` function which calls `scittle/register-plugin!`.
