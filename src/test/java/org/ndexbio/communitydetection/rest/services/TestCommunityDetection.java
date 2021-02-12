package org.ndexbio.communitydetection.rest.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.ndexbio.communitydetection.rest.engine.CommunityDetectionEngine;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithms;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResultStatus;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.ErrorResponse;
import org.ndexbio.communitydetection.rest.model.Task;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionBadRequestException;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;

/**
 *
 * @author churas
 */
public class TestCommunityDetection {
    
    @Rule
    public TemporaryFolder _folder = new TemporaryFolder();

	@After
	public void tearDown(){
		try {
			Configuration.getInstance().setCommunityDetectionEngine(null);
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Creates Dispatcher used to invoke mock request
	 * @return Dispatcher loaded with CommunityDetection clas
	 */
	public Dispatcher getDispatcher(){
		Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
		dispatcher.getRegistry().addSingletonResource(new CommunityDetection());
		return dispatcher;
	}
	
	/**
	 * Creates basic configuration with task directory set to full path
	 * of tempDir passed in
	 * @param tempDir
	 * @return
	 * @throws IOException 
	 */
	public File createBasicConfigurationFile(File tempDir) throws IOException {
		File confFile = new File(tempDir.getAbsolutePath() + File.separator + "foo.conf");
            
		FileWriter fw = new FileWriter(confFile);

		fw.write(Configuration.TASK_DIR + " = "
				+ tempDir.getAbsolutePath() + "\n");
		fw.flush();
		fw.close();
		return confFile;
	}
	
    @Test
    public void testRequestCommunityDetectionWhereEngineNotLoaded() throws Exception {
        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.post(Configuration.V_ONE_PATH);
            CommunityDetectionRequest query = new CommunityDetectionRequest();
            ObjectMapper omappy = new ObjectMapper();
            request.contentType(MediaType.APPLICATION_JSON);
            
            request.content(omappy.writeValueAsBytes(query));


            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            Configuration.getInstance().setCommunityDetectionEngine(null);
            dispatcher.invoke(request, response);
            assertEquals(500, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse er = mapper.readValue(response.getOutput(),
                    ErrorResponse.class);
            assertEquals("Error requesting CommunityDetection", er.getMessage());
            assertEquals("CommunityDetection Engine not loaded", er.getDescription());
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testRequestWhereQueryRaisesError() throws Exception {
        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
			
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.post(Configuration.V_ONE_PATH);
            CommunityDetectionRequest query = new CommunityDetectionRequest();
            ObjectMapper omappy = new ObjectMapper();
            request.contentType(MediaType.APPLICATION_JSON);
            
            request.content(omappy.writeValueAsBytes(query));


            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            expect(mockEngine.request(notNull())).andThrow(new CommunityDetectionException("some error"));
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(500, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse er = mapper.readValue(response.getOutput(),
                    ErrorResponse.class);
            assertEquals("Error requesting CommunityDetection", er.getMessage());
            assertEquals("some error", er.getDescription());
            verify(mockEngine);

        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testRequestWhereQueryRaisesBadRequestError() throws Exception {
        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.post(Configuration.V_ONE_PATH);
            CommunityDetectionRequest query = new CommunityDetectionRequest();
            ObjectMapper omappy = new ObjectMapper();
            request.contentType(MediaType.APPLICATION_JSON);
            
            request.content(omappy.writeValueAsBytes(query));


            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            expect(mockEngine.request(notNull())).andThrow(new CommunityDetectionBadRequestException("some error"));
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(500, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse er = mapper.readValue(response.getOutput(),
                    ErrorResponse.class);
            assertEquals("Bad request received", er.getMessage());
            assertEquals("some error", er.getDescription());
            verify(mockEngine);

        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testRequestWhereQueryRaisesBadRequestErrorWithErrorResponse() throws Exception {
        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.post(Configuration.V_ONE_PATH);
            CommunityDetectionRequest query = new CommunityDetectionRequest();
            ObjectMapper omappy = new ObjectMapper();
            request.contentType(MediaType.APPLICATION_JSON);
            
            request.content(omappy.writeValueAsBytes(query));


            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            ErrorResponse xer = new ErrorResponse();
            xer.setMessage("hello");
            expect(mockEngine.request(notNull())).andThrow(new CommunityDetectionBadRequestException("some error", xer));
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(500, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse er = mapper.readValue(response.getOutput(),
                    ErrorResponse.class);
            assertEquals("hello", er.getMessage());
            assertEquals(null, er.getDescription());
            verify(mockEngine);

        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testRequestWhereQueryReturnsNull() throws Exception {
        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.post(Configuration.V_ONE_PATH);
            CommunityDetectionRequest query = new CommunityDetectionRequest();
            ObjectMapper omappy = new ObjectMapper();
            request.contentType(MediaType.APPLICATION_JSON);
            
            request.content(omappy.writeValueAsBytes(query));


            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            expect(mockEngine.request(notNull())).andReturn(null);
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(500, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse er = mapper.readValue(response.getOutput(),
                    ErrorResponse.class);
            assertEquals("Error requesting CommunityDetection", er.getMessage());
            assertEquals("No id returned from CommunityDetection engine", er.getDescription());
            verify(mockEngine);

        } finally {
            _folder.delete();
        }
    }

    @Test
    public void testRequestWhereQuerySuccess() throws Exception {
        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.post(Configuration.V_ONE_PATH);
            CommunityDetectionRequest query = new CommunityDetectionRequest();
            ObjectMapper omappy = new ObjectMapper();
            request.contentType(MediaType.APPLICATION_JSON);
            
            request.content(omappy.writeValueAsBytes(query));


            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            expect(mockEngine.request(notNull())).andReturn("12345");
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(202, response.getStatus());
            
            MultivaluedMap<String, Object> resmap = response.getOutputHeaders();
            assertEquals(new URI(Configuration.V_ONE_PATH + "/12345"), resmap.getFirst("Location"));
            ObjectMapper mapper = new ObjectMapper();
            Task t = mapper.readValue(response.getOutput(),
                    Task.class);
            assertEquals("12345", t.getId());
            verify(mockEngine);

        } finally {
            _folder.delete();
        }
    }
    
        @Test
    public void testRequestWhereQuerySuccessAndHostURLSet() throws Exception {
        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            FileWriter fw = new FileWriter(confFile);
            fw.write(Configuration.HOST_URL + " = http://foo.com\n");
            fw.flush();
            fw.close();
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.post(Configuration.V_ONE_PATH);
            CommunityDetectionRequest query = new CommunityDetectionRequest();
            ObjectMapper omappy = new ObjectMapper();
            request.contentType(MediaType.APPLICATION_JSON);
            
            request.content(omappy.writeValueAsBytes(query));


            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            expect(mockEngine.request(notNull())).andReturn("12345");
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(202, response.getStatus());
            
            MultivaluedMap<String, Object> resmap = response.getOutputHeaders();
            assertEquals(new URI("http://foo.com" + Configuration.V_ONE_PATH + "/12345"), resmap.getFirst("Location"));
            ObjectMapper mapper = new ObjectMapper();
            Task t = mapper.readValue(response.getOutput(),
                    Task.class);
            assertEquals("12345", t.getId());
            verify(mockEngine);

        } finally {
            _folder.delete();
        }
    }

    @Test
    public void testGetWhereCommunityDetectionEngineNotLoaded() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.get(Configuration.V_ONE_PATH + "/12345");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            Configuration.getInstance().setCommunityDetectionEngine(null);
            dispatcher.invoke(request, response);
            assertEquals(500, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse er = mapper.readValue(response.getOutput(),
                    ErrorResponse.class);
            assertEquals("Error getting results for id: 12345", er.getMessage());
            assertEquals("CommunityDetection Engine not loaded", er.getDescription());
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testGetWhereIdDoesNotExist() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.get(Configuration.V_ONE_PATH + "/12345");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            expect(mockEngine.getResult("12345")).andReturn(null);
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(410, response.getStatus());
            verify(mockEngine);
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testGetWhereIdExists() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.get(Configuration.V_ONE_PATH +
                                                          "/12345?start=1&size=2");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            CommunityDetectionResult eqr = new CommunityDetectionResult();
            eqr.setMessage("hi");
            expect(mockEngine.getResult("12345")).andReturn(eqr);
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(200, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            CommunityDetectionResult res = mapper.readValue(response.getOutput(),
                    CommunityDetectionResult.class);
            assertEquals("hi", res.getMessage());
            verify(mockEngine);
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testGetAlgorithmsWhereCommunityDetectionEngineNotLoaded() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.get(Configuration.V_ONE_PATH + "/algorithms");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            Configuration.getInstance().setCommunityDetectionEngine(null);
            dispatcher.invoke(request, response);
            assertEquals(500, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse er = mapper.readValue(response.getOutput(),
                    ErrorResponse.class);
            assertEquals("Error trying to get list of algorithms", er.getMessage());
            assertEquals("CommunityDetection Engine not loaded", er.getDescription());
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testGetAlgorithmsWhereEngineReturnsNull() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.get(Configuration.V_ONE_PATH + "/algorithms");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            expect(mockEngine.getAlgorithms()).andReturn(null);
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(410, response.getStatus());
            verify(mockEngine);
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testGetAlgorithmsSuccess() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.get(Configuration.V_ONE_PATH + "/algorithms");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            CommunityDetectionAlgorithms cdalgos = new CommunityDetectionAlgorithms();
            expect(mockEngine.getAlgorithms()).andReturn(cdalgos);
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(200, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            CommunityDetectionAlgorithms res = mapper.readValue(response.getOutput(),
                    CommunityDetectionAlgorithms.class);
            assertEquals(null, res.getAlgorithms());
            verify(mockEngine);
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testGetStatusWhereEnrichmentEngineNotLoaded() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.get(Configuration.V_ONE_PATH + "/12345/status");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            Configuration.getInstance().setCommunityDetectionEngine(null);

            dispatcher.invoke(request, response);
            assertEquals(500, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse er = mapper.readValue(response.getOutput(),
                    ErrorResponse.class);
            assertEquals("Error getting results for id: 12345", er.getMessage());
            assertEquals("CommunityDetection Engine not loaded", er.getDescription());
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testGetStatusWhereIdDoesNotExist() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.get(Configuration.V_ONE_PATH + "/12345/status");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            expect(mockEngine.getStatus("12345")).andReturn(null);
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(410, response.getStatus());
            verify(mockEngine);
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testGetStatusWhereIdExists() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.get(Configuration.V_ONE_PATH +
                                                          "/12345/status");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            CommunityDetectionResultStatus eqs = new CommunityDetectionResultStatus();
            eqs.setProgress(55);
            expect(mockEngine.getStatus("12345")).andReturn(eqs);
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(200, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            CommunityDetectionResultStatus res = mapper.readValue(response.getOutput(),
                    CommunityDetectionResultStatus.class);
            assertEquals(55, res.getProgress());
            verify(mockEngine);
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testDeleteWhereEnrichmentEngineNotLoaded() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.delete(Configuration.V_ONE_PATH + "/12345");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            Configuration.getInstance().setCommunityDetectionEngine(null);

            dispatcher.invoke(request, response);
            assertEquals(500, response.getStatus());
            ObjectMapper mapper = new ObjectMapper();
            ErrorResponse er = mapper.readValue(response.getOutput(),
                    ErrorResponse.class);
            assertEquals("Error deleting: 12345", er.getMessage());
            assertEquals("CommunityDetection Engine not loaded", er.getDescription());
        } finally {
            _folder.delete();
        }
    }
    
    @Test
    public void testDeleteSuccessful() throws Exception {

        try {
            File tempDir = _folder.newFolder();
            File confFile = createBasicConfigurationFile(tempDir);
            Dispatcher dispatcher = getDispatcher();

            MockHttpRequest request = MockHttpRequest.delete(Configuration.V_ONE_PATH +
                                                          "/12345");

            MockHttpResponse response = new MockHttpResponse();
            Configuration.setAlternateConfigurationFile(confFile.getAbsolutePath());
            
            // create mock enrichment engine that returns null
            CommunityDetectionEngine mockEngine = createMock(CommunityDetectionEngine.class);
            mockEngine.delete("12345");
            replay(mockEngine);
            Configuration.getInstance().setCommunityDetectionEngine(mockEngine);
            
            dispatcher.invoke(request, response);
            assertEquals(200, response.getStatus());
            verify(mockEngine);
        } finally {
            _folder.delete();
        }
    }
}
