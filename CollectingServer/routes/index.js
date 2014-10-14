var config = require('../config');

exports.otp = function otp(req, res) 
{
    var email = req.body.email;
    if (email) {
        var otp = '444777'; // TODO: obtain it from the core
        res.send({ ok: true, otp:otp }); 
    }
    else {
        res.send({ ok: false }); 
    }
};

exports.cast = function welcome(req, res) 
{
    var email = req.body.email;
    var otp = req.body.otp;
    var choice = req.body.choice;
    console.log(' * I got ', email, otp, choice);
    if (email && otp && choice )
        res.send({ ok: true }); 
    else
        res.send({ ok: false }); 
};


