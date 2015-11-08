//
//  HauthServer.m
//  Hauth
//
//  Created by Rizwan Sattar on 11/7/15.
//  Copyright © 2015 Rizwan Sattar. All rights reserved.
//

#import "HauthServer.h"
#import "HauthCommon.h"

@interface HauthServer () <NSNetServiceDelegate>

@property (copy, nonatomic) NSString *displayName;
@property (copy, nonatomic) NSString *appId;
@property (assign, nonatomic) BOOL isAdvertising;

// Advertising
@property (copy, nonatomic) HauthServerRequestHandler requestHandler;
@property (strong, nonatomic) NSNetService *server;
@property (copy, nonatomic) NSString *registeredServerName;
@property (copy, nonatomic) NSString *serviceName;
@end

@implementation HauthServer

- (instancetype)initWithDisplayName:(NSString *)displayName appId:(NSString *)appId
{
    self = [super init];
    if (self) {
        self.displayName = displayName;
        self.appId = appId;
        self.serviceName = [NSString stringWithFormat:kHauthServiceNameFormat, self.appId];
    }
    return self;
}

- (void)dealloc
{
    [self stopAdvertising];
}

- (void)startAdvertisingWithRequestHandler:(HauthServerRequestHandler)requestHandler
{
    if (self.isAdvertising) {
        [self stopAdvertising];
    }

    self.requestHandler = requestHandler;

    self.server = [[NSNetService alloc] initWithDomain:@".local"
                                                              type:self.serviceName
                                                              name:self.displayName];
    self.server.includesPeerToPeer = YES;
    self.server.delegate = self;
    [self.server publishWithOptions:NSNetServiceListenForConnections];

}

- (void)stopAdvertising
{
    if (!self.isAdvertising) {
        return;
    }

    [self closeStreams];

    [self.server stop];
    self.server.delegate = nil;
    self.server = nil;
    self.registeredServerName = nil;

    self.requestHandler = nil;
}


#pragma mark - Overall Events


- (void)handleReceivedData:(NSData *)data
{
    // We send/receive information as a NSDictionary written out
    // as NSData, so convert from NSData --> NSDictionary
    NSDictionary *dict = (NSDictionary *)[NSKeyedUnarchiver unarchiveObjectWithData:data];
    [self handleIncomingRequestDictionary:dict];
}


- (void)handleIncomingRequestDictionary:(NSDictionary *)requestDict
{
    NSLog(@"Received request: \n%@", requestDict);
    NSString *displayName = requestDict[@"displayName"];
    if (self.requestHandler) {
        __weak HauthServer *_weakSelf = self;
        self.requestHandler(displayName, ^(NSData * tokenData, NSError * error) {

            NSAssert(error == nil, @"Error handling not yet implemented");

            // App has granted us some data
            if (tokenData.length) {
                NSDictionary *responseDict = @{@"tokenData" : tokenData, @"displayName" : _weakSelf.displayName};
                NSData *responseData = [NSKeyedArchiver archivedDataWithRootObject:responseDict];
                [_weakSelf sendData:responseData];
            } else {
                // Close the connection
                [_weakSelf stopAdvertising];
            }

        });
    }
}


- (void)handleStreamEnd:(NSStream *)stream
{
    [super handleStreamEnd:stream];

    [self stopAdvertising];
}


#pragma mark - NSNetServiceDelegate


- (void)netServiceDidPublish:(NSNetService *)sender
{
    self.registeredServerName = self.server.name;
}

- (void)netService:(NSNetService *)sender didAcceptConnectionWithInputStream:(NSInputStream *)inputStream outputStream:(NSOutputStream *)outputStream
{
    // Due to a bug <rdar://problem/15626440>, this method is called on some unspecified
    // queue rather than the queue associated with the net service (which in this case
    // is the main queue).  Work around this by bouncing to the main queue.
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        assert(sender == self.server);
#pragma unused(sender)
        assert(inputStream != nil);
        assert(outputStream != nil);

        assert( (self.inputStream != nil) == (self.outputStream != nil) );      // should either have both or neither

        if (self.inputStream != nil) {
            // We already have a connection, reject this one
            [inputStream open];
            [inputStream close];
            [outputStream open];
            [outputStream close];
        } else {

            // S
            [self stopAdvertising];

            // Latch the input and output sterams and kick off an open.

            self.inputStream  = inputStream;
            self.outputStream = outputStream;

            [self openStreams];
        }
    }];
}

- (void)netService:(NSNetService *)sender didNotPublish:(NSDictionary *)errorDict
// This is called when the server stops of its own accord.  The only reason
// that might happen is if the Bonjour registration fails when we reregister
// the server, and that's hard to trigger because we use auto-rename.  I've
// left an assert here so that, if this does happen, we can figure out why it
// happens and then decide how best to handle it.
{
    assert(sender == self.server);
#pragma unused(sender)
#pragma unused(errorDict)
    assert(NO);
}


#pragma mark - Streams
@end
