#!/bin/bash


COL_BLUE="\x1b[34;01m"
COL_RESET="\x1b[39;49;00m"

cd bin/
# sign the manifest
echo -e $COL_BLUE"[1]"$COL_RESET"\tSign the manifest."
java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.SignTheManifest

# delete the content of 'Results' folder
echo -e $COL_BLUE"\n[2]"$COL_RESET"\tDelete the content of 'Results' folder."
java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.DeleteResults

# run the Collecting Server
echo -e $COL_BLUE"\n[3]"$COL_RESET"\tRun the Collecting Server."
java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.CollectingServerApp
