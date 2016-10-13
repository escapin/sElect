function authChannel(){
    var trustedDomains = JSON.parse(trustedDomainsRaw);

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
		// For Chrome, the origin property is in the event.originalEvent object.
		var origin = event.origin || event.originalEvent.origin;
		// the POST message must come from the parent window which, in turn, must be a trusted domain
		if (event.source !== parent || trustedDomains.indexOf(event.origin)<0){
			return;
		}
		else if(event.data.hasOwnProperty("manifest")){
	    	sessionStorage.setItem("manifest", JSON.stringify(event.data.manifest));
	    	parent.postMessage("received", "*");
	    }
	    else if(event.data === "retrieveManifest"){
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
