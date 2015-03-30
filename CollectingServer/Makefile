
build: npm_install

java_compile:
	-mkdir ../bin
	cd .. ; javac -sourcepath src \
          -classpath "lib/*" \
          -d bin \
          src/selectvoting/system/wrappers/*.java 

npm_install:
	npm install
	cd ../node_modules/cryptofunc; npm install
	
clean:
	-rm -r ../bin
	-rm -r node_modules
	-rm -r ../node_modules/cryptofunc/node_modules

	
cleanElection:
	-rm CollectingServer/log.txt
	-rm -r data