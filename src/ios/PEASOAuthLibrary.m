//
//  PEASOAuthLibrary.m
//  PEASOAuthLibrary
//
//  Created by Gautam Zodape on 30/04/14.
//  Copyright (c) 2014 Gautam Zodape. All rights reserved.
//

//
//  oAuthManager.m
//  oAuthManager
//
//  Created by Gautam Zodape on 25/04/14.
//  Copyright (c) 2014 Gautam Zodape. All rights reserved.
//

#import "PEASOAuthLibrary.h"
#import <UIKit/UIKit.h>
#define KRESPOSECODE @"code"

static PEASOAuthLibrary* manager = nil;
@implementation PEASOAuthLibrary



@synthesize redirectUri;
@synthesize accessToken;
@synthesize secreteKey;
@synthesize serverUrl;
@synthesize consumerKey;

@synthesize callbackObject;
@synthesize callbackSelector;


- (id)initWithRedirectURL:(NSString*)schemeUrl consumerKey:(NSString*) consumersKey andSecretKey:(NSString*)secretkey {
    if (manager == nil) {
        self = [super init];
        manager = self;
    }
    manager.consumerKey = consumersKey;
    manager.secreteKey = secretkey;
    manager.redirectUri = [schemeUrl stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
	return manager;
}

+(id) sharedInstance {
    if (manager == nil) {
        manager = [[PEASOAuthLibrary alloc] init];
    }
    return manager;
}

- (NSString *)getSSOServerDetail {
    NSString* trimedUrl = [self.serverUrl stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@"/"]];
    NSString* path = [NSString stringWithFormat:@"%@/peas/ssourl",trimedUrl];
    
    NSMutableURLRequest* urlReq = [[NSMutableURLRequest alloc] initWithURL:[NSURL URLWithString:path]];
    [urlReq setValue:@"peasappv3.1" forHTTPHeaderField:@"appid"];
    NSError* e;
    NSData* data= [NSURLConnection sendSynchronousRequest:urlReq returningResponse:nil error:&e];
    NSDictionary* dicResponse = [NSJSONSerialization JSONObjectWithData:data options:
                                 NSJSONReadingAllowFragments error:&e];
    
    NSLog(@"Error %@ and \n Response %@",e,dicResponse);
    if (!e) {
        return [dicResponse objectForKey:@"url"];
        /* if ([dicResponse objectForKey:@"peasclient"] && self.serverUrl) {
         self.consumerKey = [dicResponse objectForKey:@"peasclient"];
         }
         
         if ([dicResponse objectForKey:@"peassecret"] && self.serverUrl) {
         self.secreteKey = [dicResponse objectForKey:@"peassecret"];
         }
         */
    }
    return  nil;
}





- (void)logutUserWithCallbackObject:(id)anObject selector:(SEL)selector {
    NSString* trimedUrl = [self.serverUrl stringByTrimmingCharactersInSet:[NSCharacterSet characterSetWithCharactersInString:@"/"]];
    NSString* strUrl = [NSString stringWithFormat:@"%@/logout",trimedUrl];
    NSLog(@"oAuth Logout Url: %@", strUrl);
    NSMutableURLRequest* urlReq = [[NSMutableURLRequest alloc] initWithURL:[NSURL URLWithString:strUrl]];
    [urlReq setHTTPMethod:@"POST"];
    NSString *postString = [NSString stringWithFormat:@"{\"deviceId\":\"%@\"}",[self getDeviceIdentifier]];
    [urlReq setHTTPBody:[postString dataUsingEncoding:NSUTF8StringEncoding]];
    [urlReq setValue:@"peasappv3.1" forHTTPHeaderField:@"appid"];
    [urlReq setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    [NSURLConnection sendAsynchronousRequest:urlReq queue:queue completionHandler:^(NSURLResponse *response, NSData *data, NSError *connectionError) {
        if (connectionError) {
            //            Got error in fetchning the response from server
            dispatch_async(dispatch_get_main_queue(), ^(void){
                
                NSLog(@"Logout faild");
            });
        }
        else {
            NSError* e;
            NSDictionary* dicResponse = [NSJSONSerialization JSONObjectWithData:data options:
                                         NSJSONReadingAllowFragments error:&e];
            if (e) {
                //  Got error in Parsing the response
                dispatch_async(dispatch_get_main_queue(), ^(void){
                    NSLog(@"Logout faild");
                });
            }
            else {
                dispatch_async(dispatch_get_main_queue(), ^(void){
                    NSLog(@"Logout success");
                    [anObject performSelectorInBackground:selector withObject:dicResponse];
                });
            }
        }
    }];
}


- (void)authenticateUserWithPEASUrl:(NSString *)urlString callbackObject:(id)anObject selector:(SEL)selector {
    self.serverUrl = urlString;
    [self authenticateUserWithUrl:[self getSSOServerDetail] callbackObject:anObject selector:selector];
}


- (void)authenticateUserWithUrl:(NSString *)urlString callbackObject:(id)anObject selector:(SEL)selector {
    self.callbackObject = anObject;
	self.callbackSelector = selector;
    if(![urlString hasSuffix:@"/"]) {
        urlString = [NSString stringWithFormat:@"%@/",urlString];
    }
    if ([self validateAllOauthParameters]) {
        NSString *bundleIdentifier = [[NSBundle mainBundle] bundleIdentifier];
        NSString *url_string = [NSString stringWithFormat:@"%@authorize?response_type=code&client_id=%@&scope=READ&state=%@&deviceId=%@&deviceOs=%@&deviceOsVersion=%@&packageName=%@&apiName=%@&redirect_url=%@",urlString,self.consumerKey,self.serverUrl, [self getDeviceIdentifier],[self getDeviceOS], [self getDeviceOSVersion], bundleIdentifier, @"DummyApiName",self.redirectUri ];
        
        NSURL *url = [NSURL URLWithString:url_string];
        NSLog(@"Url: %@ and URL = %@", url_string ,url);
        self.serverUrl = urlString;
        [[UIApplication sharedApplication] openURL:url];
    }
    
}




#pragma mark - DEVICE INFORMATION
/**
 * @fn     -(NSString*) getDeviceIdentifier
 * @brief  it gives the device unique identifier
 * @return NSString : device vendorId
 */

-(NSString*) getDeviceIdentifier
{
#if TARGET_IPHONE_SIMULATOR
    
    //Simulator
    //inovation BU iPAd
    return @"384A7F14-6FDE-468A-905A-71B040D69B37";
    
#endif
    
    NSString* udid = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
    
    return udid;
}


/**
 * @fn     -(NSString*) getDeviceOS
 * @brief  it gives the device OS Versions
 * @return NSString : device OS Versions
 */

- (NSString *)getDeviceOS
{
    //    NSLog(@" %@ %@",[[UIDevice currentDevice] systemName],[[UIDevice currentDevice] systemVersion]);
    //    return [[UIDevice currentDevice] systemName];
    return @"iOS";
}
- (NSString *)getDeviceOSVersion
{
#if TARGET_IPHONE_SIMULATOR
    
    //Simulator
    //inovation BU iPAd
    return @"7.1.0";
    
#endif
    
    return [[UIDevice currentDevice] systemVersion];
}


- (void)sendRequestForAccessTokenWithUrl:(NSURL*) url {
    
    @try {
        
        NSDictionary *dict = [self parseQueryString:[url query]];
        NSString* responseCode = [dict objectForKey:KRESPOSECODE];
        
        NSString* encodedCode = [self base64String:[NSString stringWithFormat:@"%@:%@",self.consumerKey,self.secreteKey]]; //Consumer key:Secret Key
        NSString* strServerUrl = [NSString stringWithFormat:@"%@token?code=%@&grant_type=authorization_code&response_type=code&redirect_uri=%@",self.serverUrl,responseCode,self.redirectUri];
        NSURL* serverUrlrl = [NSURL URLWithString:strServerUrl];
        NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:serverUrlrl];
        [request addValue:[NSString stringWithFormat:@"Basic %@",encodedCode] forHTTPHeaderField:@"Authorization"];
        NSError* e;
        
        NSData* data = [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:&e];
        NSDictionary* dic = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:&e];
        [self.callbackObject performSelectorInBackground:self.callbackSelector withObject:dic];
    }
    @catch (NSException *exception) {
        [self.callbackObject performSelectorInBackground:self.callbackSelector withObject:[NSDictionary dictionary]];
    }
    
}


- (NSDictionary *)parseQueryString:(NSString *)query {
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithCapacity:6];
    NSArray *pairs = [query componentsSeparatedByString:@"&"];
    
    for (NSString *pair in pairs) {
        NSArray *elements = [pair componentsSeparatedByString:@"="];
        NSString *key = [[elements objectAtIndex:0] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        NSString *val = [[elements objectAtIndex:1] stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        
        [dict setObject:val forKey:key];
    }
    return dict;
}

/**
 * @fn     - (NSString *)base64String:(NSString *)str
 * @brief  It convert the string into base64
 * @param  str input string for encoding
 * @return NSSsring : bast64 encoded string
 */

- (NSString *)base64String:(NSString *)str
{
    NSData *theData = [str dataUsingEncoding: NSASCIIStringEncoding];
    const uint8_t* input = (const uint8_t*)[theData bytes];
    NSInteger length = [theData length];
    
    static char table[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    
    NSMutableData* data = [NSMutableData dataWithLength:((length + 2) / 3) * 4];
    uint8_t* output = (uint8_t*)data.mutableBytes;
    
    NSInteger i;
    for (i=0; i < length; i += 3) {
        NSInteger value = 0;
        NSInteger j;
        for (j = i; j < (i + 3); j++) {
            value <<= 8;
            
            if (j < length) {
                value |= (0xFF & input[j]);
            }
        }
        
        NSInteger theIndex = (i / 3) * 4;
        output[theIndex + 0] =                    table[(value >> 18) & 0x3F];
        output[theIndex + 1] =                    table[(value >> 12) & 0x3F];
        output[theIndex + 2] = (i + 1) < length ? table[(value >> 6)  & 0x3F] : '=';
        output[theIndex + 3] = (i + 2) < length ? table[(value >> 0)  & 0x3F] : '=';
    }
    
    return [[NSString alloc] initWithData:data encoding:NSASCIIStringEncoding];
}

/**
 * @fn     -(BOOL) validateAllOauthParameters
 * @brief  It validate all the variable of lib manager class
 * @return BOOL : whether all parameter are available or not
 */

-(BOOL) validateAllOauthParameters
{
    NSString* msg = @"";
    BOOL isValid = YES;
    NSURL* urlScheme = [NSURL URLWithString:self.redirectUri];
    
    NSURL *candidateURL = [NSURL URLWithString:self.serverUrl];
    if (!(candidateURL && candidateURL.scheme && candidateURL.host)) {
        isValid = NO;
        msg = @"Please provide valid server url.";
    }
    else if(self.consumerKey.length == 0)
    {
        isValid = NO;
        msg = @"Please provide valid Consumer Key.";
    }
    else if(self.secreteKey.length == 0)
    {
        isValid = NO;
        msg = @"Please provide valid Secret Key.";
    }
    else if(![[UIApplication sharedApplication] canOpenURL:urlScheme])
    {
        isValid = NO;
        msg = @"Please provide valid URL Scheme of app.";
    }
    
    
    if (!isValid) {
        UIAlertView* alert = [[UIAlertView alloc] initWithTitle:@"oAuth" message:msg delegate:nil cancelButtonTitle:@"Ok" otherButtonTitles:nil, nil];
        [alert show];
    }
    return isValid;
}

@end
