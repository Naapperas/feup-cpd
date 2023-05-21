# CPD Assing 2

## 1. Introduction

In this assignment we were tasked with the inception and development of a Server/Client system for a game company, without using built-in concurrency libraries. The system should be able to handle multiple clients and multiple games at the same time, and should be able to handle the disconnection of clients without crashing.

## 2. Design

Both the server and the client are based on an event queue.

## 3. Running

### 3.1 Server

To run the server, simply run the following command:

```bash
./gradlew run --args="server <port> <number of concurrent games> <lobby size> <user queue (normal|ranked)>"
```

### 3.2 Client

To run the clients, simply run the following command:

```bash
./gradlew run --args="client <host> <port>"
```
