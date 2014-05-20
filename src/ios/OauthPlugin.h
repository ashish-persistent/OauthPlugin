//
//  OauthPlugin.h
//  OauthPlugin
//
//  Created by Ashish A. Solanki on 19/05/14.
//
//

#import <Cordova/CDVPlugin.h>

@interface OauthPlugin : CDVPlugin


- (void)getToken:(CDVInvokedUrlCommand *)command;
@end
