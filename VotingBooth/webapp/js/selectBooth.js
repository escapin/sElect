
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
    manifest.hash = cryptofunc.hash(electionManifestRaw).toLowerCase();
    console.log('Election hash =', manifest.hash);

    // Voter and status
    var email = null;
    var otp = null;
    var choice = null;

    var electionID = manifest.hash;
    var shortenedElectionID = electionID.slice(0,6) + '...';
    var colServVerifKey = manifest.collectingServer.verification_key;
    // retrieve the encryption and verification keys of the mix servers from the manifest
    var mixServEncKeys = manifest.mixServers.map(function (ms) { return ms.encryption_key; })
    var mixServVerifKeys = manifest.mixServers.map(function (ms) { return ms.verification_key; })
    // create the voter object
    var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys, mixServVerifKeys);
 // var voter = voterClient.create(electionID, colServEncKey, colServVerifKey, mixServEncKeys);



    //////////////////////////////////////////////////////////////////////////////
    /// AUXILIARY FUNCTIONS

    function optionsAsHTML() {
        var options = '';
        var choices = manifest.choices;
        for (var i=0; i<choices.length; ++i) {
            var choice = choices[i];
            // console.log(choice);
            var slabel = '<label for="option-' +i+ '" class="pure-radio">\n';
            // console.log(slabel);
            var sinput = '<input id="option-' +i+ '" type="radio" name="choice" value="option' +1+ 
                         '"> ' +choice+ '</label>\n';
            // console.log(sinput);
            options += slabel;
            options += sinput;
        }
        return options;
    }


    function saveReceipt(receipt) {
        console.log('Save receipt');
        if ( localStorage.getItem('receipt') !== null ) {
            console.log('There already exists a saved receipt (we are overwriting it anyway)');
        }
        var receiptJSON = JSON.stringify(receipt);
        localStorage.setItem('receipt', receiptJSON);
    }

    function getReceipt() {
        var receiptJSON = localStorage.getItem('receipt');
        if (receiptJSON === null)
            return null;
        else 
            return JSON.parse(receiptJSON);
    }

    // callback(err, data)
    function fetchData(url, callback) {
        console.log('Fetching data from', url);
        $.get(url)
         .done(function(data) {
             callback(null, data);
         })
         .fail(function(err) {
             callback(err);
         });
    }

    function checkCollectingServer(receipt) {
        console.log('CHECK COLLECTING SERVER');
        fetchData(manifest.collectingServer.URI+'/result.msg', function (err,data) {
            if (err) {
                console.log('Error:', err);
            }
            else {
                console.log('Done:', data);
                console.log('Verifying the data');
                var res = voter.checkColServerResult(data, receipt)
                console.log('Result:', res);
            }
        });
    }

    function checkMixServer(i, receipt) {
        console.log('CHECK MIX SERVER' , i);
        fetchData(manifest.mixServers[i].URI+'/result.msg', function (err,data) {
            if (err) {
                console.log('Error:', err);
            }
            else {
                console.log('Done:', data);
                console.log('Verifying the data');
                var res = voter.checkMixServerResult(i, data, receipt)
                console.log('Result:', res);
            }
        });
    }

    function verify(receipt) {
        checkCollectingServer(receipt);
        // checkMixServer(2, receipt);
        //for (var i=0; i<manifest.mixServers.length; ++i) {
            //checkMixServer(i, receipt);
        //}
    }

    function doVerification() {
        var receipt = getReceipt();
        if (receipt !== null) {
            console.log('A receipt found. Doing verification:', receipt);
            verify(receipt);
        }
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
            $.post(manifest.collectingServer.URI+"/otp", {'email': email})
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
        if (activeTabId!=='#otp') return false;
        activeTabId=''; 

        // Fetching the otp from the form
        var o = $('#inp-otp').val();
        if( !o || o==='' ) // it should not happen
            return false;
        otp = o;

        $('#otp').fadeOut(FADE_TIME, function() {
            showTab('#choice');
        });
        return false; // prevents any further submit action
    }

    function onSubmitChoice(event) {
        if (activeTabId!=='#choice') return false;
        activeTabId=''; 

        // Fetch the choice from the form
        var option = $('input[name="choice"]:checked').attr('id');
        choice = + option.slice('option-'.length);

        // Make the active tab disappear
        $('#choice').fadeOut(FADE_TIME, function() {

            // Create the ballot
            console.log('CREATING BALLOT FOR:', email, otp, choice);
            var receipt = voter.createBallot(choice);
            console.log('RECEIPT:', receipt);

            showProgressIcon();
            // Make an (ajax) cast request:
            $.post(manifest.collectingServer.URI+"/cast", {'email': email, 'otp': otp, 'ballot': receipt.ballot})
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
                    // Ballot accepted (result.ok is true). Verify the receipt.
                    // Add the obtained signature to the receipt
                    receipt.signature = result.receipt;
                    // and validate it
                    var receiptValid = voter.validateReceipt(receipt); 
                    if (receiptValid) {
                        // TODO Save the receipt
                        saveReceipt(receipt);

                        // show the "ballot accepted" tab
                        showTab('#result');
                        $('#receipt-id').text(receipt.receiptID.toUpperCase());
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
        if (activeTabId!=='#error') return false;
        activeTabId=''; 

        // make the active tab disappear
        $('#error').fadeOut(FADE_TIME, function() {
            // show the welcome tab
            $('#inp-email').focus();
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
    
    // Election data
    $('h1.title').html(manifest.title + '<div class="electionid">(election identifier: ' +shortenedElectionID+ ')</div>');
    $('h3.subtitle').text(manifest.description);
    $('#choice-list').html(optionsAsHTML());

    // Event handlers binding
    $('#welcome form').submit(onSubmitWelcome);
    $('#otp form').submit(onSubmitOTP);
    $('#choice form').submit(onSubmitChoice);
    $('#error form').submit(onSubmitError);
    $('#inp-email').on('input', enableWhenNotEmpty($('#submit-email'), $('#inp-email')));
    $('#inp-otp').on('input', enableWhenNotEmpty($('#submit-otp'), $('#inp-otp')));
    $('input[name="choice"]').change(whenChoiceChanges);
    
    // Focus on the email input
    $('#inp-email').focus();

    doVerification();
}

