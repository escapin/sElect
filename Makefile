# java libraries
#BCPROV_t=jdk15on
#BCPROV_v=1.51
BCPROV_t=jdk16
BCPROV_v=1.46
JUNIT_v=4.12

default:
	@echo Specify the goal: devenv OR  devclean OR cleanElection

devenv: compile_java npm configs copy_files download

compile_java:
	-mkdir -p lib
	wget -P lib -nc http://central.maven.org/maven2/org/bouncycastle/bcprov-${BCPROV_t}/${BCPROV_v}/bcprov-${BCPROV_t}-${BCPROV_v}.jar
	-mkdir -p bin
	javac -sourcepath src \
          -classpath lib/bcprov-${BCPROV_t}-${BCPROV_v}.jar \
          -d bin \
          src/selectvoting/system/wrappers/*.java 

copy_files:
	cp node_modules/voterClient.js VotingBooth/webapp/js/voterClient.js
	cp node_modules/cryptofunc/index.js VotingBooth/webapp/js/cryptofunc.js

download:
	-rm VotingBooth/webapp/js/jquery-1.11.1.min.js
	cd VotingBooth/webapp/js; wget http://code.jquery.com/jquery-1.11.1.min.js
	-rm VotingBooth/webapp/pure/pure-min.css
	cd VotingBooth/webapp/pure; wget http://yui.yahooapis.com/pure/0.5.0/pure-min.css
	-rm VotingBooth/webapp/pure/grids-responsive-old-ie-min.css
	cd VotingBooth/webapp/pure; wget http://yui.yahooapis.com/pure/0.5.0/grids-responsive-old-ie-min.css
	-rm VotingBooth/webapp/pure/grids-responsive-min.css
	cd VotingBooth/webapp/pure; wget http://yui.yahooapis.com/pure/0.5.0/grids-responsive-min.css 
	-rm BulletinBoard/public/js/jquery-1.11.1.min.js
	cd BulletinBoard/public/js; wget http://code.jquery.com/jquery-1.11.1.min.js
	-rm BulletinBoard/public/pure/pure-min.css
	cd BulletinBoard/public/pure; wget http://yui.yahooapis.com/pure/0.5.0/pure-min.css
	-rm BulletinBoard/public/pure/grids-responsive-old-ie-min.css
	cd BulletinBoard/public/pure; wget http://yui.yahooapis.com/pure/0.5.0/grids-responsive-old-ie-min.css
	-rm BulletinBoard/public/pure/grids-responsive-min.css
	cd BulletinBoard/public/pure; wget http://yui.yahooapis.com/pure/0.5.0/grids-responsive-min.css 


npm:
	cd BulletinBoard; npm install
	cd CollectingServer; npm install
	cd FinalServer; npm install
	cd VotingBooth; npm install
	cd node_modules/cryptofunc; npm install

configs:
	-mkdir -p tmp
	cp templates/*.pem tmp/
	cp templates/ElectionManifest.json tmp/
	cp templates/config_bb.json BulletinBoard/config.json
	cp templates/config_cs.json CollectingServer/config.json
	cp templates/config_fs.json FinalServer/config.json
	node tools/manifest2js.js templates/ElectionManifest.json > VotingBooth/webapp/ElectionManifest.js

testsuite:
	-mkdir -p lib
	wget -P lib -nc http://central.maven.org/maven2/junit/junit/${JUNIT_v}/junit-${JUNIT_v}.jar
	if [ -z $(shell npm list -g | grep -o jasmine-node) ]; then npm install -g jasmine-node;  fi
	
test:
	cd tests; npm install
	jasmine-node tests

testclean:
	-rm -r tests/node_modules

devclean:
	-rm -r bin
	-rm VotingBooth/webapp/js/voterClient.js
	-rm VotingBooth/webapp/js/cryptofunc.js
	-rm VotingBooth/webapp/ElectionManifest.js
	-rm VotingBooth/webapp/js/jquery-1.11.1.min.js
	-rm VotingBooth/webapp/pure/pure-min.css
	-rm VotingBooth/webapp/pure/grids-responsive-old-ie-min.css
	-rm VotingBooth/webapp/pure/grids-responsive-min.css
	-rm BulletinBoard/public/js/jquery-1.11.1.min.js
	-rm BulletinBoard/public/pure/pure-min.css
	-rm BulletinBoard/public/pure/grids-responsive-old-ie-min.css
	-rm BulletinBoard/public/pure/grids-responsive-min.css
	-rm -r BulletinBoard/node_modules
	-rm -r CollectingServer/node_modules
	-rm -r FinalServer/node_modules
	-rm -r VotingBooth/node_modules
	-rm -r node_modules/cryptofunc/node_modules
	-rm -r tmp
	-rm CollectingServer/log.txt
	-rm -r tests/node_modules

cleanElection:
	-rm tmp/*.msg
	-rm tmp/*.log
	-rm CollectingServer/log.txt
