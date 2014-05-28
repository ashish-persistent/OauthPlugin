//
//  EnterpriseOAuthLibrary.h
//  OauthPlugin
//
//  Created by Ashish A. Solanki on 28/05/14.
//
//

#import <Foundation/Foundation.h>

@interface EnterpriseOAuthLibrary : NSObject

@property (nonatomic, strong) NSString *serverUrl;
@property (nonatomic, strong) NSString *secreteKey;
@property (nonatomic, strong) NSString *consumerKey;
@property (nonatomic, strong) NSString *redirectUri;


@property (strong) id callbackObject;
@property (readwrite) SEL callbackSelector;

- (id)initWithServerURl:(NSString*)strUrl redirectURL:(NSString*)schemeUrl consumerKey:(NSString*)consumersKey andSecretKey:(NSString*)secretkey;
- (void)sendRequestForAccessTokenWithUrl:(NSURL*) url;
- (void)authenticateUserWithCallbackObject:(id)anObject selector:(SEL)selector;
+ (id)sharedInstance;

@end
