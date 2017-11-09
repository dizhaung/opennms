/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.config.EnhancedLinkdConfigManager;
import org.opennms.netmgt.config.enlinkd.EnlinkdConfiguration;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.topology.Bridge;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
public class BroadcastDomainTest extends EnLinkdTestHelper {

    EnhancedLinkd linkd;

    private static String location = "default";
            
    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.opennms.netmgt.model.topology", "DEBUG");
        MockLogAppender.setupLogging(p);
        linkd = new EnhancedLinkd();
        EnhancedLinkdConfig config = new EnhancedLinkdConfigManager() {
            
            @Override
            public void save() throws IOException {
                
            }
            
            @Override
            public void reload() throws IOException {
                m_config = new EnlinkdConfiguration();
                m_config.setInitialSleepTime(1000L);
                m_config.setRescanInterval(10000L);
            }
            
            @Override
            protected void saveXml(String xml) throws IOException {
            }
        };
        config.reload();
        linkd.setLinkdConfig(config);
    }

    @Test
    public void testOneBridgeOnePortOneMac() throws Exception {
        OneBridgeOnePortOneMacTopology topology = new OneBridgeOnePortOneMacTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();
        
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getId().intValue());
        topology.check(ndbt.getDomain());

    }

    @Test
    public void testOneBridgeMoreMacOnePort() throws Exception {

        OneBridgeMoreMacOnePortTopology topology = new OneBridgeMoreMacOnePortTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testOneBridgeComplete() throws Exception {

        OneBridgeCompleteTopology topology = new OneBridgeCompleteTopology();        
        
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testTwoConnectedBridge() throws Exception {

        TwoConnectedBridgeTopology topology = new TwoConnectedBridgeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();
        
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getId().intValue());
        topology.check(ndbt.getDomain(),false);
        
        domain.hierarchySetUp(domain.getBridge(topology.nodeBId));
        assertEquals(topology.nodeBId.intValue(), domain.getRootBridge().getId().intValue());
        topology.check(ndbt.getDomain(),true);
        
    }

    @Test
    public void testTwoMergeBridge() throws Exception {
        TwoMergeBridgeTopology topology = new TwoMergeBridgeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test 
    public void testTwoBridgeWithBackbonePorts() throws BridgeTopologyException {
        TwoBridgeWithBackbonePortsTopology topology = new TwoBridgeWithBackbonePortsTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testCleanTopology() throws BridgeTopologyException {
        TwoBridgeWithBackbonePortsTopology topology = new TwoBridgeWithBackbonePortsTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();
        
        domain.clearTopologyForBridge(topology.nodeBId);
        assertEquals(topology.nodeAId.intValue(), domain.getRootBridge().getId().intValue());
        assertEquals(2, domain.getTopology().size());
        assertEquals(5, domain.getMacsOnDomain().size());
        for (SharedSegment segment: domain.getTopology()) {
        	assertEquals(0, segment.getBridgeBridgeLinks().size());
        	assertEquals(1, segment.getBridgeIdsOnSegment().size());
        	assertEquals(topology.nodeAId.intValue(), segment.getBridgeIdsOnSegment().iterator().next().intValue());
        	
        	if (segment.containsMac(topology.macA11) && segment.containsMac(topology.macA12)) {
            	assertEquals(2, segment.getMacsOnSegment().size());
            	assertEquals(2, segment.getBridgeMacLinks().size());
        		for (BridgeMacLink link: segment.getBridgeMacLinks()) {
        			assertEquals(topology.portA1.intValue(), link.getBridgePort().intValue());
        		}
        	} else if (segment.containsMac(topology.macB21) 
        			&& segment.containsMac(topology.macB22) 
        			&& segment.containsMac(topology.macAB)){
            	assertEquals(3, segment.getMacsOnSegment().size());
            	assertEquals(3, segment.getBridgeMacLinks().size());
        		for (BridgeMacLink link: segment.getBridgeMacLinks()) {
        			assertEquals(topology.portAB.intValue(), link.getBridgePort().intValue());
        		}
        		
        	} else {
        		assertEquals(true, false);
        	}
        }
        
    }
    
    @Test 
    public void testCleanTopologyRoot() throws BridgeTopologyException {
        TwoBridgeWithBackbonePortsTopology topology = new TwoBridgeWithBackbonePortsTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();
                
        domain.clearTopologyForBridge(topology.nodeAId);
        assertEquals(5, domain.getMacsOnDomain().size());
        assertEquals(topology.nodeBId.intValue(), domain.getRootBridge().getId().intValue());
        assertEquals(2, domain.getTopology().size());
        for (SharedSegment segment: domain.getTopology()) {
        	assertEquals(0, segment.getBridgeBridgeLinks().size());
        	assertEquals(1, segment.getBridgeIdsOnSegment().size());
        	assertEquals(topology.nodeBId.intValue(), segment.getBridgeIdsOnSegment().iterator().next().intValue());
        	
        	if (segment.containsMac(topology.macA11) && segment.containsMac(topology.macA12) 
        		&& segment.containsMac(topology.macAB)) {
            	assertEquals(3, segment.getMacsOnSegment().size());
            	assertEquals(3, segment.getBridgeMacLinks().size());
        		for (BridgeMacLink link: segment.getBridgeMacLinks()) {
        			assertEquals(topology.portBA.intValue(), link.getBridgePort().intValue());
        		}
        	} else if (segment.containsMac(topology.macB21) 
        			&& segment.containsMac(topology.macB22) 
        			){
            	assertEquals(2, segment.getMacsOnSegment().size());
            	assertEquals(2, segment.getBridgeMacLinks().size());
        		for (BridgeMacLink link: segment.getBridgeMacLinks()) {
        			assertEquals(topology.portB2.intValue(), link.getBridgePort().intValue());
        		}
        	} else {
        		assertEquals(true, false);
        	}
        	
        }
    }

    @Test 
    public void testTwoBridgeWithBackbonePortsUsingBridgeAddressInBft() throws BridgeTopologyException {
        TwoBridgeWithBackbonePortsTopologyWithBridgeinBft topology = new TwoBridgeWithBackbonePortsTopologyWithBridgeinBft();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeBId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();
        
        assertEquals(topology.nodeBId.intValue(), domain.getRootBridge().getId().intValue());
        topology.check(ndbt.getDomain());
    }

    @Test 
    public void testTwoBridgeOneCalculation() throws BridgeTopologyException {

        TwoNodeTopology topology = new TwoNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        assertEquals(topology.nodeBId, ndbt.getDomain().getRootBridgeId());
        topology.check2nodeTopology(ndbt.getDomain(),true);
    }
    

    @Test 
    public void testTwoBridgeTwoCalculation() throws BridgeTopologyException {

        TwoNodeTopology topology = new TwoNodeTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();
        
        List<SharedSegment> shsegs = ndbt.getDomain().getTopology();
        assertEquals(3, shsegs.size());
        
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.check2nodeTopology(ndbt.getDomain(),true);
        assertEquals(topology.nodeBId, domain.getRootBridgeId());
    }

    @Test 
    public void testTwoBridgeTwoCalculationReverse() throws BridgeTopologyException {

        TwoNodeTopology topology = new TwoNodeTopology();
        
        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();
        
        List<SharedSegment> shsegs = ndbt.getDomain().getTopology();
        assertEquals(3, shsegs.size());
        
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.check2nodeTopology(ndbt.getDomain(),false);
        assertEquals(topology.nodeAId, domain.getRootBridgeId());
    }

    @Test
    public void testAB() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, 
        		new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain());

    }

    @Test
    public void testBA() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain());
    }

    @Test
    public void testAC() throws BridgeTopologyException {

        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain());
    }

    @Test
    public void testCA() throws BridgeTopologyException {

        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);
        
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain());
    }

    @Test
    public void testBC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.checkBC(ndbt.getDomain());
    }

    @Test
    public void testCB() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.checkBC(ndbt.getDomain());
    }

    @Test
    public void testABC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.check(ndbt.getDomain());

    }

    @Test
    public void testAThenBC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.setBridgeElements(topology.elemlist);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.check(ndbt.getDomain());
    }

    @Test
    public void testACThenB() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.checkAC(ndbt.getDomain());

        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        topology.check(ndbt.getDomain());

    }

    @Test
    public void testBAThenC() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        topology.checkAB(ndbt.getDomain());

        domain.addBridge(new Bridge(topology.nodeCId));
        domain.setBridgeElements(topology.elemlist);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.calculate();

        topology.check(ndbt.getDomain());

    }

    @Test
    public void testBThenCA() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();

        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();
        domain.hierarchySetUp(domain.getBridge(topology.nodeAId));
        topology.check(ndbt.getDomain());
    }

    @Test
    public void testCThenAB() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.calculate();

        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();
        domain.hierarchySetUp(domain.getBridge(topology.nodeAId));

        topology.check(ndbt.getDomain());
    }

    @Test
    public void testCBThenA() throws BridgeTopologyException {
        ABCTopology topology = new ABCTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbt.calculate();
        topology.checkBC(ndbt.getDomain());

        domain.addBridge(new Bridge(topology.nodeAId));
        domain.setBridgeElements(topology.elemlist);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbt.calculate();

        domain.hierarchySetUp(domain.getBridge(topology.nodeAId));
        topology.check(ndbt.getDomain());

    }

    @Test
    public void testDE() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.calculate();
        
        topology.checkDE(ndbt.getDomain());
    }

    @Test
    public void testDF() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkDF(ndbt.getDomain());
    }

    @Test
    public void testEF() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkEF(ndbt.getDomain());
    }

    @Test
    public void testDG() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeGId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeGId),topology.bftG);
        ndbt.calculate();
        
        topology.checkDG(ndbt.getDomain());
    }

    @Test
    public void testDEF() throws BridgeTopologyException {

        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkDEF(ndbt.getDomain());

    }

    @Test
    public void testDFThenE() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.calculate();
        
        topology.checkDF(ndbt.getDomain());
        
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.calculate();
        
        topology.checkDEF(ndbt.getDomain());

    }

    @Test 
    public void testDEFG() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.addBridge(new Bridge(topology.nodeGId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeGId),topology.bftG);
        ndbt.calculate();
        
        topology.checkDEFG(ndbt.getDomain());

    }

    @Test 
    public void testDEFGHIL() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.addBridge(new Bridge(topology.nodeGId));
        domain.addBridge(new Bridge(topology.nodeHId));
        domain.addBridge(new Bridge(topology.nodeIId));
        domain.addBridge(new Bridge(topology.nodeLId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeGId),topology.bftG);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeHId),topology.bftH);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeIId),topology.bftI);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeLId),topology.bftL);
        ndbt.calculate();
        
        topology.check(ndbt.getDomain().getTopology());

    }


    @Test 
    public void testHierarchySetUp() throws BridgeTopologyException {
        DEFGHILTopology topology = new DEFGHILTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.addBridge(new Bridge(topology.nodeFId));
        domain.addBridge(new Bridge(topology.nodeGId));
        domain.addBridge(new Bridge(topology.nodeHId));
        domain.addBridge(new Bridge(topology.nodeIId));
        domain.addBridge(new Bridge(topology.nodeLId));
        domain.setBridgeElements(topology.elemlist);
        NodeDiscoveryBridgeTopology ndbt= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
               ndbt.setDomain(domain);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeFId),topology.bftF);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeGId),topology.bftG);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeHId),topology.bftH);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeIId),topology.bftI);
        ndbt.addUpdatedBFT(domain.getBridge(topology.nodeLId),topology.bftL);
        ndbt.calculate();
        
        topology.check(ndbt.getDomain().getTopology());
        
        domain.hierarchySetUp(domain.getBridge(topology.nodeGId));
        assertEquals(topology.nodeGId, ndbt.getDomain().getRootBridgeId());
        assertEquals(true, ndbt.getDomain().getBridge(topology.nodeGId).isRootBridge());
        assertEquals(null, ndbt.getDomain().getBridge(topology.nodeGId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeDId).isRootBridge());
        assertEquals(topology.portDD, ndbt.getDomain().getBridge(topology.nodeDId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeEId).isRootBridge());
        assertEquals(topology.portEE, ndbt.getDomain().getBridge(topology.nodeEId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeFId).isRootBridge());
        assertEquals(topology.portFF, ndbt.getDomain().getBridge(topology.nodeFId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeHId).isRootBridge());
        assertEquals(topology.portHH, ndbt.getDomain().getBridge(topology.nodeHId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeIId).isRootBridge());
        assertEquals(topology.portII, ndbt.getDomain().getBridge(topology.nodeIId).getRootPort());
        assertEquals(false, ndbt.getDomain().getBridge(topology.nodeLId).isRootBridge());
        assertEquals(topology.portLL, ndbt.getDomain().getBridge(topology.nodeLId).getRootPort());
    }

    @Test
    public void testFiveSwitchTopologyBCADE() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbtB= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeBId, null, null, null,location));
        ndbtB.setDomain(domain);
        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        
        NodeDiscoveryBridgeTopology ndbtC= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeCId, null, null, null,location));
        ndbtC.setDomain(domain);
        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        NodeDiscoveryBridgeTopology ndbtA= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbtA.setDomain(domain);
        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        NodeDiscoveryBridgeTopology ndbtD= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbtD.setDomain(domain);
        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        NodeDiscoveryBridgeTopology ndbtE= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeEId, null, null, null,location));
        ndbtE.setDomain(domain);
        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyBCAED() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbtB= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeBId, null, null, null,location));
        ndbtB.setDomain(domain);
        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        
        NodeDiscoveryBridgeTopology ndbtC= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeCId, null, null, null,location));
        ndbtC.setDomain(domain);
        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        NodeDiscoveryBridgeTopology ndbtA= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbtA.setDomain(domain);
        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        NodeDiscoveryBridgeTopology ndbtE= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeEId, null, null, null,location));
        ndbtE.setDomain(domain);
        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        NodeDiscoveryBridgeTopology ndbtD= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbtD.setDomain(domain);
        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        topology.check(domain);
    }


    @Test
    public void testFiveSwitchTopologyEDCBA() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);

        NodeDiscoveryBridgeTopology ndbtE= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeEId, null, null, null,location));
        ndbtE.setDomain(domain);
        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        
        NodeDiscoveryBridgeTopology ndbtD= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbtD.setDomain(domain);
        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        
        NodeDiscoveryBridgeTopology ndbtC= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeCId, null, null, null,location));
        ndbtC.setDomain(domain);
        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        NodeDiscoveryBridgeTopology ndbtB= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeBId, null, null, null,location));
        ndbtB.setDomain(domain);
        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        NodeDiscoveryBridgeTopology ndbtA= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbtA.setDomain(domain);
        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        Bridge bridgeB = domain.getBridge(topology.nodeBId);
        domain.hierarchySetUp(bridgeB);
        topology.check(domain);
    }

    @Test
    public void testFiveSwitchTopologyBEDCA() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbtB= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeBId, null, null, null,location));
        ndbtB.setDomain(domain);
        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        NodeDiscoveryBridgeTopology ndbtE= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeEId, null, null, null,location));
        ndbtE.setDomain(domain);
        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        NodeDiscoveryBridgeTopology ndbtD= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbtD.setDomain(domain);
        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        NodeDiscoveryBridgeTopology ndbtC= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeCId, null, null, null,location));
        ndbtC.setDomain(domain);
        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        NodeDiscoveryBridgeTopology ndbtA= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbtA.setDomain(domain);
        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        topology.check(domain);

    }

    @Test
    public void testFiveSwitchTopologyEDCAB() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbtE= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeEId, null, null, null,location));
        ndbtE.setDomain(domain);
        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        NodeDiscoveryBridgeTopology ndbtD= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbtD.setDomain(domain);
        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        NodeDiscoveryBridgeTopology ndbtC= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeCId, null, null, null,location));
        ndbtC.setDomain(domain);
        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        NodeDiscoveryBridgeTopology ndbtA= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbtA.setDomain(domain);
        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        NodeDiscoveryBridgeTopology ndbtB= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeBId, null, null, null,location));
        ndbtB.setDomain(domain);
        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        Bridge bridgeB = domain.getBridge(topology.nodeBId);
        domain.hierarchySetUp(bridgeB);
        topology.check(domain);

    }

    @Test
    public void testFiveSwitchTopologyBCADEBD() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbtB= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeBId, null, null, null,location));
        ndbtB.setDomain(domain);
        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        
        NodeDiscoveryBridgeTopology ndbtC= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeCId, null, null, null,location));
        ndbtC.setDomain(domain);
        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        NodeDiscoveryBridgeTopology ndbtA= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbtA.setDomain(domain);
        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        NodeDiscoveryBridgeTopology ndbtD= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbtD.setDomain(domain);
        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        NodeDiscoveryBridgeTopology ndbtE= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeEId, null, null, null,location));
        ndbtE.setDomain(domain);
        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        topology.check(domain);

        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        topology.check(domain);

        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        topology.check(domain);

    }
    
    @Test
    public void testFiveSwitchTopologyBEDCADBAEC() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbtB= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeBId, null, null, null,location));
        ndbtB.setDomain(domain);
        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        NodeDiscoveryBridgeTopology ndbtE= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeEId, null, null, null,location));
        ndbtE.setDomain(domain);
        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        NodeDiscoveryBridgeTopology ndbtD= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbtD.setDomain(domain);
        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        NodeDiscoveryBridgeTopology ndbtC= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeCId, null, null, null,location));
        ndbtC.setDomain(domain);
        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        NodeDiscoveryBridgeTopology ndbtA= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbtA.setDomain(domain);
        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        topology.check(domain);

        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        topology.check(domain);

        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        topology.check(domain);

        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();
        topology.check(domain);

        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        topology.check(domain);

        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();
        topology.check(domain);
        
    }
    
    
    @Test
    public void testFiveSwitchTopologyBEDCABEDAC() throws BridgeTopologyException {

        FiveSwitchTopology topology = new FiveSwitchTopology();

        BroadcastDomain domain = new BroadcastDomain();
        domain.addBridge(new Bridge(topology.nodeAId));
        domain.addBridge(new Bridge(topology.nodeBId));
        domain.addBridge(new Bridge(topology.nodeCId));
        domain.addBridge(new Bridge(topology.nodeDId));
        domain.addBridge(new Bridge(topology.nodeEId));
        domain.setBridgeElements(topology.elemlist);
        
        NodeDiscoveryBridgeTopology ndbtB= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeBId, null, null, null,location));
        ndbtB.setDomain(domain);
        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();

        NodeDiscoveryBridgeTopology ndbtE= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeEId, null, null, null,location));
        ndbtE.setDomain(domain);
        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();

        NodeDiscoveryBridgeTopology ndbtD= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeDId, null, null, null,location));
        ndbtD.setDomain(domain);
        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();

        NodeDiscoveryBridgeTopology ndbtC= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeCId, null, null, null,location));
        ndbtC.setDomain(domain);
        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();

        NodeDiscoveryBridgeTopology ndbtA= new NodeDiscoveryBridgeTopology(linkd, new Node(topology.nodeAId, null, null, null,location));
        ndbtA.setDomain(domain);
        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();

        topology.check(domain);

        ndbtB.addUpdatedBFT(domain.getBridge(topology.nodeBId),topology.bftB);
        ndbtB.calculate();
        topology.check(domain);

        ndbtE.addUpdatedBFT(domain.getBridge(topology.nodeEId),topology.bftE);
        ndbtE.calculate();
        topology.check(domain);

        ndbtD.addUpdatedBFT(domain.getBridge(topology.nodeDId),topology.bftD);
        ndbtD.calculate();
        topology.check(domain);

        ndbtA.addUpdatedBFT(domain.getBridge(topology.nodeAId),topology.bftA);
        ndbtA.calculate();
        topology.check(domain);

        ndbtC.addUpdatedBFT(domain.getBridge(topology.nodeCId),topology.bftC);
        ndbtC.calculate();
        topology.check(domain);
        
    }


}
