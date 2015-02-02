
build: compile_java npm

compile_java:
	-mkdir ../bin
	cd .. ; javac -sourcepath src \
          -classpath "lib/*" \
          -d bin \
          src/selectvoting/system/wrappers/*.java 

npm:
	npm install
	cd ../node_modules/cryptofunc; npm install

clean:
	-rm -r ../bin
	-rm -r node_modules
	-rm -r ../node_modules/cryptofunc/node_modules
