var config = require('../config');
var server = require('../server');

exports.otp = function otp(req, res) 
{
    var email = req.body.email;
    if (email) {
        // TODO: come up with an otp and send it via e-mail
        res.send({ ok: true }); 
    }
    else {
        res.send({ ok: false }); 
    }
};

exports.cast = function cast(req, res) 
{
    var email = req.body.email;
    var otp = req.body.otp;
    var ballot = req.body.ballot;
    console.log('BALLOT COMMING: ', email, otp, ballot);


    // make sure that we have all the pieces:
    if (!email || !otp || !ballot ) {
        res.send({ ok: false }); 
        return;
    }

    // TODO Check the otp.
    // For now we just accept the ballot

    // Cast the ballot:
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

};


