var fs = require('fs');
var manifest = require('../manifest');
var result = require('../result');

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
