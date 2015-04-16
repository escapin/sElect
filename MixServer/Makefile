BCPROV_t=jdk16
BCPROV_v=1.46

build: javadownload java_compile npm_install


javadownload:
	-mkdir -p ../lib
	cd .. ; wget -P lib -nc http://central.maven.org/maven2/org/bouncycastle/bcprov-${BCPROV_t}/${BCPROV_v}/bcprov-${BCPROV_t}-${BCPROV_v}.jar


java_compile:
	-mkdir -p ../bin
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
	-rm -r _data_
