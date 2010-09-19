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


import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class SimpleWebServer implements Runnable
{
    public static final int DEFAULT_PORT = 6776;
    private ServerSocket listenSocket;

    public SimpleWebServer() throws IOException
    {
        this(DEFAULT_PORT);
    }

    public SimpleWebServer(int port) throws IOException
    {
        listenSocket = new ServerSocket(port);
    }

    public void run()
    {
        try
        {
            //Process HTTP service requests in an infinite loop
            while(true)
            {
                //listen for TCP connection request
                //Construct an object to process the HTTP request message
                HttpRequest request = new HttpRequest(listenSocket.accept());
                Thread thread = new Thread(request);
                thread.start();
            }
        }
        catch (Exception e)
        {
            // ignore for now
        }
    }

    public void stop() throws IOException
    {
        listenSocket.close();
    }
}

