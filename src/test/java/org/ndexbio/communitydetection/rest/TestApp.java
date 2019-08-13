package org.ndexbio.communitydetection.rest;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author churas
 */
public class TestApp {
    
    @Test
    public void testGenerateExampleConfiguration() throws Exception{
        String res = App.generateExampleConfiguration();
        assertTrue(res.contains("# Example configuration file for Enrichment service"));
    }
    
    @Test
    public void testExampleModes(){
        String[] args = {"--mode", App.EXAMPLE_CONF_MODE};
        App.main(args);
        String[] oargs = {"--mode", App.EXAMPLE_DBRES_MODE};
        App.main(oargs);
    }
}
