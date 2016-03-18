var express = require('express');
var bodyParser = require('body-parser');
var errorHandler = require('errorhandler');
var morgan = require('morgan');
var mkdirp = require('mkdirp');
var fs = require('fs');
var efs = require('extfs');

var config = require('./config');
var manifest = require('./src/manifest');
var routes = require('./src/routes');
var result = require('./src/result');


//Display the error message and halt the process.
function error(info) {
    console.log('ERROR:', info);
    console.log('\nSERVER NOT STARTED.');
    process.exit(1);
}

//CHECK IF THE RESULT ALREADY EXISTS
var cmdlineOption = process.argv[2];
if (cmdlineOption && cmdlineOption!=='--serveResult') {
    error('Wrong option.\n' + 
    	'USAGE: node bb.js [--serveResult]');
}

var resultDirExists=true;
try{
	var stats=fs.statSync(config.DATA_FOLDER)
} catch (err) {
	resultDirExists = !(err.code === 'ENOENT');
}
if (!resultDirExists && cmdlineOption === '--serveResult') {
    error('The folder with the result does not exist.\n' +
    		'USAGE: node bb.js');
} else if (resultDirExists && stats.isDirectory() && !efs.isEmptySync(config.DATA_FOLDER) && cmdlineOption !== '--serveResult') {
    error('The folder "' + config.DATA_FOLDER + '" with the result of the servers already exists: ' + 
    		'Remove this folder or run the server with "--serveResult" option.\n' +
    		'USAGE: node bb.js --serveResult');
}

//create the folder where the data coming from the other servers will be stored
mkdirp(config.DATA_FOLDER, function (err) {
    if (err) 
    	console.error("Error: ", err);
//    else 
//    	console.log("Folder '" + config.DATA_FOLDER + "' created.");
});

if(cmdlineOption==='--serveResult'){
	var NMixServers = manifest.mixServers.length;
	for(var i=NMixServers-1; i>=0; --i) {
		var mixServer_path = config.RESULTMIX_FILE.replace('%d', i);
		if(i===NMixServers-1){
			fs.readFile(mixServer_path, {encoding:'utf8'}, function (err, data) {
				
				if(err){
					if (err.code === 'ENOENT'){
						console.log("WARN:\tThe file with the result of the last mix server does not exist.");
						console.log("\tIf the last mix server is running, " +
							"this file is going to be fetched from it.");			
					} else {
						console.log("WARN:\tThe file with the result of the last mix server can not be opened.");
						console.log("\tIf the last mix server is running, " +
							"the bulletin board would fetch this file again once you manually delete it.");
						console.log("File path:\t" + err.path);
					}		    		  
				} 
				else if(result.finalResult === null){ // show the final results
					result.parseFinalResult(data);
				}
			});
		} else {
			fs.exists(mixServer_path, function(j){
				return function(exists){
					if(!exists){
						console.log("WARN:\tThe file with the result of the " + j + "th mix server does not exist.");
						console.log("\tIf the " + j + "th mix server is running, " +
							"this file is going to be fetched from it.");
					}
				}
			}(i));
		}
	}
	
	// check collecting server result file
	fs.exists(config.RESULTCS_FILE, function (exists) {
		if(!exists){
			console.log("WARN:\tThe file with the result of the collecting server does not exist.");
			console.log("\tIf the collecting server is running, " +
				"this file is going to be fetched from it.");
		}
	});
	
	if(manifest.publishListOfVoters){
		// read list of voters file
		fs.readFile(config.VOTERSLIST_FILE, {encoding:'utf8'}, function (err, data) {
			if(err){
				if (err.code === 'ENOENT'){
					console.log("WARN:\tThe file with the list of voters does not exist.");
					console.log("\tIf the collecting server is running, " +
						"this file is going to be fetched from it.");			
				} else {
					console.log("WARN:\tThe file with the list of voters can not be opened.");
					console.log("\tIf the collecting server is running, " +
							"the bulletin board would fetch this file again once you manually delete it.");
					console.log("File path:\t" + err.path);
				}
			}
			else if(result.voters === null)
				result.parseVotersList(data);
		});
	}
}


// CREATE AND CONFIGURE THE APP
var app = express();

app.set('views', './src/views');    // view engine and location of the views
app.set('view engine', 'ejs'); 
app.use(bodyParser.urlencoded({ extended: true })); // body parser (important for POST request)
app.use(express.static('./public')); // static content // was: __dirname + '/public'
app.use(errorHandler({ dumpExceptions: true, showStack: true })); // error handling (not for production)
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) );


// ROUTES
app.get('/', routes.summary);
app.get('/summary', routes.summary);
app.get('/votes', routes.votes);
if (manifest.publishListOfVoters)
    app.get('/voters', routes.voters);
app.get('/details', routes.details);

app.get('/ElectionManifest.json', routes.serveFile(config.MANIFEST_FILE));
app.get('/SignedFinalResult', routes.serveFile(config.RESULT_FILE));
// app.get('/SignedPartialResult', routes.serveFile(config.PARTIAL_RESULT_FILE));
if (manifest.publishListOfVoters)
    app.get('/VotersList', routes.serveFile(config.VOTERSLIST_FILE));
app.get('/ResultsCollectingServer', routes.serveFile(config.RESULTCS_FILE));
for(i=0; i<manifest.mixServers.length; ++i){
	var resource = '/ResultsMixServer' + i;
	var file = config.RESULTMIX_FILE.replace('%d', i);
	app.get(resource, routes.serveFile(file));
}


// SET THE BACKGROUD CHECK FOR THE RESULT FILE
//setInterval( result.loadResult, 5000);
//result.loadResult();
setInterval(result.fetchAndSaveData, 5000);
result.fetchAndSaveData();
// TODO:
// (1) We could make sure that this is alive on every request (just in case)
// (2) Remove this when the result is read.

// STARGING THE SERVER
var server = app.listen(config.port, function() {
    console.log('Bulleting Board running for election "%s" [%s]', manifest.title, manifest.hash);
    console.log('Listening on %s, port %d\n', server.address().address, server.address().port);
});

