var fs = require('fs');
var manifest = require('./manifest');
var config = require('./config');
var result = require('./result');

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

//////////////////////////////////////////////////////////////

var openingTime = Date(manifest.startTime);
var closingTime = Date(manifest.endTime);

exports.summary = function(req, res) {
    var ready = result.result !== null;
    console.log(ready);
    var summary = null;
    if (ready)
        summary = result.summary;
    res.render('summary', {
            manifest: manifest,
            ready: ready,
            closingTime: closingTime,
            summary: summary,
        });
};

exports.votes = function(req, res) {
    var ready = result.result !== null;
    res.render('votes', {
            manifest: manifest,
            ready: ready,
            result: result.result,
            closingTime: closingTime,
        });
}

exports.voters = function(req, res) {
    var ready = result.result !== null;
    console.log(result.voters);
    res.render('voters', {
            manifest: manifest,
            ready: ready,
            voters: result.voters,
            closingTime: closingTime,
        });
}

exports.details = function(req, res) {
    var ready = result.result !== null;
    res.render('details', {
            manifest: manifest,
            ready: ready,
            closingTime: closingTime,
            openingTime: openingTime,
        });
}
