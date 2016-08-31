#import "RequestLocationAccuracy.h"

@implementation RequestLocationAccuracy

// Initialization
@synthesize locationManager;

- (void)pluginInitialize
{
    self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.delegate = self;
    __locationStarted = NO;
}

// Plugin API
- (void) request:(CDVInvokedUrlCommand*)command;{
    CDVPluginResult* pluginResult;
    @try {
        if (__locationStarted) {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Location is already being requested"];
        }else if(![self canRequest]){
            // Location services is already enabled
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }else{
            [self.locationManager startUpdatingLocation];
            __locationStarted = YES;
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        }
    }@catch (NSException *exception) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.reason];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) isRequesting:(CDVInvokedUrlCommand*)command;{
    int result;
    if(__locationStarted){
        result = 1;
    }else{
        result = 0;
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:result] callbackId:command.callbackId];
}

- (void) canRequest:(CDVInvokedUrlCommand*)command;{
    int result;
    if([self canRequest]){
        result = 1;
    }else{
        result = 0;
    }
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:result] callbackId:command.callbackId];
}

// Internals

- (BOOL) canRequest
{
    return ![self isLocationServicesEnabled] && !__locationStarted;
}


- (void)stopRequestingLocation
{
    if (__locationStarted) {
        [self.locationManager stopUpdatingLocation];
        __locationStarted = NO;
    }
}

- (BOOL)isAuthorized
{
    BOOL authorizationStatusClassPropertyAvailable = [CLLocationManager respondsToSelector:@selector(authorizationStatus)]; // iOS 4.2+

    if (authorizationStatusClassPropertyAvailable) {
        NSUInteger authStatus = [CLLocationManager authorizationStatus];
#ifdef __IPHONE_8_0
        if ([self.locationManager respondsToSelector:@selector(requestWhenInUseAuthorization)]) {  //iOS 8.0+
            return (authStatus == kCLAuthorizationStatusAuthorizedWhenInUse) || (authStatus == kCLAuthorizationStatusAuthorizedAlways) || (authStatus == kCLAuthorizationStatusNotDetermined);
        }
#endif
        return (authStatus == kCLAuthorizationStatusAuthorized) || (authStatus == kCLAuthorizationStatusNotDetermined);
    }

    // by default, assume YES (for iOS < 4.2)
    return YES;
}

- (BOOL)isLocationServicesEnabled
{
    BOOL locationServicesEnabledInstancePropertyAvailable = [self.locationManager respondsToSelector:@selector(locationServicesEnabled)]; // iOS 3.x
    BOOL locationServicesEnabledClassPropertyAvailable = [CLLocationManager respondsToSelector:@selector(locationServicesEnabled)]; // iOS 4.x

    if (locationServicesEnabledClassPropertyAvailable) { // iOS 4.x
        return [CLLocationManager locationServicesEnabled];
    } else if (locationServicesEnabledInstancePropertyAvailable) { // iOS 2.x, iOS 3.x
        return [(id)self.locationManager locationServicesEnabled];
    } else {
        return NO;
    }
}

#pragma mark - locationManager delegate

- (void)locationManager:(CLLocationManager*)manager
    didUpdateToLocation:(CLLocation*)newLocation
           fromLocation:(CLLocation*)oldLocation
{
    [self stopRequestingLocation];

}

- (void)locationManager:(CLLocationManager*)manager didFailWithError:(NSError*)error
{
    NSLog(@"locationManager::didFailWithError %@", [error localizedFailureReason]);
    [self stopRequestingLocation];
}

@end