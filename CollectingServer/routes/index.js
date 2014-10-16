var config = require('../config');
var server = require('../server');
var sendEmail = require('../sendEmail');

///////////////////////////////////////////////////////////////////////////////////////
// OTP STORE.
//
// We assign a fresh OTP when the voter asks for an OTP for the first time. 
// Then the OTP is returned (send via e-mail) whenever the voter queries 
// for it. 

var otp_store = {};


///////////////////////////////////////////////////////////////////////////////////////
// ROUTES
//

exports.otp = function otp(req, res) 
{
    var email = req.body.email;
    // TODO Check that the voter is eligible

    if (email) {

        function send_otp_and_continue(otp) // to pefrom, once we have an opt
        {
            console.log('Sending an emal with otp to', email, otp);
            sendEmail(email, 'Your One Time Password for sElect', otp, function (err,info) {
                if (err) {
                    console.log(' ...Error:', err);
                }else{
                    console.log(' ...E-mail sent: ' + info.response);
                }
                res.send({ ok: true }); 
            })
        }

        // Does the voter already have an otp?
        if ( otp_store[email] ) {
            send_otp_and_continue(otp_store[email]);            
        }
        else {
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
                send_otp_and_continue(otp);
            }); 
        }
    }
    else 
        res.send({ ok: false }); 
};

exports.cast = function cast(req, res) 
{
    var email = req.body.email;
    var otp = req.body.otp;
    var ballot = req.body.ballot;
    console.log('BALLOT COMMING:', email, otp, ballot);

    // make sure that we have all the pieces:
    if (!email || !otp || !ballot ) {
        res.send({ ok: false }); 
        return;
    }

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
        console.log(' ...otp not correct!');
        res.send({ ok: false }); 
    }
};


