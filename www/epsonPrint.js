/*global cordova, module*/

module.exports = {
    printReceipt: function (ip_address, base64_image_str, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "EpsonPrint", "printReceipt", [ip_address, base64_image_str]);
    },
    findPrinters: function (foundPrinters, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "EpsonPrint", "findPrinters", [foundPrinters]);
    },
    stopSearch: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "EpsonPrint", "stopSearch", []);
    }
};
