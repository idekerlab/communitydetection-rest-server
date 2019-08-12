/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ndexbio.communitydetection.rest.services;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.servlet.ServletException;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.ndexbio.communitydetection.rest.engine.BasicEnrichmentEngineFactory;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ndexbio.communitydetection.rest.engine.CommunityDetectionEngine;
/**
 *
 * @author churas
 */
public class CommunityDetectionHttpServletDispatcher extends HttpServletDispatcher {
    
    static Logger _logger = LoggerFactory.getLogger(CommunityDetectionHttpServletDispatcher.class.getSimpleName());

    private static String _version = "";
    private static String _buildNumber = "";
    private CommunityDetectionEngine _enrichmentEngine;
    private Thread _enrichmentEngineThread;
    
    
    public CommunityDetectionHttpServletDispatcher() throws CommunityDetectionException{
        super();
        _logger.info("In constructor");
        createAndStartEnrichmentEngine();

    }
    
    protected void createAndStartEnrichmentEngine() throws CommunityDetectionException {
        
        try {
            BasicEnrichmentEngineFactory fac = new BasicEnrichmentEngineFactory(Configuration.getInstance());
            _logger.debug("Creating CommunityDetection Engine from factory");
            _enrichmentEngine = fac.getCommunityDetectionEngine();
            _logger.debug("Starting CommunityDetection Engine thread");
            _enrichmentEngineThread = new Thread(_enrichmentEngine);
            _enrichmentEngineThread.start();
            _logger.debug("CommunityDetection Engine thread running id => " + Long.toString(_enrichmentEngineThread.getId()));
            Configuration.getInstance().setCommunityDetectionEngine(_enrichmentEngine);
        }
        catch(CommunityDetectionException ex){
            _logger.error("Unable to start enrichment engine", ex);
            throw ex;
        }
    }

    @Override
    public void init(javax.servlet.ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        _logger.info("Entering init()");
        updateVersion();
        _logger.info("Exiting init()");
    }
    
    @Override
    public void destroy() {
        super.destroy();
        _logger.info("In destroy()");
        if (_enrichmentEngine != null){
            _enrichmentEngine.shutdown();
            _logger.info("Waiting for enrichment engine to shutdown");
            try {
                if (_enrichmentEngineThread != null){
                    _enrichmentEngineThread.join(10000);
                }
            }
            catch(InterruptedException ie){
                _logger.error("Caught exception waiting for enrichment engine to exit", ie);
            }
        } else {
            _logger.error("No enrichment engine found to destroy");
        
        }
    }
    
    /**
     * Reads /META-INFO/MANIFEST.MF for version and build information
     * setting _version and _buildNumber to those values if found.
     */
    private void updateVersion(){
        String jarPath = CommunityDetectionHttpServletDispatcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        JarFile jar = null;
        try {
            jar = new JarFile(jarPath);
            Manifest manifest = jar.getManifest();
           
            Attributes aa = manifest.getMainAttributes();	

            String ver = aa.getValue("NDExEnrichment-Version");
            String bui = aa.getValue("NDExEnrichment-Build"); 
            _logger.info("NDEx-Version: " + ver + ",Build:" + bui);
            if (_buildNumber != null && _version != null){
                _buildNumber= bui.substring(0, 5);
                _version = ver;
            }
        } catch (IOException e) {
            _logger.error("failed to read MANIFEST.MF", e);
        } finally {
            
            if (jar != null){
                try {
                    jar.close();
                } catch(IOException io){
                    _logger.warn("Not a show stopper, but caught IOException closing jar", io);
                }
            }
        }   
        
    }
    
    public static String getVersion(){
        return _version;
    }
    
    public static String getBuildNumber(){
        return _buildNumber;
    }
}
