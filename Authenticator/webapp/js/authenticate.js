function authenticate(){	
	
    //////////////////////////////////////////////////////////////////////////////
    /// STATE

    // For navigation
    var FADE_TIME = 260;
	
    var activeTabId = null;
    // var activeTabId = "#welcome";

    // Manifest
    var manifest = null

    // Status
    var email = null;
    var otp = null;
    var receipt = null;

    var electionID = null;
    var printableElID = null; // electionID.slice(0,6) + '...';
	
    var trustedDomains = JSON.parse(trustedDomainsRaw);
    var votingBooth = document.referrer;
    var iframePath = decodeURIComponent(window.location.search.substring(1));
    var parse_url = /^(?:([A-Za-z]+):)?(\/{0,3})([0-9.\-A-Za-z]+)(?::(\d+))?(?:\/([^?#]*))?(?:\?([^#]*))?(?:#(.*))?$/;
    var parts = parse_url.exec( iframePath );
    var authDomain = parts[1]+':'+parts[2]+parts[3] ;
    var tempAddr = iframePath.split(":")
    if(tempAddr.length > 2){
    	tempAddr = tempAddr[2].split("/");
    	authDomain = iframePath.replace("/"+tempAddr[1], '');
    }
    if(authDomain.charAt(authDomain.length-1) === '/'){
    	authDomain = authDomain.slice(0, -1);
    }
    // the domain of the Authentication Channel received from the Voting Booth must be in the list of trusted domains
    if(trustedDomains.indexOf(authDomain)<0){
    	console.log("URI of the Authentication Channel recieved from the Voting Booth is not trusted!")
    	iframePath = "";
    }
	document.getElementById("authChannel").src = iframePath;
	var iframe = document.getElementById("authChannel").contentWindow;
	var url = window.location.href;
	history.pushState("", "", url.replace(window.location.search, ""));
    //////////////////////////////////////////////////////////////////////////////
    /// AUXILIARY FUNCTIONS

    // Check if the user agent is IE
    function isIE(userAgent) {
        userAgent = userAgent || navigator.userAgent;
        return userAgent.indexOf("MSIE ") > -1 || userAgent.indexOf("Trident/") > -1;
    }

    function makeBreakable(str) {
        var r = '', n = Math.ceil(str.length/4);
        for (i=0; i<n; ++i) {
            r += str.slice(4*i,4*(i+1));
            if (i+1<n) r += ' '; // '<wbr>';
        }
        return r;
    }
    
    
    /////////////////////////////////////////////////////////////////////
    /// Initiate
    
	function initiate(){
	    // Election data
		if(manifest.showOtp){
    		document.getElementById('mock_info').innerHTML = "<br>(Since you're trying the demo, no email will be sent to you: You can provide a <em>fake</em> one as well.)";
    	}
		showTab('#welcome');
	}

	
    //////////////////////////////////////////////////////////////////////////////
    /// HANDLERS FOR SUMBITTING DATA 
	
    function showError(errorMessage) {
        $('#processing').hide();
        $('#errorMsg').text(errorMessage);
        activeTabId = '#error';
        $(activeTabId).fadeIn(FADE_TIME);
    }
	
    function showTab(tabId) {
        $('#processing').hide();
        activeTabId = tabId;
        $(tabId).fadeIn(FADE_TIME);
    }

    function showProgressIcon() {
        $('#processing').fadeIn(FADE_TIME*2);
    }

    
    function onSubmitWelcome(event) 
    {
        if (activeTabId!=='#welcome') return false;
        activeTabId=''; 

        // Fetching the email from the form
        var e = $('#inp-email').val();
        if( !e || e==='' ) // it should not happen
            return false;
        email = e;
        
        // Make the active tab disappear
        $('#welcome').fadeOut(FADE_TIME, function() {
        	
            // show processing icon
            showProgressIcon();
            // Make an (ajax) otp request:
            $.post(manifest.collectingServer.URI+"/otp", {'electionID': electionID, 'email': email})
             .done(function otpRequestDone(result) {
                if (!result) {
                    showError('Unexpected error');
                }
                else if (!result.ok) {
                    showError("Server's responce: " + result.descr);
                }
                else {
                    // Show the next window (OTP)
                    $('#inp-otp').val(''); // emtpy the otp input field
                    showTab('#otp');
                    
                    if(result.otp && manifest.showOtp){
                    	console.log('OTP: ' + result.otp);
                    	otp = result.otp;
                    	
                    	document.getElementById("disp-title").innerHTML += '&nbsp&nbsp'+manifest.title;
                    	document.getElementById("disp-otp").innerHTML = '&nbsp&nbsp<b>'+result.otp+'</b>';
                    	document.getElementById("disp-id").innerHTML = '&nbsp&nbsp'+printableElID;
                    	
                    	document.getElementById("showOtp").style.visibility = "visible";
                		$("#close-otp").focus();
                    }
                }
              })
             .fail(function otpRequestFailed() {
                showError('Cannot connect with the server');
              });
        });
        return false; // prevents any further submit action
    };
    
	$("#close-otp").click(function() {
		$('#inp-otp').val(otp);
		document.getElementById("showOtp").style.visibility = "hidden";
		$("#submit-otp").prop('disabled', null);
	});

    function onSubmitOTP(event) {
        if (activeTabId!=='#otp') return false;
        activeTabId=''; 

        // Fetching the otp from the form
        var o = $('#inp-otp').val();
        if( !o || o==='' ) // it should not happen
            return false;
        otp = o.trim();

        $('#otp').fadeOut(FADE_TIME, function() {
        	
        	showProgressIcon();
        	
        	iframe.postMessage({credentials: {'email': email, 'otp': otp}}, "*");        	
            window.location.replace(votingBooth+"?done");

        });
        return false; // prevents any further submit action
    }
    
    function onSubmitError(event) {
        if (activeTabId!=='#error') return false;
        activeTabId=''; 

        // make the active tab disappear
        $('#error').fadeOut(FADE_TIME, function() {
            // show the welcome tab
            showTab('#welcome');
        });
        return false; // prevents any further submit action
    }
    
    
    //////////////////////////////////////////////////////////////////////////////
    /// OTHER HANDLERS 

    function enableWhenNotEmpty(button, input) {
        return function() {
            var v = input.val();
            if( v==='' ) 
                button.prop('disabled', true);
            else 
                button.prop('disabled', null);
        };
    }
    
    //////////////////////////////////////////////////////////////////////////////
    /// INITIALISATION AND BINDING
	
    // Event handlers binding
    $('#welcome form').submit(onSubmitWelcome);
    $('#otp form').submit(onSubmitOTP);
    $('#error form').submit(onSubmitError);
    $('#inp-email').on('input', enableWhenNotEmpty($('#submit-email'), $('#inp-email')));
    $('#inp-otp').on('input', enableWhenNotEmpty($('#submit-otp'), $('#inp-otp')));
    
    if (isIE()) {
        $('#verCodeLink').hide();
    }
    showProgressIcon();
    
    //respond to events
    window.addEventListener('message',function(event) {
    	if (event.source !== iframe || trustedDomains.indexOf(event.origin)<0){
			return;
		}
    	if(event.data.hasOwnProperty("manifest")){
    		clearInterval(getManifest);
    		manifest = event.data.manifest;
    		console.log('Election hash = ' + manifest.hash);
    		electionID = manifest.hash;
    		printableElID = makeBreakable(electionID.slice(0,16).toUpperCase()); // electionID.slice(0,6) + '...';
    	    $('h1.title').html(manifest.title + '<div class="electionid">Election Identifier: ' +printableElID+ '</div>');
    	    $('h3.subtitle').html(manifest.description);
    		initiate(); // shows welcome tab
    	}
    },false);
    var getManifest = window.setInterval(function() {
    	iframe.postMessage('retrieveManifest', "*");  
    }, 100);
}
