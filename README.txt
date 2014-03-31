PREREQUISITES:
=============

- Java 1.7 SDK
       To run this project within the Oracle (previously Sun) distribution of Java,
       you need to download and install:
      	   	
		Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files
		
  	For instance, see at the bottom of this page:
	    	http://www.oracle.com/technetwork/java/javase/downloads/index.html
	
	On the other hand, OpenJDK does not require special handling of the JCE policy files 
	since it is open source and therefore not export-restricted in the United States.

- Node.js (platform for server-side network applications)
  System tested on the 0.10.20 version.

- npm (not an abbreviation for 'Node Package Manager')
  System tested on the 1.3.11 version.

- Apache Ant 
  System tested on the 1.8.2 version.


HOW-TO compile:
======
How-To Compile the System (supported by build.xml):

- 'ant build': builds the system in the directory './bin' and the nodejs modules './BulletinBoard/node_modules'
- 'ant clean': cleans up the binary files in the directory './bin' and the nodejs modules in './BulletinBoard/node_modules'


HOW-TO run the E-voting System process:
======

 a) Run class de.uni.trier.infsec.functionalities.pki.PKIServerApp

    This starts the public key environment which is needed for
    registration and lookup of public and verification keys.  
    The PKIServer stores the registered keys at %TEMP%PKI_server.db -
    if you want to delete registered keys, you will have to delete this
    file.

 b) Run classes	de.uni.trier.infsec.eVotingSystem.apps.RegisterCollectingServer
    and		de.uni.trier.infsec.eVotingSystem.apps.RegisterFinalServer
    
    This runs the registration process for the collecting server and the
    final server, respectively. 
    Servers will register their keys at the PKI environment and store the
    serialized keys to folder %TEMP%/eVotingSystem/server1.info
    and %TEMP%/eVotingSystem/server2.info, respectively.
    
 c) Run classes de.uni.trier.infsec.eVotingSystem.apps.CollectingServerApp
    and 	de.uni.trier.infsec.eVotingSystem.apps.FinalServerApp
    
    This run respectively the collecting and the final server.

 d) Run class de.uni.trier.infsec.eVotingSystem.apps.VoterRegisterApp
    	with parameter <voter_id [int]> 
    
    This runs the registration process for the voter with that voter_ID.
    It registers its keys at the PKI environment and store the serialized 
    keys to the file %TEMP%/eVotingSystem/voter$(voter_id).info.
    This will run registration process for the voter with that voter_id. It will
    register its keys at the PKI environment and store the serialized
    keys to folder %TEMP%/eVotingSystem/voter$(voter_id).info
    NOTE:
    	we require that the voterID is a number between 1 and Params.NumberOfVoters:
    				0 <= voterID < Params.NumberOfVoters

 e) Run classes	de.uni.trier.infsec.eVotingSystem.apps.VoterApp 
    or		de.uni.trier.infsec.eVotingSystem.apps.VotingCmdLine
    
    This will run respectively the GUI or the command line allowing voters to vote.  
 
 f) Classes de.uni.trier.infsec.eVotingSystem.apps.VerifYourVote 
    or	    de.uni.trier.infsec.eVotingSystem.apps.VerifierCmdLine
    
    This runs respectively the GUI and the command line allowing voters 
    to verify their vote when the election is over.
 
 g) Run class de.uni.trier.infsec.eVotingSystem.apps.DeleteLocalFiles
    
    In order to delete local files created (e.g. the databases) which are stored
    in your %TEMP%/PKIServer.db, %TEMP%/eVotingSystem and both the Collecting and 
    Final servers' output. 


EXAMPLE:
========

Run the following commands from bin-folder of the compiled project:

1. java -cp ".:../lib/*" de.uni.trier.infsec.functionalities.pki.PKIServerApp

2. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.RegisterCollectingServer
3. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.CollectingServerApp

4. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.RegisterFinalServer
5. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.FinalServerApp

6. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.RegisterVoter

GUI:
7a. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.VoterApp
Command Line:
7b. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.VotingCmdLine <voter_id [int]> <candidate_number [int]>


When the election is over, it is possible to inspect the election outcome at the web address 
	http://localhost:3000/ 
by running the following command from the BulletinBoard-folder

8. 	node app.js

In order to verify whether the vote has been properly counted, run the following 
command from bin-folder of the compiled project:

GUI:
9a. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.VerifYourVote
Command Line:
9b. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.VerifierCmdLine  <receipt_fname> <partial_result_fname> <final_result_fname>
    where    <receipt_fname> 		%TEMP%/eVotingSystem/receipt_$(voter_id).msg 
	     <partial_result_fname>	../BulletinBoard/public/SignedPartialResult.msg
	     <final_result_fname>	../BulletinBoard/public/SignedFinalResult.msg

In order to delete all local files produced:

10. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.DeleteLocalFiles
