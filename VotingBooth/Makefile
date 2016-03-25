build:
	npm install
	cp ../node_modules/voterClient.js webapp/js/voterClient.js
	cp ../node_modules/cryptofunc/index.js webapp/js/cryptofunc.js
	-rm webapp/js/jquery-1.11.1.min.js
	cd webapp/js; wget http://code.jquery.com/jquery-1.11.1.min.js
	-rm webapp/pure/pure-min.css
	cd webapp/pure; wget http://yui.yahooapis.com/pure/0.5.0/pure-min.css
	-rm webapp/pure/grids-responsive-old-ie-min.css
	cd webapp/pure; wget http://yui.yahooapis.com/pure/0.5.0/grids-responsive-old-ie-min.css
	-rm webapp/pure/grids-responsive-min.css
	cd webapp/pure; wget http://yui.yahooapis.com/pure/0.5.0/grids-responsive-min.css 
	-rm webapp/js/bluebird.min.js
	cd webapp/js; wget https://cdn.jsdelivr.net/bluebird/latest/bluebird.min.js
	-rm webapp/pic/2tsd397wLxj96qwHyNIkxPesZW2xOQ-xsNqO47m55DA.woff2
	cd webapp/pic; wget https://fonts.gstatic.com/s/roboto/v15/2tsd397wLxj96qwHyNIkxPesZW2xOQ-xsNqO47m55DA.woff2
	
clean:
	-rm -r node_modules
	-rm -r webapp/js/voterClient.js
	-rm -r webapp/js/cryptofunc.js
	-rm webapp/ElectionManifest.js
	-rm webapp/js/jquery-1.11.1.min.js
	-rm webapp/js/bluebird.min.js
	-rm VotingBooth/webapp/pure/pure-min.css
	-rm VotingBooth/webapp/pure/grids-responsive-old-ie-min.css
	-rm VotingBooth/webapp/pure/grids-responsive-min.css
