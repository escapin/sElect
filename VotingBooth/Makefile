build:
	npm install
	cp ../node_modules/voterClient.js webapp/js/voterClient.js
	cp ../node_modules/cryptofunc/index.js webapp/js/cryptofunc.js
	-rm -r webapp/js/jquery-1.11.1.min.js
	cd webapp/js; wget http://code.jquery.com/jquery-1.11.1.min.js
	node ../tools/manifest2js.js ElectionManifest.json > webapp/ElectionManifest.js

clean:
	-rm -r node_modules
	-rm -r webapp/js/voterClient.js
	-rm -r webapp/js/cryptofunc.js
	-rm webapp/ElectionManifest.js
	-rm webapp/js/jquery-1.11.1.min.js
