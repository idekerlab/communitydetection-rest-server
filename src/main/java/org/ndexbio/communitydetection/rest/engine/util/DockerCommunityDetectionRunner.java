package org.ndexbio.communitydetection.rest.engine.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author churas
 */
public class DockerCommunityDetectionRunner implements Callable {

    
    static Logger _logger = LoggerFactory.getLogger(DockerCommunityDetectionRunner.class);

    public static final String INPUT_FILE = "input.txt";
    public static final String STD_OUT_FILE = "stdout.txt";
    public static final String STD_ERR_FILE = "stderr.txt";
    public static final String CMD_RUN_FILE = "cmdrun.sh";
    
    private String _id;
    private CommunityDetectionRequest _cdr;
    private String _dockerCmd;
    private String _dockerImage;
    private String _taskDir;
    private String _workDir;
    private long _startTime;
    private long _timeOut;
    private TimeUnit _timeUnit;
 
    private CommandLineRunner _runner;
    
    public DockerCommunityDetectionRunner(final String id,
            final CommunityDetectionRequest cdr, final long startTime, final String taskDir,
            final String dockerCmd, final String dockerImage, final long timeOut,
            final TimeUnit unit) throws Exception{
        _id = id;
        _cdr = cdr;
        _dockerCmd = dockerCmd;
        _dockerImage = dockerImage;
        _startTime = startTime;
        _taskDir = taskDir;
        _workDir = _taskDir + File.separator + _id;
        _timeOut = timeOut;
        _timeUnit = unit;
        writeInputFile();
       
        _runner = new CommandLineRunnerImpl();
        
    }
    
    public void setAlternateCommandLineRunner(CommandLineRunner clr){
        _runner = clr;
    }
    
    protected String writeInputFile() throws Exception{
        File workDir = new File(_workDir);
        
        if (workDir.isDirectory() == false){
            if (workDir.mkdirs() == false){
                throw new Exception("Unable to create directory: " + _workDir);
            }
        }
        File destFile = new File(_workDir + File.separator + INPUT_FILE);
        if (_cdr.getData()instanceof TextNode){
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(destFile))){
                bw.write(_cdr.getData().asText());
            }
        }
        else {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(destFile, _cdr.getData()); 
        }
        return destFile.getAbsolutePath();
    }
    
    protected File getStandardOutFile(){
        return new File(_workDir + File.separator + STD_OUT_FILE);
    }
    
    protected File getStandardErrorFile(){
        return new File(_workDir + File.separator + STD_ERR_FILE);
    }
    
    protected File getCommandRunFile(){
        return new File(_workDir + File.separator + CMD_RUN_FILE);
    }
    
    protected CommunityDetectionResult createCommunityDetectionResult(){
        CommunityDetectionResult cdr = new CommunityDetectionResult();
        cdr.setId(_id);
        cdr.setProgress(0);
        cdr.setStartTime(_startTime);
        cdr.setStatus(CommunityDetectionResult.PROCESSING_STATUS);
        return cdr;
    }
    
    protected void updateCommunityDetectionResultWithFileContents(CommunityDetectionResult cdr, File outFile) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (outFile.isFile() == false){
            _logger.error(outFile.getAbsolutePath() + " does not exist or is not a file");
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            cdr.setResult(mapper.readTree(outFile));
        } catch(JsonParseException jpe){
            _logger.debug("Received a json parsing error going to try to store result as string: ", jpe);
            try (BufferedReader br = new BufferedReader(new FileReader(outFile))){
                String line = br.readLine();
                while(line != null){
                    sb.append(line).append("\n");
                    line = br.readLine();
                }
                cdr.setResult(new TextNode(sb.toString()));
            }
        }
    }
    
    protected void updateCommunityDetectionResult(int exitValue, File stdOutFile, File stdErrFile, CommunityDetectionResult cdr) throws Exception {
        
        File outFile = stdOutFile;
        if (exitValue != 0){
                cdr.setStatus(CommunityDetectionResult.FAILED_STATUS);
                cdr.setMessage("Received non zero exit code: " +
                        Integer.toString(exitValue) + " when running algorithm for task: " + cdr.getId());
                outFile = stdErrFile;
                _logger.error(cdr.getMessage());
        } else {
            
                cdr.setStatus(CommunityDetectionResult.COMPLETE_STATUS);
        }
        updateCommunityDetectionResultWithFileContents(cdr, outFile);
    }
    
    protected void writeCommandRunToFile(){
        File outFile = getCommandRunFile();
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outFile))){
            bw.write(_runner.getLastCommand());
        } catch(IOException io){
            _logger.error("Error writing command run to: " + outFile.getAbsolutePath(), io);
        }
    }
    
    @Override
    public CommunityDetectionResult call() throws Exception {
        
        File workDir = new File(_workDir);
        
        String mapDir = _workDir + ":" + _workDir + ":ro";
        _runner.setWorkingDirectory(_workDir);
        
        String inputFile = writeInputFile();
        
        File stdOutFile = getStandardOutFile();
        File stdErrFile = getStandardErrorFile();
        
        CommunityDetectionResult cdr = createCommunityDetectionResult();
        
        try {
            if (workDir.isDirectory() == false){
                throw new Exception(_workDir + " directory does not exist");
            }
            int  exitValue = _runner.runCommandLineProcess(_timeOut, _timeUnit, stdOutFile, stdErrFile, _dockerCmd, "run", "-v", mapDir,
                                                         _dockerImage, inputFile);
            writeCommandRunToFile();
            updateCommunityDetectionResult(exitValue, stdOutFile, stdErrFile, cdr);
            
        } catch(Exception ex){
            cdr.setStatus(CommunityDetectionResult.FAILED_STATUS);
            cdr.setMessage("Received error trying to run detection: " + ex.getMessage());
            _logger.error("Received error trying to run algorithm for task in " + _workDir, ex);
        }
        cdr.setProgress(100);
        cdr.setWallTime(System.currentTimeMillis() - cdr.getStartTime());
        return cdr;          
    }
    
}
