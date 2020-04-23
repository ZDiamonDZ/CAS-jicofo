import cas.servlet.HelloHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class CASHandler
{
    public static Server createServer(int port)
    {
        Server server = new Server(port);

        ContextHandler context = new ContextHandler("/");
        context.setContextPath("/");
        context.setHandler(new HelloHandler("Root Hello"));

        ContextHandler contextFR = new ContextHandler("/fr");
        contextFR.setHandler(new HelloHandler("Bonjour"));

        ContextHandler contextIT = new ContextHandler("/cas/login   ");
        contextIT.setHandler(new HelloHandler("CAS","ZopaZ"));

        ContextHandler contextV = new ContextHandler("/");
        contextV.setVirtualHosts(new String[]{"127.0.0.2"});
        contextV.setHandler(new HelloHandler("Virtual Hello"));

        ContextHandlerCollection contexts = new ContextHandlerCollection(
                context, contextFR, contextIT, contextV
        );

        server.setHandler(contexts);
        return server;
    }

    public static void main(String[] args) throws Exception
    {
        //int port = ExampleUtil.getPort(args, "jetty.http.port", 8080);
        Server server = createServer(8080);
        server.start();
        server.dumpStdErr();
        server.join();
    }
}