import os
import re
import subprocess

pattern="config_mix[0-9]+.json"
electionManifest="ElectionManifest.json"
tools_path = "../tools/"
keyGenerator_file="genKeys4mixServer.js"
current = os.getcwd();

def runMake():
    os.chdir(tools_path);
    os.system("make build");
    os.chdir(current);

# to be run after the 'config_mix[0-9]+.json' files are in the current folder
def updateKeys():
    files = os.listdir(current)
    p=re.compile(pattern)
    configMix_files=filter(p.search, files);
    for file in configMix_files:
        print "***\t" + file + "\t***\n";
        subprocess.call(["node", os.path.join(tools_path,keyGenerator_file),
                os.path.join(current,electionManifest), os.path.join(current,file)]);
        print;

if __name__ == '__main__':
    runMake()
    print
    updateKeys()
