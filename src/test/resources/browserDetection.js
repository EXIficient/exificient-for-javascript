    // Browser detection
     
    // Internet Explorer
    var ie  = document.all != null;  //ie4 and above
    var ie5 = document.getElementById && document.all;
    var ie6 = document.getElementById && document.all&&(navigator.appVersion.indexOf("MSIE 6.")>=0);
     
    // Netscape
    var ns4 = document.layers != null;
    var ns6 = document.getElementById && !document.all;
    var ns  = ns4 || ns6;
     
    // Firefox
    var ff  = !document.layers && !document.all;
     
    // Opera
    var op  = navigator.userAgent.indexOf("opera")>0;
    var op7 = op && operaVersion() <= 7;
    var op8 = op && operaVersion() >= 8;
     
    // Detects the Opera version
    function operaVersion() {
    	agent = navigator.userAgent;
    	idx = agent.indexOf("opera");	
    	if (idx>-1) {
    		return parseInt(agent.subString(idx+6,idx+7));
    	}
    }
