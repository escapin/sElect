var fs = require('fs');
var request = require('request');
var hashmap = require('hashmap');

var logURL = "https://select.uni-trier.de/logger/fullLog.log";
var logFile = "fullLog.log";


function fetchData(url, cont) {
    request(url, function (err, response, body) {
        if (!err && response.statusCode == 200) {
            cont(null, body);
        }
        else {
        	var info = 'Cannot fetch the page: ' + url + '\t\(' + err + ')';
            cont(info);
        }
    });

}


function saveData(data, file) {
    fs.writeFile(file, data, function (err) {
        if (err) 
            console.log('Problems with saving the data:\n', data);
        else {
            console.log('Result saved in: ', file);
            resultReady = true;
        }
    });
}


function parseLog(logData){
	var toBeParsed = logData.substring(0, logData.length-1); // rm EOF
	//console.log(logData);
	var logArray=[];
	var i=0;
	toBeParsed.split(/\r?\n/).forEach(function (line) {
//		if(i==267){
//			console.log("%j", logArray[i-1]);
//        	console.log(line);
//		}
		var obj = JSON.parse(line);
		
		logArray.push(obj);
        i++;
    });
}


fetchData(logURL, function (err, data) {
	if (!err) {
		console.log('Log Fetched');
		saveData(data, logFile);
		parseLog(data);
	}
	else {
		console.log("\t" + err);
	}
});