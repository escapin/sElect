var nodemailer = require('nodemailer');
var smtpTransport = require('nodemailer-smtp-transport');
var config = require('../config');

// Create an SMTP transporter
var transporter = nodemailer.createTransport(smtpTransport({
    host: config.smtp_host,
    port: config.smtp_port,
    // the following two lines are used to force nodemailer to use STARTTLS
    secure: false,
    requireTLS: true,
    auth: {
        user: config.smtp_user,
        pass: config.smtp_pass
    },
    tls: {rejectUnauthorized: false}
}));


function sendEmail(address, subject, text, callback) 
{
    var mailOptions = {
        from: config.sender,
        to: address,
        subject: subject,
        text: text
    };
    transporter.sendMail(mailOptions, callback);
}

module.exports = sendEmail;

