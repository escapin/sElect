# java libraries: version
#BCPROV_t=jdk15on
#BCPROV_v=1.51
BCPROV_t=jdk16
BCPROV_v=1.46
JUNIT_v=4.12
HARMCRESTCORE_v=1.3

default:
	@echo Specify the goal: devenv OR  devclean OR cleanElection

devenv: java_download java_compile npm_install configs mix_configs copy_files download

test: test_download test_configs test_run

java_download:
	-mkdir -p lib
	wget -P lib -nc http://central.maven.org/maven2/org/bouncycastle/bcprov-${BCPROV_t}/${BCPROV_v}/bcprov-${BCPROV_t}-${BCPROV_v}.jar

java_compile:
	-mkdir -p bin
	javac -sourcepath src \
          -classpath "lib/*" \
          -d bin \
          src/selectvoting/system/wrappers/*.java 

npm_install:
	cd BulletinBoard; npm install
	cd CollectingServer; npm install
	cd MixServer; npm install
	cd VotingBooth; npm install
	cd node_modules/cryptofunc; npm install
	
configs:
	-mkdir -p tmp
	cp templates/*.pem tmp/
	cp templates/ElectionManifest.json tmp/
	cp templates/config_bb.json BulletinBoard/config.json
	cp templates/config_cs.json CollectingServer/config.json
	cp templates/config_mix.json MixServer/config.json
	node tools/manifest2js.js templates/ElectionManifest.json > VotingBooth/webapp/ElectionManifest.js
	
mix_configs:
	python configMixServers.py

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

test_download:
	-mkdir -p lib
	wget -P lib -nc http://central.maven.org/maven2/junit/junit/${JUNIT_v}/junit-${JUNIT_v}.jar
	wget -P lib -nc http://central.maven.org/maven2/org/bouncycastle/bcprov-${BCPROV_t}/${BCPROV_v}/bcprov-${BCPROV_t}-${BCPROV_v}.jar
	wget -P lib -nc https://hamcrest.googlecode.com/files/hamcrest-core-${HARMCRESTCORE_v}.jar


test_configs:
	-mkdir -p bin
	javac -sourcepath src \
          -classpath "lib/*" \
          -d bin \
          src/tests/*.java
    
	cd node_modules/cryptofunc; npm install
	cd tests; npm install

	
test_run:
	@echo	
	@echo     [RUN] tests: java
	cd bin; java -cp ".:../lib/*" tests.RunTestSuite
	@echo
	@echo     [RUN] tests: node.js
	./tests/node_modules/.bin/jasmine-node tests

testclean:
	-rm -r bin/selectvoting/tests/
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
	-rm -r MixServer/node_modules
	-rm -r VotingBooth/node_modules
	-rm -r node_modules/cryptofunc/node_modules
	-rm -r tmp
	-rm CollectingServer/log.txt
	-rm -r tests/node_modules
	$(shell ls | egrep "MixServer[0-9]+" | xargs rm -r)

cleanElection:
	-rm tmp/*.msg
	-rm tmp/*.log
	-rm CollectingServer/log.txt