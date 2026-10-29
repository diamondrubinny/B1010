// Renders every SVG in a folder to PNG with Chromium (exact SVG rendering, transparent background).
// Usage: node tools/art/render.cjs <svgDir> <pngDir>
const { chromium } = require('playwright');
const fs = require('node:fs');
const path = require('node:path');

(async () => {

const [svgDir, pngDir] = process.argv.slice(2);
fs.mkdirSync(pngDir, { recursive: true });
// Embedded as base64: pages loaded with setContent cannot read file:// URLs.
const fontData = fs.readFileSync(path.resolve('app/src/main/res/font/atkinson_bold.ttf')).toString('base64');
const browser = await chromium.launch();
const page = await browser.newPage();
for (const file of fs.readdirSync(svgDir).filter((f) => f.endsWith('.svg')).sort()) {
  const svg = fs.readFileSync(path.join(svgDir, file), 'utf8');
  const w = Number(/data-w="(\d+)"/.exec(svg)[1]);
  const h = Number(/data-h="(\d+)"/.exec(svg)[1]);
  await page.setViewportSize({ width: w, height: h });
  await page.setContent(`<!doctype html><html><head><style>
    @font-face { font-family: Atkinson; src: url(data:font/ttf;base64,${fontData}); font-weight: 700; }
    html,body{margin:0;padding:0;background:transparent} svg{display:block}
  </style></head><body>${svg}</body></html>`);
  await page.evaluate(() => document.fonts.ready);
  await page.locator('svg').first().screenshot({ path: path.join(pngDir, file.replace(/\.svg$/, '.png')), omitBackground: true });
}
await browser.close();
console.log('rendered');
})();
