# sElect -- Secure Election

sElect is a remote electronic voting system designed to provide 
**privacy** and **verifiability**.

One of our goals is to establish privacy on the
**implementation level** (code level). The code-level
verification of the core of the system (implemented in Java) 
is carried out within the DFG Priority
Programme *Reliably Secure Software Systems* (RS3)
(https://www.spp-rs3.de).

## Dependencies

* Java JDK (tested with openjdk-7).
* Bouncy Castle crypto Java library (please, put the appropriate jar file in the 'lib' folder).
* Junit Java library (please, put the appropriate jar file in the 'lib' folder).
* node.js and npm.
* node-jasmine (only for unit testing; type "npm install node-jasmine -g"
  to install).

## Development Environment

The development environment can be created with

```
make devenv
```

It creates a locally runnable configuration with example config files and 
an example election manifest. The election manifest contains, in particular, the
list of (e-mail addresses of) eligible voters. The created files can be removed by 
`make devclean`. 

Once the development environment is created, the components of the system can 
be started in the following way.

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

Simply open VotingBooth/votingBooth.html in your browser.

The files created during the voting process (including logs and partial 
and final results) can be removed with
```
make cleanElection
```

**Remark.**
In the development version, one-time passwords are not sent to
voters via e-mail; they are logged on the console by the
collecting server and must be copied from there. Also, the
collecting server, as of now, ignores the opening and closing
times. The server needs to be triggered manually to close the
election, which can be done through the status page served by
this server.

**Unit Testing.**
To run the test suite, type
```
make test
```
