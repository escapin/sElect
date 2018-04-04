# sElect - Secure and Simple Elections

sElect is a lightweight remote e-voting system designed to provide the
following security properties:

- **privacy** of the votes: no one (in particular, no component of the
system) is able to find out how each voter voted;

- end-to-end **verifiability**: in case a component misbehaves
 (manipulates or removes some ballots), this misbehavior is noticed by
 the voters with high probability;

- **accountability**: a stronger form of verifiability ensuring that in
 case a component misbehaves, blaming evidence is shown to the voters to
 properly hold this component accountable.

The protocol of sElect has been cryptographically analyzed in
[[KMST16](https://eprint.iacr.org/2016/438)] w.r.t. these three security
properties.

The other research goal on sElect has been to establish the privacy
property directly on the **implementation level** (code
level). Code-level verification of the core of the system (implemented
in Java) has been carried out within the German DFG Priority Programme
Reliably Secure Software Systems (RS3) (https://www.spp-rs3.de).


## Dependencies

* Java JDK - tested with both openjdk-7 and oraclejdk-8.
* Java Cryptography Extension - only needed for oraclejdk.
* Node JS and Npm - tested on v6.11.2 LTS and 3.10.10, respectively.
* Python - tested with Python 2.7.10.

The system has been developed and deployed on Ubuntu Server 16.04.3 LTS.


## The Design

Unlike most other e-voting systems aiming at providing verifiability,
accountability, and privacy, sElect uses only standard cryptographic
operations, such as public-key encryption and digital signatures. The
design is also relatively simple (considering, again, the security goals
the system is designed to achieve).

The system consists in five components: an **authenticator**, a **voting
booth** (both of them implemented as static web-pages), a **collecting
server**, and a cascade of **mix severs**.  All the data outputted by
these servers, i.e., the lists of voters and both the intermediate and
final results, are collected and reported by a publicly available
**bulletin board**.

**Voting phase.** After inserting valid authentication credentials,
voters prepare their ballots using the voting booth. A ballot contains
the voter's choice (for example, the name of the candidate chosen by the
voter) and a verification code, which can be either entirely generated
by the system or also partially inserted by the user.  Cryptographic
nonces are used to eusure the randomness of the automatically generated
(part of the) verification codes. To construct a ballot, the choice
along with the verification code is encrypted several times with the
public key of each mix server, from the last to the first.  Such a
(complete) ballot is then submitted to the collecting server which
authenticates the voter and, if the authentication succeeds, replies by
sending back a digitally signed acknowledgment.

**Mixing phase.** When the voting phase is over, the system enters the
mixing phase. In this phase, the collecting server outputs the list of
ciphertexts to the first mix server which decrypts the outer encryption
layer, shuffles the inner ballots, and sends the signed result to the
next mix server. Next, the bulletin board reads the list of
(unencrypted) ballots produced by the last mix server. It then publishes
the resulting list containing the voters' choices along with
the verification codes, in alphabetical order and digitally signed. This
list constitutes the official result of the election process.

Altogether, the core of sElect is a variant of a _Chaumian mix
net_.


## Security Properties

**Verifiability** is achieved in a very direct way: once the result has
been published, every voter can simply check whether her verification
code is included in the published election result, along with her
choice. For this mechanism to work, one needs to make sure that the
voting booth is honest and indeed uses a randomly chosen, and hence
unique, verification code. In case part of the verification code is
randomly chosen by the user, the assumption of an honest verification
client is not necessary anymore.

Furthermore, sElect also provides a reasonable level of
**accountability**: when a voter has a signed acknowledgment from the
collecting server and then the verification fails (the expected
verification code is not listed as required), it is possible to tell
which of the servers has misbehaved and even (by making use of the
digital signatures) to provide an evidence for this misbehavior.  For
this purpose, a _fully automated verification procedure_ implemented
within the voting booth is triggered as soon as the voter looks at the
election result: cryptographic checks are performed and, if a problem is
encountered, the specific misbehaving party is singled out and blaming
evidence of this misbehavior is produced to hold him accountable.

sElect provides **privacy** of the votes under the assumption that at
least one of the mix servers is honest. The steps taken by an honest mix
server, by design, hide the link between its input and output entries.
Therefore, no one can link the ballot of a given voter to her
choice/identifier pair in the final output.


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

*Authenticator*:
```
cd Authenticator
./run.sh
```

*Voting booth*:
```
cd VotingBooth
./run.sh
```

The web-page of the system (the voting booth) is then available at `localhost:3333/votingBooth.html`.

- To manually close the election, you have to access `localhost:3299/admin/panel` with credentials:

	* user: admin
	* pwd: 999


- Once the election is closed, to check the election result, visit/reload the voting booth page again `localhost:3333/votingBooth.html`.


The files created during the voting process (including logs, partial,
and final results) can be removed with
```
make cleanElection
```

**Remarks.**

* All the dates are displayed in the current browser's timezone.
* The closing time of the collecting server is set to _Nov 10, 2018_.
* In development mode, the OTP (one-time password) is sent directly to the voting booth which shows it in a pop-up. 



**Unit Testing.**
To run the test suite, type
```
make test
```
The created files can be removed by `make testclean`.
