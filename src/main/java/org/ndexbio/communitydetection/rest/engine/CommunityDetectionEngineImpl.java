package org.ndexbio.communitydetection.rest.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequestStatus;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.ServerStatus;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

import org.ndexbio.communitydetection.rest.services.CommunityDetectionHttpServletDispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs enrichment 
 * @author churas
 */
public class CommunityDetectionEngineImpl implements CommunityDetectionEngine {

    public static final String CDR_JSON_FILE = "communitydetectionresult.json";
    
    static Logger _logger = LoggerFactory.getLogger(CommunityDetectionEngineImpl.class);

    private String _dbDir;
    private String _taskDir;
    private boolean _shutdown;
    
    /**
     * This should be a map of <query UUID> => EnrichmentQuery object
     */
    private ConcurrentHashMap<String, CommunityDetectionRequest> _queryTasks;
    
    private ConcurrentLinkedQueue<String> _queryTaskIds;
    
    /**
     * This should be a map of <query UUID> => EnrichmentQueryResults object
     */
    private ConcurrentHashMap<String, CommunityDetectionResult> _queryResults;
        
    /**
     * This should be a map of <database UUID> => Map<Gene => Set of network UUIDs>
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<String, HashSet<String>>> _databases;
    
    
    private long _threadSleep = 10;
    
    public CommunityDetectionEngineImpl(final String dbDir,
            final String taskDir){
        _shutdown = false;
        _dbDir = dbDir;
        _taskDir = taskDir;
        _queryTasks = new ConcurrentHashMap<>();
        _queryResults = new ConcurrentHashMap<>();
        _queryTaskIds = new ConcurrentLinkedQueue<>();
    }
    
    /**
     * Sets milliseconds thread should sleep if no work needs to be done.
     * @param sleepTime 
     */
    public void updateThreadSleepTime(long sleepTime){
        _threadSleep = sleepTime;
    }

    protected void threadSleep(){
        try {
            Thread.sleep(_threadSleep);
        }
        catch(InterruptedException ie){

        }
    }
    
    /**
     * Processes any query tasks, looping until {@link #shutdown()} is invoked
     */
    @Override
    public void run() {
        while(_shutdown == false){
            String id = _queryTaskIds.poll();
            if (id == null){
                threadSleep();
                continue;
            }
            //processQuery(id,_queryTasks.remove(id));            
        }
        _logger.debug("Shutdown was invoked");
    }

    @Override
    public void shutdown() {
        _shutdown = true;
    }

    @Override
    public String request(CommunityDetectionRequest request) throws CommunityDetectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CommunityDetectionResult getResult(String id) throws CommunityDetectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CommunityDetectionRequestStatus getStatus(String id) throws CommunityDetectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(String id) throws CommunityDetectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ServerStatus getServerStatus() throws CommunityDetectionException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
