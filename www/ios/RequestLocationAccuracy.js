/**
 *  Request Location Accuracy plugin for Android
 *
 *  Copyright (c) 2015 Working Edge Ltd.
**/
var RequestLocationAccuracy = function(){
};


/**
 * Requests a position to invoke to native dialog to turn on Location Services.
 *
 * @param [Function} successCallback - callback to be invoked on successful position request.
 * @param {Function} errorCallback - callback to be invoked on failure to request position.
 */
RequestLocationAccuracy.prototype.request = function(successCallback, errorCallback) {
	return cordova.exec(successCallback, errorCallback, 'RequestLocationAccuracy', 'request', []);
};

module.exports = new RequestLocationAccuracy();

