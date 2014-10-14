var config = require('../config');

exports.otp = function otp(req, res) 
{
    var email = req.body.email;
    if (email) {
        // TODO: come by with an otp and send it via e-mail
        res.send({ ok: true }); 
    }
    else {
        res.send({ ok: false }); 
    }
};

exports.cast = function welcome(req, res) 
{
    var email = req.body.email;
    var otp = req.body.otp;
    var ballot = req.body.ballot;
    console.log(' * I got ', email, otp, ballot);
    if (email && otp && ballot )
        res.send({ ok: true }); 
    else
        res.send({ ok: false }); 
};


