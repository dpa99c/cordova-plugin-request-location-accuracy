/**
 *  Request Location Accuracy plugin
 *
 *  Copyright (c) 2016 Dave Alden (Working Edge Ltd.)
 **/
var RequestLocationAccuracy = function(){
	this.requesting = false;
};

/**
 * Request location mode priority "no power": the best accuracy possible with zero additional power consumption.
 * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html#PRIORITY_NO_POWER
 * @type {number}
 */
RequestLocationAccuracy.prototype.REQUEST_PRIORITY_NO_POWER = 0;

/**
 * Request location mode priority "low power":  "city" level accuracy (about 10km accuracy)
 * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html#PRIORITY_LOW_POWER
 * @type {number}
 */
RequestLocationAccuracy.prototype.REQUEST_PRIORITY_LOW_POWER = 1;

/**
 * Request location mode priority "balanced power":  "block" level accuracy (about 100 meter accuracy)
 * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html#PRIORITY_BALANCED_POWER_ACCURACY
 * @type {number}
 */
RequestLocationAccuracy.prototype.REQUEST_PRIORITY_BALANCED_POWER_ACCURACY = 2;

/**
 * Request location mode priority "high accuracy":  the most accurate locations available. This will use GPS hardware to retrieve positions.
 * https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest.html#PRIORITY_HIGH_ACCURACY
 * @type {number}
 */
RequestLocationAccuracy.prototype.REQUEST_PRIORITY_HIGH_ACCURACY = 3;


/**
 * Success due to current location settings already satisfying requested accuracy
 * @type {number}
 */
RequestLocationAccuracy.prototype.SUCCESS_SETTINGS_SATISFIED = 0;

/**
 * Success due to user agreeing to requested accuracy change
 * @type {number}
 */
RequestLocationAccuracy.prototype.SUCCESS_USER_AGREED = 1;

/**
 * Error due an unresolved request already being in progress.
 * @type {number}
 */
RequestLocationAccuracy.prototype.ERROR_ALREADY_REQUESTING = -1;

/**
 * Error due invalid action requested
 * @type {number}
 */
RequestLocationAccuracy.prototype.ERROR_INVALID_ACTION = 0;

/**
 * Error due invalid accuracy requested
 * @type {number}
 */
RequestLocationAccuracy.prototype.ERROR_INVALID_ACCURACY = 1;

/**
 * Error due to exception in the native code
 * @type {number}
 */
RequestLocationAccuracy.prototype.ERROR_EXCEPTION = 2;

/**
 * Error due to not being able to change location accuracy to requested state
 * @type {number}
 */
RequestLocationAccuracy.prototype.ERROR_CANNOT_CHANGE_ACCURACY = 3;

/**
 * Error due to user rejecting requested accuracy change
 * @type {number}
 */
RequestLocationAccuracy.prototype.ERROR_USER_DISAGREED = 4;

/**
 * Error due to failure to connect to Google Play Services API
 * @type {number}
 */
RequestLocationAccuracy.prototype.ERROR_GOOGLE_API_CONNECTION_FAILED = 5;

/**
 * Requests a specific accuracy for Location Services.
 *
 * @param [Function} successCallback - callback to be invoked on successful resolution of the requested accuracy.
 * A single object argument will be passed which has two keys: "code" in an integer corresponding to a SUCCESS constant and indicates the reason for success;
 * "message" is a string containing a description of the success.
 * @param {Function} errorCallback - callback to be invoked on failure to resolve the requested accuracy.
 * A single object argument will be passed which has two keys: "code" in an integer corresponding to an ERROR constant and indicates the reason for failure;
 * "message" is a string containing a description of the error.
 * @param {Integer} accuracy - The location accuracy to request defined by an integer corresponding to a REQUEST constant.
 */
RequestLocationAccuracy.prototype.request = function(successCallback, errorCallback, accuracy) {
	var _this = this;

	if(this.requesting){
		return errorCallback({
			code: _this.ERROR_ALREADY_REQUESTING,
			message: "A request is already in progress"
		});
	}

	this.requesting = true;

	return cordova.exec(function(data){
			_this.requesting = false;
			successCallback(data)
		},
		function(err) {
			_this.requesting = false;
			errorCallback(err);
		},
		'RequestLocationAccuracy',
		'request',
		[accuracy]);
};

/**
 * Indicates if a request is currently in progress.
 *
 * @param [Function} successCallback - callback to pass result to.
 * This is passed a boolean argument indicating if a request is currently in progress;
 */
RequestLocationAccuracy.prototype.isRequesting = function(successCallback) {
	successCallback(!!this.requesting);
};

/**
 * Indicates if it is possible to request a specific location accuracy.
 * This will return true if the app is authorized to use location.
 *
 * @param [Function} successCallback - callback to pass result to.
 * This is passed a boolean argument indicating if a request can be made.
 */
RequestLocationAccuracy.prototype.canRequest = function(successCallback) {
	return cordova.exec(successCallback, null, 'RequestLocationAccuracy', 'canRequest', []);
};

module.exports = new RequestLocationAccuracy();

