import os
import json
import shutil
from shutil import ignore_patterns
import collections
import errno
import datetime
import sys
import hashlib
import codecs
import subprocess
 
def copy(src, dest):
    try:
        shutil.copytree(src, dest, symlinks=True, ignore=ignore_patterns("*.py", "00", "01", "02"))
    except OSError as e:
        # source is a file, not a directory
        if e.errno == errno.ENOTDIR:
            shutil.copy(src, dest)
        else:
            print("Directory not copied. Error: %s" % e)
            
def link(dest):
    os.mkdir(dstroot+"/mix/00")
    os.mkdir(dstroot+"/mix/01")
    os.mkdir(dstroot+"/mix/02")
    os.symlink(dstroot+"/MixServer/run.sh", dstroot+"/mix/00/run.sh")
    os.symlink(dstroot+"/MixServer/cleanData.sh", dstroot+"/mix/00/cleanData.sh")
    os.symlink(dstroot+"/MixServer/mixServer.js", dstroot+"/mix/00/mixServer.js")
    os.symlink(dstroot+"/templates/config_mix00.json", dstroot+"/mix/00/config.json")
    
    os.symlink(dstroot+"/MixServer/run.sh", dstroot+"/mix/01/run.sh")
    os.symlink(dstroot+"/MixServer/cleanData.sh", dstroot+"/mix/01/cleanData.sh")
    os.symlink(dstroot+"/MixServer/mixServer.js", dstroot+"/mix/01/mixServer.js")
    os.symlink(dstroot+"/templates/config_mix01.json", dstroot+"/mix/01/config.json")
    
    os.symlink(dstroot+"/MixServer/run.sh", dstroot+"/mix/02/run.sh")
    os.symlink(dstroot+"/MixServer/cleanData.sh", dstroot+"/mix/02/cleanData.sh")
    os.symlink(dstroot+"/MixServer/mixServer.js", dstroot+"/mix/02/mixServer.js")
    os.symlink(dstroot+"/templates/config_mix02.json", dstroot+"/mix/02/config.json")

def copyFast(src, dest):
    if sys.platform.startswith("win"):
        os.system("xcopy /s " + src + " " + dest)
    else:
        os.system("cp -rf " + src + " " + dest)

def jwrite(src, key, value):
    try:
        jsonFile = open(src, 'r+')
        jsonData = json.load(jsonFile, object_pairs_hook=collections.OrderedDict)
        jsonData[key] = value
        jsonFile.seek(0)
    except IOError:
        jsonFile = open(src, 'w')
        jsonData = {key: value}
    json.dump(jsonData, jsonFile, indent = 4)
    jsonFile.close()
    
def jwriteAdv(src, key, value, pos="", key2=""):
    if pos is "" and key2 is "":
        jwrite(src, key, value)
    else:
        try:
            jsonFile = open(src, 'r+')
            jsonData = json.load(jsonFile, object_pairs_hook=collections.OrderedDict)
            if key2 is "":
                jsonData[key][pos] = value
            else:
                jsonData[key][pos][key2] = value
            jsonFile.seek(0)
        except IOError:
            jsonFile = open(src, 'w')
            if key2 is "":
                jsonData = {key: {pos: value}}
            else:
                jsonData = {key: {key2: value}}
            jsonData = {key: value}
        json.dump(jsonData, jsonFile, indent = 4)
        jsonFile.close()
        
def jAddList(src, key, value):
    try:
        jsonFile = open(src, 'r+')
        jsonData = json.load(jsonFile, object_pairs_hook=collections.OrderedDict)
        iDs = jsonData[key]
        iDs.append(value)
        jsonData[key] = iDs
        jsonFile.seek(0)
    except IOError:
        jsonFile = open(src, 'w')
        jsonData = {key: value}
    json.dump(jsonData, jsonFile, indent = 4)
    jsonFile.close()

def getTime():
    utcTime = datetime.datetime.utcnow()
    utcTime = utcTime + datetime.timedelta(hours=1)
    return utcTime

def addSec(tm, secs):
    fulldate = tm + datetime.timedelta(seconds=secs)
    return fulldate

def createElec():
    jsonFile = open(electionConfig, 'w')
    jsonData = {"electionIDs": [], "maxElections": (3500-3300)//5, "available-ports": [3300, 3500], "used-ports": [["VotingBooth", 3333]]}
    json.dump(jsonData, jsonFile, indent = 4)
    jsonFile.close()

def usePortsOld():
    newPorts = []
    try:
        jsonFile = open(electionConfig, 'r+')
        jsonData = json.load(jsonFile, object_pairs_hook=collections.OrderedDict)
        rangePorts = jsonData["available-ports"]
        usingPorts = jsonData["used-ports"]
        for openPort in range(rangePorts[0], rangePorts[1]):
            if openPort in usingPorts:
                continue
            newPorts.append(openPort)
            if len(newPorts) >= 5:
                break
        if len(newPorts) < 5:
            print("Not enough ports available.")
            quit()
        usingPorts.extend(newPorts)
        jsonData["used-ports"] = usingPorts
        jsonFile.seek(0)
        json.dump(jsonData, jsonFile, sort_keys=True, indent = 4)
        jsonFile.close()
    except IOError:
        createElec()
        newPorts = [3300, 3301, 3302, 3111, 3299]
    return newPorts

def usePorts():
    newPorts = []
    try:
        jsonFile = open(electionConfig, 'r')
        jsonData = json.load(jsonFile, object_pairs_hook=collections.OrderedDict)
        rangePorts = jsonData["available-ports"]
        listPorts = jsonData["used-ports"]
        usingPorts = []
        for x in range(len(listPorts)):
            usingPorts.extend(listPorts[x])        
        for openPort in range(rangePorts[0], rangePorts[1]):
            if openPort in usingPorts:
                continue
            newPorts.append(openPort)
            if len(newPorts) >= 5:
                break
        if len(newPorts) < 5:
            print("Not enough ports available.")
            quit()
        listPorts.append(newPorts)
        jsonData["used-ports"] = listPorts
        jsonFile.close()
    except IOError:
        createElec()
        newPorts = [3300, 3301, 3302, 3111, 3299]
    return newPorts

def getID():
    manifestHash = hashManifest()
    elecID = manifestHash[:5]
    print(manifestHash)
    return elecID

def hashManifest():
    manifest_raw = codecs.open(manifest, 'r', encoding='utf8').read()
    manifest_raw = manifest_raw.replace("\n", '').replace("\r", '').strip()
    m = hashlib.sha1()
    m.update(manifest_raw)
    return m.hexdigest()

electionConfig = "ElectionConfigFile.json"
manifest = "_sElectConfigFiles_/ElectionManifest.json"
collectingConf = "CollectingServer/config.json"
bulletinConf = "BulletinBoard/config.json"
mixConf = "MixServer/config.json"       #needed? same as mix00
mix00Conf = "templates/config_mix00.json"
mix01Conf = "templates/config_mix01.json"
mix02Conf = "templates/config_mix02.json"
nginxConf = "nginx_select.conf"


votingTime = 300    #sec
ports = usePorts()

#modify ElectionManifest
currentTime = getTime().strftime("%Y.%m.%d %H:%M GMT+0100")
endingTime = addSec(getTime(), votingTime).strftime("%Y.%m.%d %H:%M GMT+0100")
jwrite(manifest, "startTime", currentTime)
jwrite(manifest, "endTime", endingTime)
jwriteAdv(manifest, "collectingServer", "http://localhost:" + str(ports[4]), "URI")
jwriteAdv(manifest, "bulletinBoards", "http://localhost:" + str(ports[3]), 0, "URI")
jwriteAdv(manifest, "collectingServer", "http://localhost:" + str(ports[4]), "URI")
jwriteAdv(manifest, "mixServers", "http://localhost:" + str(ports[0]), 0, "URI")
jwriteAdv(manifest, "mixServers", "http://localhost:" + str(ports[1]), 1, "URI")
jwriteAdv(manifest, "mixServers", "http://localhost:" + str(ports[2]), 2, "URI")

#get ID after modifying Manifest
electionID = getID()
srcfile = os.getcwd()
dstroot = os.path.join(os.path.split(srcfile)[0], electionID + "_" + os.path.split(srcfile)[1])
jAddList(electionConfig, "electionIDs", electionID)
ports.insert(0, electionID)
jAddList(electionConfig, "used-ports", ports)

#modify Server ports
jwrite(mix00Conf, "port", ports[0])
jwrite(mix01Conf, "port", ports[1])
jwrite(mix02Conf, "port", ports[2])
jwrite(bulletinConf, "port", ports[3])
jwrite(collectingConf, "port", ports[4])
jwrite(mixConf, "port", ports[0])   #needed?

#modify nginx File
nginxFile = open(nginxConf, 'r+')
nginxData = nginxFile.readlines()
lastBracket = 0
counter = 0
for line in nginxData:
    if line == "}\n":
        lastBracket = counter
    counter = counter + 1
bracketIt = nginxData[lastBracket:]
del nginxData[lastBracket:]
comments = ["\n     # Collecting server_" + electionID + " \n", "    location /collectingServer_" + electionID + "/ {\n", "        proxy_pass http://localhost:" + str(ports[4]) + "/;\n", "    }\n", "\n",
            "    # Mix server_" + electionID + " #1\n", "    location /mix/00_" + electionID + "/ {\n", "        proxy_pass http://localhost:" + str(ports[0]) + "/;\n", "    }\n", "\n",
            "    # Mix server_" + electionID + " #2\n", "    location /mix/01_" + electionID + "/ {\n", "        proxy_pass http://localhost:" + str(ports[1]) + "/;\n", "    }\n", "\n",
            "    # Mix server_" + electionID + " #3\n", "    location /mix/03_" + electionID + "/ {\n", "        proxy_pass http://localhost:" + str(ports[2]) + "/;\n", "    }\n", "\n",
            "    # Bulletin board_" + electionID + " \n", "    location /bulletinBoard_" + electionID + "/ {\n", "        proxy_pass http://localhost:" + str(ports[3]) + "/;\n", "    }\n"]
comments.extend(bracketIt)
nginxData.extend(comments)
nginxFile.seek(0)
nginxFile.writelines(nginxData)
nginxFile.close()


copy(srcfile, dstroot)
link(dstroot)

os.system("nginx -s reload")
subprocess.call([dstroot + "/start.sh"], cwd=dstroot)

#subprocess.call([dstroot + "/clean.sh"], cwd=dstroot)
#subprocess.call([dstroot + "/CollectingServer", "node collectingServer.js &"], cwd=dstroot+"/CollectingServer")
#subprocess.call([dstroot + "/BulletinBoard/node bb.js &"], cwd=dstroot+"/BulletinBoard")
#subprocess.call([dstroot + "/mix/00/node mixServer.js &"], cwd=dstroot+"/mix/00")
#subprocess.call([dstroot + "/mix/01/node mixServer.js &"], cwd=dstroot+"/mix/01")
#subprocess.call([dstroot + "/mix/02/node mixServer.js"], cwd=dstroot+"/mix/02")

#os.system("cd "+dstroot+"/CollectingServer && node collectingServer.js &")
#os.system("cd "+dstroot+"/BulletinBoard && node bb.js &")
#os.system("cd "+dstroot+"/mix/00 && node mixServer.js &")
#os.system("cd "+dstroot+"/mix/01 && node mixServer.js &")
#os.system("cd "+dstroot+"/mix/02 && node mixServer.js")
