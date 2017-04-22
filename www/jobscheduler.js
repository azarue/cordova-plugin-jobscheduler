var exec    = require('cordova/exec'),
    channel = require('cordova/channel');

exports._jobid = 0;

exports.onrun = function() {
}

exports.schedule = function(args) {
    args.jobId = this._jobid++;
    cordova.exec(function() { console.log('schedule called'); }, null, 'JobScheduler', 'schedule', [args]);
};
