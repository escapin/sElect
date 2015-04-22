var fs = require('fs');
var manifest = require('./manifest');
var config = require('../config');
var result = require('./result');
var printableElID = makeBreakable(manifest.hash);

manifest.shortHash = manifest.hash.slice(0,6) + '...';

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
function makeBreakable(str) {
    var r = '', n = Math.ceil(str.length/4);
    for (i=0; i<n; ++i) {
        r += str.slice(4*i,4*(i+1));
        if (i+1<n) r += ' '; // '<wbr>';
    }
    return r;
}


// Retrieve the files containing the partial results of the MixServers
// export.retrieveMixDataFiles = function retrieveMixDataFiles

//////////////////////////////////////////////////////////////

var openingTime = new Date(manifest.startTime);
var closingTime = new Date(manifest.endTime);
console.log(manifest.startTime);
console.log(manifest.endTime);

exports.summary = function(req, res) {
    var ready = result.finalResult !== null;
    console.log(ready);
    var summary = null;
    if (ready)
        summary = result.summary;
    res.render('summary', {
            printableElID: printableElID,
            manifest: manifest,
            ready: ready,
            closingTime: closingTime,
            summary: summary,
        });
};

exports.votes = function(req, res) {
    var ready = result.finalResult !== null;
    res.render('votes', {
            printableElID: printableElID,
            manifest: manifest,
            ready: ready,
            result: result.finalResult,
            closingTime: closingTime,
        });
}

exports.voters = function(req, res) {
    var ready = (result.finalResult !== null);
    var partialResultReady = (result.voters !== null);
    console.log(result.voters);
    res.render('voters', {
            printableElID: printableElID,
            manifest: manifest,
            ready: ready,
            partialResultReady: partialResultReady,
            voters: result.voters,
            closingTime: closingTime,
        });
}

exports.details = function(req, res) {
    var ready = result.finalResult !== null;
    res.render('details', {
            printableElID: printableElID,
            manifest: manifest,
            ready: ready,
            closingTime: closingTime,
            openingTime: openingTime,
        });
}
