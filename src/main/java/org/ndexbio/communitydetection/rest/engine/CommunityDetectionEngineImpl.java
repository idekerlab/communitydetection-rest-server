package org.ndexbio.communitydetection.rest.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.ndexbio.communitydetection.rest.engine.util.DockerCommunityDetectionRunner;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResultStatus;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.ServerStatus;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

import org.ndexbio.communitydetection.rest.services.CommunityDetectionHttpServletDispatcher;
import org.ndexbio.communitydetection.rest.services.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs enrichment 
 * @author churas
 */
public class CommunityDetectionEngineImpl implements CommunityDetectionEngine {

    public static final String CDREQEUST_JSON_FILE = "cdrequest.json";
    
    public static final String CDRESULT_JSON_FILE = "cdresult.json";
    
    static Logger _logger = LoggerFactory.getLogger(CommunityDetectionEngineImpl.class);

    private String _taskDir;
    private boolean _shutdown;
    private ExecutorService _executorService;
    private  List<Future> _futureTaskList;
    private HashMap<String, String> _algoToDockerMap;
    private String _dockerCmd;
        
    /**
     * This should be a map of <query UUID> => EnrichmentQueryResults object
     */
    private ConcurrentHashMap<String, CommunityDetectionResult> _results;

    private long _threadSleep = 10;
    
    public CommunityDetectionEngineImpl(ExecutorService es,
            final String taskDir,
            final String dockerCmd,
            final HashMap<String, String> algoToDockerMap){
        _executorService = es;
        _shutdown = false;
        _futureTaskList = Collections
            .synchronizedList(new LinkedList<Future>());
        _taskDir = taskDir;
        _dockerCmd = dockerCmd;
        _algoToDockerMap = algoToDockerMap;
        _results = new ConcurrentHashMap<>();
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
            Future f;
            Iterator<Future> itr = _futureTaskList.iterator();
            while(itr.hasNext()){
                f = itr.next();
                if (f.isCancelled() || f.isDone()){
                    if (f.isDone()){
                       CommunityDetectionResult cdr;
                        try {
                            cdr = (CommunityDetectionResult) f.get();
                        } catch (InterruptedException ex) {
                            _logger.error("Got interrupted exception", ex);
                            continue;
                        } catch (ExecutionException ex) {
                            _logger.error("Got execution exception", ex);
                            continue;
                        }
                        saveCommunityDetectionResultToFilesystem(cdr);
                    }
                    itr.remove();
                }
            }
            threadSleep();
        }
        _logger.debug("Shutdown was invoked");
    }

    @Override
    public void shutdown() {
        _shutdown = true;
    }
    
    
    protected String getCommunityDetectionResultFilePath(final String id){
        return this._taskDir + File.separator + id + File.separator + CommunityDetectionEngineImpl.CDRESULT_JSON_FILE;
    }

    protected void saveCommunityDetectionResultToFilesystem(final CommunityDetectionResult cdr){
        if (cdr == null){
            return;
        }
        
        File destFile = new File(getCommunityDetectionResultFilePath(cdr.getId()));
        ObjectMapper mappy = new ObjectMapper();
        try (FileOutputStream out = new FileOutputStream(destFile)){
            mappy.writeValue(out, cdr);
        } catch(IOException io){
            _logger.error("Caught exception writing " + destFile.getAbsolutePath(), io);
        }
        _results.remove(cdr.getId());
    }
    
    protected CommunityDetectionResult getCommunityDetectionResultFromDbOrFilesystem(final String id){
        CommunityDetectionResult cdr = _results.get(id);
        if (cdr != null){
            return cdr;
        }
        ObjectMapper mappy = new ObjectMapper();
        File cdrFile = new File(getCommunityDetectionResultFilePath(id));
        if (cdrFile.isFile() == false){
            _logger.error(cdrFile.getAbsolutePath() + " is not a file");
            return null;
        }
        try {
            return mappy.readValue(cdrFile, CommunityDetectionResult.class);
        }catch(IOException io){
            _logger.error("Caught exception trying to load " + cdrFile.getAbsolutePath(), io);
        }
        return null;
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

        CommunityDetectionResult cdr = new CommunityDetectionResult(System.currentTimeMillis());
        cdr.setStatus(CommunityDetectionResult.SUBMITTED_STATUS);
        cdr.setId(id);
        _results.put(id, cdr);
        
        if (_algoToDockerMap.containsKey(request.getAlgorithm()) == false){
            throw new CommunityDetectionException(request.getAlgorithm() + " is not a valid algorithm");
        }
        
        String dockerImage = _algoToDockerMap.get(request.getAlgorithm());
        try {
            DockerCommunityDetectionRunner task = new DockerCommunityDetectionRunner(id, request, cdr.getStartTime(),
            _taskDir, _dockerCmd, dockerImage, Configuration.getInstance().getAlgorithmTimeOut(),
            TimeUnit.SECONDS);
            _futureTaskList.add(_executorService.submit(task));
            return id;
        } catch(Exception ex){
            throw new CommunityDetectionException(ex.getMessage());
        }
    }

    @Override
    public CommunityDetectionResult getResult(String id) throws CommunityDetectionException {
        CommunityDetectionResult cdr = getCommunityDetectionResultFromDbOrFilesystem(id);
        if (cdr == null){
            throw new CommunityDetectionException("No task with " + id + " found");
        }
        return cdr;
    }

    @Override
    public CommunityDetectionResultStatus getStatus(String id) throws CommunityDetectionException {
        CommunityDetectionResult cdr = getCommunityDetectionResultFromDbOrFilesystem(id);
        if (cdr == null){
            throw new CommunityDetectionException("No task with " + id + " found");
        }
        return new CommunityDetectionResultStatus(cdr);
    }

    @Override
    public void delete(String id) throws CommunityDetectionException {
        _logger.debug("Deleting task " + id);
        if (_results.containsKey(id) == true){
            _results.remove(id);
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
