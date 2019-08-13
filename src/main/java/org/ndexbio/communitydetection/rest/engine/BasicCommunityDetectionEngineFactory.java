package org.ndexbio.communitydetection.rest.engine;

import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.ndexbio.communitydetection.rest.services.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
public class BasicCommunityDetectionEngineFactory {
    
    static Logger _logger = LoggerFactory.getLogger(BasicCommunityDetectionEngineFactory.class);

    private String _dbDir;
    private String _taskDir;
    
    /**
     * Temp directory where query results will temporarily be stored.
     * @param tmpDir 
     */
    public BasicCommunityDetectionEngineFactory(Configuration config){
        
        _dbDir = config.getDatabaseDirectory();
        _taskDir = config.getTaskDirectory();
    }
    
    
    /**
     * Creates CommunityDetectionEngine
     * @return 
     */
    public CommunityDetectionEngine getCommunityDetectionEngine() throws CommunityDetectionException {
        CommunityDetectionEngineImpl engine = new CommunityDetectionEngineImpl(_dbDir,
                _taskDir);
        return engine;
    }
}
