package org.ndexbio.communitydetection.rest.engine.util;

import com.fasterxml.jackson.databind.node.TextNode;
import java.util.HashMap;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionAlgorithm;
import org.ndexbio.communitydetection.rest.model.CommunityDetectionRequest;
import org.ndexbio.communitydetection.rest.model.CustomParameter;
import org.ndexbio.communitydetection.rest.model.ErrorResponse;

/**
 *
 * @author churas
 */
public class TestCommunityDetectionRequestValidatorImpl {

    @Test
    public void testNullAlgorithmAndNullRequest(){
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(null, null);
        assertEquals("Algorithm is null", er.getMessage());
    }
    
    @Test
    public void testNullRequest(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, null);
        assertEquals("Request is null", er.getMessage());
    }
    
    @Test
    public void testDataInRequestIsNull(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("foo");
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("No data passed in with request", er.getMessage());
    }
    
    @Test
    public void testAlgorithmNameIsNull(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Algorithm name is null", er.getMessage());
    }
    
    @Test
    public void testNoCustomParameters(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("foo");

        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testNonMatchingCustomParameterCauseThereAreNoCustomParameters(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--foo", "blah");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Invalid custom parameter", er.getMessage());
        assertEquals("--foo is not a custom parameter for algorithm: "
                + cda.getName(), er.getDescription());
         
    }
    
    @Test
    public void testNonMatchingCustomParameter(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        aParams.add(cp);
        cp = new CustomParameter();
        cp.setName("--fo");
        aParams.add(cp);
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--foo", "blah");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Invalid custom parameter", er.getMessage());
        assertEquals("--foo is not a custom parameter for algorithm: "
                + cda.getName(), er.getDescription());
        
    }
    
    @Test
    public void testAlgorithmParameterTypeNotSet(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "blah");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testAlgorithmParameterUnknownType(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType("someunknowntype");
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "blah");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Unknown parameter type", er.getMessage());
    }
    
    @Test
    public void testSingleFlagParameterValid(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.FLAG_TYPE);
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", null);
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
        
    }
    
    @Test
    public void testSingleFlagParameterWithWhiteSpaceValueButValid(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.FLAG_TYPE);
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", " ");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
        
    }
    
    @Test
    public void testSingleFlagParameterPassedValue(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.FLAG_TYPE);
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "foo");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Flag only given a value", er.getMessage());
    }
    
    @Test
    public void testSingleStringParameterValueIsNull(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.STRING_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", null);
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Parameter missing value", er.getMessage());
    }
    
    @Test
    public void testSingleStringParameterValueIsEmptyWhiteSpace(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.STRING_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "  ");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Parameter missing value", er.getMessage());
    }
    
    @Test
    public void testSingleStringParameterNoRegex(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.STRING_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "foo");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testSingleStringParameterInvalidRegex(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.STRING_VALIDATION);
        cp.setValidationRegex("[");
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "foo");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Malformed validation expression", er.getMessage());
    }
    
    @Test
    public void testSingleStringParameterPassesRegex(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.STRING_VALIDATION);
        cp.setValidationRegex("foo|bar");
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "foo");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testSingleStringParameterFailsRegexNoHelp(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.STRING_VALIDATION);
        cp.setValidationRegex("^x.*v$");
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "foo");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Invalid parameter value", er.getMessage());
    }
    
    @Test
    public void testSingleStringParameterFailsRegexWithHelp(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.STRING_VALIDATION);
        cp.setValidationRegex("^x.*v$");
        cp.setValidationHelp("some help");
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "foo");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("some help", er.getMessage());
    }
    
    @Test
    public void testSingleNumericParameterNullValue(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.NUMBER_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", null);
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Parameter missing value", er.getMessage());
    }
    
    @Test
    public void testSingleNumericParameterWhitespaceValue(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.NUMBER_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", " ");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("Parameter missing value", er.getMessage());
    }
    
    @Test
    public void testSingleDigitsParameterValidValue(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.DIGITS_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "10");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testSingleDigitsParameterNegativeValue(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.DIGITS_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "-45");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testSingleDigitsParameterInvalidFloatValue(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.DIGITS_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "10.5");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("10.5 does not appear to be a whole number", er.getDescription());
    }
    
    @Test
    public void testSingleDigitsParameterValidValueWithMinMaxSet(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.DIGITS_VALIDATION);
        cp.setMinValue(9);
        cp.setMaxValue(11);
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "10");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testSingleDigitsParameterValidValueWithValueBelowMin(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.DIGITS_VALIDATION);
        cp.setMinValue(9);
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "8");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("8 is less then minimum value: 9 allowed for this parameter", er.getDescription());
    }
    
    @Test
    public void testSingleDigitsParameterValidValueWithValueAboveMax(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.DIGITS_VALIDATION);
        cp.setMaxValue(7);
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "8");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("8 is greater then maximum value: 7 allowed for this parameter", er.getDescription());
    }
    
    @Test
    public void testSingleNumberParameterInValidValue(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.NUMBER_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "xxx");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("xxx is not a valid number", er.getDescription());
    }
    
    @Test
    public void testSingleNumberParameterScientificNotationValue(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.NUMBER_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "1e-5");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testSingleNumberParameterValidValue(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.NUMBER_VALIDATION);
        
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "10.5");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testSingleNumberParameterValidValueWithMinMaxSet(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.NUMBER_VALIDATION);
        cp.setMinValue(1.5);
        cp.setMaxValue(6.3);
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "4.2");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals(null, er);
    }
    
    @Test
    public void testSingleNumberParameterValidValueWithValueBelowMin(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.NUMBER_VALIDATION);
        cp.setMinValue(9.6);
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "8.9");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("8.9 is less then minimum value: 9.6 allowed for this parameter", er.getDescription());
    }
    
     @Test
    public void testSingleNumberParameterValidValueWithValueAboveMax(){
        CommunityDetectionAlgorithm cda = new CommunityDetectionAlgorithm();
        cda.setName("somealgo");
        HashSet<CustomParameter> aParams = new HashSet<>();
        CustomParameter cp = new CustomParameter();
        cp.setName("--somearg");
        cp.setType(CustomParameter.VALUE_TYPE);
        cp.setValidationType(CustomParameter.NUMBER_VALIDATION);
        cp.setMaxValue(7.2);
        aParams.add(cp);
       
        cda.setCustomParameters(aParams);
        
        CommunityDetectionRequest cdr = new CommunityDetectionRequest();
        HashMap<String, String> cParams = new HashMap<>();
        cParams.put("--somearg", "8.3");
        cdr.setCustomParameters(cParams);
        cdr.setData(new TextNode("hi"));
        CommunityDetectionRequestValidatorImpl validator = new CommunityDetectionRequestValidatorImpl();
        ErrorResponse er = validator.validateRequest(cda, cdr);
        assertEquals("8.3 is greater then maximum value: 7.2 allowed for this parameter", er.getDescription());
    }
    
}
