
build: npm_install

npm_install:
	npm install
	cd ../node_modules/cryptofunc; npm install
	
clean:
	-rm -r ../bin
	-rm -r node_modules
	-rm -r ../node_modules/cryptofunc/node_modules
	
cleanElection:
	-rm CollectingServer/log.txt
	-rm -r _data_
