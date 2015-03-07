import os
import re
import subprocess

path = "../templates/"
pattern="config_mix[0-9]+.json"
electionManifest="ElectionManifest.json"
keyGenerator_file="../tools/genKeys4mixServer.js"

# to be run after the MixServer has been configured by the make file
def updateKeys():
    files = os.listdir(path)
    p=re.compile(pattern) 
    configMix_files=filter(p.search, files);
    for file in configMix_files:
        print "***\t" + file + "\t***\n";
        subprocess.call(["node", keyGenerator_file, 
                os.path.join(path, electionManifest), os.path.join(path, file)]);
        print;

if __name__ == '__main__':
    updateKeys()