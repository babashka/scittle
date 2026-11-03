import { test, expect } from '@playwright/test';

// The scittle build must exist under resources/public/js (run `bb prod` first).
// The battery in the two pages is identical; asserting the JIT and interpreter
// runs agree is the same differential guarantee SCI itself is tested on.

const EXPECTED =
  '{:loop 100000, :fib 610, :iset 42, :case [1 2 0], :throw "boom", :interop "AB"}';

async function readOut(page, path) {
  await page.goto(path);
  const out = page.locator('#out');
  await expect(out).not.toBeEmpty();
  return {
    out: (await out.textContent()).trim(),
    jit: (await page.locator('#jit').textContent()).trim(),
  };
}

test('JIT (default) evaluates the battery correctly', async ({ page }) => {
  const { out, jit } = await readOut(page, '/test/smoke.html');
  expect(jit).toBe('true');
  expect(out).toBe(EXPECTED);
});

test('interpreter fallback (SCI_DISABLE_JIT) agrees with the JIT', async ({ page }) => {
  const { out, jit } = await readOut(page, '/test/smoke_nojit.html');
  expect(jit).toBe('false');
  expect(out).toBe(EXPECTED);
});

// Guards that the test exercises the locally built scittle, not a published
// copy from a CDN: every scittle*.js request must come from the local server.
test('scittle is loaded from the local build, not a CDN', async ({ page, baseURL }) => {
  const scittleReqs = [];
  page.on('request', (r) => {
    if (/\bscittle[^/]*\.js\b/.test(r.url())) scittleReqs.push(r.url());
  });
  await page.goto('/test/smoke.html');
  await expect(page.locator('#out')).not.toBeEmpty();
  expect(scittleReqs.length).toBeGreaterThan(0);
  for (const url of scittleReqs) expect(url.startsWith(baseURL)).toBe(true);
});

// Exercises the upgraded runtime plugins (replicant, re-frame + reagent): the
// core smoke test never loads plugin code, so a plugin broken by a bump would
// pass there.
test('replicant and re-frame plugins render', async ({ page }) => {
  await page.goto('/test/plugins.html');
  await expect(page.locator('#rep')).toHaveText('replicant-ok');
  await expect(page.locator('#rf')).toHaveText('reframe-ok');
});

async function nreplRequests(page, messages, doneCount = messages.length) {
  const replies = [];
  let socket;
  let done;
  let doneSeen = 0;
  const finished = new Promise((resolve) => { done = resolve; });
  await page.routeWebSocket(/_nrepl$/, (ws) => {
    socket = ws;
    ws.onMessage((reply) => {
      replies.push(reply);
      if (reply.includes('"done"') && ++doneSeen === doneCount) done();
    });
  });
  await page.goto('/test/nrepl.html');
  for (const message of messages) socket.send(message);
  await finished;
  return replies.join('\n');
}

function nreplRequest(page, message) {
  return nreplRequests(page, [message]);
}

function nreplEval(page, code) {
  return nreplRequest(page, `{:op :eval, :code ${JSON.stringify(code)}, :id "1", :session "s"}`);
}

test('nREPL eval replies with :out and a :value per form', async ({ page }) => {
  const replies = await nreplEval(page, '(println "hi") (+ 1 2) (+ 3 4)');
  expect(replies).toContain(':out "hi"');
  expect(replies).toContain(':value "nil"');
  expect(replies).toContain(':value "3"');
  expect(replies).toContain(':value "7"');
});

test('nREPL eval replies with the resolved value of a promise', async ({ page }) => {
  const replies = await nreplEval(page, '(js/Promise.resolve 2)');
  expect(replies).toContain(':value "#<Promise 2>"');
});

test('nREPL eval error replies with :err, :ex and eval-error status', async ({ page }) => {
  const replies = await nreplEval(page, '(throw (ex-info "boom" {}))');
  expect(replies).toContain(':err "boom\\n"');
  expect(replies).toContain(':ex "ExceptionInfo"');
  expect(replies).toContain(':status ["eval-error"]');
});

function evalMessage(id, session, code, extra = '') {
  return `{:op :eval, :code ${JSON.stringify(code)}, :id "${id}", :session "${session}"${extra}}`;
}

test('nREPL session keeps its namespace and :ns applies to one eval', async ({ page }) => {
  const replies = await nreplRequests(page, [
    evalMessage('1', 's', '(ns session-ns)'),
    evalMessage('2', 's', '(str "in " *ns*)'),
    evalMessage('3', 's', '(str "with ns " *ns*)', ', :ns "user"'),
    evalMessage('4', 'other', '(str "other " *ns*)'),
  ]);
  expect(replies).toContain(':value "\\"in session-ns\\""');
  expect(replies).toContain(':value "\\"with ns user\\""');
  expect(replies).toContain(':value "\\"other user\\""');
});

test('nREPL clone starts in the namespace of its parent', async ({ page }) => {
  const replies = await nreplRequests(page, [
    evalMessage('1', 's', '(ns parent-ns)'),
    '{:op :clone, :id "2", :session "s", :new-session "t"}',
    evalMessage('3', 't', '(str "clone " *ns*)'),
  ], 2);
  expect(replies).toContain(':value "\\"clone parent-ns\\""');
});

test('nREPL info without :ns resolves in the session namespace', async ({ page }) => {
  const replies = await nreplRequests(page, [
    evalMessage('1', 's', '(ns info-ns) (defn in-info-ns [])'),
    '{:op :info, :sym "in-info-ns", :id "2", :session "s"}',
  ]);
  expect(replies).toContain(':name in-info-ns');
});

test('nREPL unknown op replies with unknown-op status', async ({ page }) => {
  const replies = await nreplRequest(page, '{:op :frobnicate, :code "(+ 1 2)", :id "1", :session "s"}');
  expect(replies).toContain(':status ["done" "unknown-op" "error"]');
  expect(replies).toContain(':op "frobnicate"');
  expect(replies).not.toContain(':code');
});
