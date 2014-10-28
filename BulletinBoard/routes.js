var fs = require('fs');
var manifest = require('./manifest');
var config = require('./config');
var result = require('./result');

exports.index = function(req, res) {
    // check if the result is ready 
    if (result.result) {
        res.render('result', {  manifest: manifest,
                                title: 'sElect Result', 
                                result: result.result 
                             });
    }
    else { // there is no file with result
        res.render('no_result', { title: 'sElect: No result', manifest: manifest }); 
    }
};

// Serve a particular static file

exports.serveFile = function serveFile(path) {
    return function (req, res) {
        fs.exists(path, function(exists) {
            if (exists) {
                fs.createReadStream(path).pipe(res);
            } else {
                res.status(404).send('404: Not found');
            }
        });
    }
}

