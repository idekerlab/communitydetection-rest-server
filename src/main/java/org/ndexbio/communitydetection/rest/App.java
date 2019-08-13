package org.ndexbio.communitydetection.rest;


import ch.qos.logback.classic.Level;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.ndexbio.communitydetection.rest.services.Configuration;
import org.ndexbio.communitydetection.rest.services.CommunityDetectionHttpServletDispatcher;


/**
 *
 * @author churas
 */
public class App {
    
    static Logger _logger = LoggerFactory.getLogger(App.class);

    public static final String DESCRIPTION = "\nNDEx Community Detection REST service\n\n"
            + "For usage information visit:  https://github.com/coleslaw481/communitydetection-rest-server\n\n";
    
    /**
     * Sets logging level valid values DEBUG INFO WARN ALL ERROR
     */
    public static final String RUNSERVER_LOGLEVEL = "runserver.log.level";

    /**
     * Sets log directory for embedded Jetty
     */
    public static final String RUNSERVER_LOGDIR = "runserver.log.dir";
    
    /**
     * Sets port for embedded Jetty
     */
    public static final String RUNSERVER_PORT = "runserver.port";
        
    /**
     * Sets context path for embedded Jetty
     */
    public static final String RUNSERVER_CONTEXTPATH = "runserver.contextpath";
    
    public static final String MODE = "mode";
    public static final String CONF = "conf";    
    public static final String CREATEDB_MODE = "createdb";
    public static final String EXAMPLE_CONF_MODE = "exampleconf";
    public static final String EXAMPLE_DBRES_MODE = "exampledbresults";
    public static final String RUNSERVER_MODE = "runserver";
    
    public static final String SUPPORTED_MODES = CREATEDB_MODE +
                                                    ", " + EXAMPLE_CONF_MODE +
                                                    ", " + EXAMPLE_DBRES_MODE +
                                                    ", " + RUNSERVER_MODE;
    
    public static void main(String[] args){

        final List<String> helpArgs = Arrays.asList("h", "help", "?");
        try {
            OptionParser parser = new OptionParser() {

                {
                    accepts(MODE, "Mode to run. Supported modes: " + SUPPORTED_MODES).withRequiredArg().ofType(String.class).required();
                    accepts(CONF, "Configuration file")
                            .withRequiredArg().ofType(String.class);
                    acceptsAll(helpArgs, "Show Help").forHelp();
                }
            };
            
            OptionSet optionSet = null;
            try {
                optionSet = parser.parse(args);
            } catch (OptionException oe) {
                System.err.println("\nThere was an error parsing arguments: "
                        + oe.getMessage() + "\n\n");
                parser.printHelpOn(System.err);
                System.exit(1);
            }

            //help check
            for (String helpArgName : helpArgs) {
                if (optionSet.has(helpArgName)) {
                    System.out.println(DESCRIPTION);
                    parser.printHelpOn(System.out);
                    System.exit(2);
                }
            }
            
            String mode = optionSet.valueOf(MODE).toString();

            if (mode.equals(EXAMPLE_CONF_MODE)){
                System.out.println(generateExampleConfiguration());
                System.out.flush();
                return;
            }
            if (mode.equals(EXAMPLE_DBRES_MODE)){
                System.out.println("TODO");
                System.out.flush();
                return;
            }
            
            if (mode.equals(RUNSERVER_MODE)){
                Configuration.setAlternateConfigurationFile(optionSet.valueOf(CONF).toString());
                Properties props = getPropertiesFromConf(optionSet.valueOf(CONF).toString());
                ch.qos.logback.classic.Logger rootLog = 
        		(ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
                rootLog.setLevel(Level.toLevel(props.getProperty(App.RUNSERVER_LOGLEVEL, "INFO")));

                String logDir = props.getProperty(App.RUNSERVER_LOGDIR, ".");
                RolloverFileOutputStream os = new RolloverFileOutputStream(logDir + File.separator + "communitydetection_yyyy_mm_dd.log", true);
		
		
                final int port = Integer.valueOf(props.getProperty(App.RUNSERVER_PORT, "8081"));
                System.out.println("\nSpinning up server for status invoke: http://localhost:" + Integer.toString(port) + "/communitydetection/v1/status\n\n");
                System.out.flush();
                
                //We are creating a print stream based on our RolloverFileOutputStream
		PrintStream logStream = new PrintStream(os);

                //We are redirecting system out and system error to our print stream.
		System.setOut(logStream);
		System.setErr(logStream);

                final Server server = new Server(port);

                final ServletContextHandler webappContext = new ServletContextHandler(server, props.getProperty(App.RUNSERVER_CONTEXTPATH, "/"));
                
                HashMap<String, String> initMap = new HashMap<>();
                initMap.put("resteasy.servlet.mapping.prefix",
                            Configuration.APPLICATION_PATH);
                initMap.put("javax.ws.rs.Application", "org.ndexbio.communitydetection.rest.CommunityDetectionApplication");
                final ServletHolder restEasyServlet = new ServletHolder(
                     new CommunityDetectionHttpServletDispatcher());
                
                restEasyServlet.setInitOrder(1);
                restEasyServlet.setInitParameters(initMap);
                webappContext.addServlet(restEasyServlet,
                                         Configuration.APPLICATION_PATH + "/*");
                webappContext.addFilter(CorsFilter.class,
                                        Configuration.APPLICATION_PATH + "/*", null);
                ContextHandlerCollection contexts = new ContextHandlerCollection();
                contexts.setHandlers(new Handler[] { webappContext });
 
                server.setHandler(contexts);
                
                server.start();
                Log.getRootLogger().info("Embedded Jetty logging started.", new Object[]{});
	    
                System.out.println("Server started on port " + port);
                server.join();
                return;
            }
            
        }
        catch(Exception ex){
            ex.printStackTrace();
        }

    }
    
    public static Properties getPropertiesFromConf(final String path) throws IOException, FileNotFoundException {
        Properties props = new Properties();
        props.load(new FileInputStream(path));
        return props;
    }
   
    /**
     * Generates example Configuration file writing to standard out
     * @throws Exception 
     */
    public static String generateExampleConfiguration() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("# Example configuration file for Enrichment service\n\n");
        
        sb.append("# Sets Enrichment database directory\n");
        sb.append(Configuration.DATABASE_DIR + " = /tmp/db\n\n");
        
        sb.append("# Sets Enrichment task directory where results from queries are stored\n");
        sb.append(Configuration.TASK_DIR + " = /tmp/tasks\n\n");
        
        sb.append("# Sets HOST URL prefix (value is prefixed to Location header when query is invoked. Can be commented out)\n");
        sb.append("# " + Configuration.HOST_URL + " = http://ndexbio.org\n");
        
        sb.append("# Sets directory where log files will be written for Jetty web server\n");
        sb.append(App.RUNSERVER_LOGDIR + " = /tmp/logs\n\n");
        
        sb.append("# Sets port Jetty web service will be run under\n");
        sb.append(App.RUNSERVER_PORT + " = 8081\n\n");
        
        sb.append("# sets Jetty Context Path for Enrichment\n");
        sb.append(App.RUNSERVER_CONTEXTPATH + " = /\n\n");
        
        sb.append("# Valid log levels DEBUG INFO WARN ERROR ALL\n");
        sb.append(App.RUNSERVER_LOGLEVEL + " = INFO\n");

        return sb.toString();
    }
}
