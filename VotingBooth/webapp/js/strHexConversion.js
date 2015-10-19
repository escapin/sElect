// Message/String conversion
var  strHexConversion = function() {
var exports = {};	
	
	exports.hexEncode = function(str){
		//str = encodeURIComponent(str);
		
		var hex, i;
	
		var result = "";
		for (i=0; i<str.length; i++) {
			hex = str.charCodeAt(i).toString(16);
			result += ("000"+hex).slice(-4);
		}

		return result;
	}

	exports.hexDecode = function(hexStr) {
		var j;
		var hexes = hexStr.match(/.{1,4}/g) || [];
		var result = "";
		for(j = 0; j<hexes.length; j++) {
			result += String.fromCharCode(parseInt(hexes[j], 16));
		}
		//result = unescape(result);
		return result;
	}
	
/////////////////////////////////////////////////////////////////////
return exports;
}();

if(typeof(module)!=='undefined' && module.exports) {
	module.exports = strHexConversion;
}