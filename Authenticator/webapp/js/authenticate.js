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
	
    var votingBooth = null;
	
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
	    $('h1.title').html(manifest.title + '<div class="electionid">(election identifier: ' +printableElID+ ')</div>');
	    $('h3.subtitle').text(manifest.description);
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
                    //if(result.otp != null){
                    	//alerting(result.otp);
                    //}
                    // Show the next window (OTP)
                    $('#inp-otp').val(''); // emtpy the otp input field
                    showTab('#otp');
                }
              })
             .fail(function otpRequestFailed() {
                showError('Cannot connect with the server');
              });
        });
        return false; // prevents any further submit action
    };

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
            // Make an (ajax) cast request:
            $.post(manifest.collectingServer.URI+"/cast", {'email': email, 'otp': otp, 'electionID': electionID, 'ballot': receipt.ballot})
             .fail(function otpRequestFailed() {  // request failed
                showError('Cannot connect with the server');
              })
             .done(function castRequestDone(result) {  // we have some response
                parent.window.opener.postMessage(result, votingBooth);
                window.close();
              });

        	
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
	
	function alerting(data){
		$('#showing').html(data);
		document.getElementById("otp-display").style.visibility = "visible";
	}

	$("#reload").click(function() {
		document.getElementById("otp-display").style.visibility = "hidden";
	});
	
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
    	//if(event.origin !== 'http://localhost') return;
    	if(event.data.ballot !== undefined && event.origin === votingBooth){
    		var origConf = confirm("If your VotingBooth was " + event.origin + " proceed, otherwise cancel.")
    		if (origConf){
    			console.log('message received from ' + event.origin +':  ' + event.data.ballot,event);
    			receipt = event.data;
    		}
    		else{
    			alert('Wrong VotingBooth, window will close');
    			window.close();
    		}
    	}
    	else if(event.data.ballot !== undefined){
			alert("Ballot and Election ID do not match, two different VotingBooths");
			window.close();
    	}
    	else if(event.data.title !== undefined){
    		votingBooth = event.origin;
    		console.log('Election hash = ' + event.data.hash);
    		manifest = event.data;
    		electionID = manifest.hash;
    		printableElID = makeBreakable(electionID.toUpperCase()); // electionID.slice(0,6) + '...';
    	    initiate(); // shows welcome tab
    	}
    },false);
    console.log('addressing parent');
    parent.window.opener.postMessage('loaded', "*");    

}
