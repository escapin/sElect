function forwardAuthentication(){
	
	function castBallot(req){
	    var manifest = JSON.parse(sessionStorage.getItem("manifest"));
            // Make an (ajax) cast request:
            $.post(manifest.collectingServer.URI+"/cast", req)
		.fail(function otpRequestFailed() {  // request failed
		    showError('Cannot connect with the server');
		})
		.done(function castRequestDone(result) {  // the signed acknowledgment
		    parent.postMessage({result: result}, "*");
		});
	}
	
	window.addEventListener('message',function(event) {
	    //console.log("frame recieved: "+event.data);
	    if(event.data.hasOwnProperty("manifest")){
		sessionStorage.setItem("manifest", JSON.stringify(event.data.manifest));
	    }
	    else if(event.data === "loaded"){
		var manifest = JSON.parse(sessionStorage.getItem("manifest"));
		parent.postMessage({manifest: manifest}, "*");
	    }
	    else if(event.data.hasOwnProperty("credentials")){
		sessionStorage.setItem("toBeSubmitted", JSON.stringify(event.data.credentials));
	    }
	    else if(event.data.hasOwnProperty("ballot")){
		var submit = JSON.parse(sessionStorage.getItem("toBeSubmitted"));
		var manifest = JSON.parse(sessionStorage.getItem("manifest"));
		submit.electionID = manifest.hash;
		submit.ballot = event.data.ballot;
		castBallot(submit);
	    }
	},false);

}
