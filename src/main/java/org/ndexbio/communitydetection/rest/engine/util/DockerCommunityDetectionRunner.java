package org.ndexbio.communitydetection.rest.engine.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.Callable;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;

/**
 *
 * @author churas
 */
public class DockerCommunityDetectionRunner implements Callable {

    public static final String INPUTEDGE_FILE = "edgelist.txt";
    
    private String _id;
    private CommunityDetectionRequest _cdr;
    private String _dockerCmd;
    private String _dockerImage;
    private String _workDir;
    private long _startTime;
 
    private CommandLineRunner _runner;
    
    public DockerCommunityDetectionRunner(final String id,
            final CommunityDetectionRequest cdr, final long startTime, final String taskDir,
            final String dockerCmd, final String dockerImage) throws Exception{
        _id = id;
        _cdr = cdr;
        _dockerCmd = dockerCmd;
        _dockerImage = dockerImage;
        _startTime = startTime;
        _workDir = taskDir + File.separator + _id;
        writeEdgeListFile();
       
        _runner = new CommandLineRunnerImpl();
        
    }
    
    public void setAlternateCommandLineRunner(CommandLineRunner clr){
        _runner = clr;
    }
    
    protected String writeEdgeListFile() throws Exception{
        File workDir = new File(_workDir);
        
        if (workDir.isDirectory() == false){
            if (workDir.mkdirs() == false){
                throw new Exception("Unable to create directory: " + _workDir);
            }
        }
        
        File destFile = new File(_workDir + File.separator + DockerCommunityDetectionRunner.INPUTEDGE_FILE);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(destFile));
            bw.write(_cdr.getEdgeList());
            bw.flush();
            return destFile.getAbsolutePath();
        }
        finally {
            if (bw != null){
                bw.close();
            }
        }
    }
    
    @Override
    public CommunityDetectionResult call() throws Exception {
        
        File workDir = new File(_workDir);
        
        String mapDir = _workDir + ":" + _workDir + ":ro";
        _runner.setWorkingDirectory(_workDir);
        
        String inputFile = writeEdgeListFile();
        
        CommunityDetectionResult cdr = new CommunityDetectionResult();
        cdr.setId(_id);
        cdr.setProgress(0);
        cdr.setStartTime(_startTime);
        cdr.setStatus(CommunityDetectionResult.PROCESSING_STATUS);
        try {
            if (workDir.isDirectory() == false){
                throw new Exception(_workDir + " directory does not exist");
            }
            String res = _runner.runCommandLineProcess(_dockerCmd, "run", "-v", mapDir,
                                                       _dockerImage, inputFile);
            
            cdr.setResult(res);
            cdr.setStatus(CommunityDetectionResult.COMPLETE_STATUS);
            cdr.setProgress(100);
        } catch(Exception ex){
            cdr.setStatus(CommunityDetectionResult.FAILED_STATUS);
            cdr.setMessage("Received error trying to run detection: " + ex.getMessage());
        }
        
        cdr.setWallTime(System.currentTimeMillis() - cdr.getStartTime());
        return cdr;          
    }
    
}
