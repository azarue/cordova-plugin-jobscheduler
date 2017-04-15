var exec    = require('cordova/exec'),
    channel = require('cordova/channel');


exports.schedule = function(args) {
    cordova.exec(function() { console.log('schedule called'); }, null, 'JobScheduler', 'schedule', [args]);
};
