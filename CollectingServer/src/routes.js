var fs = require('fs');
var util = require('util');
var request = require('request-json');
var winston = require('winston');
var config = require('../config');
var manifest = require('./manifest');
var csCore = require('./csCore.js');
var sendEmail = require('./sendEmail');
var crypto = require('cryptofunc');
var selectUtils = require('selectUtils');

///////////////////////////////////////////////////////////////////////////////////////
// State

var otp_store = {};
var mail_timestamp = {};

var electionID = manifest.hash;
var colSerSigKey = config.signing_key;
var cs = csCore.create(electionID, colSerSigKey);
var openElection = (manifest.voters.length === 0); // emtpy list of voters means that the election is open (everybody can vote)
if (openElection)
    console.log('Empty list of voters => election is open (it ballots from everybody)')
var listOfEligibleVoters = manifest.voters.map(function(k){ return k.email; });

// Map of eligible voters
var eligibleVoters = {};
for (var i=0; i<listOfEligibleVoters.length; ++i) eligibleVoters[listOfEligibleVoters[i]] = true;

// Checks if the voter is eligible. In an election is open, then every voter is eligible.
function isEligibleVoter(voter) {
    return  (openElection && validEmail(voter)) || (eligibleVoters.hasOwnProperty(voter) && eligibleVoters[voter]===true);
}

var emailPattern = /^\S+@\S+$/;
var emailNegPattern = /[';\n\r]/;
function validEmail(email) {
    return emailPattern.test(email) && !emailNegPattern.test(email);
}

//  status and opening/closing time
var resultReady = fs.existsSync(config.RESULT_FILE);

// If there is no result yet, but there are some accepted
// ballots, we need to read these ballots to resume the operation:
if (!resultReady && fs.existsSync(config.ACCEPTED_BALLOTS_LOG_FILE)) {
    console.log('Resuming (reading already accepted ballots)');
    var log_data = fs.readFileSync(config.ACCEPTED_BALLOTS_LOG_FILE, {flags:'r', encoding:'utf8'});
    log_data.split('\n').forEach(function (entry) { // for each entry in the log file
        if (entry==='') return;
        entry = JSON.parse(entry);
        console.log('  - processing a ballot of', entry.email);
        cs.collectBallot(entry.email, entry.ballot);
    });
}

// Status
function Status() {
    var _open = false;
    var _closed = false;

    return {
        isOpen : function () {
            return _open;
        },
        isClosed : function () {
            return _closed;
        },
        isActive : function () {
            return _open && !_closed;
        },
        status : function () {
            if (_closed) return 'closed'
            else if (_open) return 'open'
            else return 'not open yet';
        },
        open : function () {
            if (!_closed) _open = true;
        },
        close : function () {
            _closed = true;
        }
    }
}
var status = Status();

// Time
var startTime = new Date(manifest.startTime);
var endTime = new Date(manifest.endTime);
var now = new Date();
var timeToOpen = startTime - now;
var timeToClose = endTime - now;
console.log('Time to open:  %s (%s)', selectUtils.timeDelta2String(timeToOpen), startTime);
console.log('Time to close: %s (%s)', selectUtils.timeDelta2String(timeToClose), endTime);

// DETERMINING THE INITIAL STATUS

// if the result is ready (saved in the file), set the status to
// closed (no further ballots are accepted; the result will not be
// sent)
if (resultReady) {
    console.log('Result already exits. Election status set to closed');
    status.close();
}
// if the result is not ready yet, but the time is over, close
// election (this will save and send the result, and set the
// status to closed):
else if (timeToClose <= 0) {
    closeElection();
}
// otherwise (the result is not ready, time is not over) set the
// timeouts for the opening and closing:
else {
    if (timeToClose > 2147483647) { // it is too much for node (v8); the event would fire immediately
        console.log('WARNING: the closing time is too far in the future.');
        console.log('         The election will not close automatically.');
        // FIXME This is clearly a temporary solution. We should handle this limitation.
    }
    else {
        setTimeout(closeElection, timeToClose);
    }

    if (timeToOpen > 2147483647) { // again, too much for v8
        console.log('WARNING: the opening time is too far in the future.');
        console.log('         The election will not open automatically.');
        // FIXME As above.
    }
    else {
        setTimeout(function() {
            status.open();
            winston.info('OPENING ELECTION.');
        }, timeToOpen); // fires right away (in the next click), if timeToOpen <= 0;
    }
}

var log = fs.createWriteStream(config.ACCEPTED_BALLOTS_LOG_FILE, {flags:'a', encoding:'utf8'});

///////////////////////////////////////////////////////////////////////////////////////
// ROUTE otp
//

exports.otp = function otp(req, res) 
{
    var email = req.body.email.trim();
    var reqElId = req.body.electionID;

    if (!status.isActive()) { // if the status is not active, reject the request
        var descr = (status.isClosed() ? 'Election closed' : 'Election not opened yet');
        winston.info('OTP request (%s) ERROR: %s.', email, descr);
        res.send({ ok: false, descr: descr }); 
        return;
    }

    if (!email || !reqElId) {
        winston.info('OTP request (%s) ERROR: Malformed request', email);
        res.send({ ok: false, descr: 'Malformed request' }); 
        return;
    }

    if (reqElId !== electionID) {
        winston.info('OTP request (%s) ERROR: Wrong election id', email);
        res.send({ ok: false, descr: 'Wrong election ID' }); 
        return;
    }

    if (!isEligibleVoter(email)) // not eligible voter
    {
        winston.info('OTP request (%s) ERROR: Voter not eligible', email);
        res.send({ ok: false, descr: 'Invalid voter identifier (e-mail)' });
    }
    else // voter is eligible; create of retrieve an OTP and send it to the voter
    {
        // Generate OTP (if not generated yet):
        if (otp_store[email]==null) { // OTP not generated yet (for the given voter)
            // generate store the OTP under the voter id (email)
            otp_store[email] = crypto.nonce().slice(0,10); // an OTP will have 5 bytes
            winston.info('OTP request (%s) accepted. Fresh OTP = %s', email, otp_store[email]);
        }
        else // OTP already generated
            winston.info('OTP request (%s) accepted. Already stored OTP = %s', email, otp_store[email]);

        // Send email:
        if (config.sendEmail) {
            var now = new Date();
            var sentRecently = mail_timestamp[email] != null
                               && (now - mail_timestamp[email]) < config.timespanEmail*60000; // timespan is specified in minutes (in the config file)
            if (!sentRecently) {
                // Sent an e-mail with the OTP
                winston.info('Sending an email to \'%s\' with OTP  ', email, otp_store[email]);
                var emailContent = "This e-mail contains your one time password (OTP) for the sElect voting system. \n\n";
                emailContent += 'Election title: ' + manifest.title + '\n\nOne time password: ' + otp_store[email] + '\n\n';
                emailContent += 'If you have not logged into the sElect voting system using this e-mail address, please ignore this e-mail.\n';
                sendEmail(email, 'Your One Time Password for sElect', emailContent, function (err,info) {
                    if (err) {
                        winston.info(' ...Error:', err);
                        // TODO: what to do if we are here (the e-mail has not been sent)?
                        res.send({ ok: false, descr: 'Problems in sending the E-mail.' });
                    } else {
                        winston.info(' ...E-mail sent: ' + info.response);
                        mail_timestamp[email] = new Date(); // now
                        res.send({ ok: true });
                    }
                });
            }
            else { // otp was sent recently
                winston.info('E-mail to \'%s\' was sent recently (not sent this time).', email );
                res.send({ ok: true });
            }
        }
        else { // ! config.sentEmail
            res.send({ ok: true });
        }
    }
};

///////////////////////////////////////////////////////////////////////////////////////
// ROUTE cast
//

exports.cast = function cast(req, res) 
{
    var email = req.body.email.trim();
    var otp = req.body.otp.trim();
    var ballot = req.body.ballot;
    var reqElID = req.body.electionID;
    
    // make sure that we have all the pieces:
    if (!email || !otp || !ballot || !reqElID) {
        winston.info('Cast request (%s) ERROR: Invalid request', email)
        res.send({ ok: false, descr: 'Invalid request' });
        return;
    }

    // do the election ID's match?
    if (electionID !== reqElID) {
        winston.info('Cast request (%s) BALLOT REJECTED - WRONG ELECTION ID', email);
        res.send({ ok: false, descr: 'Wrong election ID' }); 
        return;    
    }
    
    // is the server active?
    if (!status.isActive()) {
        var descr = (status.isClosed() ? 'Election closed' : 'Election not opened yet');
        winston.info('Cast request (%s) ERROR: %s.', email, descr)
        res.send({ ok: false, descr: descr }); 
        return;
    }

    // Check the OTP (and, implicitly, the identifier)
    if (otp_store.hasOwnProperty(email) && otp_store[email] === otp) {
        // Cast the ballot:
    	var response = cs.collectBallot(email, ballot); 
    	if (!response.ok) {
    		winston.info('Cast request (%s/%s) BALLOT REJECTED. Response = %s', email, otp, response.data);
    		res.send({ ok: false, descr: response.data }); 
    	}
    	else { // everything ok
    		winston.info('Cast request (%s/%s) accepted', email, otp);
    		res.send({ ok: true, receipt: response.data }); 
    		// log the accepted ballot
    		log.write(JSON.stringify({ email:email, ballot:ballot })+'\n', null,
    				function whenFlushed(e,r) {
                    winston.info('Ballot for %s logged', email);
    		});
    		// TODO: how to make sure that this stream is flushed right away?
    	}
    }
    else // OTP not correct
    {
        winston.info('Cast request (%s/%s) ERROR: Invalid OTP', email, otp);
        res.send({ ok: false, descr: 'Invalid OTP (one time password)' });
    }
};



///////////////////////////////////////////////////////////////////////////////////////
// ROUTE info
//
exports.info = function info(req, res)  {
    var now = new Date();
    var timeToOpen  = selectUtils.timeDelta2String(startTime - now);
    var timeToClose = selectUtils.timeDelta2String(endTime - now);
    var openingTime = util.format('Time to open:  %s (%s)', selectUtils.timeDelta2String(timeToOpen), startTime);
    var closingTime = util.format('Time to close: %s (%s)', selectUtils.timeDelta2String(timeToClose), endTime);

    res.send({
        electionID : manifest.hash,
        status: status.status(),
        title: manifest.title,
        timeToOpen: timeToOpen,
        timeToClose: timeToClose,
    });
}


///////////////////////////////////////////////////////////////////////////////////////
// ROUTE controlPanel
//
exports.controlPanel = function info(req, res)  {
    var now = new Date();
    var timeToOpen = startTime - now;
    var timeToClose = endTime - now;
    var openingTime = util.format('Time to open:  %s (%s)', selectUtils.timeDelta2String(timeToOpen), startTime);
    var closingTime =     util.format('Time to close: %s (%s)', selectUtils.timeDelta2String(timeToClose), endTime);
    res.render('info', {manifest:manifest, 
                        status: status.status(),
                        openingTime: openingTime, 
                        closingTime: closingTime,
                        active: status.isActive(),
                        resultReady:resultReady});
}

///////////////////////////////////////////////////////////////////////////////////////
// ROUTE close
//
exports.close = function close(req, res)  {
    if (status.isClosed()) {
        res.send({ ok: false, info: "election already closed" }); 
    }
    else {
        res.send({ ok: true, info: "triggered to close the election" }); 
        closeElection();
    }
}


var mixserv_options = {};
if (config.ignore_fin_serv_cert)
    mixserv_options = {rejectUnauthorized: false};

function closeElection() {
    if (status.isClosed()) return;
    status.close();
    winston.info('CLOSING ELECTION.');
    // get the result, send it to the first mix server, and save it.
    var result = cs.getResult();
    // send the result to the first mixserver
    sendData(result, manifest.mixServers[0].URI, mixserv_options);
    saveData(result, config.RESULT_FILE);
    
    if (manifest.publishListOfVoters) {
        var signedVotersList = cs.getVotersList();
        saveData(signedVotersList, config.VOTERSLIST_FILE);
    }
}

// Send data to the server with that URI
// The last parameter is *optional*
function sendData(data, URI, destserv_options) {
	winston.info("Sending data to '%s'", URI);
	if(destserv_options)
		var destServ = request.newClient(URI, destserv_options);
	else
		var destServ = request.newClient(URI);
    var toBeSent = {data: data};
    // one could add something like {timeout:10000} to the request below, after 'toBeSent'
    destServ.post('data', toBeSent, function(err, otp_res, body) {
        if (err) {
            winston.info(" ...Error: Cannot send the result to '%s': ", URI, err);
        }
        else {
            winston.info(" ...Result sent to '%s'", URI);
            winston.info(" ...Response: ", body);
        }
    });
}

// Save data in a file
function saveData(data, file) {
    fs.writeFile(file, data, function (err) {
        if (err) 
            winston.info('Problems with saving the data:\n', data);
        else {
            winston.info('Result saved in: ', file);
            resultReady = true;
        }
    });
}


///////////////////////////////////////////////////////////////////////////////////////
// Serve a particular static file
exports.serveFile = function serveFile(path) {
    return function (req, res) {
        fs.exists(path, function(exists) {
            if (exists) {
                fs.createReadStream(path).pipe(res);
            } else {
                res.status(404).send('404: Not found');
            }
        });
    }
}

