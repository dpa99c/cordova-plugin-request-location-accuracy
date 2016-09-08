Cordova Request Location Accuracy Plugin
========================================
<!-- START table-of-contents -->
**Table of Contents**

- [Overview](#overview)
  - [Android overview](#android-overview)
    - [Pre-requisites](#pre-requisites)
  - [iOS overview](#ios-overview)
    - [iOS "Cancel" button caveat](#ios-cancel-button-caveat)
- [Example project](#example-project)
- [Installation](#installation)
  - [Using the Cordova/Phonegap CLI](#using-the-cordovaphonegap-cli)
  - [PhoneGap Build](#phonegap-build)
- [Usage](#usage)
  - [Android & iOS](#android-&-ios)
    - [request()](#request)
      - [Android](#android)
      - [iOS](#ios)
      - [Combined Android & iOS example](#combined-android-&-ios-example)
    - [isRequesting()](#isrequesting)
    - [canRequest()](#canrequest)
  - [Android-only](#android-only)
    - [Request constants](#request-constants)
    - [Callback constants](#callback-constants)
      - [Success constants](#success-constants)
      - [Error constants](#error-constants)
- [License](#license)

<!-- END table-of-contents -->

# Overview

This Cordova/Phonegap plugin for Android and iOS to request enabling/changing of Location Services by triggering a native dialog from within the app, avoiding the need for the user to leave your app to change location settings manually.

## Android overview

[![Example Android app screencapture](https://j.gifs.com/KRL8Mb.gif)](https://www.youtube.com/watch?v=pbNdnMDRstg)

On Android, this plugin allows an app to request a specific accuracy for Location Services.
If the requested accuracy is higher than the current Location Mode setting of the device, the user is asked to confirm the change with a Yes/No dialog.

For example, if a navigation app that requires GPS, the plugin is able to switch on Location Services or change the Location Mode from low accuracy to high accuracy,
without the user needing to leave the app to do this manually on the Location Settings page.

It uses the Google Play Services Location API (v7+) to change the device location settings. In case the user doesn't have an up-to-date version of Google Play Services or there's some other problem accessing it, you may want to use another of my plugins, [cordova.plugins.diagnostic](https://github.com/dpa99c/cordova-diagnostic-plugin) as a fallback. This is able to switch the user directly to the Location Settings page where they can manually change the Location Mode.

**So why is this plugin not just part of [cordova.plugins.diagnostic](https://github.com/dpa99c/cordova-diagnostic-plugin)?**

Because you may not wish to use the location features of the diagnostic plugin and the dependency on the Google Play Services library increases the size of the app APK by about 2Mb.

### Pre-requisites

**IMPORTANT:** This plugin depends on the Google Play Services library, so you must install the "Google Repository" package under the "Extras" section in Android SDK Manager.
Otherwise the build will fail.

![SDK Manager](http://i.stack.imgur.com/jPqsW.png)

## iOS overview

[![Example iOS app screencapture](https://j.gifs.com/1woNPj.gif)](https://www.youtube.com/watch?v=PBZKH7u5RlQ)

If Location Services is turned OFF on an iOS device, no app on the device can access the location.

It is not programmatically possible to switch Location Services ON or to directly open the Location Services page in the Settings app.

The best that can be done by direct programmatic invocation of the Settings app is to open the app's own permissions page - the [`switchToSettings()`](https://github.com/dpa99c/cordova-diagnostic-plugin#switchtosettings) of [cordova-diagnostic-plugin](https://github.com/dpa99c/cordova-diagnostic-plugin#switchtosettings) enables you to do this. However, the user must still manually navigate from the app permissions page in the Settings app to the Location Services setting on the Privacy page.

If Location Services is turned OFF, this plugin enables an app to display a native iOS system dialog which gives user the option of directly opening the Privacy page in the Settings app which contains the switch to turn Location Services ON.

In order to show the native dialog allowing direct opening of the Privacy page, a location must be requested via the native location manager. So why can't you just use [cordova-plugin-geolocation](https://github.com/apache/cordova-plugin-geolocation) to request the location? Because when Location Services is OFF, the app reports that use of location is unauthorized, and [cordova-plugin-geolocation](https://github.com/apache/cordova-plugin-geolocation) will not request a location if it determines location is unauthorized: see [this Cordova issue](https://issues.apache.org/jira/browse/CB-10478).

### iOS "Cancel" button caveat

As highlighted by [issue #16](https://github.com/dpa99c/cordova-plugin-request-location-accuracy/issues/16), there is one scenario in which the iOS implementation of this plugin fails: if, upon successfully showing the native dialog, the user presses "Cancel" instead of "Settings", any subsequent requests via this plugin **will not** show the dialog again. Ever! This is because iOS assumes that if the user pressed "Cancel", they don't want your app to use their location, so iOS prevents you from asking them again to switch on Location Services.

There's no way to tell which button the user pressed in the native dialog or whether "Cancel" was pressed and the dialog is not being shown. Consequently, if the user has pressed "Cancel" in the native dialog, any subsequent calls to the plugin will still result in the success callback being invoked, since (as far as the plugin is concerned), it successfully requested a location from the native location manager.

The best approach to workaround this is to recheck the state of Location Services using [canRequest()](#canrequest) on each [resume event](https://cordova.apache.org/docs/en/latest/cordova/events/events.html#resume). If the user has pressed "Settings", your app will be put in the background while the Settings app is brought into the foreground, so when the user returns to your app, it will resume from the background.

# Example project

An example project illustrating use of this plugin can be found here: [https://github.com/dpa99c/cordova-plugin-request-location-accuracy-example](https://github.com/dpa99c/cordova-plugin-request-location-accuracy-example)

# Installation

## Using the Cordova/Phonegap CLI

    $ cordova plugin add cordova-plugin-request-location-accuracy
    $ phonegap plugin add cordova-plugin-request-location-accuracy

## PhoneGap Build
Add the following xml to your config.xml to use the latest version of this plugin from [npm](https://www.npmjs.com/package/cordova-plugin-request-location-accuracy):

    <plugin name="cordova-plugin-request-location-accuracy" source="npm" />



# Usage

The plugin is exposed via the `cordova.plugins.locationAccuracy` object.

## Android & iOS

### request()

This is the main plugin method.

#### Android

Requests a specific accuracy for Location Services.

    cordova.plugins.locationAccuracy.request(successCallback, errorCallback, accuracy)

Parameters:

- {Function} successCallback - callback to be invoked on successful resolution of the requested accuracy.
A single object argument will be passed which has two keys:
"code" in an integer corresponding to a [SUCCESS constant](#success-constants) and indicates the reason for success;
"message" is a string containing a description of the success.
- {Function} errorCallback - callback to be invoked on failure to resolve the requested accuracy.
A single object argument will be passed which has two keys:
"code" in an integer corresponding to an [ERROR constant](#error-constants) and indicates the reason for failure;
"message" is a string containing a description of the error.
- {Integer} accuracy - The location accuracy to request defined by an integer corresponding to a [REQUEST constant](#request-constants).

Example usage:

    cordova.plugins.locationAccuracy.canRequest(function(canRequest){
        if(canRequest){
            cordova.plugins.locationAccuracy.request(function (success){
                console.log("Successfully requested accuracy: "+success.message);
            }, function (error){
               console.error("Accuracy request failed: error code="+error.code+"; error message="+error.message);
               if(error.code !== cordova.plugins.locationAccuracy.ERROR_USER_DISAGREED){
                   if(window.confirm("Failed to automatically set Location Mode to 'High Accuracy'. Would you like to switch to the Location Settings page and do this manually?")){
                       cordova.plugins.diagnostic.switchToLocationSettings();
                   }
               }
            }, cordova.plugins.locationAccuracy.REQUEST_PRIORITY_HIGH_ACCURACY);
        }
    });

#### iOS

If Location Services is OFF, invokes the native dialog to directly open the Location Services page in the Settings app.

    cordova.plugins.locationAccuracy.request(successCallback, errorCallback)

Parameters:

- {Function} successCallback - callback to be invoked on successful request to invoke the dialog.
- {Function} errorCallback - callback to be invoked on failure to request a location and invoked the dialog.

Example usage:

    cordova.plugins.locationAccuracy.canRequest(function(canRequest){
        if(canRequest){
            cordova.plugins.locationAccuracy.request(function(){
                console.log("Successfully made request to invoke native Location Services dialog");
            }, function(){
                console.error("Failed to invoke native Location Services dialog");
            });
        }
    });

#### Combined Android & iOS example

    cordova.plugins.locationAccuracy.canRequest(function(canRequest){
        if(canRequest){
            cordova.plugins.locationAccuracy.request(function(){
                console.log("Request successful");
            }, function (error){
                console.error("Request failed");
                if(error){
                    // Android only
                    console.error("error code="+error.code+"; error message="+error.message);
                    if(error.code !== cordova.plugins.locationAccuracy.ERROR_USER_DISAGREED){
                        if(window.confirm("Failed to automatically set Location Mode to 'High Accuracy'. Would you like to switch to the Location Settings page and do this manually?")){
                            cordova.plugins.diagnostic.switchToLocationSettings();
                        }
                    }
                }
            }, cordova.plugins.locationAccuracy.REQUEST_PRIORITY_HIGH_ACCURACY // iOS will ignore this
            );
        }
    });



### isRequesting()

Indicates if a request is currently in progress.

    cordova.plugins.locationAccuracy.isRequesting(successCallback);

Parameters:

- {Function} successCallback - callback to pass result to.
This is passed a boolean argument indicating if a request is currently in progress.

Example usage:

     cordova.plugins.locationAccuracy.isRequesting(function(requesting){
        console.log("A request " + (requesting ? "is" : "is not") + " currently in progress");
     });

### canRequest()

Indicates if a request is possible to invoke a request.
On iOS, this will return true if Location Services is currently OFF and request is not currently in progress.
On Android, this will return true if the app has authorization to use location.

    cordova.plugins.locationAccuracy.canRequest(successCallback);


Parameters:

- {Function} successCallback - callback to pass result to.
This is passed a boolean argument indicating if a request can be made.

Example usage:

    cordova.plugins.locationAccuracy.canRequest(function(canRequest){
        console.log("A request " + (canRequest ? "can" : "cannot") + " currently be made");
    });

## Android-only

### Request constants

The location accuracy which is to be requested is defined as a set of REQUEST constants on the `cordova.plugins.locationAccuracy` object:

- `cordova.plugins.locationAccuracy.REQUEST_PRIORITY_NO_POWER`: Request location mode priority "no power": the best accuracy possible with zero additional power consumption.
- `cordova.plugins.locationAccuracy.REQUEST_PRIORITY_LOW_POWER`: Request location mode priority "low power":  "city" level accuracy (about 10km accuracy).
- `cordova.plugins.locationAccuracy.REQUEST_PRIORITY_BALANCED_POWER_ACCURACY`: Request location mode priority "balanced power":  "block" level accuracy (about 100 meter accuracy).
- `cordova.plugins.locationAccuracy.REQUEST_PRIORITY_HIGH_ACCURACY`: Request location mode priority "high accuracy":  the most accurate locations available. This will use GPS hardware to retrieve positions.


See [https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#constants](https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#constants)

### Callback constants

Both the `successCallback()` and `errorCallback()` functions will be passed an object which contains both a descriptive message and a code indicating the result of the operation.
These constants are defined on the `cordova.plugins.locationAccuracy` object.

#### Success constants

The `successCallback()` function will be pass an object where the "code" key may correspond to the following values:

- `cordova.plugins.locationAccuracy.SUCCESS_SETTINGS_SATISFIED`: Success due to current location settings already satisfying requested accuracy.
- `cordova.plugins.locationAccuracy.SUCCESS_USER_AGREED`: Success due to user agreeing to requested accuracy change

#### Error constants

The `errorCallback()` function will be pass an object where the "code" key may correspond to the following values:

- `cordova.plugins.locationAccuracy.ERROR_ALREADY_REQUESTING`: Error due an unresolved request already being in progress.
- `cordova.plugins.locationAccuracy.ERROR_INVALID_ACTION`: Error due invalid action requested.
- `cordova.plugins.locationAccuracy.ERROR_INVALID_ACCURACY`: Error due invalid accuracy requested.
- `cordova.plugins.locationAccuracy.ERROR_EXCEPTION`: Error due to exception in the native code.
- `cordova.plugins.locationAccuracy.ERROR_CANNOT_CHANGE_ACCURACY`: Error due to not being able to change location accuracy to requested state.
- `cordova.plugins.locationAccuracy.ERROR_USER_DISAGREED`: Error due to user rejecting requested accuracy change.
- `cordova.plugins.locationAccuracy.ERROR_GOOGLE_API_CONNECTION_FAILED`: Error due to failure to connect to Google Play Services API. The "message" key will contain a detailed description of the Google Play Services error.


# License

The MIT License

Copyright (c) 2016 Dave Alden (Working Edge Ltd.)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.