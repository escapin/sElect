
devenv: compile_java npm configs

compile_java:
	-mkdir bin
	javac -sourcepath src \
          -classpath lib/bcprov-jdk16-146.jar \
          -d bin \
          src/de/uni/trier/infsec/eVotingSystem/wrappers/*.java 

npm:
	cd BulletinBoard; npm install
	cd VotingBooth; npm install
	cd CollectingServer; npm install
	cd FinalServer; npm install

configs:
	-mkdir tmp
	cp templates/*.pem tmp/
	cp templates/ElectionManifest.json tmp/
	cp templates/config_bb.json BulletinBoard/config.json
	cp templates/config_cs.json CollectingServer/config.json
	cp templates/config_fs.json FinalServer/config.json
	cp templates/config_vb.json VotingBooth/config.json

devclean:
	-rm -r bin
	-rm -r BulletinBoard/node_modules
	-rm -r VotingBooth/node_modules
	-rm -r CollectingServer/node_modules
	-rm -r FinalServer/node_modules
	-rm -r tmp
