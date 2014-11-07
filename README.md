# sElect -- Secure Election

sElect is a remote electronic voting system designed to provide 
**privacy** and **verifiability**.

One of our goal is to establish the privacy property on the
**implementation level** (code level). The code-level
verification of sElect is carried out within the DFG Priority
Programme *Reliably Secure Software Systems* (RS3)
(https://www.spp-rs3.de).

The core of the system is written in Java, while the applications 
built on top of the core are written in node.js.

## Dependencies

* Java JDK (tested with openjdk-7).
* Bouncy Castle crypto Java library (please, put the appropriate jar file in the 'lib' folder).
* Junit Java library (please, put the appropriate jar file in the 'lib' folder).
* node.js and npm.

## Development Environment

The development environment can be created with

```
make devenv
```

This will create a locally runnable configuration with example config files and 
an example election manifest. (These files can be removed by `make devclean`). Once the 
development environment is created, the components of the system can be 
started in the following way.

*Collecting server*:
```
cd CollectingServer
node collectingServer.js
```

*Final server*:
```
cd FinalServer
node finalServer.js
```

*Bulletin board*:
```
cd BulletinBoard
node bb.js
```

*Voting booth*:
```
cd VotingBooth
node votingBooth.js
```

The files created during the voting process (including logs and partial 
and final results) can be removed with
```
make clean
```
