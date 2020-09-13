package com.wang.server;

import com.sun.net.httpserver.HttpServer;
import com.wang.handler.CreateHandler;
import com.wang.handler.DeleteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;

@Component
public class httpServer {
    @Autowired
    private CreateHandler createHandler;

    @Autowired
    private DeleteHandler deleteHandler;

    public void run() throws IOException{
        HttpServer createServer = HttpServer.create(new InetSocketAddress(8080), 0);
        createServer.createContext("/ManageNodeServer/Call/create", createHandler);
        createServer.start();

        HttpServer deleteServer = HttpServer.create(new InetSocketAddress(8081), 0);
        deleteServer.createContext("/ManageNodeServer/Call/delete", deleteHandler);
        deleteServer.start();
    }
}
