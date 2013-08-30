    var exec = require('cordova/exec');

    var PushPlugin = {

        activateNotification:function(success,error,senderId) {
            	exec(success, error, "PushPlugin", "activatePush", [{"senderID": senderId}]);
        },
        deactivateNotification:function(success,error) {
            exec(success, error, "PushPlugin", "deactivatePush", []);
        },
        isPushActivated: function(success,error) {
            exec(success, error, "PushPlugin", "isPushActivated", []);
        },
        bootstrap: function(){
            exec(null,null,"PushPlugin","bootstrap",[]);
        },
        notificationCallback: function(notificationObject){
        }
    };
    PushPlugin.bootstrap();
    module.exports = PushPlugin;

