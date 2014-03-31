#!/bin/bash
fileName=RS3TargetSystem
tmpDir=$(date +%s | sha256sum | base64 | head -c 32 ; echo)

mkdir $tmpDir
git archive master -o $tmpDir/$fileName.zip  src BulletinBoard README.txt build.xml

# add the libraries (not under the version control) 
# to the archive 
cd $tmpDir
unzip $fileName.zip -d $fileName/
rm $fileName.zip
mkdir $fileName/lib
cp ../lib/*.jar $fileName/lib/
zip -r $fileName.zip $fileName

# copy the file in the root directory
cd ..
cp $tmpDir/$fileName.zip .

#remove the tmpDir
rm -r $tmpDir

