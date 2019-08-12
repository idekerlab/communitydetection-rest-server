package org.ndexbio.communitydetection.rest.engine;

import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.ndexbio.communitydetection.rest.services.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
public class BasicEnrichmentEngineFactory {
    
    static Logger _logger = LoggerFactory.getLogger(BasicEnrichmentEngineFactory.class);

    private String _dbDir;
    private String _taskDir;
    
    /**
     * Temp directory where query results will temporarily be stored.
     * @param tmpDir 
     */
    public BasicEnrichmentEngineFactory(Configuration config){
        
        _dbDir = config.getDatabaseDirectory();
        _taskDir = config.getTaskDirectory();
    }
    
    
    /**
     * Creates CommunityDetectionEngine
     * @return 
     */
    public CommunityDetectionEngine getCommunityDetectionEngine() throws CommunityDetectionException {
        CommunityDetectionEngineImpl enricher = new CommunityDetectionEngineImpl(_dbDir,
                _taskDir);
        return enricher;
    }
}
