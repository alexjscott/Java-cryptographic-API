# Java-cryptographic-API

## Client-server architecture, public and private keys
The system consists of a client and a server Java program, and they must be named Client.java and Server.java respectively. They are started by running the commands:
```bash
java Server port
```
```bash
java Client host port userid
```

specifying the hostname and port number of the server, and the userid of the client.

The server program is always running once started, and listens for incoming connections at the port specified. When a client is connected, the server handles the request, then waits for the next request (i.e., the server never terminates). For simplicity, you can assume that only one client will connect to the server at any one time.

Each user has a unique userid, which is a simple string like alice, bob etc. Each user is associated with a pair of RSA public and private keys, with filenames that have .pub or .prv after the userid, respectively. Thus the key files are named alice.pub, bob.prv, etc. These keys are generated separately by a program RSAKeyGen.java. More details are in the comment of that program.

It is assumed that the server already has the public keys of all legitimate users, and each client program user already has their own private key as well as the public keys of anyone to whom they want to send secret messages. They obtained these keys via some offline method not described here, prior to the execution of the client and server programs. The client and server programs never create any new keys.

All the key files are in the same folder where the client/server programs run from. They must not be read from other folders. Your programs must not require keys that they are not supposed to have.
