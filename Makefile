# java libraries: version
#BCPROV_t=jdk15on
#BCPROV_v=1.51
BCPROV_t=jdk16
BCPROV_v=1.46
JUNIT_v=4.12
HARMCRESTCORE_v=1.3

default:
	@echo Specify the goal: devenv OR devclean OR cleanElection OR updateCryptoKeys OR prod

prod:
	cd VotingBooth ; make
	cd CollectingServer ; make
	cd MixServer ; make
	cd BulletinBoard ; make

updateCryptoKeys: updatecryptokeys_cs updatecryptokeys_mix

updatecryptokeys_cs:
	cd node_modules/cryptofunc; npm install
	cd tools; node genKeys4collectingServer.js ../templates/ElectionManifest.json ../templates/config_cs.json

updatecryptokeys_mix:
	cd node_modules/cryptofunc; npm install
	cd templates; python updateMixServersCryptoKeys.py



devenv: javabuild npminstall configs filescopy libdownload

javadownload:
	-mkdir -p lib
	wget -P lib -nc http://central.maven.org/maven2/org/bouncycastle/bcprov-${BCPROV_t}/${BCPROV_v}/bcprov-${BCPROV_t}-${BCPROV_v}.jar

javabuild: javadownload
	-mkdir -p bin
	javac -sourcepath src \
          -classpath "lib/*" \
          -d bin \
          src/selectvoting/system/wrappers/*.java    

npminstall:
	cd BulletinBoard; npm install
	cd CollectingServer; npm install
	cd MixServer; npm install
	cd VotingBooth; npm install
	cd node_modules/cryptofunc; npm install



configs: filesconfigs mixconfigs


filesconfigs: 
	-mkdir -p _sElectConfigFiles_
	cp templates/select.* _sElectConfigFiles_
	cp templates/ElectionManifest.json _sElectConfigFiles_
	cp templates/config_bb.json BulletinBoard/config.json
	cp templates/config_cs.json CollectingServer/config.json
	cp templates/config_mix.json MixServer/config.json
	ln -fs ../_sElectConfigFiles_/ElectionManifest.json VotingBooth/

mixconfigs:
	python configMixServers.py


filescopy:
	cp node_modules/voterClient.js VotingBooth/webapp/js/voterClient.js
	cp node_modules/cryptofunc/index.js VotingBooth/webapp/js/cryptofunc.js
	cp node_modules/strHexConversion.js VotingBooth/webapp/js/strHexConversion.js

libdownload:
	-rm VotingBooth/webapp/js/jquery-1.11.1.min.js
	cd VotingBooth/webapp/js; wget http://code.jquery.com/jquery-1.11.1.min.js
	-rm VotingBooth/webapp/js/bluebird.min.js
	cd VotingBooth/webapp/js; wget https://cdn.jsdelivr.net/bluebird/latest/bluebird.min.js
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



devclean: cleanElection javaclean npmclean votingboothclean bbclean configsclean


javaclean:	
	-rm -r bin

npmclean:
	-rm -r BulletinBoard/node_modules
	-rm -r CollectingServer/node_modules
	-rm -r MixServer/node_modules
	-rm -r VotingBooth/node_modules
	-rm -r node_modules/cryptofunc/node_modules

votingboothclean:
	-rm VotingBooth/ElectionManifest.json
	-rm VotingBooth/webapp/js/voterClient.js
	-rm VotingBooth/webapp/js/cryptofunc.js
	-rm VotingBooth/webapp/ElectionManifest.js
	-rm VotingBooth/webapp/js/jquery-1.11.1.min.js
	-rm VotingBooth/webapp/js/bluebird.min.js
	-rm VotingBooth/webapp/pure/pure-min.css
	-rm VotingBooth/webapp/pure/grids-responsive-old-ie-min.css
	-rm VotingBooth/webapp/pure/grids-responsive-min.css

bbclean:	
	-rm BulletinBoard/public/js/jquery-1.11.1.min.js
	-rm BulletinBoard/public/pure/pure-min.css
	-rm BulletinBoard/public/pure/grids-responsive-old-ie-min.css
	-rm BulletinBoard/public/pure/grids-responsive-min.css

configsclean: configfilesclean mixdirsclean

configfilesclean:
	-rm -r _sElectConfigFiles_

mixdirsclean:
	@echo   Removing: 	$(shell ls MixServer | egrep "mix[0-9]+")
	rm -r mix


logclean:
	-rm CollectingServer/log.txt


cleanElection:
	-rm -r CollectingServer/_data_
	-rm CollectingServer/log.txt
	-rm -r mix/*/_data_
	-rm -r BulletinBoard/_data_

test: testdownload testconfigs testrun


testdownload:
	-mkdir -p lib
	wget -P lib -nc http://central.maven.org/maven2/junit/junit/${JUNIT_v}/junit-${JUNIT_v}.jar
	wget -P lib -nc http://central.maven.org/maven2/org/bouncycastle/bcprov-${BCPROV_t}/${BCPROV_v}/bcprov-${BCPROV_t}-${BCPROV_v}.jar
	wget -P lib -nc https://hamcrest.googlecode.com/files/hamcrest-core-${HARMCRESTCORE_v}.jar

testconfigs:
	-mkdir -p bin
	javac -sourcepath src \
          -classpath "lib/*" \
          -d bin \
          src/tests/*.java \
          src/selectvoting/system/wrappers/*.java

	cd node_modules/cryptofunc; npm install
	cd CollectingServer; npm install
	cd MixServer; npm install
	cd tests; npm install

testrun:
	@echo	
	@echo     [RUN] tests: java
	cd bin; java -cp ".:../lib/*" tests.RunTestSuite
	@echo
	@echo     [RUN] tests: nodejs
	cd tests; ./node_modules/.bin/jasmine-node .



testclean:
	-rm -r bin/selectvoting/tests/
	-rm -r tests/node_modules
	-rm -r tests/_data_Test


