# nREPL

To connect to a Scittle nREPL server from your editor, follow these steps. The
setup described here, can be found in this directory.

In babashka or Clojure JVM, use the
[sci.nrepl](https://github.com/babashka/sci.nrepl) dependency and run:

``` clojure
(require '[sci.nrepl.browser-server :as nrepl])
(nrepl/start! {:nrepl-port 1339 :websocket-port 1340})
```

This will run an nREPL server on port 1339 and a websocket server on port 1340.
Your editor's nREPL client will connect to port 1339 and your browser, running
scittle, will connect to port 1340. The nREPL server forwards messages to the
browser via the websocket connection.

In your scittle website, you will need to include the following, in addition to
the normal routine:

``` html
<script>var SCITTLE_NREPL_WEBSOCKET_PORT = 1340;</script>
<script src="https://cdn.jsdelivr.net/npm/scittle@0.7.26/dist/scittle.nrepl.js" type="application/javascript"></script>
```

Also include the CLJS file that you want to evaluate with nREPL:

``` html
<script src="playground.cljs" type="application/x-scittle"></script>
```

Then visit `playground.cljs` in your editor and connect to the nREPL server,
and start evaluating!

See the `index.html` file for an example.

When you run `bb dev` in this directory, and then open `http://localhost:1341`
you should be able evaluate expressions in `playground.cljs`. See a demo
[here](https://twitter.com/borkdude/status/1526285565343281159).

Note that the nREPL server connection stays alive even after the browser window
refreshes.

### CIDER

Choose `cider-connect-cljs`, select port `1339`, followed by the `nbb` REPL
type.  If you use multiple REPLs in your project, choose
`sesman-link-with-buffer` to choose the right REPL for the right buffer.
