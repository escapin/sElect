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
	cd webapp/roboto; wget -N https://fonts.gstatic.com/s/roboto/v15/Jzo62I39jc0gQRrbndN6nfesZW2xOQ-xsNqO47m55DA.ttf
	cd webapp/roboto; wget -N https://fonts.gstatic.com/s/roboto/v15/Hgo13k-tfSpn0qi1SFdUfaCWcynf_cDxXwCLxiixG1c.ttf
	cd webapp/roboto; wget -N https://fonts.gstatic.com/s/roboto/v15/zN7GBFwfMP4uA6AR0HCoLQ.ttf
	cd webapp/roboto; wget -N https://fonts.gstatic.com/s/roboto/v15/RxZJdnzeo3R5zSexge8UUaCWcynf_cDxXwCLxiixG1c.ttf
	cd webapp/roboto; wget -N https://fonts.gstatic.com/s/roboto/v15/12mE4jfMSBTmg-81EiS-YS3USBnSvpkopQaUR-2r7iU.ttf
	cd webapp/roboto; wget -N https://fonts.gstatic.com/s/roboto/v15/7m8l7TlFO-S3VkhHuR0at50EAVxt0G0biEntp43Qt6E.ttf
	cd webapp/roboto; wget -N https://fonts.gstatic.com/s/roboto/v15/W4wDsBUluyw0tK3tykhXEfesZW2xOQ-xsNqO47m55DA.ttf
	cd webapp/roboto; wget -N https://fonts.gstatic.com/s/roboto/v15/W4wDsBUluyw0tK3tykhXEfesZW2xOQ-xsNqO47m55DA.ttf
	
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
	-rm webapp/roboto/*.ttf
