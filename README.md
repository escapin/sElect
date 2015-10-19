# sElect - Secure Elections

sElect is a remote electronic voting system designed to provide 
**privacy** and **verifiability**.

One of our goals is to establish privacy on the
**implementation level** (code level). The code-level
verification of the core of the system (implemented in Java) 
is carried out within the DFG Priority
Programme *Reliably Secure Software Systems* (RS3)
(https://www.spp-rs3.de).

## Dependencies

* Java JDK (tested with both openjdk-7 and oraclejdk-8).
* Java Cryptography Extension (only for oraclejdk).
* node.js and npm.
* wget (used only in the makefiles for getting the proper libraries).
* python (used only in the root makefile for configuring the mix servers).

## The Design

To achieve its security goals (verifiability and privacy), 
the sElect voting system uses only standard cryptographic
operations, such as public key encryption and digital signatures
(unlike most other e-voting systems that aim at providing these security
goals). The design is also relatively simple (considering, again,
the security goals the system is designed to achieve).

There are three core components of the system: the **client
program**, the **collecting server** and the **final sever** (a
longer cascade of servers is conceivable in order to further
increase privacy).  Both servers post data, such as lists of
voters, intermediate data, and the final result on a publicly
available bulletin board, for which we also provide a reference
implementation.

**Voting phase.** In the voting phase, voters prepare their
ballots using the client program.  A ballot contains the voter's
choice (for example, the name of the candidate chosen by the
voter) and a unique, randomly chosen _verification code_.
To construct the ballot, first the choice along with the
verification code is encrypted with the public key of the final
server. The resulting ciphertext, called an _inner ballot_,
is then encrypted with the public key of the collecting
server. Such a (complete) ballot is then submitted to the
collecting server which authenticates the voter and, if the
authentication succeeds, replies by sending back a digitally
signed acknowledgment.

**Tallying phase.** When the voting phase is over, the system
enters the tallying phase. In this phase, first the collecting
server performs the following operations. It decrypts the outer
encryption layer of all the collected ballots and publishes the
resulting list of inner ballots in alphabetical order. This list
is digitally signed by the server. The collecting server also
outputs the list of all the voters who have successfully
submitted their ballots, again in alphabetical order and
digitally signed.

Next, the final server reads and decrypts the list of inner ballots
produced by the collecting server. The server then publishes the
resulting list containing the voters' choices along with verification
codes, again in alphabetical order and digitally signed. This
list constitutes the official result of the election process.

Altogether, the core of sElect is a variant of a _Chaumian mix
net_.


## Security Properties

_Verifiability_ is achieved in a very direct way: once the result
has been published, every voter can simply check whether her
verification code is included in the published election result,
along with the voter's choice. For this mechanism to work, one needs to make
sure that the client program is honest and indeed uses a randomly
chosen, and hence unique, verification code.

Furthermore, sElect also provides a
reasonable level of _accountability_: when a voter has a signed
acknowledgment from the collecting server and then the
verification fails (the expected verification code is not listed
as required), it is possible to tell which of the servers has
misbehaved and even (by making use of the digital signatures)
to provide an evidence for this misbehavior.

sElect provides _privacy_ under the assumption that one of the
servers is honest (the two servers do not collude). The steps
taken by an honest server, by design, hide the link between its
input and output entries. Therefore, no one can link the ballot
of a given voter to his/her choice-identifier-pair in the final
output.


## Development Environment (under construction)

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

*Mix server*:
```
cd MixServer
node mixServer.js
```

*Bulletin board*:
```
cd BulletinBoard
node bb.js
```

*Voting booth*:

Simply open VotingBooth/webapp/votingBooth.html in your browser.

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