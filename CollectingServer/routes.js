var fs = require('fs');
var request = require('request-json');
var config = require('./config');
var manifest = require('./manifest');
var server = require('./server');
var sendEmail = require('./sendEmail');

///////////////////////////////////////////////////////////////////////////////////////

// OTP STORE

var otp_store = {};

///////////////////////////////////////////////////////////////////////////////////////
// ROUTE otp
//

exports.otp = function otp(req, res) 
{
    var email = req.body.email;
    if (email) {
        if (!server.eligibleVoters[email]) // Check if the voter is eligible
        {
            console.log('Voter not eligible', email);
            res.send({ ok: false, descr: 'Invalid voter identifier (e-mail)' }); 
        }
        else // eligible voter create a fresh OTP and send it
        {
            // Get a fresh otp (Java async call)
            console.log('Obtaining OTP...');
            server.getFreshOTP(function(err, otp) {
                if (err) {
                    console.log(' ...Internal error:', err);
                    res.send({ ok: false, descr: 'Internal error. Cannot obtain an OTP' }); 
                    return;
                }
                // We have a fresh otp
                console.log(' ...Obtained a fresh OTP: ', otp);
                otp_store[email] = otp // store the opt under the voter id (email)
                // schedule reset of the otp
                setTimeout( function(){ otp_store[email]=null; }, 10*60000); // 10 min

                // Send an email
                /*
                console.log('Sending an emal with otp to', email, otp);
                sendEmail(email, 'Your One Time Password for sElect', otp, function (err,info) {
                    if (err) {
                        console.log(' ...Error:', err);
                    }else{
                        console.log(' ...E-mail sent: ' + info.response);
                    }
                    res.send({ ok: true }); 
                })
                */
                res.send({ ok: true }); // TODO: this is nestead of tha above
            }); 
        }
    }
    else 
        res.send({ ok: false, descr: 'Empty e-mail address' }); 
};

///////////////////////////////////////////////////////////////////////////////////////
// ROUTE cast
//

exports.cast = function cast(req, res) 
{
    var email = req.body.email;
    var otp = req.body.otp;
    var ballot = req.body.ballot;
    console.log('BALLOT COMING:', email, otp, ballot);

    // make sure that we have all the pieces:
    if (!email || !otp || !ballot ) {
        res.send({ ok: false, descr: 'Wrong request' }); 
        return;
    }

    // Check the otp (and, implicitly, the identifier)
    console.log('Checking the otp for a voter:', email, otp);
    if (otp_store[email] === otp) {
        console.log(' ...otp correct');

        // Cast the ballot:
        console.log('CollectBallot for', email );
        server.collectBallot(email, ballot, function(err, response) {
            if (err) {
                console.log(' ...Internal error: ', err);
                res.send({ ok: false, descr: 'Internal error' }); 
            }
            else if (!response.ok) {
                console.log(' ...Ballot rejected: ', response.data);
                res.send({ ok: false, descr: response.data }); 
            }
            else { // everything ok
                // TODO Now the result has a different form
                console.log(' ...Balloc accepted. Response = ', response);
                res.send({ ok: true, receipt: response.data }); 
            }
        });
    }
    else // otp not correct
    {
        console.log(' ...Invalid OTP');
        res.send({ ok: false, descr: 'Invalid OTP (one time password)' }); 
        // if an invalid otp is given, we require that a new otp be generated (reset otp):
        otp_store[email] = null;
    }
};



///////////////////////////////////////////////////////////////////////////////////////
// ROUTE info
//
exports.info = function info(req, res)  {
    res.render('info', {manifest:manifest});
}

///////////////////////////////////////////////////////////////////////////////////////
// ROUTE end
//

var finServ = request.newClient(config.finalServURI); // FIXME (this should be read from the manifest)

// Save result in a file
function saveResult(result) {
    fs.writeFile(config.RESULT_FILE, result, function (err) {
        if (err) 
            console.log('Problems with saving result', config.RESULT_FILE);
        else 
            console.log('Result saved in', config.RESULT_FILE);
    });
}

// Send result to the final server
function sendResult(result) {
    console.log('Sending result to the final server');
    var data = {data: result}
    finServ.post('data', data, function(err, otp_res, body) {
        if (err) {
            console.log(' ...Error: Cannot send the result to the final server: ', err);
        }
        else {
            console.log(' ...Result sent to the final server.');
            console.log(' ...Response:', body);
        }
    });
}

exports.close = function close(req, res)  {
    res.send({ ok: true, info: "triggered to close the election" }); 
    console.log('Closing election.');
    console.log('Getting the result...');
    server.getResult(function(err, result) {
        if (err) {
            console.log(' ...Internal error. Cannot fetch the result: ', err);
        }
        else { 
            console.log('Result:', result);
            sendResult(result);
            saveResult(result);
        }
    });
}

