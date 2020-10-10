package com.wang.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.wang.handler.*;
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
    private ConfigHandler configHandler;

    @Autowired
    private NodeRegisterHandler nodeRegisterHandler;

    @Autowired
    private HeartBeatHandler heartBeatHandler;

    public void run() throws IOException{
        HttpServer Server = HttpServer.create(new InetSocketAddress(8080), 0);
        Server.createContext("/ManageNodeServer/Call/create", createHandler);
        Server.createContext("/ManageNodeServer/Call/config", configHandler);
        Server.createContext("/ManageNodeServer/Call/delete", deleteHandler);
        Server.createContext("/ManageNodeServer/Register", nodeRegisterHandler);
        Server.createContext("/ManageNodeServer/HeartBeat", heartBeatHandler);
        Server.start();

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
