package org.ndexbio.communitydetection.rest.engine.util;

import java.util.Map;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CustomParameter;
import org.ndexbio.communitydetection.rest.model.ErrorResponse;

/**
 * Performs validation of the {@link org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest}
 * via the {@link #validateRequest(org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm, org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest) } method
 * 
 * 
 * @author churas
 */
public class CommunityDetectionRequestValidatorImpl implements CommunityDetectionRequestValidator  {

    
    /**
     * Verifies custom parameters in the request 'cdr' match those in the algorithm 'cda'
     * by first checking for a match by name, then utilizing the validation methods to verify
     * data is the correct type and passes any min/max rules as well as any regular expressions
     * 
     * @param cda The algorithm to run
     * @param cdr The request to validate
     * @return null if no error otherwise {@link org.ndexbio.communitydetection.rest.model.ErrorResponse} object with issues found
     */
    @Override
    public ErrorResponse validateRequest(CommunityDetectionAlgorithm cda, CommunityDetectionRequest cdr) {
       
        if (cdr.getData() == null){
            ErrorResponse er = new ErrorResponse();
            er.setMessage("No data passed in with request");
            er.setDescription("All requests require some data to be set in the data field");
            return er;
        }
        Map<String, String> customParams = cdr.getCustomParameters();
        if (customParams == null){
            return null;
        }
        Map<String,CustomParameter> algoCustomParams = cda.getCustomParameterMap();
        for (String pName : cdr.getCustomParameters().keySet()){
            if (algoCustomParams == null || algoCustomParams.containsKey(pName) == false){
                ErrorResponse er = new ErrorResponse();
                er.setMessage("Invalid custom parameter");
                er.setDescription(pName + " is not a custom parameter for algorithm: " + cda.getName());
                return er;
            }
            ErrorResponse er = validateParameter(algoCustomParams.get(pName), pName, customParams.get(pName));
            if (er != null){
                return er;
            }
            
        }
        return null;  
    }
    
    protected ErrorResponse validateParameter(final CustomParameter algoParam,
            final String userParamName,
            final String userParamValue){
        return null;
    }
}
