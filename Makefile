
default:
	@echo Specify the goal: devenv OR  devclean OR cleanElection

devenv: compile_java npm configs copy_files download

compile_java:
	-mkdir bin
	javac -sourcepath src \
          -classpath lib/bcprov-jdk16-146.jar \
          -d bin \
          src/selectvoting/system/wrappers/*.java 

copy_files:
	cp node_modules/voterClient.js VotingBooth/webapp/js/voterClient.js
	cp node_modules/cryptofunc/index.js VotingBooth/webapp/js/cryptofunc.js

download:
	-rm -r VotingBooth/webapp/js/jquery-1.11.1.min.js
	cd VotingBooth/webapp/js; wget http://code.jquery.com/jquery-1.11.1.min.js

npm:
	cd BulletinBoard; npm install
	cd CollectingServer; npm install
	cd FinalServer; npm install
	cd VotingBooth; npm install
	cd node_modules/cryptofunc; npm install

configs:
	-mkdir tmp
	cp templates/*.pem tmp/
	cp templates/ElectionManifest.json tmp/
	cp templates/config_bb.json BulletinBoard/config.json
	cp templates/config_cs.json CollectingServer/config.json
	cp templates/config_fs.json FinalServer/config.json
	node tools/manifest2js.js templates/ElectionManifest.json > VotingBooth/webapp/ElectionManifest.js

test:
	cd tests; npm install
	jasmine-node tests

testclean:
	-rm -r tests/node_modules

devclean:
	-rm -r bin
	-rm -r VotingBooth/webapp/js/voterClient.js
	-rm -r VotingBooth/webapp/js/cryptofunc.js
	-rm -r BulletinBoard/node_modules
	-rm -r CollectingServer/node_modules
	-rm -r FinalServer/node_modules
	-rm -r VotingBooth/node_modules
	-rm -r node_modules/cryptofunc/node_modules
	-rm -r tmp
	-rm VotingBooth/webapp/ElectionManifest.js
	-rm VotingBooth/webapp/js/jquery-1.11.1.min.js
	-rm CollectingServer/log.txt
	-rm -r tests/node_modules

cleanElection:
	-rm tmp/*.msg
	-rm tmp/*.log
	-rm CollectingServer/log.txt
