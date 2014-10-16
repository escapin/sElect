var request = require('request-json');
var config = require('../config');
var manifest = require('../manifest');
var server = require('../server');
var sendEmail = require('../sendEmail');

///////////////////////////////////////////////////////////////////////////////////////

// OTP STORE
// We assign a fresh OTP when the voter asks for an OTP for the first time. 
// Then the OTP is returned (send via e-mail) whenever the voter queries 
// for it. 

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
            res.send({ ok: false, descr: 'Voter not eligible' }); 
        }
        else // eligible voter create a fresh OTP and send it
        {
            // Get a fresh otp (Java async call)
            console.log('Obtaining OTP...');
            server.getFreshOTP(function(err, otp) {
                if (err) {
                    console.log(' ...Internal error', err);
                    res.send({ ok: false, descr: 'Internal error. Cannot obtain an OTP' }); 
                }
                // We have a fresh otp now
                console.log(' ...Obtained a fresh OTP: ', otp);
                otp_store[email] = otp // store the opt under the voter id (email)
                console.log('Sending an emal with otp to', email, otp);
                // Send e-mail
                sendEmail(email, 'Your One Time Password for sElect', otp, function (err,info) {
                    if (err) {
                        console.log(' ...Error:', err);
                    }else{
                        console.log(' ...E-mail sent: ' + info.response);
                    }
                    res.send({ ok: true }); 
                })
            }); 
        }
    }
    else 
        res.send({ ok: false }); 
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
        res.send({ ok: false }); 
        return;
    }

    // Check the otp (and, implicitly, the identifier)
    console.log('Checking the otp for a voter:', email, otp);
    if (otp_store[email] === otp) {
        console.log(' ...otp correct');

        // Cast the ballot:
        console.log('CollectBallot for', email );
        server.collectBallot(email, ballot, function(err, receipt) {
            if (err) {
                console.log(' ...Internal error: ', err);
                res.send({ ok: false, descr: 'Internal error' }); 
            }
            else if (receipt=='') {
                console.log(' ...Ballot rejected: ' );
                res.send({ ok: false, descr: 'Ballot rejected' }); 
                return;
            }
            else { // everything ok
                console.log(' ...Balloc accepted. Receipt = ', receipt);
                res.send({ ok: true, receipt: receipt }); 
            }
        });
    }
    else // otp not correct
    {
        console.log(' ...authorisation problem!');
        res.send({ ok: false }); 
    }
};


///////////////////////////////////////////////////////////////////////////////////////
// ROUTE end
//

var finServ = request.newClient(config.finalServURI);

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
            console.log('Sending result to the final server');
            var data = {data: result}
            finServ.post('data', data, function(err, otp_res, body){
                if (err) {
                    console.log(' ...Error: Cannot send the result to the final server: ', err);
                }
                else {
                    console.log(' ...Result sent to the final server.');
                    console.log(' ...Response:', body);
                }
            });
        }
    })
}

