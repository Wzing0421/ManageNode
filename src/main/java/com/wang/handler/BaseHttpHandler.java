package com.wang.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * base http handler for http client request
 */
public abstract class BaseHttpHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException{
        try{
            switch (httpExchange.getRequestMethod()){
                case "GET":
                    handleGet(httpExchange);
                    break;
                case "POST":
                    handlePost(httpExchange);
                    break;
                case "PUT":
                    handlePut(httpExchange);
                    break;
                default:
                    break;
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    };

    protected abstract void handleGet(HttpExchange httpExchange) throws Exception;

    protected abstract void handlePost(HttpExchange httpExchange) throws Exception;

    protected abstract void handlePut(HttpExchange httpExchange) throws Exception;

}