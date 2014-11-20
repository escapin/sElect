
function selectBooth() {

    // PARAMETERS

    var FADE_TIME = 260;

    // STATE

    // For navigation
    var activeTabId = "#welcome";

    // Voter
    var email = null;
    var otp = null;
    var choice = null;


    // FUNCTIONS AND HANDLERS
    
    function onSubmitWelcome(event) 
    {
        // Consuming the input
        var e = $('#inp-email').val()
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

            // prepare the next tab

            // make the otp tab appear
            $(activeTabId).fadeIn(FADE_TIME);
            $('#inp-email').focus();
        });
        return false; // prevents any further submit action
    }


    // INITIALISATION AND BINDING
    $('#welcome form').submit(onSubmitWelcome);
    $('#otp form').submit(onSubmitOTP);
    $('#choice form').submit(onSubmitChoice);
    $('#error form').submit(onSubmitError);
    
    // Focus on the email input
    $('#inp-email').focus();
}

