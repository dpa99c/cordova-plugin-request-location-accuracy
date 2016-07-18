
#ifdef CORDOVA_FRAMEWORK
#import <Cordova/CDVPlugin.h>
#else
#import "Cordova/CDVPlugin.h"
#endif

#import <MapKit/MapKit.h>


@interface RequestLocationAccuracy :CDVPlugin {
}

- (void) request:(CDVInvokedUrlCommand*)command;

@end
