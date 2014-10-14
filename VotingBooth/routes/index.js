var request = require('request-json');
var config = require('../config');

var colServ = request.newClient(config.colServURI);
///////////////////////////////////////////////////////////////////////////////////////////
// Helper functions


function renderError(res, error) {
    res.render('error',   {error: error} );
}

function createBallot(email, otp, choice) {
    // TODO implement it
    return "12fa7789ff8988";
}

///////////////////////////////////////////////////////////////////////////////////////////

exports.welcome = function welcome(req, res) 
{
    res.render('welcome', {title: "sElect Welcome", manifest: config.manifest});
};

exports.prompt_for_otp = function prompt_for_otp(req, res) 
{
    var email = req.body.email;

    // TODO check if the e-mail is eligible, if not, redirect to the welcome page.

    // Send the otp request to the collecting server:
    var data = { email:email };
    console.log('Trying to send: ', data);
    colServ.post('otp', data, function(err, otp_res, body){
        if(err) {
            renderError(res, "No otp responce from the collecting server");
        }
        else if (!body.ok) {
            renderError(res, "ERROR otp responce from the collecting server");
        }
        else {
            console.log('The collecting server accepted an otp reqest');
            // Render the page (prompting for the otp):
            res.render('otp', {title: "sElect Welcome", email:email, manifest: config.manifest});
        }
    });
}


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
    var email = req.body.email;
    var otp   = req.body.otp;
    var choice_nr = req.body.choice; 
    if (!choice_nr) {
        renderError(res, "No candidate chosen");
        return;
    }
    var choice = manifest.choicesList[choice_nr];
    if (!choice) {
        renderError(res, "Wrong candidate number");
        return;
    }
    if (!email || !otp || !choice ) {
        renderError(res, "Internal Error");
        return;
    }

    // create the ballot
    var ballot = createBallot(email, otp, choice);

    // send the ballot to the collecting server
    var data = { ballot:ballot, email:email, otp:otp };
    console.log('Trying to send: ', data);
    colServ.post('cast', data, function(err, otp_res, body){
        if (err) {
            renderError(res, "No responce from the collecting server. Ballot might have been not cast.");
            return;
        }
        if (!body.ok) {
            renderError(res, "Ballot not accepted by the collecting server");
            console.log('body: ', body);
            return
        }
        
        console.log('The collecting server accepted a ballto reqest');
        res.render('cast',   { title: "sElect Welcome", 
                               email: req.body.email, 
                               choice: choice,
                               manifest: config.manifest });
    });





    // TODO create the ballot
    //
    // TODO cast the ballot (send it to the collecting server)
    //
    // TODO depending on the server's response:
    //      - say that the ballot was cast, present the
    //        receipt-id and allow the user to save the receipt
    //      - report the problem and ask what to do (start again?)
    
}

