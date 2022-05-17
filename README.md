# Scittle

The [Small Clojure Interpreter](https://github.com/babashka/sci) exposed for usage in script tags.

Try it out on [CodePen](https://codepen.io/Prestance/pen/PoOdZQw)!

See [Github pages](https://babashka.org/scittle/) for usage.

See
[babashka-scittle-guestbook](https://github.com/kloimhardt/babashka-scittle-guestbook)
for a minimal full stack web application.

See [releases](https://github.com/babashka/scittle/releases) for links to
[JSDelivr](https://www.jsdelivr.com) to get versioned artifacts.

## Serving assets

To serve assets you can use the
[babashka.http-server](https://github.com/babashka/http-server) dependency (with
babashka or Clojure JVM):

``` clojure
(require '[babashka.http-server :as http])
(http/serve {:port 1341 :dir "resoures/public"}
@(promise) ;; wait until process is killed
```

### nREPL

To connect to a Scittle nREPL server from your editor, follow these steps:

In babashka or Clojure JVM, use the
[sci.nrepl](https://github.com/babashka/sci.nrepl) dependency and run:

```
(require 'sci.nrepl.browser-server :as nrepl)
(nrepl/start! {:nrepl-port 1339 :websocket-port 1340})
```

This will run an nREPL server on port 1339 and a websocket server on port 1340.
Your editor's nREPL client will connect to port 1339 and your browser, running
scittle, will connect to port 1340. The nREPL server forwards messages to the
browser via the websocket connection.

In your scittle website, you will need to include the following, in addition to
the normal routine:

```
<script>var SCITTLE_NREPL_WEBSOCKET_PORT = 1340;</script>
<script src="https://cdn.jsdelivr.net/npm/scittle@0.2.0/dist/scittle.nrepl.js" type="application/javascript"></script>
```

Also include the CLJS file that you want to evaluate with nREPL:

```
<script src="cljs/script.cljs" type="application/x-scittle"></script>
```

Then visit `cljs/script.cljs` in your editor and connect to the nREPL server,
and start evaluating!

See the `resources/public/nrepl.html` file for an example. When you run `bb dev`
in this repository, and then open `http://localhost:1341/nrepl.html` you should
be able evaluate expressions in
`resources/public/cljs/nrepl_playground.cljs`. See a demo
[here](https://twitter.com/borkdude/status/1526285565343281159).

### CIDER

Currently when connecting from CIDER, you need to use this snippet:

```
(cider-register-cljs-repl-type 'sci-js "(+ 1 2 3)")

(defun mm/cider-connected-hook ()
  (when (eq 'sci-js cider-cljs-repl-type)
    (setq-local cider-show-error-buffer nil)
    (cider-set-repl-type 'cljs)))

(add-hook 'cider-connected-hook #'mm/cider-connected-hook)
```

Then choose `cider-connect-cljs`, select port `1339`, followed by the `sci-js`
REPL type.

## Tasks

Run `bb tasks` to see all available tasks:

```
$ bb tasks
The following tasks are available:

clean   Start from clean slate.
dev     Development build. Starts webserver and watches for changes.
prod    Builds production artifacts.
release Updates Github pages with new release build.
```

## Credits

Idea by Arne Brasseur a.k.a [plexus](https://github.com/plexus).

## License

Copyright © 2021 - 2022 Michiel Borkent

Distributed under the EPL License. See LICENSE.
