# Scittle in a service worker

You can use Scittle to bootstrap a ClojureScript based service worker.

Put the following code into e.g. `scittle-sw.js` to create a JavaScript based service worker, load Scittle, then fetch your script and eval it.

```javascript
importScripts("scittle.min.js");

const request = await fetch("sw.cljs");
const text = await request.text();
const result = scittle.core.eval_string(text);
```

Then load `scittle-sw.js` in your HTML:

```html
<script>
  if('serviceWorker' in navigator)
    navigator.serviceWorker.register('scittle-sw.js');
</script>
```

This will load `sw.cljs` and eval it in the context of the service worker.

A ready-made example can be found at [chr15m/scittle-template-serviceworker](https://github.com/chr15m/scittle-template-serviceworker).
