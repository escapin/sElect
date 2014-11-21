
default:
	@echo Specify the goal: devenv OR  devclean OR clean

devenv: compile_java npm configs copy_files

compile_java:
	-mkdir bin
	javac -sourcepath src \
          -classpath lib/bcprov-jdk16-146.jar \
          -d bin \
          src/selectvoting/system/wrappers/*.java 

copy_files:
	cp node_modules/voterClient.js VotingBooth/js/voterClient.js
	cp node_modules/cryptofunc/index.js VotingBooth/js/cryptofunc.js

npm:
	cd BulletinBoard; npm install
	cd CollectingServer; npm install
	cd FinalServer; npm install
	cd node_modules/cryptofunc; npm install

configs:
	-mkdir tmp
	cp templates/*.pem tmp/
	cp templates/ElectionManifest.json tmp/
	cp templates/config_bb.json BulletinBoard/config.json
	cp templates/config_cs.json CollectingServer/config.json
	cp templates/config_fs.json FinalServer/config.json

test:
	cd tests; npm install
	jasmine-node tests

testclean:
	-rm -r tests/node_modules

devclean:
	-rm -r bin
	-rm -r VotingBooth/js/voterClient.js
	-rm -r VotingBooth/js/cryptofunc.js
	-rm -r BulletinBoard/node_modules
	-rm -r CollectingServer/node_modules
	-rm -r FinalServer/node_modules
	-rm -r node_modules/cryptofunc/node_modules
	-rm -r tmp
	-rm CollectingServer/log.txt
	-rm -r tests/node_modules

clean:
	-rm tmp/*.msg
	-rm tmp/*.log
	-rm CollectingServer/log.txt
