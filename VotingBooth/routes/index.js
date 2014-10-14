var config = require('../config');

exports.welcome = function welcome(req, res) 
{
    res.render('welcome', {title: "sElect Welcome", manifest: config.manifest});
};

exports.prompt_for_otp = function prompt_for_otp(req, res) 
{
    // TODO check if the e-mail is eligible, if not, redirect to
    //      the welcome page.
    //
    // TODO send the otp request to the collecting server

    // Render the page (prompting for the otp):
    res.render('otp', {title: "sElect Welcome", email:req.body.email, manifest: config.manifest});
};


exports.select = function select(req, res) 
{
    // render the page
    res.render('select', { title: "sElect Welcome", 
                           email: req.body.email, 
                           otp:   req.body.otp,
                           manifest: config.manifest});
}

exports.cast = function cast(req, res) 
{
    // TODO create the ballot
    //
    // TODO cast the ballot (send it to the collecting server)
    //
    // TODO depending on the server's response:
    //      - say that the ballot was cast, present the
    //        receipt-id and allow the user to save the receipt
    //      - report the problem and ask what to do (start again?)
    
    var choice_nr = req.body.choice; 
    if (choice_nr) {
        var choice = manifest.choicesList[choice_nr]
        res.render('cast',   { title: "sElect Welcome", 
                               email: req.body.email, 
                               choice: choice,
                               manifest: config.manifest });
    }
    else { // no candidate chosen
        res.render('error',   {error: "No candidate chosen"} );
    }
}

