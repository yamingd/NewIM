NewIM
=====

This is a simple NewIM, created for Mobile App.
using Netty, Zookeeper and Protobuf

### Gateway
this node receives connections from Mobile and holds them, receives message from Mobile and then deliver the message to Chat Node.
It uses zookeeper to store connection info.

### Chat
this node routes message to all those mobile client in P2P or P2G way.
it uses Zookeeper to get connections on Gateway node.

