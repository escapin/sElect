node ../tools/manifest2js.js ElectionManifest.json > webapp/ElectionManifest.js
node ../tools/config2js.js config.json configRaw > webapp/config.js
node ./webapp/ejs/compileEJS.js
node server.js
