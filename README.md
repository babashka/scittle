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
(http/serve {:port 1341 :dir "resources/public"})
@(promise) ;; wait until process is killed
```

### nREPL

See [doc/nrepl](doc/nrepl).

### Service worker

See [doc/serviceworker.md](doc/serviceworker.md).

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
