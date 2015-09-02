# sElect - Secure Elections

sElect is a remote electronic voting system designed to provide 
**privacy** and **verifiability**.


## Dependencies

* Java JDK (tested with both openjdk-7 and oraclejdk-8).
* Java Cryptography Extension (only for oraclejdk).
* node.js and npm.
* wget (used in the makefiles for getting the proper libraries).
* python (used in the root makefile for configuring the mix servers).

The system has been developed and deployed on Ubuntu Server 14.04.2 LTS.

## The Design

To achieve its security goals (verifiability and privacy), 
the sElect voting system uses only standard cryptographic
operations, such as public key encryption and digital signatures
(unlike most other e-voting systems that aim at providing these security
goals). The design is also relatively simple (considering, again,
the security goals the system is designed to achieve).

There are three core components of the system: the **client
program**, the **collecting server** and a cascade of **mix severs**.
All servers post data, such as lists of voters, intermediate results, and 
the final result on a publicly available bulletin board, 
for which we also provide a reference implementation.

**Voting phase.** In the voting phase, every voter prepare her ballots
using the client program.  A ballot contains the voter's choice (for
example, the name of the candidate chosen by the voter) and a 
verification code, whose first nine characters are inserted by the 
user, whereas the remaining are randomly chosen by the system.
To construct the ballot, the choice along with the verification code 
is encrypted several times with the public key of each mix server, 
from the last to the first.  
Such a (complete) ballot is then submitted to the collecting server which
authenticates the voter and, if the authentication succeeds, replies by
sending back a digitally signed acknowledgment.

**Mixing phase.** When the voting phase is over, the system enters the
mixing phase. In this phase, the collecting server outputs the list of
ciphertexts to the first mix server which decrypts the outer encryption
layer, shuffles the inner ballots and sends the signed result both to
the next mix server and to the bulletin board.


Next, the bulletin board reads the list of (unencrypted) ballots
produced by the last mix server. It then publishes the
resulting list containing the voters' choices along with verification
codes, again in alphabetical order and digitally signed. This
list constitutes the official result of the election process.

Altogether, the core of sElect is a variant of a _Chaumian mix
net_.


## Security Properties

_Verifiability_ is achieved in a very direct way: once the result has
been published, every voter can simply check whether her verification
code is included in the published election result, along with the
voter's choice. For this mechanism to work, one needs to make sure that
the client program is honest and indeed uses a randomly chosen, and
hence unique, verification code.

Furthermore, sElect also provides a
reasonable level of _accountability_: when a voter has a signed
acknowledgment from the collecting server and then the
verification fails (the expected verification code is not listed
as required), it is possible to tell which of the servers has
misbehaved and even (by making use of the digital signatures)
to provide an evidence for this misbehavior.

sElect provides _privacy_ under the assumption that at least one of the
mix servers is honest. The steps taken by an honest server, by design,
hide the link between its input and output entries. Therefore, no one
can link the ballot of a given voter to his/her choice-identifier-pair
in the final output.


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
./run.sh
```

*Mix server(s)*:

for each 'dir' in the 'mix' folder,
```
cd mix/dir
./run.sh
```

*Bulletin board*:
```
cd BulletinBoard
./run.sh
```

*Voting booth*:
```
cd VotingBooth
./run.sh
```


The files created during the voting process (including logs and partial 
and final results) can be removed with
```
make cleanElection
```

**Remark.**
In the development version, one-time passwords are not sent
to voters via e-mail; they are logged on the console by the collecting
server and must be copied from there.  The list of the valid voters'
emails can be found in the file 'ElectionManifest.json' of the folder
'template'.  Also, the closing time of the collecting server is far away
in the future (Nov 10, 2018). The server needs to be triggered manually
to close the election, which can be done through the status page served
by this server.

**Unit Testing.**
To run the test suite, type
```
make test
```
The created files can be removed by 
`make testclean`.
