#import <Cordova/CDVPlugin.h>
#import <CoreLocation/CoreLocation.h>


@interface RequestLocationAccuracy : CDVPlugin <CLLocationManagerDelegate>{
@private BOOL __locationStarted;
}

@property (nonatomic, strong) CLLocationManager* locationManager;

- (void) request:(CDVInvokedUrlCommand*)command;
- (void) isRequesting:(CDVInvokedUrlCommand*)command;
- (void) canRequest:(CDVInvokedUrlCommand*)command;

@end
