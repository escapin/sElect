
function selectBooth() {

    //////////////////////////////////////////////////////////////////////////////
    /// PARAMETERS

    var FADE_TIME = 260;

    //////////////////////////////////////////////////////////////////////////////
    /// STATE

    // For navigation
    var activeTabId = "#welcome";

    // Voter and status
    var email = null;
    var otp = null;
    var choice = null;

    // FIXME This should be taken from the manifest
    var electionID = "0e64cae3f0f3fa302504d9a7fb4d0c40edb7c069";
    var colServEncKey = "30819F300D06092A864886F70D010101050003818D0030818902818100B21E1FA56085DFEF9DA015A731CA2243FFF2A6354CD6C3AC5210C9D047702908A876F4E822A35A097BF0D8E0397A1B9C3F7BB4A055239E3F67500A707A3B5659FBCA35A1CEFFC251D72BE04F313A4B11451845E01F3A30B18546A521B268772051BC2ADC22EBDA6B9ECE530460A6DFE8818B1F53363E5C91BB7BA450C21AFCE90203010001";
    var colServVerifKey = "305C300D06092A864886F70D0101010500034B003048024100863DD199FAAF19FDAA696E8ADECB5D1324B49E6AE904646875AEC215A48A69FB34996431C1938CA1A6796FD3FA65759E4A44A1D1313ABFFF9DEEF72404ACA0810203010001";
    var finServEncKey = "30819F300D06092A864886F70D010101050003818D003081890281810081C2A661FB08D41F742A2EE037FBB41724AE0D04C1D4804E67147E0BBB3FFD4373B460000C3CFABC2A2A536BEAAA8FDFB188C7CDA4D05C8AA61EDB63B697217B1B12B8B5E9AF1BF918C13D8621715AA34539279B6E9AAD8F3792D1ECA4AAA7F78ACA1023DA580541EAC647E3CA7CDC50BC4648AEC16028F23979F329D83BF8550203010001";
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
                    activeTabId = '#otp';
                    $(activeTabId).fadeIn(FADE_TIME);
                    $('#inp-otp').focus();
                }
              })
             .fail(function otpRequestFailed() {
                showError('Cannot connect with the server');
              });
        });
        return false;
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
        console.log('CHOICE:', choice);

        console.log('CREATING BALLOT FOR:', email, otp, choice);
        var ballotInfo = voter.createBallot(choice);
        console.log('BALLOT_INFO:', ballotInfo);
        // TODO Send the ballot
        // TODO Verify the receipt

        // make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {
            activeTabId = '#result';

            // make the otp tab appear
            $(activeTabId).fadeIn(FADE_TIME);
        });
        return false; // prevents any further submit action
    }

    function onSubmitError(event) {
        // make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {
            activeTabId = '#welcome';

            // make the otp tab appear
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

