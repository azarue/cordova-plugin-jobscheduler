var exec    = require('cordova/exec'),
    channel = require('cordova/channel');

var callbacks = {};

exports.onrun = function(jobid) {
    var cb;

    if (cb = callbacks[jobid]) {
        cb();
    }
};

exports.schedule = function(args, successCallback, errorCallback) {
    var callback = args.callback;

    if (typeof callback !== 'function'){
      errorCallback(new Error("missing callback parameter"))
    }

    // get new random number
    const maxId = 100000 - 1;
    var id = Math.floor(Math.random() * maxId) + 1;
    while (callbacks[id]){
      id = Math.floor(Math.random() * maxId) + 1;
    }

    callbacks[id] = callback;

    args.jobId = id;
    cordova.exec(successCallback, errorCallback, 'JobScheduler', 'schedule', [args]);
};
