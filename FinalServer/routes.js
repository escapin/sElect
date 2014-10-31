var fs = require('fs');
var config = require('./config');
var manifest = require('./manifest');
var server = require('./server');

var resultReady = false;
if (fs.existsSync(config.RESULT_FILE))
    resultReady = true;

exports.process = function process(req, res) 
{
    if (resultReady) {
        console.log('Error: result already exists');
        res.send({ok: false, info: 'result already exists'})
        return;
    }

    var data = req.body.data;
    console.log('DATA COMING:', data);

    console.log('Processing the data...');
    server.processTally(data, function (err, result) {
        if (err) {
            console.log(' ...Internal error. Cannot process the tally: ', err);
            res.send({ ok: false, info: 'Internal error' });
        }
        else if (result.ok) { 
            var finalRes = result.data;
            console.log('Result produced:', finalRes);
            // Save the result:
            fs.writeFileSync(config.RESULT_FILE, finalRes);
            console.log('Result saved in', config.RESULT_FILE);
            resultReady = true;
            res.send({ ok: true, info: 'Data accepted'});
        }
        else {
            console.log('Error:', result.data);
            res.send({ ok: false, info: result.data });
        }
    })
};

exports.statusPage = function statusPage(req, res) {
    res.render('status', {manifest: manifest, ready: resultReady});
}

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

