
function selectBooth() {

    //////////////////////////////////////////////////////////////////////////////
    /// PARAMETERS

    var FADE_TIME = 260;

    //////////////////////////////////////////////////////////////////////////////
    /// STATE

    // For navigation
    var activeTabId = "#welcome";

    // Voter
    var email = null;
    var otp = null;
    var choice = null;


    //////////////////////////////////////////////////////////////////////////////
    /// HANDLERS FOR SUMBITTING DATA 

    function onSubmitWelcome(event) 
    {
        // Consuming the input
        var e = $('#inp-email').val();
        if( !e || e==='' ) {
            console.log('Undefined or empty email');
        }
        else {
            console.log(e);
            email = e;
        }

        // Make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {
            activeTabId = '#otp';
            // Make the otp tab appear
            $(activeTabId).fadeIn(FADE_TIME);
            $('#inp-otp').focus();
        });
        return false; // prevents any further submit action
    }

    function onSubmitOTP(event) {
        // make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {
            activeTabId = '#choice';

            // make the otp tab appear
            $(activeTabId).fadeIn(FADE_TIME);
        });
        return false; // prevents any further submit action
    }

    function onSubmitChoice(event) {
        // make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {
            activeTabId = '#error';

            // make the otp tab appear
            $(activeTabId).fadeIn(FADE_TIME);
        });
        return false; // prevents any further submit action
    }

    function onSubmitError(event) {
        // make the active tab disappear
        $(activeTabId).fadeOut(FADE_TIME, function() {
            activeTabId = '#welcome';

            // prepare the next tab

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
            console.log('disabling');
        }
        else { // some choice selected
            $('#submit-choice').prop('disabled', null);
            console.log('enabling');
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

