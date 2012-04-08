package org.openspaces.itest.session.jetty;

import junit.framework.Assert;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.openspaces.core.GigaSpace;
import org.openspaces.jee.sessions.jetty.GigaSessionIdManager;
import org.openspaces.jee.sessions.jetty.GigaSessionManager;
import org.openspaces.jee.sessions.jetty.SessionData;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public class JettySessionManagerTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;
    protected GigaSessionManager sessionManager;
    protected GigaSessionIdManager idManager;
    protected SessionHandler handler;
    protected Server server = new Server();
    private TestHttpRequest request;
    private static HttpSession session;

    public JettySessionManagerTests() {
        setPopulateProtectedVariables(true);
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/session/jetty/space-context.xml"};
    }


    protected void onSetUp() throws Exception {
        server = new Server();
        idManager = new GigaSessionIdManager(server);
        sessionManager = new GigaSessionManager();
        sessionManager.setSpace(gigaSpace.getSpace());
        sessionManager.setMaxInactiveInterval(3);
        handler = new SessionHandler(sessionManager);
        idManager.setWorkerName("node0");
        sessionManager.setIdManager(idManager);
        sessionManager.setRefreshCookieAge(10000);
        sessionManager.setScavengePeriod(500);
        ContextHandler context = new ContextHandler();
        sessionManager.setSessionHandler(handler);
        server.setHandler(context);
        context.setHandler(handler);
        server.start();
        request = new TestHttpRequest();


    }

    protected void onTearDown() throws Exception {
        session = null;
        sessionManager.stop();
        idManager.stop();
        server.stop();
        gigaSpace.clear(null);
    }

    public void testSetAttribute() throws Exception {
        session = sessionManager.newHttpSession(request);
        session.setAttribute("foo", 1);

        SessionData session = gigaSpace.read(new SessionData());
        Assert.assertEquals(getAttributeMap(session).get("foo"), 1);
        Assert.assertEquals(getAttributeMap(session).size(), 1);
    }

    public void testSetNullAttribute() throws Exception {
        session = sessionManager.newHttpSession(request);
        session.setAttribute("foo", null);

        SessionData sessionData = gigaSpace.read(new SessionData());
        Assert.assertNull(getAttributeMap(sessionData).get("foo"));
        Assert.assertEquals(getAttributeMap(sessionData).size(), 0);
    }

    public void testGetNullAttribute() throws Exception {
        session = sessionManager.newHttpSession(request);
        session.setAttribute("foo", 11);

        Assert.assertNull(session.getAttribute(null));

        SessionData sessionData = gigaSpace.read(new SessionData());
        Assert.assertEquals(getAttributeMap(sessionData).get("foo"), 11);
        Assert.assertEquals(getAttributeMap(sessionData).size(), 1);
    }

    public void testGetAttribute() throws Exception {
        session = sessionManager.newHttpSession(request);
        session.setAttribute("bar", 2);

        SessionData sessionData = gigaSpace.read(new SessionData());
        Assert.assertEquals(getAttributeMap(sessionData).get("bar"), 2);
        Assert.assertEquals(getAttributeMap(sessionData).size(), 1);

        Assert.assertEquals(2, session.getAttribute("bar"));
    }

    private Map<String,Object> getAttributeMap(SessionData session) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method myMethod = session.getClass().getDeclaredMethod("getAttributeMap");
        myMethod.setAccessible(true);
       return  (Map) myMethod.invoke(session);
    }

    static class TestHttpRequest implements HttpServletRequest {


        @Override
        public String getAuthType() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Cookie[] getCookies() {
            return new Cookie[0];  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public long getDateHeader(String s) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getHeader(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Enumeration<String> getHeaders(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getIntHeader(String s) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getMethod() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getPathInfo() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getPathTranslated() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getContextPath() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getQueryString() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getRemoteUser() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isUserInRole(String s) {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Principal getUserPrincipal() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getRequestedSessionId() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getRequestURI() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public StringBuffer getRequestURL() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getServletPath() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public HttpSession getSession(boolean b) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public HttpSession getSession() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void login(String s, String s1) throws ServletException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void logout() throws ServletException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Part getPart(String s) throws IOException, ServletException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getAttribute(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getCharacterEncoding() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getContentLength() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getContentType() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getParameter(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String[] getParameterValues(String s) {
            return new String[0];  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getProtocol() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getScheme() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getServerName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getServerPort() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getRemoteAddr() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getRemoteHost() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void setAttribute(String s, Object o) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void removeAttribute(String s) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Locale getLocale() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Enumeration<Locale> getLocales() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isSecure() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getRealPath(String s) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getRemotePort() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getLocalName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getLocalAddr() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getLocalPort() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public ServletContext getServletContext() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isAsyncStarted() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean isAsyncSupported() {
            return false;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public AsyncContext getAsyncContext() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public DispatcherType getDispatcherType() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

}

