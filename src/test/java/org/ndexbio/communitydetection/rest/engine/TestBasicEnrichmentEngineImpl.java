/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ndexbio.communitydetection.rest.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.*;
import static org.junit.Assert.*;
import org.junit.Test;


/**
 *
 * @author churas
 */
public class TestBasicEnrichmentEngineImpl {
    
    public Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
    
    
    public TestBasicEnrichmentEngineImpl() {
    }
   
    @Test
    public void testfoo() throws Exception {
        assertEquals(1,1);
    }
}