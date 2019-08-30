package org.ndexbio.communitydetection.rest.engine.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
/**
 *
 * @author churas
 */
public class TestDockerCommunityDetectionRunner {
    
    @Rule
    public TemporaryFolder _folder = new TemporaryFolder();
    
    @Test
    public void testConstructorWriteInputThrowsException() throws Exception {
        File tempDir = _folder.newFolder();
        try {
            File taskDir = new File(tempDir.getAbsolutePath() + File.separator + "someid");
            assertTrue(taskDir.createNewFile());
            
            DockerCommunityDetectionRunner runner = new DockerCommunityDetectionRunner("someid", new CommunityDetectionRequest(),
                    0, taskDir.getAbsolutePath(), "docker", "hello-world", 1,
                    TimeUnit.SECONDS);
            fail("Expected Exception");
            
        } catch(CommunityDetectionException cde){
            assertTrue(cde.getMessage().contains("Unable to create directory:"));
        }finally {
            _folder.delete();
        } 
    }
    
    @Test
    public void testConstructorSuccessfulTextData() throws Exception {
        File tempDir = _folder.newFolder();
        try {
            
            CommunityDetectionRequest cdr = new CommunityDetectionRequest();
            cdr.setAlgorithm("somealgo");
            cdr.setData(new TextNode("blah"));
            DockerCommunityDetectionRunner runner = new DockerCommunityDetectionRunner("someid", cdr,
                    0, tempDir.getAbsolutePath(), "docker", "hello-world", 1,
                    TimeUnit.SECONDS);
            
            try (BufferedReader br = new BufferedReader(new FileReader(runner.getInputFile()))){
                assertEquals("blah", br.readLine());
            }
        }finally {
            _folder.delete();
        } 
    }
    
    @Test
    public void testConstructorSuccessfulJsonData() throws Exception {
        File tempDir = _folder.newFolder();
        try {
            
            CommunityDetectionRequest cdr = new CommunityDetectionRequest();
            cdr.setAlgorithm("somealgo");
            ObjectMapper mapper = new ObjectMapper();
            cdr.setData(mapper.readTree("{\"blah\": \"data\"}"));
            DockerCommunityDetectionRunner runner = new DockerCommunityDetectionRunner("someid", cdr,
                    0, tempDir.getAbsolutePath(), "docker", "hello-world", 1,
                    TimeUnit.SECONDS);
            
            try (BufferedReader br = new BufferedReader(new FileReader(runner.getInputFile()))){
                assertEquals("{\"blah\":\"data\"}", br.readLine());
            }
        }finally {
            _folder.delete();
        } 
    }
}
