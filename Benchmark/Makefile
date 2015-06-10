
build: javabuild npm_install

javabuild:
	-mkdir -p "../bin"
	javac -sourcepath "../src" \
          -classpath "../lib/*" \
          -d "../bin" \
          ../src/selectvoting/system/wrappers/*.java    


npm_install:
	npm install
	cd ../node_modules/cryptofunc; npm install
	cd ../CollectingServer; npm install
	cd ../MixServer; npm install
	
clean:
	-rm -r node_modules
	-rm -r _data_Test
