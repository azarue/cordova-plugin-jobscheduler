var exec    = require('cordova/exec'),
    channel = require('cordova/channel');

exports._jobid = 0;
exports._callbacks = {};

exports.onrun = function(jobid) {
    var cb;

    if (cb = this._callbacks[jobid]) {
        cb();
    }
};

exports.schedule = function(args) {
    var callback = args.callback;
    delete args.callback;

    this._callbacks[this._jobid] = callback;

    args.jobId = this._jobid++;
    cordova.exec(function() { console.log('schedule called'); }, null, 'JobScheduler', 'schedule', [args]);
};
