//
//  EnterpriseOAuthLibrary.m
//  OauthPlugin
//
//  Created by Ashish A. Solanki on 28/05/14.
//
//

#import "EnterpriseOAuthLibrary.h"
#import <UIKit/UIKit.h>
#define KRESPOSECODE @"code"

@implementation EnterpriseOAuthLibrary



static EnterpriseOAuthLibrary* manager = nil;
@synthesize redirectUri;
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
    if([strUrl hasSuffix:@"/"])
    {
        self.serverUrl = strUrl;
    }
    else
    {
        manager.serverUrl = [NSString stringWithFormat:@"%@/",strUrl];
    }
    
    manager.consumerKey = consumersKey;
    manager.secreteKey = secretkey;
    manager.redirectUri = [schemeUrl stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
//    manager.redirectUri = [schemeUrl stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
//    NSURL *scheme = [NSURL URLWithString:manager.redirectUri];
//    
//    if (scheme && manager.redirectUri.length > 0) {
//        if (!scheme.scheme && !scheme.host) {
//            manager.redirectUri = [NSString stringWithFormat:@"%@://oauthcallback",manager.redirectUri];
//        }
//        else if (!scheme.host)
//        {
//            manager.redirectUri = [NSString stringWithFormat:@"%@://oauthcallback",manager.redirectUri];
//        }
//    }
//    NSLog(@"SCheme %@, Host %@ \n%@",scheme.scheme , scheme.host,self.redirectUri);
	return manager;
}

+(id) sharedInstance {
    if (manager == nil) {
        manager = [[EnterpriseOAuthLibrary alloc] init];
    }
    return manager;
}


-(void)authenticateUserWithCallbackObject:(id)anObject selector:(SEL)selector {
    self.callbackObject = anObject;
	self.callbackSelector = selector;
    if ([self validateAllOauthParameters]) {
//        https://pi-api.persistent.co.in:9443/v1/oauth2/authorize?response_type=code&client_id=BW1ybSTIvu2Lr3UHJIbNeYqf7jl62sq4&scope=Read&redirect_uri=igreet://oauthcallback
        NSString *bundleIdentifier = [[NSBundle mainBundle] bundleIdentifier];
        NSString *url_string = [NSString stringWithFormat:@"%@authorize?response_type=code&client_id=%@&scope=Read&state=%@&redirect_uri=%@",self.serverUrl, self.consumerKey, bundleIdentifier, self.redirectUri ];
        NSURL *url = [NSURL URLWithString:url_string];
        NSLog(@"Url: %@ and URL = %@", url_string ,url);
        [[UIApplication sharedApplication] openURL:url];
    }
}



- (void)sendRequestForAccessTokenWithUrl:(NSURL*) url {
    NSDictionary *dict = [self parseQueryString:[url query]];
    NSString* responseCode = [dict objectForKey:KRESPOSECODE];
    
    NSString* encodedCode = [self base64String:[NSString stringWithFormat:@"%@:%@",self.consumerKey,self.secreteKey]]; //Consumer key:Secret Key
//    https://pi-api.persistent.co.in:9443/v1/oauth2/token?grant_type=authorization_code&scope=Read&code=ZbdDSJVK&client_id=BW1ybSTIvu2Lr3UHJIbNeYqf7jl62sq4&redirect_uri=igreet://oauthcallback
    NSString* strServerUrl = [NSString stringWithFormat:@"%@token?code=%@&grant_type=authorization_code&scope=Read&redirect_uri=%@&client_id=%@",self.serverUrl,responseCode,self.redirectUri, self.consumerKey];
    NSURL* serverUrlrl = [NSURL URLWithString:strServerUrl];
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:serverUrlrl];
    [request setHTTPMethod:@"POST"];
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
