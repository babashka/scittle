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
