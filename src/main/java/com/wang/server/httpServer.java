package com.wang.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.wang.handler.CreateHandler;
import com.wang.handler.DeleteHandler;
import com.wang.handler.HeartBeatHandler;
import com.wang.handler.NodeRegisterHandler;
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

    @Autowired
    private NodeRegisterHandler nodeRegisterHandler;

    @Autowired
    private HeartBeatHandler heartBeatHandler;

    public void run() throws IOException{
        HttpServer createServer = HttpServer.create(new InetSocketAddress(8080), 0);
        createServer.createContext("/ManageNodeServer/Call/create", createHandler);


        createServer.createContext("/ManageNodeServer/Call/delete", deleteHandler);
        createServer.createContext("/ManageNodeServer/Register", nodeRegisterHandler);
        createServer.createContext("/ManageNodeServer/HeartBeat", heartBeatHandler);
        createServer.start();

        /*
        HttpServer deleteServer = HttpServer.create(new InetSocketAddress(8081), 0);
        deleteServer.createContext("/ManageNodeServer/Call/delete", deleteHandler);
        deleteServer.start();

        HttpServer registerServer = HttpServer.create(new InetSocketAddress(8082), 0);
        registerServer.createContext("/ManageNodeServer/Register", nodeRegisterHandler);
        registerServer.start();

        HttpServer heartBeatServer = HttpServer.create(new InetSocketAddress(8083), 0);
        heartBeatServer.createContext("/ManageNodeServer/HeartBeat", heartBeatHandler);
        heartBeatServer.start();*/

    }
}
