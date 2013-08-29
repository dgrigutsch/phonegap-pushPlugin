//
//  PushPlugin.h
//  pgPush
//
//  Created by Thorsten Maus on 17.04.12.
//  Copyright (c) 2012 eDarling. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Cordova/CDVPlugin.h"


@interface PushPlugin : CDVPlugin {
       
}


+(void) deliverActivationResult:(NSData*)msg forResult:(BOOL)status;

+(void) deliverNotification:(NSDictionary*) userInfo;

-(void)bootstrap:(CDVInvokedUrlCommand*)command;
    
-(void)activatePush:(CDVInvokedUrlCommand*)command;
-(void)deactivatePush:(CDVInvokedUrlCommand*)command;
-(void)isPushActivated:(CDVInvokedUrlCommand*)command;

@end
