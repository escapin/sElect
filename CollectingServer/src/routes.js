var fs = require('fs');
var util = require('util');
var request = require('request-json');
var winston = require('winston');
var config = require('../config');
var manifest = require('./manifest');
var server = require('./server');
var sendEmail = require('./sendEmail');
var crypto = require('cryptofunc');
var selectUtils = require('selectUtils');

///////////////////////////////////////////////////////////////////////////////////////
// State

var otp_store = {};

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
        server.collectBallotSync(entry.email, entry.ballot);
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
    var email = req.body.email;

    if (!status.isActive()) { // if the status is not active, reject the request
        var descr = (status.isClosed() ? 'Election closed' : 'Election not opened yet');
        winston.info('OTP request (%s) ERROR: %s.', email, descr)
        res.send({ ok: false, descr: descr }); 
        return;
    }

    if (email) {
        if (!server.eligibleVoters[email]) // Check if the voter is eligible
        {
            winston.info('OTP request (%s) ERROR: Voter not eligible', email);
            res.send({ ok: false, descr: 'Invalid voter identifier (e-mail)' });
        }
        else // eligible voter create a fresh OTP and send it
        {
            // Generate a fresh otp
            var otp = crypto.nonce().slice(0,10); // an otp will have 5 bytes
            winston.info('OTP request (%s) accepted. Fresh OTP = %s', email, otp);
            otp_store[email] = otp // store the opt under the voter id (email)
            // schedule reset of the otp
            setTimeout( function(){ otp_store[email]=null; }, 10*60000); // 10 min

            // Send an email
            if (config.sendEmail) {
                winston.info('Sending an emal with otp to', email, otp);
                var emailContent = 'Election: ' + manifest.title + '\n\nOne time password: ' + otp + '\n';
                sendEmail(email, 'Your One Time Password for sElect', emailContent, function (err,info) {
                    if (err) {
                        winston.info(' ...Error:', err);
                        // TODO: what to do if we are here (the e-mail has not been sent)?
                    }else{
                        winston.info(' ...E-mail sent: ' + info.response);
                    }
                });
            }

            res.send({ ok: true });
        }
    }
    else {
        res.send({ ok: false, descr: 'Empty e-mail address' }); 
    }
};

///////////////////////////////////////////////////////////////////////////////////////
// ROUTE cast
//

exports.cast = function cast(req, res) 
{
    var email = req.body.email;
    var otp = req.body.otp;
    var ballot = req.body.ballot;

    // make sure that we have all the pieces:
    if (!email || !otp || !ballot ) {
        winston.info('Cast request (%s) ERROR: Invalid request', email)
        res.send({ ok: false, descr: 'Invalid request' });
        return;
    }

    // is the server active?
    if (!status.isActive()) {
        var descr = (status.isClosed() ? 'Election closed' : 'Election not opened yet');
        winston.info('Cast request (%s) ERROR: %s.', email, descr)
        res.send({ ok: false, descr: descr }); 
        return;
    }

    // Check the otp (and, implicitly, the identifier)
    if (otp_store[email] === otp) {
        // Cast the ballot:
        server.collectBallot(email, ballot, function(err, response) {
            if (err) {
                winston.info('Cast request (%s/%s) INTERNAL ERROR %s', email, otp, err.toString());
                res.send({ ok: false, descr: 'Internal error' }); 
            }
            else if (!response.ok) {
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
                // TODO: how to make sure that this stream is flushed right away
            }
        });
    }
    else // otp not correct
    {
        winston.info('Cast request ERROR: Invalid OTP:', otp);
        res.send({ ok: false, descr: 'Invalid OTP (one time password)' }); 
        // if an invalid otp is given, we require that a new otp be generated (reset otp):
        otp_store[email] = null;
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
    var closingTime =     util.format('Time to close: %s (%s)', selectUtils.timeDelta2String(timeToClose), endTime);

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

var mixserv_options = {};
if (config.ignore_fin_serv_cert)
    mixserv_options = {rejectUnauthorized: false};


// Save result in a file
function saveResult(result) {
    fs.writeFile(config.RESULT_FILE, result, function (err) {
        if (err) 
            winston.info('Problems with saving result', config.RESULT_FILE);
        else {
            winston.info('Result saved in', config.RESULT_FILE);
            resultReady = true;
        }
    });
}

// Send result to the first mix server
function sendResult(result) {
    winston.info('Sending result to the first mix server');
    var mixServ = request.newClient(manifest.mixServers[0].URI, mixserv_options);
    var data = {data: result}
    // one could add something like {timeout:10000} to the request below, after 'data'
    mixServ.post('data', data, function(err, otp_res, body) {
        if (err) {
            winston.info(' ...Error: Cannot send the result to the first mix server: ', err);
        }
        else {
            winston.info(' ...Result sent to the first mix server.');
            winston.info(' ...Response:', body);
        }
    });
}

function closeElection() {
    if (status.isClosed()) return;
    status.close();
    winston.info('CLOSING ELECTION.');
    // get the result, send it to the final server, and save it.
    server.getResult(function(err, result) {
        if (err) {
            winston.info(' ...INTERNAL ERROR. Cannot fetch the result: ', err);
        }
        else { 
            sendResult(result);
            saveResult(result);
        }
    });
}

exports.close = function close(req, res)  {
    if (status.isClosed()) {
        res.send({ ok: false, info: "election already closed" }); 
    }
    else {
        res.send({ ok: true, info: "triggered to close the election" }); 
        closeElection();
    }
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

