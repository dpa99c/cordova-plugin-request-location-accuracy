#import "RequestLocationAccuracy.h"

@implementation RequestLocationAccuracy

@synthesize locationManager, locationData;

- (void)pluginInitialize
{
    self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.delegate = self;
    [self.locationManager startUpdatingLocation];
    [self.locationManager stopUpdatingLocation];
}

- (void) request:(CDVInvokedUrlCommand*)command;{
    @try {

        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }@catch (NSException *exception) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.reason] callbackId:command.callbackId];
    }
}
@end