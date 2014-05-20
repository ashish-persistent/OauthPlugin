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
#import <Cordova/CDVPlugin.h>
#define KRESPOSECODE @"code"

static PEASOAuthLibrary* manager = nil;


@interface PEASOAuthLibrary () <UIWebViewDelegate> {
    UIWebView           *wvLogin;
}

@end

@implementation PEASOAuthLibrary



@synthesize redirectUri;
@synthesize accessToken;
@synthesize secreteKey;
@synthesize serverUrl;
@synthesize consumerKey;

@synthesize callbackObject;
@synthesize callbackSelector;


- (id)initWithServerURl:(NSString*) strUrl redirectURL:(NSString*)schemeUrl consumerKey:(NSString*) consumersKey andSecretKey:(NSString*)secretkey {
    if (manager == nil) {
        self = [super init];
        manager = self;
    }
    if([strUrl hasSuffix:@"/"]) {
        self.serverUrl = strUrl;
    } else {
        manager.serverUrl = [NSString stringWithFormat:@"%@/",strUrl];
    }
    
    manager.consumerKey = consumersKey;
    manager.secreteKey = secretkey;
    manager.redirectUri = [schemeUrl stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    
    NSURL *scheme = [NSURL URLWithString:manager.redirectUri];
    
    if (scheme && manager.redirectUri.length > 0) {
        if (!scheme.scheme && !scheme.host) {
            manager.redirectUri = [NSString stringWithFormat:@"%@://persistent",manager.redirectUri];
        } else if (!scheme.host) {
            manager.redirectUri = [NSString stringWithFormat:@"%@://persistent",manager.redirectUri];
        }
    }
    NSLog(@"SCheme %@, Host %@ \n%@",scheme.scheme , scheme.host,self.redirectUri);
	return manager;
}

+(id) sharedInstance {
    if (manager == nil) {
        manager = [[PEASOAuthLibrary alloc] init];
    }
    return manager;
}


-(void)authenticateUserWithCallbackObject:(id)anObject selector:(SEL)selector {
    self.callbackObject = anObject;
	self.callbackSelector = selector;
    if ([self validateAllOauthParameters]) {
        NSString *bundleIdentifier = [[NSBundle mainBundle] bundleIdentifier];
        
        NSString *url_string = [NSString stringWithFormat:@"%@authorize?response_type=code&client_id=%@&scope=READ&state=%@&redirect_url=%@&deviceId=%@&deviceOs=%@&deviceOsVersion=%@&packageName=%@&apiName=%@", self.serverUrl,self.consumerKey,[self getDeviceIdentifier], @"www.example.com",[self getDeviceIdentifier],[self getDeviceOS], [self getDeviceOSVersion], bundleIdentifier, @"DummyApiName"];
        NSLog(@"Url: %@", url_string);
        NSURL *url = [NSURL URLWithString:url_string];
        
        CDVPlugin *plugin = (CDVPlugin *)self.callbackObject;
        UIViewController *vc = plugin.viewController;
        wvLogin=[[UIWebView alloc]initWithFrame:CGRectMake(0, 20, vc.view.frame.size.width,vc.view.frame.size.height)];
        wvLogin.scrollView.scrollEnabled = TRUE;
        wvLogin.scalesPageToFit = TRUE;
        wvLogin.delegate = self;
        NSURLRequest *nsrequest=[NSURLRequest requestWithURL:url];
        [wvLogin loadRequest:nsrequest];
        [vc.view addSubview:wvLogin];
    }
}

- (void)webViewDidFinishLoad:(UIWebView *)webView {
    NSLog(@"My URL %@", [webView.request.URL absoluteString]);
    NSURL *url = webView.request.URL;
    NSString *string = @"code=";
    if ([[url absoluteString] rangeOfString:string].location != NSNotFound) {
        [[PEASOAuthLibrary sharedInstance] sendRequestForAccessTokenWithUrl:webView.request.URL];
    }
}



#pragma mark - DEVICE INFORMATION
/**
 * @fn     -(NSString*) getDeviceIdentifier
 * @brief  it gives the device unique identifier
 * @return NSString : device vendorId
 */

-(NSString*) getDeviceIdentifier {
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

- (NSString *)getDeviceOS {
    //    NSLog(@" %@ %@",[[UIDevice currentDevice] systemName],[[UIDevice currentDevice] systemVersion]);
    //    return [[UIDevice currentDevice] systemName];
    return @"iOS";
}


- (NSString *)getDeviceOSVersion {
#if TARGET_IPHONE_SIMULATOR
    
    //Simulator
    //inovation BU iPAd
    return @"7.1.0";
    
#endif
    
    return [[UIDevice currentDevice] systemVersion];
}


- (void)sendRequestForAccessTokenWithUrl:(NSURL*) url {
    NSDictionary *dict = [self parseQueryString:[url query]];
    NSString* responseCode = [dict objectForKey:KRESPOSECODE];
    
    NSString* encodedCode = [self base64String:[NSString stringWithFormat:@"%@:%@",self.consumerKey,self.secreteKey]]; //Consumer key:Secret Key
    //    http://abhiejit-test.apigee.net/v1/test/token?code=%@&grant_type=authorization_code&response_type=code&redirect_uri=%@
    NSString* strServerUrl = [NSString stringWithFormat:@"%@token?code=%@&grant_type=authorization_code&response_type=code&redirect_uri=%@",self.serverUrl,responseCode,@"www.example.com"];
    NSURL* serverUrlrl = [NSURL URLWithString:strServerUrl];
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:serverUrlrl];
    [request addValue:[NSString stringWithFormat:@"Basic %@",encodedCode] forHTTPHeaderField:@"Authorization"];
    NSError* e;
    
    NSData* data = [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:&e];
    NSDictionary* dic = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingAllowFragments error:&e];
    [self.callbackObject performSelectorInBackground:self.callbackSelector withObject:dic];
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

- (NSString *)base64String:(NSString *)str {
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

-(BOOL) validateAllOauthParameters {
    NSString* msg = @"";
    BOOL isValid = YES;
    NSURL* urlScheme = [NSURL URLWithString:self.redirectUri];
    
    NSURL *candidateURL = [NSURL URLWithString:self.serverUrl];
    if(self.consumerKey.length == 0)
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
