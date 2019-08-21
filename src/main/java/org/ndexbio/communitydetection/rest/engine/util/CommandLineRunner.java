package org.ndexbio.communitydetection.rest.engine.util;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author churas
 */
public interface CommandLineRunner {
    
    public void setWorkingDirectory(final String workingDir);

    public void setEnvironmentVariables(Map<String, String> envVars);

    public String getLastCommand();
    
    /**
     * Runs command line program specified by first argument.
     * @param command - First argument should be full path to command followed by arguments
     * @return containing exit code of program or 500 if the process exceeded timeout
     * @throws java.lang.Exception if there was an error invoking the process
     */
    public int runCommandLineProcess(long timeOut, TimeUnit unit, File stdOutFile, File stdErrFile, String... command) throws Exception;
    
}
