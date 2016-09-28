node ../tools/manifest2js.js ElectionManifest.json > webapp/ElectionManifest.js
node ./webapp/ejs/compileEJS.js
node server.js
