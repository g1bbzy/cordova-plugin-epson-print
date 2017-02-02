/*global cordova, module*/

module.exports = {
    ShowAlert: function (name, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "EpsonPrint", "ShowAlert");
    }
};
