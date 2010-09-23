/*
 * Copyright 2010 Ronnie Kolehmainen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.cssxfire.webserver;

import com.googlecode.cssxfire.CssXFireConnector;

import java.net.Socket;
import java.net.URLDecoder;
import java.io.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class HttpRequest implements Runnable
{
    private Socket socket;

    public HttpRequest(Socket socket) throws Exception
    {
        this.socket = socket;
    }

    public void run()
    {
        try
        {
            processRequest();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception
    {
        HttpResponse response = null;

        //Get references to sockets input and output streams
        InputStream is = this.socket.getInputStream();

        //Set up input stream filter
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        //Get the request line of HTTP message
        String requestLine = br.readLine().trim();

        boolean isFireFox = false;
        String req;
        while ((req = br.readLine()) != null)
        {
            req = req.trim().toLowerCase();
            if (req.length() == 0)
            {
                break;
            }
            if (req.startsWith("user-agent") && req.contains("firefox/"))
            {
                isFireFox = true;
            }
        }

        String q = requestLine.toLowerCase();
        int six = q.indexOf(' ');
        int eix = q.indexOf(" http");
        if (eix != -1)
        {
            requestLine = requestLine.substring(six, eix).trim();
        }

        if (!isFireFox && "/files/cssxfire.xpi".equals(requestLine))
        {
            response = HttpResponse.createFileResponse("about.html");
        }
        else if (requestLine.startsWith("/files/"))
        {
            response = HttpResponse.createFileResponse(requestLine.substring("/files/".length()));
        }
        else
        {
            try
            {
                // Parse query params
                Map<String, String> params = getQueryMap(requestLine);

                // Extract the filename
                String property = params.get("property");
                String value = params.get("value");
                String selector = params.get("selector");
                boolean deleted = Boolean.parseBoolean(params.get("deleted"));

                // Notify application component
                if (property != null && value != null && selector != null)
                {
                    CssXFireConnector.getInstance().processCss(selector, property, value, deleted);
                }

                response = HttpResponse.createEmptyOkResponse();
            }
            catch (MalformedQueryException e)
            {
                response = HttpResponse.createErrorResponse(e.getMessage());
            }
        }

        response.sendResponse(this.socket.getOutputStream());

        //Close the streams
        br.close();
        socket.close();
    }


     private static Map<String, String> getQueryMap(String query) throws MalformedQueryException
     {
         Map<String, String> map = new HashMap<String, String>();
         try
         {
             int i = query.indexOf('?');
             if (i == -1)
             {
                 return map;
             }
             query = query.substring(i + 1);
             String[] params = query.split("&");
             for (String param : params)
             {
                 int ix = param.indexOf('=');
                 String name = param.substring(0, ix);
                 name = URLDecoder.decode(name, "utf-8");
                 String value = param.substring(ix + 1);
                 value = URLDecoder.decode(value, "utf-8");
                 map.put(name, value);
             }
             return map;
         }
         catch (Exception e)
         {
             throw new MalformedQueryException(query);
         }
     }

    public static class MalformedQueryException extends Exception
    {
        private MalformedQueryException(String message)
        {
            super(message);
        }

        @Override
        public String toString()
        {
            return getClass().getSimpleName() + ": " + getMessage();
        }
    }
}
