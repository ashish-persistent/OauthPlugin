//
//  OauthPlugin.m
//  OauthPlugin
//
//  Created by Ashish A. Solanki on 19/05/14.
//
//

#import "OauthPlugin.h"
#import "PEASOAuthLibrary.h"

@interface OauthPlugin () {
    CDVInvokedUrlCommand        *command;
}

@end

@implementation OauthPlugin
- (void)getToken:(CDVInvokedUrlCommand *)cmd {
    
    command = cmd;
    PEASOAuthLibrary *library = [[PEASOAuthLibrary alloc] initWithServerURl:@"http://abhiejit-test.apigee.net/v1/test/"  redirectURL:@"OauthPlugin" consumerKey:@"dzCeNDaslpucNXeLrjzek4peM3DwoMlB" andSecretKey:@"DM7sAM3Dsb3MeAF1"];
    
    [library authenticateUserWithCallbackObject:self selector:@selector(loginSuccess:)];
    //    [library authenticateUserWithSuccess:^(id responseObject) {
    //        CDVPluginResult *pluginResult = [ CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:responseObject];
    //        [pluginResult setKeepCallbackAsBool:YES];
    //        if(command) {
    //            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    //        }
    //
    //    } failure:^(NSError *error) {
    //
    //    }];
    
}

- (void)loginSuccess:(id)data {
    CDVPluginResult *pluginResult = [ CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
    [pluginResult setKeepCallbackAsBool:YES];
    if(command) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

@end
