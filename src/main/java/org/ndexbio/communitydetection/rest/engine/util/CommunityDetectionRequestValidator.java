/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ndexbio.communitydetection.rest.engine.util;

import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.ErrorResponse;

/**
 *
 * @author churas
 */
public interface CommunityDetectionRequestValidator {
    
    /**
     * Validates the request 
     * @param cdr The request to validate
     * @return null upon success otherwise {@link org.ndexbio.communitydetection.rest.model.ErrorResponse} describing the error
     */
    public ErrorResponse validateRequest(CommunityDetectionAlgorithm cda, CommunityDetectionRequest cdr);
    
}
