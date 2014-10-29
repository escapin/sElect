var fs = require('fs');
var config = require('./config');
var manifest = require('./manifest');
var server = require('./server');

exports.process = function process(req, res) 
{
    var data = req.body.data;
    console.log('DATA COMING:', data);
    res.send({ ok: true, info: 'thanks for the data' }); // we return this answer no matter what (is the data correct)

    console.log('Processing the data...');
    server.processTally(data, function (err, result) {
        if (err) {
            console.log(' ...Internal error. Cannot process the tally: ', err);
        }
        else if (result.ok) { 
            var finalRes = result.data;
            console.log('Result produced:', finalRes);
            // Save the result:
            fs.writeFileSync(config.RESULT_FILE, finalRes);
            console.log('Result saved in', config.RESULT_FILE);
        }
        else {
            console.log('Error:', result.data);
        }
    })
};

