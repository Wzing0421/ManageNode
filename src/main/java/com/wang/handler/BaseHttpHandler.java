package com.wang.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.wang.enumstatus.EnumHttpStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

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

    private void handleGet(HttpExchange httpExchange) throws Exception{
        return;
    }

    private void handlePost(HttpExchange httpExchange) throws Exception{
        Map<String, String> parameters = parsePostParameters(httpExchange);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
        Integer nodeId = doHandlePost(parameters);
        //responseCode 是状态码
        int responseCode = 200;
        handleResponse(httpExchange, Integer.toString(nodeId), responseCode);
    }

    private void handlePut(HttpExchange httpExchange) throws Exception{
        Map<String, String> parameters = parsePutParameters(httpExchange);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
        }
        EnumHttpStatus enumHttpStatus = doHandlePut(parameters);
        handleResponse(httpExchange, enumHttpStatus.getDescription(), enumHttpStatus.getStatus());
    }


    private Map<String, String> parsePutParameters(HttpExchange exchange)
            throws UnsupportedEncodingException {

        Map<String, String> parameters = new HashMap<String, String>();
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
        return parameters;
    }

    @SuppressWarnings("unchecked")
    private void parseQuery(String query, Map<String, String> parameters)
            throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");

            for (String pair : pairs) {
                String param[] = pair.split("[=]");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0],
                            System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1],
                            System.getProperty("file.encoding"));
                }

                parameters.put(key, value);

            }
        }
    }

    private Map<String, String> parsePostParameters(HttpExchange exchange)
            throws UnsupportedEncodingException{
        Map<String, String> parameters = new HashMap<String, String>();
        //获取请求方的IP
        String RemoteNodeAddr = exchange.getRemoteAddress().getHostString();
        //System.out.println(RemoteNodeAddr);
        parameters.put("RemoteNodeIp", RemoteNodeAddr);
        URI requestedUri = exchange.getRequestURI();
        String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
        return parameters;
    }
    /**
     * 处理响应
     *
     * @param httpExchange
     * @param responsetext
     * @throws Exception
     */
    private void handleResponse(HttpExchange httpExchange, String responsetext, int responseCode) throws Exception {
        //生成html
        StringBuilder responseContent = new StringBuilder();
        responseContent.append("<html>")
                .append("<body>")
                .append(responsetext)
                .append("</body>")
                .append("</html>");
        String responseContentStr = responseContent.toString();
        byte[] responseContentByte = responseContentStr.getBytes("utf-8");

        //设置响应头，必须在sendResponseHeaders方法之前设置！
        httpExchange.getResponseHeaders().add("Content-Type:", "text/html;charset=utf-8");

        //设置 响应码 和响应体长度
        httpExchange.sendResponseHeaders(responseCode, responseContentByte.length);

        OutputStream out = httpExchange.getResponseBody();
        out.write(responseContentByte);
        out.flush();
        out.close();
    }

    abstract protected EnumHttpStatus doHandlePut(Map<String, String> parameters) throws Exception;

    abstract protected Integer doHandlePost(Map<String, String> parameters) throws Exception;

}