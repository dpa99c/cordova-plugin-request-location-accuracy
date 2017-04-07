/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package cordova.plugin;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.lang.reflect.Method;

public class RequestLocationAccuracy extends CordovaPlugin implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {
    public static final String TAG = "RequestLocationAccuracy";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient = null;

    /**
     * Google API availability
     */
    protected GoogleApiAvailability googleApiAvailability;


    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    protected LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * Constant to indicated request for LocationRequest.PRIORITY_NO_POWER
     */
    protected static final int REQUEST_PRIORITY_NO_POWER = 0;

    /**
     * Constant to indicated request for LocationRequest.PRIORITY_LOW_POWER
     */
    protected static final int REQUEST_PRIORITY_LOW_POWER = 1;

    /**
     * Constant to indicated request for LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
     */
    protected static final int REQUEST_PRIORITY_BALANCED_POWER_ACCURACY = 2;

    /**
     * Constant to indicated request for LocationRequest.HIGH_ACCURACY
     */
    protected static final int REQUEST_PRIORITY_HIGH_ACCURACY = 3;

    /**
     * Success due to current location settings already satisfying requested
     * accuracy
     */
    protected static final int SUCCESS_SETTINGS_SATISFIED = 0;

    /**
     * Success due to user agreeing to requested accuracy change
     */
    protected static final int SUCCESS_USER_AGREED = 1;

    /**
     * Error due invalid action requested
     */
    protected static final int ERROR_INVALID_ACTION = 0;

    /**
     * Error due invalid accuracy requested
     */
    protected static final int ERROR_INVALID_ACCURACY = 1;

    /**
     * Error due to exception
     */
    protected static final int ERROR_EXCEPTION = 2;

    /**
     * Error due to not being able to change location accuracy to requested state
     */
    protected static final int ERROR_CANNOT_CHANGE_ACCURACY = 3;

    /**
     * Error due to user rejecting requested accuracy change
     */
    protected static final int ERROR_USER_DISAGREED = 4;

    /**
     * Error due to failure to connect to Google Play Services API
     */
    protected static final int ERROR_GOOGLE_API_CONNECTION_FAILED = 5;

    /**
     * Cordova callback context
     */
    protected CallbackContext context = null;

    /**
     * Indicates a permanent error, so doesn't recheck since the Google APIs won't invoke the listeners on subsequent calls.
     */
    protected ConnectionResult permanentError = null;

    /**
     * Constructor.
     */
    public RequestLocationAccuracy() {}


    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        try {
            googleApiAvailability = GoogleApiAvailability.getInstance();
            buildGoogleApiClient();
        }catch(Exception e ) {
            handleError(e.getMessage(), ERROR_EXCEPTION);
        }
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action            The action to execute.
     * @param args              JSONArry of arguments for the plugin.
     * @param callbackContext   The callback id used when calling back into JavaScript.
     * @return                  True if the action was valid, false if not.
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        boolean result;
        context = callbackContext;
        try {
            if(action.equals("request")) {
                result = request(args.getInt(0));
            }else if(action.equals("canRequest")) {
                result = canRequest();
            }else {
                handleError("Invalid action", ERROR_INVALID_ACTION);
                result = false;
            }
        }catch(Exception e ) {
            handleError(e.getMessage(), ERROR_EXCEPTION);
            result = false;
        }
        return result;
    }

    public boolean request(int requestedAccuracy) throws Exception{
        if(permanentError != null){
            this.onConnectionFailed(permanentError);
            return true;
        }

        if(mGoogleApiClient == null){
            handleError("Google Play Services Client failed to initialize", ERROR_GOOGLE_API_CONNECTION_FAILED);
            return true;
        }

        int priority;
        switch(requestedAccuracy){
            case REQUEST_PRIORITY_NO_POWER:
                priority = LocationRequest.PRIORITY_NO_POWER;
                break;
            case REQUEST_PRIORITY_LOW_POWER:
                priority = LocationRequest.PRIORITY_LOW_POWER;
                break;
            case REQUEST_PRIORITY_BALANCED_POWER_ACCURACY:
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                break;
            case REQUEST_PRIORITY_HIGH_ACCURACY:
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
                break;
            default:
                handleError("'"+requestedAccuracy+"' is not a valid accuracy constant", ERROR_INVALID_ACCURACY);
                return false;
        }

        createLocationRequest(priority);
        buildLocationSettingsRequest();
        checkLocationSettings();
        return true;
    }

    public boolean canRequest() throws Exception{
        boolean _canRequest = isLocationAuthorized();
        context.success(_canRequest ? 1 : 0);
        return true;
    }

    private boolean isLocationAuthorized() throws Exception {
        boolean authorized = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        Log.v(TAG, "Location permission is " + (authorized ? "authorized" : "unauthorized"));
        return authorized;
    }

    private boolean hasPermission(String permission) throws Exception{
        boolean hasPermission = true;
        Method method = null;
        try {
            method = cordova.getClass().getMethod("hasPermission", permission.getClass());
            Boolean bool = (Boolean) method.invoke(cordova, permission);
            hasPermission = bool.booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Cordova v" + CordovaWebView.CORDOVA_VERSION + " does not support runtime permissions so defaulting to GRANTED for " + permission);
        }
        return hasPermission;
    }

    protected void handleError(String errorMsg, int errorCode){
        try {
            Log.e(TAG, errorMsg);
            if(context != null){
                JSONObject error = new JSONObject();
                error.put("message", errorMsg);
                error.put("code", errorCode);
                context.error(error);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    protected void handleSuccess(String msg, int code){
        try {
            Log.i(TAG, msg);
            if(context != null){
                JSONObject success = new JSONObject();
                success.put("message", msg);
                success.put("code", code);
                context.success(success);
            }
        } catch (JSONException e) {
            handleError(e.getMessage(), ERROR_EXCEPTION);
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this.cordova.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        Log.i(TAG, "Connect Google API client");
        mGoogleApiClient.connect();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest(int priority) {
        Log.i(TAG, "Create location request");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(priority);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        Log.i(TAG, "Build location settings request");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Check if the device's location settings are adequate for the app's needs using the
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} method, with the results provided through a {@code PendingResult}.
     */
    protected void checkLocationSettings() {
        Log.i(TAG, "Check location settings");
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    /**
     * The callback invoked when
     * {@link com.google.android.gms.location.SettingsApi#checkLocationSettings(GoogleApiClient,
     * LocationSettingsRequest)} is called. Examines the
     * {@link com.google.android.gms.location.LocationSettingsResult} object and determines if
     * location settings are adequate. If they are not, begins the process of presenting a location
     * settings dialog to the user.
     */
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        Log.i(TAG, "onResult()");
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                String msg = "All location settings are satisfied.";
                Log.i(TAG, msg);
                handleSuccess(msg, SUCCESS_SETTINGS_SATISFIED);
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    this.cordova.setActivityResultCallback(this);
                    status.startResolutionForResult(this.cordova.getActivity(), REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    handleError("PendingIntent unable to execute request: ".concat(e.getMessage()), ERROR_CANNOT_CHANGE_ACCURACY);
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                handleError("Location settings are inadequate, and cannot be fixed here. Dialog not created.", ERROR_CANNOT_CHANGE_ACCURACY);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult()");
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        String msg = "User agreed to make required location settings changes.";
                        Log.i(TAG, msg);
                        handleSuccess(msg, SUCCESS_USER_AGREED);
                        break;
                    case Activity.RESULT_CANCELED:
                        handleError("User chose not to make required location settings changes.", ERROR_USER_DISAGREED);
                        break;
                }
                break;
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }

    @Override
    public void onStart() {
        Log.i(TAG, "On start");
        super.onStart();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "On onDestroy");
        if(mGoogleApiClient != null){
            super.onStop();
            Log.i(TAG, "Disconnect Google API client");
            try {
                mGoogleApiClient.disconnect();
            }catch(Exception e ) {
                handleError(e.getMessage(), ERROR_EXCEPTION);
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // https://developers.google.com/android/reference/com/google/android/gms/common/ConnectionResult
        permanentError = result;
        String reason;
        switch (result.getErrorCode()){
            case ConnectionResult.API_UNAVAILABLE:
                reason = "One of the API components you attempted to connect to is not available.";
                break;
            case ConnectionResult.CANCELED:
                reason = "The connection was canceled.";
                break;
            case ConnectionResult.DEVELOPER_ERROR:
                reason = "The application is misconfigured.";
                break;
            case ConnectionResult.INTERNAL_ERROR:
                reason = "An internal error occurred.";
                break;
            case ConnectionResult.INTERRUPTED:
                reason = "An interrupt occurred while waiting for the connection complete.";
                break;
            case ConnectionResult.INVALID_ACCOUNT:
                reason = "he client attempted to connect to the service with an invalid account name specified.";
                break;
            case ConnectionResult.LICENSE_CHECK_FAILED:
                reason = "The application is not licensed to the user.";
                break;
            case ConnectionResult.NETWORK_ERROR:
                reason = "A network error occurred.";
                break;
            case ConnectionResult.RESOLUTION_REQUIRED:
                reason = "Completing the connection requires some form of resolution.";
                break;
            case ConnectionResult.SERVICE_DISABLED:
                reason = "The installed version of Google Play services has been disabled on this device.";
                break;
            case ConnectionResult.SERVICE_INVALID:
                reason = "The version of the Google Play services installed on this device is not authentic.";
                break;
            case ConnectionResult.SERVICE_MISSING:
                reason = "Google Play services is missing on this device.";
                break;
            case ConnectionResult.SERVICE_MISSING_PERMISSION:
                reason = "Google Play service doesn't have one or more required permissions.";
                break;
            case ConnectionResult.SERVICE_UPDATING:
                reason = "Google Play service is currently being updated on this device.";
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                reason = "The installed version of Google Play services is out of date.";
                break;
            case ConnectionResult.SIGN_IN_FAILED:
                reason = "The client attempted to connect to the service but the user is not signed in.";
                break;
            case ConnectionResult.SIGN_IN_REQUIRED:
                reason = "The client attempted to connect to the service but the user is not signed in.";
                break;
            case ConnectionResult.TIMEOUT:
                reason = "The timeout was exceeded while waiting for the connection to complete.";
                break;
            default:
                reason = "Unknown reason";
        }
        handleError("Failed to connect to Google Play Services: ".concat(reason), ERROR_GOOGLE_API_CONNECTION_FAILED);

        int status = googleApiAvailability.isGooglePlayServicesAvailable(cordova.getActivity().getApplicationContext());
        if (googleApiAvailability.isUserResolvableError (status)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(cordova.getActivity(),status, 0);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    cordova.getActivity().finish();
                }
            });
            dialog.show();
            permanentError = null;
        }
    }
}