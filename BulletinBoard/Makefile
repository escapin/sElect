
build:
	npm install
	cd ../node_modules/cryptofunc; npm install
	-rm public/js/jquery-1.11.1.min.js
	cd public/js; wget http://code.jquery.com/jquery-1.11.1.min.js
	-rm public/pure/pure-min.css
	cd public/pure; wget http://yui.yahooapis.com/pure/0.5.0/pure-min.css
	-rm public/pure/grids-responsive-old-ie-min.css
	cd public/pure; wget http://yui.yahooapis.com/pure/0.5.0/grids-responsive-old-ie-min.css
	-rm public/pure/grids-responsive-min.css
	cd public/pure; wget http://yui.yahooapis.com/pure/0.5.0/grids-responsive-min.css 

clean:
	-rm -r node_modules
	-rm -r ../node_modules/cryptofunc/node_modules
	-rm public/js/jquery-1.11.1.min.js
	-rm public/pure/pure-min.css
	-rm public/pure/grids-responsive-old-ie-min.css
	-rm public/pure/grids-responsive-min.css


cleanElection:
	-rm -r _data_
