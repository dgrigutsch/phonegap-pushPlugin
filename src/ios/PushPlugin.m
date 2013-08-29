//
//  PushPlugin.m
//  pgPush
//
//  Created by Thorsten Maus on 17.04.12.
//  Copyright (c) 2012 eDarling. All rights reserved.
//
#import "AppDelegate.h"
#import "PushPlugin.h"
#import "NSData+HexString.h"
#import <Cordova/CDVJSON.h>

@implementation PushPlugin

static NSString* activatePushCallbackId = @"";
    
- (void)bootstrap:(CDVInvokedUrlCommand*)command
{
     NSLog(@"bootstrapping plugin");
}

+(void) deliverActivationResult:(NSData*)token forResult:(BOOL)status{
    CDVPluginResult* pluginResult;

    UIWebView *webView = [[(AppDelegate*)[[UIApplication sharedApplication] delegate] viewController] webView];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:[token hexString]];

    if (status) {

        [webView stringByEvaluatingJavaScriptFromString:[pluginResult toSuccessCallbackString:activatePushCallbackId]];
    } else {
        [webView stringByEvaluatingJavaScriptFromString:[pluginResult toErrorCallbackString:activatePushCallbackId]];
    }
}

+(void) deliverNotification:(NSDictionary*) userInfo{
    NSLog(@"notification message on delivery: %@",userInfo);
    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
    UIWebView *webView = [[(AppDelegate*)[[UIApplication sharedApplication] delegate] viewController] webView];
    NSString *jsStatement = [NSString stringWithFormat:@"window.plugins.pushNotification.notificationCallback(%@);", [userInfo JSONString]];
    [webView stringByEvaluatingJavaScriptFromString:jsStatement];
}

- (void)activatePush:(CDVInvokedUrlCommand*)command{
    
    NSLog(@"activate push notification");

    activatePushCallbackId = [command.arguments objectAtIndex:0];
    
    NSLog(@"activatePushCallbackId:%@",activatePushCallbackId);
    
    [[UIApplication sharedApplication] registerForRemoteNotificationTypes:(UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound)];
	[[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
}

- (void)deactivatePush:(CDVInvokedUrlCommand*)command{
    NSLog(@"deactivate push notification");
    [[UIApplication sharedApplication] unregisterForRemoteNotifications];
}



- (void)isPushActivated:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;


    UIRemoteNotificationType types = [[UIApplication sharedApplication] enabledRemoteNotificationTypes];
    if (types == UIRemoteNotificationTypeNone) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];

    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:1];


    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


@end
