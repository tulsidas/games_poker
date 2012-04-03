package poker.server;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import server.GameServiceManager;
import server.HttpHandler;

public class PokerServer {
   private static final int SERVER_PORT = 8860;

   private static final int HTTP_PORT = 8861;

   private static final int JMX_PORT = 8862;

   private static final int RMI_PORT = 8863;

   private static Logger log = LoggerFactory.getLogger(PokerServer.class);

   public static void main(String[] args) throws Throwable {
      IoAcceptor acceptor = new SocketAcceptor();

      SocketSessionConfig ssc = (SocketSessionConfig) acceptor
            .getDefaultConfig().getSessionConfig();
      ssc.setReuseAddress(true);

      PokerSessionHandler server = new PokerSessionHandler();

      if (args.length == 1 && "noconnect".equals(args[0])) {
         server.setNoConnect();
      }

      // JMX CONFIGURATION
      GameServiceManager serviceManager = new GameServiceManager(acceptor,
            server);
      serviceManager.startCollectingStats(1000);
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName bean = new ObjectName("PokerServer:type=PokerServiceManager");
      mbs.registerMBean(serviceManager, bean);

      LocateRegistry.createRegistry(JMX_PORT);
      Map<String, Object> env = new HashMap<String, Object>();
      env.put("com.sun.management.jmxremote.authenticate", false);
      env.put("com.sun.management.jmxremote.ssl", false);

      JMXServiceURL url = new JMXServiceURL("service:jmx:rmi://localhost:"
            + RMI_PORT + "/jndi/rmi://localhost:" + JMX_PORT + "/jmxrmi");
      JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
            url, env, mbs);
      cs.start();

      // JMX CONFIGURATION
      acceptor.bind(new InetSocketAddress(SERVER_PORT), server);
      acceptor.bind(new InetSocketAddress(HTTP_PORT), new HttpHandler(server));

      log.info("Listening on ports\n\tServer\t" + SERVER_PORT + "\n\tHTTP\t"
            + HTTP_PORT + "\n\tJMX\t" + JMX_PORT + "\n\tRMI\t" + RMI_PORT);
   }
}