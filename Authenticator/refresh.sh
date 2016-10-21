#!/bin/bash
node ../tools/config2js.js config.json > webapp/config.js configRaw
node ../tools/config2js.js ElectionManifest.json > webapp/ElectionManifest.js electionManifestRaw

