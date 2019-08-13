package org.ndexbio.communitydetection.rest.engine;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.commons.io.FileUtils;
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

    private String _taskDir;
    private boolean _shutdown;
    private ExecutorService _executorService;
    
    private List<Future> _futureTaskList = Collections
            .synchronizedList(new LinkedList<Future>());
    
    private ConcurrentLinkedQueue<Callable> _tasksToProcess
            = new ConcurrentLinkedQueue<>();
    
    /**
     * This should be a map of <query UUID> => CommunityDetectionRequest object
     */
    private ConcurrentHashMap<String, CommunityDetectionRequest> _queryTasks;
    
    private ConcurrentLinkedQueue<String> _queryTaskIds;
    
    /**
     * This should be a map of <query UUID> => CommunityDetectionResult object
     */
    private ConcurrentHashMap<String, CommunityDetectionResult> _queryResults;
    
    private long _threadSleep = 10;
    
    public CommunityDetectionEngineImpl(ExecutorService es,
            final String taskDir){
        _executorService = es;
        _shutdown = false;
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
            //_futureTaskList.add(_executorService.submit(_cdFactory.getRunner(id)));
            _queryTasks.remove(id);
            
            //need to clean up _futureTaskList?
        }
        _logger.debug("Shutdown was invoked");
    }

    @Override
    public void shutdown() {
        _shutdown = true;
    }

    @Override
    public String request(CommunityDetectionRequest request) throws CommunityDetectionException {
        if (request == null){ 
            throw new CommunityDetectionException("Request is null");
        }
        if (request.getAlgorithm() == null){
            throw new CommunityDetectionException("No algorithm specified");
        }
        if (request.getEdgeList() == null){
            throw new CommunityDetectionException("Edge list is null");
        }
        String id = UUID.randomUUID().toString();
        _queryTasks.put(id, request);
        _queryTaskIds.add(id);
        CommunityDetectionResult cdr = new CommunityDetectionResult(System.currentTimeMillis());
        cdr.setStatus(CommunityDetectionResult.SUBMITTED_STATUS);
        this._queryResults.merge(id, cdr, (oldval, newval) -> newval.updateStartTime(oldval));
        return id;
    }

    @Override
    public CommunityDetectionResult getResult(String id) throws CommunityDetectionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommunityDetectionRequestStatus getStatus(String id) throws CommunityDetectionException {
        throw new UnsupportedOperationException("Not supported yet.");
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
            throw new CommunityDetectionException("Exception raised when getting ServerStatus: " + ex.getMessage());
        }
    }
}
