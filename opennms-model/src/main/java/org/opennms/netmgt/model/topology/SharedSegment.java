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

package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;

public class SharedSegment implements BridgeTopology{
    
    Integer m_designatedBridge;
    Set<String> m_macsOnSegment = new HashSet<String>();
    Set<BridgePort> m_portsOnSegment = new HashSet<BridgePort>();
    BroadcastDomain m_domain;
    
    public SharedSegment(){
        
    }
            
    //Constructor for single single port
    public SharedSegment(BroadcastDomain domain, BridgePort port, Set<String> macs) {
        m_domain =domain;
        m_designatedBridge = port.getNodeId();
        m_macsOnSegment = macs;
        m_portsOnSegment.add(port);
        m_domain.add(this);
    }

    //General constructor
    public SharedSegment(BroadcastDomain domain, Set<BridgePort> ports, Set<String> macs, Integer designatedBridge) {
        m_domain = domain;
        m_designatedBridge = designatedBridge;
        m_portsOnSegment = ports;
        m_macsOnSegment = macs;
        m_domain.add(this);
    }
        
    public boolean setDesignatedBridge(Integer designatedBridge) throws BridgeTopologyException {
        if (designatedBridge == null) 
            return false;
        if (m_designatedBridge == null)
            throw new BridgeTopologyException("DesignatedBridge cannot be null", this);
        if (m_designatedBridge.intValue() == designatedBridge.intValue())
            return true;
        if (getBridgePort(designatedBridge) != null ) {
            m_designatedBridge = designatedBridge;
            return true;
        }
        return false;
    }

    public Integer getFirstNoDesignatedBridge() {
        for (BridgePort port: m_portsOnSegment ) {
            if (port == null 
                    || port.getNodeId() == null
                    ||port.getBridgePort() == null) {
            continue;
            }
            if (m_designatedBridge == null || port.getNodeId().intValue() != m_designatedBridge.intValue()) {
                return port.getNodeId();
            }
        }
        return null;
    }

    public Integer getDesignatedBridge() {
        return m_designatedBridge;
    }

    public BridgePort getDesignatedPort() throws BridgeTopologyException {
        if (m_designatedBridge == null) {
            throw new BridgeTopologyException("Designated Bridge NodeId cannot be null", this);
        }
        BridgePort designatedbridge= getBridgePort(m_designatedBridge);
        if (designatedbridge == null) {
            throw new BridgeTopologyException("Designated BridgePort cannot be null", this);
        }
        return designatedbridge;
    }

    public boolean isEmpty() {
        return m_portsOnSegment.isEmpty();
    }

    public Set<BridgePort> getBridgePortsOnSegment() {
        return m_portsOnSegment;
    }        

    public List<BridgeBridgeLink> getBridgeBridgeLinks() throws BridgeTopologyException {
        BridgePort designatedBridge = getDesignatedPort();
        OnmsNode designatedNode = new OnmsNode();
        designatedNode.setId(designatedBridge.getNodeId());
        List<BridgeBridgeLink> links = new ArrayList<BridgeBridgeLink>();
        for (BridgePort port: m_portsOnSegment) {
            if (port.getNodeId().intValue()  == m_designatedBridge.intValue()) {
                continue;
            }
            BridgeBridgeLink link = new BridgeBridgeLink();
            OnmsNode node = new OnmsNode();
            node.setId(port.getNodeId());
            link.setNode(node);
            link.setBridgePort(port.getBridgePort());
            link.setBridgePortIfIndex(port.getBridgePortIfIndex());
            link.setBridgePortIfName(port.getBridgePortIfName());
            link.setVlan(port.getVlan());
            link.setDesignatedNode(designatedNode);
            link.setDesignatedPort(designatedBridge.getBridgePort());
            link.setDesignatedPortIfIndex(designatedBridge.getBridgePortIfIndex());
            link.setDesignatedPortIfName(designatedBridge.getBridgePortIfName());
            link.setDesignatedVlan(designatedBridge.getVlan());
            link.setBridgeBridgeLinkCreateTime(designatedBridge.getCreateTime());
            link.setBridgeBridgeLinkLastPollTime(designatedBridge.getPollTime());
            links.add(link);
        }
        return links;
    }


    public List<BridgeMacLink> getBridgeMacLinks() throws BridgeTopologyException {
    	List<BridgeMacLink> maclinks = new ArrayList<BridgeMacLink>();
    	for (String mac: m_macsOnSegment) {
    		for (BridgePort bp: m_portsOnSegment) {
    		    if (bp == null)
    	                throw new BridgeTopologyException("BridgePort on segment should not be null", this);
    		    maclinks.add(BridgePort.getBridgeMacLink(bp, mac));
    		}
    	}
        return maclinks;
    }
    
    public boolean noMacsOnSegment() {
        return m_macsOnSegment.isEmpty();
    }

    public Set<Integer> getBridgeIdsOnSegment() {
        Set<Integer> nodes = new HashSet<Integer>();
        for (BridgePort link : m_portsOnSegment) {
            if (link == null || link.getNodeId() == null)
                continue;
            nodes.add(link.getNodeId());
        }
        return nodes;
    }

    public void add(BridgeMacLink link) {
        m_macsOnSegment.add(link.getMacAddress());
        m_portsOnSegment.add(BridgePort.getBridgeFromBridgeMacLink(link));
    }

    public void add(BridgeBridgeLink link) {
        m_portsOnSegment.add(BridgePort.getFromBridgeBridgeLink(link));
        m_portsOnSegment.add(BridgePort.getFromDesignatedBridgeBridgeLink(link));
    }

    //   this=topSegment {tmac...} {(tbridge,tport)....}U{bridgeId, bridgeIdPortId} 
    //        |
    //     bridge Id
    //        |
    //      shared {smac....} {(sbridge,sport).....}U{bridgeId,bridgePort)
    //       | | |
    //       A B C
    //    move all the macs and port on shared
    //  ------> topSegment {tmac...}U{smac....} {(tbridge,tport)}U{(sbridge,sport).....}
    public boolean mergeBridge(SharedSegment shared, Integer bridgeId) throws BridgeTopologyException {
        if (bridgeId == null) {
            return false;
        }
        BridgePort toberemoved = getBridgePort(bridgeId);
        if (toberemoved == null) {
            return false;
        }
        BridgePort toberemovedonsegment = shared.getBridgePort(bridgeId); 
        if (toberemovedonsegment == null) {
            return false;
        }
    	Set<BridgePort> portsOnSegment = new HashSet<BridgePort>();
        for (BridgePort bp: m_portsOnSegment) {
        	if (   bp.getNodeId().intValue() == bridgeId.intValue()) {
        		continue;
        	}
        	portsOnSegment.add(bp);
        }
        for (BridgePort port: shared.getBridgePortsOnSegment()) {
            if ( port.getNodeId().intValue() == bridgeId.intValue()) {
                continue;
            }
            portsOnSegment.add(port);
        }
        m_portsOnSegment = portsOnSegment;
    	m_macsOnSegment.addAll(shared.getMacsOnSegment());    
    	return true;
    }

    public void retain(Set<String> macs, BridgePort dlink) {
        m_portsOnSegment.add(dlink);
        m_macsOnSegment.retainAll(macs);
    }
    
    public void assign(Set<String> macs, BridgePort dlink) {
        m_portsOnSegment.add(dlink);
        m_macsOnSegment = macs;
    }

    //FIXME what happens when i remove designated bridge?
    //FIXME and also if the shared is empty
    public boolean removeBridge(Integer bridgeId) throws BridgeTopologyException {
        if (bridgeId == null ) {
            return false;
        }
        if (m_portsOnSegment.isEmpty()) {
            return false;
        }
        BridgePort bridgetoremove = getBridgePort(bridgeId);
        if ( bridgetoremove == null ) {
            return false;
        }
        if (m_designatedBridge != null && m_designatedBridge.intValue() == bridgeId.intValue()) {
            m_designatedBridge = null;
        }
        return m_portsOnSegment.remove(bridgetoremove);
    }
    
    public void removeMacs(Set<String> mactoberemoved) {
        m_macsOnSegment.removeAll(mactoberemoved);
    }
    
    public Set<String> getMacsOnSegment() {
        return m_macsOnSegment;
    }

    public BroadcastDomain getBroadcastDomain() {
        return m_domain; 
    }

    public void setBroadcastDomain(BroadcastDomain domain) {
        m_domain = domain; 
    }

    public boolean containsMac(String mac) {
        return m_macsOnSegment.contains(mac);
    }

    public BridgePort getBridgePort(Integer nodeid) throws BridgeTopologyException {
        if (nodeid == null)
            return null;
        for (BridgePort port: m_portsOnSegment) {
            if (port == null) {
                throw new BridgeTopologyException("Shared Segment: BridgePort cannot be null.", this);
            }
            if (port.getNodeId() == null) {
                throw new BridgeTopologyException("Shared Segment: BridgePort nodeid cannot be null.", this);
            }
            if (port.getBridgePort() == null) {
                throw new BridgeTopologyException("Shared Segment: BridgePort bridgeport cannot be null.", this);
            }
            if ( port.getNodeId().intValue() == nodeid.intValue()) {
                return port;
            }
        }
        return null;        
    }
    
    public boolean containsPort(BridgePort port) {
        return m_portsOnSegment.contains(port);
    }
    
    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
            strbfr.append("segment ->\n:");
            strbfr.append(", designated bridge:[");
            strbfr.append(getDesignatedBridge());
            strbfr.append("]\n");
            for (BridgePort blink:  m_portsOnSegment) {
                if (blink == null) {
                    strbfr.append("bridge port:[null]");
                    strbfr.append("\n");
                } else {
                    strbfr.append(blink.printTopology());
                }
            }
            for (String mac: getMacsOnSegment()) {
                strbfr.append("segment mac:");
                strbfr.append(mac);
                strbfr.append("\n");
            }
            
            return strbfr.toString();    	
    }
}
