# Demo

A demo project of a custom scittle build.

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

- SCI configuration (this can be shared with users in the [sci.configs](https://github.com/babashka/sci.configs) project
- Adding a `scittle_plugin.edn` file on the plugin's classpath (e.g. in the `src` directory). This EDN file contains:
  - `:name`, name of the plugin
  - `:namespaces`: the namespaces exposed to SCI
  - `:js`: the name of the produced `.js` module file
  - `:shadow-config`: the shadow-cljs configuration specific to this plugin
- A `.cljs` file with an `init` function which calls `scittle/register-plugin!`.
