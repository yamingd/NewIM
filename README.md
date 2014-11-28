NewIM
=====

This is a simple NewIM, created for Mobile App.
using Netty, Zookeeper and Protobuf

### Gateway
this node receives connections from Mobile and holds them, receives message from Mobile and then deliver the message to Chat Node.
It uses zookeeper to store connection info.

### Chat
this node routes message to all those mobile client in P2P, P2G way.
it uses Zookeeper to get connections on Gateway node.

### Protobuf Definition
```
message ChatMessage {
  required string boxid = 1;
  required int32 group = 2 [default = 0];
  optional string uuid = 3;
  optional string sender = 4;
  optional string receiver = 5;
  optional string body = 6;
  enum MessageType {
      SmallText = 0;
      LongText = 1;
      AUDIO = 2;
      IMAGE = 3;
      VIDEO = 4;
      FILE = 5;
      LINK = 6;
      SYNC = 7;
  }
  required MessageType mtype = 7 [default = SYNC];

  enum ChatOp {
      JOIN = 0;
      QUIT = 1;
      CHAT = 2;
      ACK = 3;
  }
  required ChatOp op = 8 [default = ACK];
}

message ChatStatus {
  required string sender = 1;
  required int32 syncMark = 2;
}

message ExchangeMessage {
  required string messageId = 1;
  optional string message = 2;
  optional string chatPath = 3;
  optional string chatRoomId = 4;
  optional string msgPath = 5;
  repeated int32 channelId = 6;
}

```

### TODO
1. receive image, audio, video file from clients
2. support node monitor