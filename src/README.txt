Prerequisites:
	Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files
	
	 To download and apply the JCE, see at the bottom of this page:
	 	http://www.oracle.com/technetwork/java/javase/downloads/index.html



How-To run the E-voting System process:

 1. Run class de.uni.trier.infsec.functionalities.pki.PKIServerApp

    This starts the public key environment which is needed for
    registration and lookup of public and verification keys.  
    The PKIServer stores the registered keys at %TEMP%PKI_server.db -
    if you want to delete registered keys, you will have to delete this
    file.

 2. Run class de.uni.trier.infsec.eVotingSystem.apps.ServersRegisterApp

    This will run the registration process for the collecting server and the 
    final server. 
    Server will register its keys at the PKI environment and store the serialized
    keys to folder %TEMP%/eVotingSystem/server1.info and %TEMP%/eVotingSystem/server2.info,
    respectively.

 3. Run class de.uni.trier.infsec.eVotingSystem.apps.VoterRegisterApp
	with parameter <voter_id [int]>

    This will run registration process for the voter with that voter_id. It will
    register its keys at the PKI environment and store the serialized
    keys to folder %TEMP%/eVotingSystem/voter$(voter_id).info
    NOTE:
    	we require that the voterID is a number between 1 and Params.NumberOfVoters:
    				0 <= voterID < Params.NumberOfVoters


EXAMPLE:
========

Run following commands from bin-folder of the compiled project:

1. java -cp ".:../lib/*" de.uni.trier.infsec.functionalities.pki.PKIServerApp

2. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.ServersRegisterApp

3. java -cp ".:../lib/*" de.uni.trier.infsec.eVotingSystem.apps.VoterRegisterApp



