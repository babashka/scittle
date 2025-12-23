# Loading JS libraries

Since `v0.7.30` scittle allows to load libraries from the global enviroment.
This means you can load a library in a `<script>` tag and use it via `:require` in scittle.

An example:

``` html
<html>
  <head>
    <script src="https://cdn.jsdelivr.net/npm/scittle@0.7.30/dist/scittle.js" type="application/javascript"></script>
    <script src="https://cdn.jsdelivr.net/npm/js-confetti@latest/dist/js-confetti.browser.js"></script>
    <script type="application/x-scittle">
      (require '["JSConfetti" :as confetti])
      (.addConfetti (confetti.))
    </script>
  </head>
  <body>
  </body>
</html>
```

## ES modules

The async nature of ES modules makes them a litte bit more difficult to work
with in scittle. You need to disable automatic evaluation of script tags first
using `scittle.core.disable_auto_eval()`.  In a `module` type `<script>` tag you
can then load ES modules, attach them to the global object and manually invoke
`scittle.core.eval_script_tags();` when setup is completed.

``` html
<html>
  <head>
    <script src="https://cdn.jsdelivr.net/npm/scittle@0.7.30/dist/scittle.js" type="application/javascript"></script>
    <script>scittle.core.disable_auto_eval()</script>
    <script type="module">
      import confetti from "https://esm.sh/canvas-confetti@1.6.0"
      globalThis.JSConfetti = confetti;
      scittle.core.eval_script_tags();
    </script>
    <script type="application/x-scittle">
      (require '["JSConfetti" :as confetti])
      (confetti)
    </script>
  </head>
  <body>
  </body>
</html>
```
