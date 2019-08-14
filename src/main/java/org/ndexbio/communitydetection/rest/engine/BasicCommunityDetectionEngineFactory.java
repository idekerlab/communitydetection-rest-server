package org.ndexbio.communitydetection.rest.engine;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private int _numWorkers;
    private String _taskDir;
    private String _dockerCmd;
    private HashMap<String, String> _algoMap;
    
    /**
     * Temp directory where query results will temporarily be stored.
     * @param tmpDir 
     */
    public BasicCommunityDetectionEngineFactory(Configuration config){
        
        _numWorkers = config.getNumberWorkers();
        _taskDir = config.getTaskDirectory();
        _dockerCmd = config.getDockerCommand();
        _algoMap = config.getAlgorithmToDockerMap();
       
    }

    /**
     * Creates CommunityDetectionEngine
     * @return 
     */
    public CommunityDetectionEngine getCommunityDetectionEngine() throws CommunityDetectionException {
        ExecutorService es = Executors.newFixedThreadPool(_numWorkers);
        CommunityDetectionEngineImpl engine = new CommunityDetectionEngineImpl(es, _taskDir, _dockerCmd, _algoMap);
        return engine;
    }
}
