var fs = require('fs');

// where to find files with the result
var FINAL_RESULT = 'FinalResult.txt';
var SIGNED_RESULT = 'public/SignedFinalResult.msg'
var ELECTION_NAME = 'Favourite RS3 Project'

exports.index = function(req, res) {
    // check if file with the result exists
    if (fs.existsSync(FINAL_RESULT) && fs.existsSync(SIGNED_RESULT)) {
        // read the file:
        fs.readFile('FinalResult.txt', {encoding:'utf8'}, function(err,data) {
            if (err) throw err;
            // split it into lines
            lines = data.split('\n')
            // and transform into a list of objects of the form {vote: ..., nonce ...}
            tt = lines
                 .filter( function(line) {
                    return line !== '';
                 })
                 .map( function (line) {
                          t = line.split(/ \t+/);
                          return {vote:t[0], nonce:t[1]};
                  })
            // render the response
            res.render('result', { title: 'TrustVote Result', electionName: ELECTION_NAME, result: tt });
        })
    }
    else { // there is no file with result
        res.render('no_result', { title: 'TrustVote: No result', electionName: ELECTION_NAME }); 
    }
};
