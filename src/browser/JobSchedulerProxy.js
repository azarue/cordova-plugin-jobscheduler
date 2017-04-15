exports.enable = function (success, error) {
    success();
};

exports.disable = function (success, error) {
    success();
};

cordova.commandProxy.add('JobScheduler', exports);
