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
            processQuery(id,_queryTasks.remove(id));            
        }
        _logger.debug("Shutdown was invoked");
    }

    @Override
    public void shutdown() {
        _shutdown = true;
    }
    
    protected void updateCommunityDetectionResultInDb(final String id,
            final String status, int progress,
            CommunityDetectionResult result){
        CommunityDetectionResult eqr = getCommunityDetectionResultFromDb(id);
        
        eqr.setProgress(progress);
        eqr.setStatus(status);
        if (result != null){
            eqr.setResult(result.getResult());
        }
        eqr.setWallTime(System.currentTimeMillis() - eqr.getStartTime());
        _queryResults.merge(id, eqr, (oldval, newval) -> newval.updateStartTime(oldval));        
    }
    /**
     * First tries to get EnrichmentQueryResults from _queryResults list
     * and if that fails method creates a new EnrichmentQueryResults setting
     * current time in constructor.
     * @param id
     * @return 
     */
    protected CommunityDetectionResult getCommunityDetectionResultFromDb(final String id){
        CommunityDetectionResult eqr = _queryResults.get(id);
        if (eqr == null){
            eqr = new CommunityDetectionResult(System.currentTimeMillis());
        }
        return eqr;
    }
    
    protected CommunityDetectionResult getCommunityDetectionResultFromDbOrFilesystem(final String id){
        CommunityDetectionResult eqr = _queryResults.get(id);
        if (eqr != null){
            return eqr;
        }
        ObjectMapper mappy = new ObjectMapper();
        File eqrFile = new File(getCommunityDetectionResultFilePath(id));
        if (eqrFile.isFile() == false){
            _logger.error(eqrFile.getAbsolutePath() + " is not a file");
            return null;
        }
        try {
            return mappy.readValue(eqrFile, CommunityDetectionResult.class);
        }catch(IOException io){
            _logger.error("Caught exception trying to load " + eqrFile.getAbsolutePath(), io);
        }
        return null;
    }
    
    /**
     * Runs enrichment on query storing results in _queryResults and _queryStatus
     * @param id 
     */
    protected void processQuery(final String id, CommunityDetectionRequest request){
        
        File taskDir = new File(this._taskDir + File.separator + id);
        _logger.debug("Creating new task directory:" + taskDir.getAbsolutePath());

        if (taskDir.mkdirs() == false){
            _logger.error("Unable to create task directory: " + taskDir.getAbsolutePath());
            updateCommunityDetectionResultInDb(id, CommunityDetectionResult.FAILED_STATUS, 100, null);
            return;
        }
        
        CommunityDetectionResult communityDetectionResult = new CommunityDetectionResult();
        
        updateEnrichmentQueryResultsInDb(id, CommunityDetectionResult.COMPLETE_STATUS, 100, communityDetectionResult);
        saveCommunityDetectionResultToFilesystem(id);
    }
    
    protected String getCommunityDetectionResultFilePath(final String id){
        return this._taskDir + File.separator + id + File.separator + CommunityDetectionEngineImpl.CDR_JSON_FILE;
    }

    protected void saveCommunityDetectionResultToFilesystem(final String id){
        CommunityDetectionResult eqr = getCommunityDetectionResultFromDb(id);
        if (eqr == null){
            return;
        }
        File destFile = new File(getCommunityDetectionResultFilePath(id));
        ObjectMapper mappy = new ObjectMapper();
        try (FileOutputStream out = new FileOutputStream(destFile)){
            mappy.writeValue(out, eqr);
        } catch(IOException io){
            _logger.error("Caught exception writing " + destFile.getAbsolutePath(), io);
        }
        _queryResults.remove(id);
    } 
    
    @Override
    public String request(CommunityDetectionRequest thequery) throws CommunityDetectionException {
        
       
        // @TODO get Jing's uuid generator code that can be a poormans cache
        String id = UUID.randomUUID().toString();
        _queryTasks.put(id, thequery);
        _queryTaskIds.add(id);
        CommunityDetectionResult eqr = new CommunityDetectionResult(System.currentTimeMillis());
        eqr.setStatus(CommunityDetectionResult.SUBMITTED_STATUS);
        _queryResults.merge(id, eqr, (oldval, newval) -> newval.updateStartTime(oldval));        
        return id;
    }
    
    /**
     * Returns
     * @param id Id of the query. 
     * @return {@link org.ndexbio.enrichment.rest.model.EnrichmentQueryResults} object
     *         or null if no result could be found. 
     * @throws EnrichmentException If there was an error getting the results
     */
    @Override
    public EnrichmentQueryResults getQueryResult(String id) throws CommunityDetectionException {
        EnrichmentQueryResults eqr = getEnrichmentQueryResultsFromDbOrFilesystem(id);
        if (start < 0){
            throw new EnrichmentException("start parameter must be value of 0 or greater");
        }
        if (size < 0){
            throw new EnrichmentException("size parameter must be value of 0 or greater");
        }

        if (start == 0 && size == 0){
            return eqr;
        }
        List<EnrichmentQueryResult> eqrSubList = new LinkedList<>();
        int numElementsAdded = 0;
        for (EnrichmentQueryResult element : eqr.getResults()){
            if (element.getRank() < start){
                continue;
            }
            eqrSubList.add(element);
            numElementsAdded++;
            if (numElementsAdded >= size){
                break;
            }
        }
        return new EnrichmentQueryResults(eqr, eqrSubList);
    }

    @Override
    public CommunityDetectionRequestStatus getStatus(String id) throws CommunityDetectionException {
        CommunityDetectionResult eqr = this.getCommunityDetectionResultFromDbOrFilesystem(id);
        if (eqr == null){
            return null;
        }
        return new CommunityDetectionRequestStatus(eqr);
    }

    @Override
    public void delete(String id) throws CommunityDetectionException {
        _logger.debug("Deleting task " + id);
        if (_queryResults.containsKey(id) == true){
            _queryResults.remove(id);
        }
        File thisTaskDir = new File(this._taskDir + File.separator + id);
        if (thisTaskDir.exists() == false){
            return;
        }
        _logger.debug("Attempting to delete task from filesystem: " + thisTaskDir.getAbsolutePath());
        if (FileUtils.deleteQuietly(thisTaskDir) == false){
            _logger.error("There was a problem deleting the directory: " + thisTaskDir.getAbsolutePath());
        }
    }

    /**
     * Gets ServerStatus
     * @return 
     * @throws EnrichmentException If there was a problem
     */
    @Override
    public ServerStatus getServerStatus() throws CommunityDetectionException {
        try {
            String version = "unknown";
            ServerStatus sObj = new ServerStatus();
            sObj.setStatus(ServerStatus.OK_STATUS);
            sObj.setRestVersion(CommunityDetectionHttpServletDispatcher.getVersion());
            OperatingSystemMXBean omb = ManagementFactory.getOperatingSystemMXBean();
            float unknown = (float)-1;
            float load = (float)omb.getSystemLoadAverage();
            sObj.setLoad(Arrays.asList(load, unknown, unknown));
            File taskDir = new File(this._taskDir);
            sObj.setPcDiskFull(100-(int)Math.round(((double)taskDir.getFreeSpace()/(double)taskDir.getTotalSpace())*100));
            return sObj;
        } catch(Exception ex){
            _logger.error("ServerStatus error", ex);
            throw new EnrichmentException("Exception raised when getting ServerStatus: " + ex.getMessage());
        }
    }
    
    
}
