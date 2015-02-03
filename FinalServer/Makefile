
build: java_compile npm_install config

cleanall: clean cleanElection

java_compile:
	-mkdir -p ../bin
	cd .. ; javac -sourcepath src \
          -classpath "lib/*" \
          -d bin \
          src/selectvoting/system/wrappers/*.java 

npm_install:
	npm install
	cd ../node_modules/cryptofunc; npm install
	
config:
	-mkdir -p ../tmp
	cp -u ../templates/ElectionManifest.json ../tmp/
	cp -u ../templates/*.pem ../tmp/
	cp -u ../templates/config_fs.json config.json

clean:
	-rm -r ../bin
	-rm -r node_modules
	-rm -r ../node_modules/cryptofunc/node_modules
	-rm ../tmp/ElectionManifest.json
	-rm ../tmp/*.pem
	-rm config.json
	
cleanElection: 
	-rm ../tmp/finalResult.msg