
function selectBooth() {


    //////////////////////////////////////////////////////////////////////////////
    /// PARAMETERS

    var FADE_TIME = 260;

    //////////////////////////////////////////////////////////////////////////////
    /// STATE


    // For navigation
    var activeTabId = "#welcome";

    // Manifest
    var manifest = JSON.parse(electionManifestRaw);
    manifest.hash = cryptofunc.hash(electionManifestRaw);
    console.log('Election hash =', manifest.hash);

    // Voter and status
    var email = null;
    var otp = null;
    var choice = null;

    var electionID = manifest.hash;
    var colServEncKey = manifest.collectingServer.encryption_key;
    var colServVerifKey = manifest.collectingServer.verification_key;
    var finServEncKey = manifest.finalServer.encryption_key;
    var voter = voterClient.create(electionID, colServEncKey, colServVerifKey, finServEncKey);

    //////////////////////////////////////////////////////////////////////////////
    /// HANDLERS FOR SUMBITTING DATA 

    function showError(errorMessage) {
        $('#errorMsg').text(errorMessage);
        activeTabId = '#error';
        $(activeTabId).fadeIn(FADE_TIME);
    }

    function onSubmitWelcome(event) 
    {
        // Fetching the email from the form
        var e = $('#inp-email').val();
        if( !e || e==='' ) // it should not happen
            return false;
        email = e;

        // Make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {

            // Make an (ajax) otp request:
            // TODO Show something to indicate work in progress
            // FIXME Use the address from the manifest
            $.post("https://localhost:3300/otp", {'email': email})
             .done(function otpRequestDone(result) {
                if (!result) {
                    showError('Unexpected error');
                }
                else if (!result.ok) {
                    showError("Server's responce: " + result.descr);
                }
                else {
                    // Show the next window (OTP)
                    // TODO The otp field should be always emptied 
                    activeTabId = '#otp';
                    $(activeTabId).fadeIn(FADE_TIME);
                    $('#inp-otp').focus();
                }
              })
             .fail(function otpRequestFailed() {
                showError('Cannot connect with the server');
              });
        });
        return false; // prevents any further submit action
    };

    function onSubmitOTP(event) {
        // Fetching the otp from the form
        var o = $('#inp-otp').val();
        if( !o || o==='' ) // it should not happen
            return false;
        otp = o;

        // make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {
            activeTabId = '#choice';

            // make the otp tab appear
            $(activeTabId).fadeIn(FADE_TIME);
        });
        return false; // prevents any further submit action
    }

    function onSubmitChoice(event) {
        // Fetch the choice from the form
        var option = $('input[name="choice"]:checked').attr('id');
        choice = + option.slice('option-'.length);

        // Make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {

            // Create the ballot
            console.log('CREATING BALLOT FOR:', email, otp, choice);
            var ballotInfo = voter.createBallot(choice);
            console.log('BALLOT_INFO:', ballotInfo);

            // Make an (ajax) cast request:
            // TODO Show something to indicate work in progress
            // FIXME Use the address from the manifest
            $.post("https://localhost:3300/cast", {'email': email, 'otp': otp, 'ballot': ballotInfo.ballot})
             .fail(function otpRequestFailed() {  // request failed
                showError('Cannot connect with the server');
              })
             .done(function castRequestDone(result) {  // we have some response
                if (!result) {  // but for some reason this is not set!
                    showError('Unexpected error');
                }
                else if (!result.ok) {  // server has not accepted the ballot
                    showError("Server's responce: " + result.descr);
                }
                else {
                    // Ballot accepted. Verify the receipt
                    var receiptValid = voter.validateReceipt(result.receipt, ballotInfo.innerBallot); 
                    if (receiptValid) {
                        // show the "ballot accepted" tab
                        activeTabId = '#result';
                        $('#receipt-id').text(ballotInfo.nonce);
                        $(activeTabId).fadeIn(FADE_TIME);
                    }
                    else { // receipt not valid
                        showError('Invalid receipt');
                    }
                }
              });

        });

        return false; // prevents any further submit action
    }

    function onSubmitError(event) {
        // make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {
            // show the welcome tab
            activeTabId = '#welcome';
            $(activeTabId).fadeIn(FADE_TIME);
            $('#inp-email').focus();
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

    function whenChoiceChanges() {
        // The selected item should be displayed in a stronger way. So:
        // Reset 'checked' class and add notchecked class for all the labels in the form
        $("#choice-form label").addClass('notchecked').removeClass('checked');
        // And do the oposite for the selected one
        $("#choice-form label[for='" + this.id + "']").addClass('checked').removeClass('notchecked');

        // Update the status of the submit button (active only when something is selected)
        if($('input[name="choice"]:checked').length == 0) { // no choice selected 
            $('#submit-choice').prop('disabled', true);
        }
        else { // some choice selected
            $('#submit-choice').prop('disabled', null);
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    /// INITIALISATION AND BINDING
    
    $('#welcome form').submit(onSubmitWelcome);
    $('#otp form').submit(onSubmitOTP);
    $('#choice form').submit(onSubmitChoice);
    $('#error form').submit(onSubmitError);
    $('#inp-email').on('input', enableWhenNotEmpty($('#submit-email'), $('#inp-email')));
    $('#inp-otp').on('input', enableWhenNotEmpty($('#submit-otp'), $('#inp-otp')));
    $('input[name="choice"]').change(whenChoiceChanges);
    
    // Focus on the email input
    $('#inp-email').focus();
}

