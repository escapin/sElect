function authChannel(){
	
	function castBallot(req){
	    var manifest = JSON.parse(sessionStorage.getItem("manifest"));
	    // Make an (ajax) cast request:
            $.post(manifest.collectingServer.URI+"/cast", req)
		.fail(function otpRequestFailed() { // request failed
		    showError('Cannot connect with the server');
		})
		.done(function castRequestDone(result) { // the signed acknowledgment
		    parent.postMessage({result: result}, "*");
		});
	}
	
	window.addEventListener('message',function(event) {
	    //console.log("frame recieved: "+event.data);
	    if(event.data.hasOwnProperty("manifest")){
	    	sessionStorage.setItem("manifest", JSON.stringify(event.data.manifest));
	    	parent.postMessage("recieved", "*");
	    }
	    else if(event.data === "loaded"){
		// when the parent page is 'loaded', provide to this page the election manifest
		var manifest = JSON.parse(sessionStorage.getItem("manifest"));
			parent.postMessage({manifest: manifest}, "*");
	    }
	    else if(event.data.hasOwnProperty("credentials")){
	    	// email and otp
	    	sessionStorage.setItem("toBeSubmitted", JSON.stringify(event.data.credentials));
	    }
	    else if(event.data.hasOwnProperty("ballot")){
			// electionID and ballot
			var submit = JSON.parse(sessionStorage.getItem("toBeSubmitted"));
			var manifest = JSON.parse(sessionStorage.getItem("manifest"));
			submit.electionID = manifest.hash;
			submit.ballot = event.data.ballot;
			// submit := {email, otp, electionID, ballot}
			castBallot(submit);
	    }
	},false);
}
