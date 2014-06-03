//
//  OauthPlugin.m
//  OauthPlugin
//
//  Created by Ashish A. Solanki on 19/05/14.
//
//

#import "OauthPlugin.h"
#import "PEASOAuthLibrary.h"
#import "EnterpriseOAuthLibrary.h"

@interface OauthPlugin () {
    CDVInvokedUrlCommand        *command;
}

@end

@implementation OauthPlugin


- (void)enterpriseAuthorize:(CDVInvokedUrlCommand *)cmd {
    command = cmd;
    NSDictionary *options = [cmd.arguments objectAtIndex:0];
    NSString *baseURL = [options objectForKey:@"baseUrl"];
    NSString *consumerKey = [options objectForKey:@"consumerKey"];
    NSString *secretKey = [options objectForKey:@"secretKey"];
    NSString *redirectUrl = [options objectForKey:@"redirectUrl"];
    
    EnterpriseOAuthLibrary *library = [[EnterpriseOAuthLibrary alloc] initWithServerURl:baseURL  redirectURL:redirectUrl consumerKey:consumerKey andSecretKey:secretKey];
    [library authenticateUserWithCallbackObject:self selector:@selector(loginSuccess:)];
}


- (void)getToken:(CDVInvokedUrlCommand *)cmd {
    command = cmd;
    NSDictionary *options = [cmd.arguments objectAtIndex:0];
    NSString *baseURL = [options objectForKey:@"baseUrl"];
    NSString *consumerKey = [options objectForKey:@"consumerKey"];
    NSString *secretKey = [options objectForKey:@"secretKey"];
    NSString *redirectUrl = [options objectForKey:@"redirectUrl"];

    PEASOAuthLibrary *library = [[PEASOAuthLibrary alloc] initWithServerURl:baseURL  redirectURL:redirectUrl consumerKey:consumerKey andSecretKey:secretKey];
    [library authenticateUserWithCallbackObject:self selector:@selector(loginSuccess:)];
}

- (void)loginSuccess:(id)data {
    CDVPluginResult *pluginResult = [ CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
    [pluginResult setKeepCallbackAsBool:YES];
    if(command) {
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void)handleOpenURL:(NSNotification *)notification {
    NSURL *redirectUrl = notification.object;
    NSString *url = [redirectUrl absoluteString];
    NSRange range = [url rangeOfString:@":"];
    NSString *scheme = [url substringToIndex:range.location];
    
    if([scheme isEqualToString:@"igreetpeas"]) {
        [[PEASOAuthLibrary sharedInstance] sendRequestForAccessTokenWithUrl:notification.object];
    } else if([scheme isEqualToString:@"igreet"]) {
        [[EnterpriseOAuthLibrary sharedInstance] sendRequestForAccessTokenWithUrl:notification.object];
    }

}

@end
