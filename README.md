# Scittle

The [Small Clojure Interpreter](https://github.com/babashka/sci) exposed for usage in script tags.

Try it out on [CodePen](https://codepen.io/Prestance/pen/PoOdZQw)!

See [Github pages](https://babashka.org/scittle/) for usage.

See
[babashka-scittle-guestbook](https://github.com/kloimhardt/babashka-scittle-guestbook)
for a minimal full stack web application.

See [releases](https://github.com/babashka/scittle/releases) for links to
[JSDelivr](https://www.jsdelivr.com) to get versioned artifacts.

## REPL

To connect to a Scittle REPL from your editor, scittle provides an nREPL
implementation. To run the nREPL server you need to follow these steps:

In babashka or Clojure JVM, use the
[sci.nrepl](https://github.com/babashka/sci.nrepl) dependency and run:

```
(require 'sci.nrepl.browser-server :as bp)
(bp/start! {:nrepl-port 1339 :websocket-port 1340})
```

This will run an nREPL server on port 1339 and a websocket server on port 1340.
Your editor's nREPL client will connect to port 1339 and your browser, running
scittle, will connect to port 1340.

In your scittle website, you will need to include the following, in addition to
the normal routine:

```
<script>var SCITTLE_NREPL_WEBSOCKET_PORT = 1340;</script>
<script src="js/scittle.nrepl.js" type="application/javascript"></script>
```

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
