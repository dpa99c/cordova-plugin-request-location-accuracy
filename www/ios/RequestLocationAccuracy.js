/**
 *  Request Location Accuracy plugin
 *
 *  Copyright (c) 2016 Dave Alden (Working Edge Ltd.)
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

/**
 * Indicates if a request is possible to invoke to native dialog to turn on Location Services.
 * This will return true if Location Services is currently OFF and request is not currently in progress.
 *
 * @param [Function} successCallback - callback to pass result to.
 * This is passed a boolean argument indicating if a request can be made.
 */
RequestLocationAccuracy.prototype.canRequest = function(successCallback) {
	return cordova.exec(successCallback, null, 'RequestLocationAccuracy', 'canRequest', []);
};

/**
 * Indicates if a request is currently in progress.
 *
 * @param [Function} successCallback - callback to pass result to.
 * This is passed a boolean argument indicating if a request is currently in progress;
 */
RequestLocationAccuracy.prototype.isRequesting = function(successCallback) {
	return cordova.exec(successCallback, null, 'RequestLocationAccuracy', 'isRequesting', []);
};

module.exports = new RequestLocationAccuracy();

