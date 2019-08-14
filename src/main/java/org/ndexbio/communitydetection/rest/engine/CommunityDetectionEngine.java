package org.ndexbio.communitydetection.rest.engine;

import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResultStatus;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionResult;
import org.ndexbio.communitydetection.rest.model.exceptions.CommunityDetectionException;
import org.ndexbio.communitydetection.rest.model.ServerStatus;

/**
 *
 * @author churas
 */
public interface CommunityDetectionEngine extends Runnable {
    
    /**
     * Submits request for processing
     * @param request to process
     * @return UUID as a string that is an identifier for query
     */
    public String request(CommunityDetectionRequest request) throws CommunityDetectionException;
     
    /**
     * Gets query results
     * @param id

     * @return
     * @throws CommunityDetectionException  if there is an error
     */
    public CommunityDetectionResult getResult(final String id) throws CommunityDetectionException;
    
    
    /**
     * Gets query status
     * @param id
     * @return
     * @throws CommunityDetectionException if there is an error
     */
    public CommunityDetectionResultStatus getStatus(final String id) throws CommunityDetectionException;
    
    /**
     * Deletes query
     * @param id
     * @throws CommunityDetectionException if there is an error
     */
    public void delete(final String id) throws CommunityDetectionException;
 
    /**
     * Gets status of server
     * @return
     * @throws CommunityDetectionException 
     */
    public ServerStatus getServerStatus() throws CommunityDetectionException;
    
    /**
     * Tells implementing objects to shutdown
     */
    public void shutdown();
    
}
