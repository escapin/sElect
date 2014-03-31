#!/bin/bash
tmpDir=$(date +%s | sha256sum | base64 | head -c 32 ; echo)
mkdir $tmpDir 
#git archive master -o tmpDir/RS3TargetSystem.zip  src BulletinBoard README.txt build.xml

