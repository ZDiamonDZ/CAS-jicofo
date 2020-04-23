package org.jitsi.jicofo.auth;

import jdk.internal.org.xml.sax.InputSource;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.jitsi.utils.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

public class CASLoginHandler
            extends AbstractHandler

{
    String CAS_SERVER_URL = "https://cas.example.com/";
    String SERVICE_URL = "https%3A%2F%2Ftestmeet.citytok.one%2F";

    /**
     * The logger instance used by CAS handler.
     */
    private static final Logger logger
            = Logger.getLogger(CASLoginHandler.class);

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        if (logger.isDebugEnabled())
        {
            dumpRequestInfo(baseRequest);
        }
        /**
         * Last session ID< granted by CAS server;
         */

        String jsessionID=null;

        /**
         * Handle request, and try to find COOKIE with last CAS session ID.
         */

        if (httpServletRequest.getCookies() != null)
        {
            Cookie[] cookies = httpServletRequest.getCookies();
            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie =  cookies[i];
                if (cookie.getName().equals("CASTGC"))
                {
                  jsessionID = cookie.getValue();
                }
            }
        }

        if (jsessionID != null) // Finded a cookie with session ID, validate it before login
        {
             if (validateCasSession(jsessionID))
            {
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            }
            else
                {
                    httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    httpServletResponse.sendRedirect(loginUrl());
                }
            baseRequest.setHandled(true);
        }
        else // Dont find cookie with session ID, redirect to CAS server
        {
           httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
           httpServletResponse.sendRedirect(loginUrl());
           baseRequest.setHandled(true);
        }
    }

    private boolean validateCasSession(String jsessionID)
    {
        try {
            URL url = new URL(validateUrl(jsessionID));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputStream is = connection.getInputStream();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            String inputLine;

            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Document doc = ((DocumentBuilder) db).parse(String.valueOf(response));

            // simple validate method, if we get in XML success tag than true.
            NodeList nlist = doc.getElementsByTagName("cas:authenticationSuccess");
                if (nlist != null)
                    return true;
                else
                    return  false;
        }
        catch (IOException | ParserConfigurationException | SAXException exc) {
            System.out.println(exc.toString());
        }
        return false;
    }

    private String validateUrl(String jsessionID) {
        return CAS_SERVER_URL+"validate?serviceValidate="+SERVICE_URL+"&ticket="+jsessionID;
    }

    private String loginUrl() {
        return CAS_SERVER_URL+"login?service="+SERVICE_URL;
    }

    @Override
    public String dumpSelf() {
        return null;
    }

    /**
     * Method prints to the log debug information about HTTP request
     * @param request <tt>HttpServletRequest</tt> for which debug info will
     *                be logged.
     */
    private void dumpRequestInfo(HttpServletRequest request)
    {
        logger.debug(request.getRequestURL());
        logger.debug("REMOTE USER: " + request.getRemoteUser());
        logger.debug("Headers: ");
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements())
        {
            String headerName = headers.nextElement();
            logger.debug(headerName + ": " + request.getHeader(headerName));
        }
        logger.debug("Attributes: ");
        Enumeration<String> attributes = request.getAttributeNames();
        while (attributes.hasMoreElements())
        {
            String attributeName = attributes.nextElement();
            logger.debug(
                    attributeName + ": " + request.getAttribute(attributeName));
        }
    }
}
