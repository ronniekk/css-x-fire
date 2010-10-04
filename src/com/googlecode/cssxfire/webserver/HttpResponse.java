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

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class HttpResponse
{
    private static final String CRLF = "\r\n";
    private static final String CONTENT_TYPE_TEXT_PLAIN = "Content-Type: text/plain; charset=utf-8";
    private static final byte[] RESPONSE_EMPTY = new  byte[0];
    private static final String STATUS_200_OK = "HTTP/1.0 200 OK";

    public static HttpResponse createEmptyOkResponse()
    {
        return new HttpResponse();
    }

    public static HttpResponse createFileResponse(String filename)
    {
        InputStream is = HttpResponse.class.getResourceAsStream("/com/googlecode/cssxfire/www/" + filename);
        if (is != null)
        {
            return new FileResponse(filename, is);
        }

        return new Response404(filename);
    }

    public static HttpResponse createErrorResponse(String errorMessage)
    {
        return new Response500(errorMessage);
    }

    private HttpResponse() {}

    protected String getStatusLine()
    {
        return STATUS_200_OK;
    }

    protected String getContentTypeLine()
    {
        return CONTENT_TYPE_TEXT_PLAIN;
    }

    protected InputStream getResponseStream()
    {
        return new ByteArrayInputStream(RESPONSE_EMPTY);
    }

    public void sendResponse(OutputStream socketOutputStream) throws IOException
    {
        DataOutputStream os = new DataOutputStream(socketOutputStream);

        // Send the status line.
        os.writeBytes(getStatusLine());
        os.writeBytes(CRLF);

        // Send the content type line.
        os.writeBytes(getContentTypeLine());
        os.writeBytes(CRLF);

        // Send a blank line to indicate the end of the header lines.
        os.writeBytes(CRLF);

        InputStream is = getResponseStream();
        byte[] buffer = new byte[1024];

        int bytes;

        // Copy requested file into the socket's output stream.
        while ((bytes = is.read(buffer)) != -1)
        {
            os.write(buffer, 0, bytes);
        }
    }

    private static class FileResponse extends HttpResponse
    {
        private String filename;
        private InputStream is;

        public FileResponse(String filename, InputStream is)
        {
            this.filename = filename;
            this.is = is;
        }

        @Override
        protected InputStream getResponseStream()
        {
            return is;
        }

        @Override
        protected String getContentTypeLine()
        {
            // a massive list of content types...
            if (filename.endsWith(".xpi"))
            {
                return "Content-Type: application/x-xpinstall";
            }
            if (filename.endsWith(".png"))
            {
                return "Content-Type: image/png";
            }
            if (filename.endsWith(".html"))
            {
                return "Content-Type: text/html; charset=utf-8";
            }
            if (filename.endsWith(".js"))
            {
                return "Content-Type: application/x-javascript";
            }

            return "Content-Type: application/octet-stream";
        }
    }

    private static class Response404 extends HttpResponse
    {
        private String filename;

        public Response404(String filename)
        {
            this.filename = filename;
        }

        @Override
        protected String getStatusLine()
        {
            return "HTTP/1.0 404 File not found: " + filename;
        }
    }

    private static class Response500 extends HttpResponse
    {
        private String message;

        public Response500(String message)
        {
            this.message = message;
        }

        @Override
        protected String getStatusLine()
        {
            return "HTTP/1.0 500 Internal server error: " + message;
        }
    }
}
