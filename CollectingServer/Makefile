
build: java_compile npm_install configs

cleanall: clean cleanElection

java_compile:
	-mkdir ../bin
	cd .. ; javac -sourcepath src \
          -classpath "lib/*" \
          -d bin \
          src/selectvoting/system/wrappers/*.java 

npm_install:
	npm install
	cd ../node_modules/cryptofunc; npm install
	
configs:
	-mkdir -p ../tmp
	cp -u ../templates/*.pem ../tmp/
	cp -u ../templates/ElectionManifest.json ../tmp/
	cp -u ../templates/config_cs.json config.json

clean:
	-rm -r ../bin
	-rm -r node_modules
	-rm -r ../node_modules/cryptofunc/node_modules
	-rm ../tmp/ElectionManifest.json
	-rm ../tmp/*.pem
	-rm config.json
	
cleanElection:
	-rm ../tmp/partialResult.msg
	-rm ../tmp/accepted_ballots.log
	-rm log.txt
