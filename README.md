Cordova Request Location Accuracy Plugin for Android
====================================================

* [Overview](#overview)
* [Installation](#installation)
* [Usage](#usage)
* [Example project](#example-project)
* [License](#license)

# Overview

This Cordova/Phonegap plugin for Android allows an app to request a specific accuracy for Location Services.
If the requested accuracy is higher than the current Location Mode setting of the device, the user is asked to confirm the change with a Yes/No dialog.

For example, if a navigation app that requires GPS, the plugin is able to switch on Location Services or change the Location Mode from low accuracy to high accuracy,
without the user needing to leave the app to do this manually on the Location Settings page.

It uses the Google Play Services Location API (v7+) to change the device location settings. In case the user doesn't have an up-to-date version of Google Play Services or there's some other problem accessing it, you may want to use another of my plugins, [cordova.plugins.diagnostic](https://github.com/dpa99c/cordova-diagnostic-plugin) as a fallback. This is able to switch the user directly to the Location Settings page where they can manually change the Location Mode.

So why is this plugin not just part of [cordova.plugins.diagnostic](https://github.com/dpa99c/cordova-diagnostic-plugin)?
Because you may not wish to use the location features of the diagnostic plugin and the dependency on the Google Play Services library increases the size of the app APK by about 2Mb.

[![Example app demo](https://j.gifs.com/KRL8Mb.gif)](https://www.youtube.com/watch?v=pbNdnMDRstg)

# Installation

## Using the Cordova/Phonegap [CLI](http://docs.phonegap.com/en/edge/guide_cli_index.md.html)

    $ cordova plugin add cordova-plugin-request-location-accuracy
    $ phonegap plugin add cordova-plugin-request-location-accuracy

## Using [Cordova Plugman](https://github.com/apache/cordova-plugman)

    $ plugman install --plugin=cordova-plugin-request-location-accuracy --platform=<platform> --project=<project_path> --plugins_dir=plugins

For example, to install for the Android platform

    $ plugman install --plugin=cordova-plugin-request-location-accuracy --platform=android --project=platforms/android --plugins_dir=plugins

## PhoneGap Build
Add the following xml to your config.xml to use the latest version of this plugin from [npm](https://www.npmjs.com/package/cordova-plugin-request-location-accuracy):

    <gap:plugin name="cordova-plugin-request-location-accuracy" source="npm" />

# Usage

The plugin is exposed via the `cordova.plugins.locationAccuracy` object and provides a single function:

    `cordova.plugins.locationAccuracy.request(successCallback, errorCallback, accuracy)`

## Parameters

- {Function} successCallback - callback to be invoked on successful resolution of the requested accuracy.
A single object argument will be passed which has two keys:
"code" in an integer corresponding to a [SUCCESS constant](#success-constants) and indicates the reason for success;
"message" is a string containing a description of the success.
- {Function} errorCallback - callback to be invoked on failure to resolve the requested accuracy.
A single object argument will be passed which has two keys:
"code" in an integer corresponding to an [ERROR constant](#error-constants) and indicates the reason for failure;
"message" is a string containing a description of the error.
- {Integer} accuracy - The location accuracy to request defined by an integer corresponding to a [REQUEST constant](#request-constants).

## Request constants

The location accuracy which is to be requested is defined as a set of REQUEST constants on the `cordova.plugins.locationAccuracy` object:

- `cordova.plugins.locationAccuracy.REQUEST_PRIORITY_NO_POWER`: Request location mode priority "no power": the best accuracy possible with zero additional power consumption.
- `cordova.plugins.locationAccuracy.REQUEST_PRIORITY_LOW_POWER`: Request location mode priority "low power":  "city" level accuracy (about 10km accuracy).
- `cordova.plugins.locationAccuracy.REQUEST_PRIORITY_BALANCED_POWER_ACCURACY`: Request location mode priority "balanced power":  "block" level accuracy (about 100 meter accuracy).
- `cordova.plugins.locationAccuracy.REQUEST_PRIORITY_HIGH_ACCURACY`: Request location mode priority "high accuracy":  the most accurate locations available. This will use GPS hardware to retrieve positions.


See [https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#constants](https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#constants)

## Callback constants

Both the `successCallback()` and `errorCallback()` functions will be passed an object which contains both a descriptive message and a code indicating the result of the operation.
These constants are defined on the `cordova.plugins.locationAccuracy` object.

### Success constants

The `successCallback()` function will be pass an object where the "code" key may correspond to the following values:

- `cordova.plugins.locationAccuracy.SUCCESS_SETTINGS_SATISFIED`: Success due to current location settings already satisfying requested accuracy.
- `cordova.plugins.locationAccuracy.SUCCESS_USER_AGREED`: Success due to user agreeing to requested accuracy change

### Error constants

The `errorCallback()` function will be pass an object where the "code" key may correspond to the following values:

- `cordova.plugins.locationAccuracy.ERROR_INVALID_ACTION`: Error due invalid action requested.
- `cordova.plugins.locationAccuracy.ERROR_INVALID_ACCURACY`: Error due invalid accuracy requested.
- `cordova.plugins.locationAccuracy.ERROR_EXCEPTION`: Error due to exception in the native code.
- `cordova.plugins.locationAccuracy.ERROR_CANNOT_CHANGE_ACCURACY`: Error due to not being able to change location accuracy to requested state.
- `cordova.plugins.locationAccuracy.ERROR_USER_DISAGREED`: Error due to user rejecting requested accuracy change.
- `cordova.plugins.locationAccuracy.ERROR_GOOGLE_API_CONNECTION_FAILED`: Error due to failure to connect to Google Play Services API. The "message" key will contain a detailed description of the Google Play Services error.



## Example usage

    function onRequestSuccess(success){
        console.log("Successfully requested accuracy: "+success.message);
    }

    function onRequestFailure(error){
        console.error("Accuracy request failed: error code="+error.code+"; error message="+error.message);
        if(error.code !== cordova.plugins.locationAccuracy.ERROR_USER_DISAGREED){
            if(window.confirm("Failed to automatically set Location Mode to 'High Accuracy'. Would you like to switch to the Location Settings page and do this manually?")){
                cordova.plugins.diagnostic.switchToLocationSettings();
            }
        }
    }

    cordova.plugins.locationAccuracy.request(onRequestSuccess, onRequestFailure, cordova.plugins.locationAccuracy.REQUEST_PRIORITY_HIGH_ACCURACY);

# Example project

An example project illustrating use of this plugin can be found here: [https://github.com/dpa99c/cordova-plugin-request-location-accuracy-example](https://github.com/dpa99c/cordova-plugin-request-location-accuracy-example)

# License

The MIT License

Copyright (c) 2015 Working Edge Ltd.

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