# Changelog

[Scittle](https://github.com/babashka/scittle): execute Clojure(Script) directly from browser script tags via SCI!

## Unreleased

- Use `window.location.hostname` for WebSocket connection
- Upgrade sci configs to `bf8d209e`
- Update nrepl implementation, implement `eldoc` (`info`, `lookuup`)

## v0.5.14 (2023-01-05)

- Fix destructuring in `defmethod` (by upgrading SCI)

## v0.5.13 (2022-12-23)

- Fix `cljs.pprint` plugin

## v0.5.12 (2022-12-23)

- Fix `reagent` `with-let` macro with advanced compiled builds
- Upgrade promesa and shadow-cljs
- Fix `#queue` literal
- SCI: performance improvements

## v0.4.11 (2022-11-23)

- Add `scittle.re-frame` plugin. This gives access to the
  [re-frame](https://github.com/day8/re-frame) library.
- Fix for [44](https://github.com/babashka/scittle/issues/44): Honoring `SCITTLE_NREPL_WEBSOCKET_PORT` in `scittle.nrepl`
- Add all public vars of `cljs-ajax` `ajax.core`
- Upgrade several built-in libraries

## v0.3.10

- Add `scittle.promesa.js` plugin. This gives access to the [promesa](https://cljdoc.org/d/funcool/promesa/8.0.450/doc/user-guide) library.
- Add `scittle.pprint.js` plugin. This gives access to [cljs.pprint](https://cljs.github.io/api/cljs.pprint/).
- Improve error messages

## v0.2.8

- Upgrade to SCI 0.3.1
- Upgrade to Reagent 1.1.0
- Add support for Reagent's `create-class` and `with-let`

## v0.1.1

- Upgrade to SCI 0.3.0

## v0.1.0

- Fixes for `try/catch`

## v0.0.1

Initial release.
