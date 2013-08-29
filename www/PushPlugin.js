/*
*
*
*
 */
cordova.define("com.plugin.gcm.PushPlugin", function(require, exports, module) {

    var exec = require('cordova/exec');

    var PushPlugin = {
        activateNotification:function(success,error,senderId) {
	    if (device.platform == 'android' || device.platform == 'Android') {
            	exec(success, error, "PushPlugin", "activatePush", [{"senderID": senderId}]);
	    }
	    else {
	        exec(success, error, "PushPlugin", "activatePush", []);
	    }
        },
        deactivateNotification:function(success,error) {
            exec(success, error, "PushPlugin", "deactivatePush", []);
        },
        notificationCallback: function(e){

        },
        isPushActivated: function(success,error) {
            exec(success, error, "PushPlugin", "isPushActivated", []);
        },
        bootstrap: function(){
            exec(null,null,"PushPlugin","bootstrap",[]);
        }
    };
    module.exports = PushPlugin;

});
