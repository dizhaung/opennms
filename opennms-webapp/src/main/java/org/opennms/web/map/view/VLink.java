//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/*
 * Created on 8-mag-2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

import org.opennms.web.map.MapsConstants;

/**
 * @author antonio
 * 
 */

final public class VLink {
	private String elem1Type;
	private int elem1Id;
	private String elem2Type;
    private int elem2Id;	
	private int nodeid1;
	private int nodeid2;
    //the link type defined in the map properties file
	private int linkTypeId;
	
	private String linkStatusString;
	
    private String id;
	
	public VLink(int elem1Id, String elem1Type, int elem2Id, String elem2Type, int linkTypeId) {
		this.elem1Type = elem1Type;
		this.elem2Type = elem2Type;
        this.elem1Id = elem1Id;
        this.elem2Id = elem2Id;
        this.linkTypeId = linkTypeId;

 //       int id1=elem1Id;
 //       int id2=elem2Id;
//        String type1=elem1Type;
 //       String type2=elem2Type;
        String  a = elem1Id+elem1Type;
        String  b = elem2Id+elem2Type;
        String id = a + "-" + b;
        
        if (elem1Id > elem2Id) {
            id = b + "-" + a;
        }
        
        if (elem1Id == elem2Id && elem2Type.equals(MapsConstants.MAP_TYPE)) {
            id = b + "-" + a;
        }
		id = id+"-"+linkTypeId;
	}
	
	public String getLinkStatusString() {
	    return linkStatusString;
	}

    public void setLinkStatusString(String linkStatusString) {
        this.linkStatusString = linkStatusString;
    }

    /**
	 * Asserts if the links are linking the same elements without considering their statuses
	 */
	public boolean equals(Object otherLink) {
		if (!(otherLink instanceof VLink)) return false;
		VLink link = (VLink) otherLink;
		return ( getId().equals(link.getId()));
	}
	
		
	public int hashCode() {
		int molt1 = 11;
		if(elem1Type.equals(MapsConstants.NODE_TYPE))
			molt1 = 13;
		int molt2 = 15;
		if(elem2Type.equals(MapsConstants.NODE_TYPE))
			molt2 = 17;

		return (3*elem1Id)+(5*elem2Id)+(7*(linkTypeId+1))*molt1*molt2;
	}

	public String getFirst() {
		return elem1Id+elem1Type;
	}

	public String getSecond() {
		return elem2Id+elem2Type;
	}
	
	public int getLinkTypeId() {
		return linkTypeId;
	}
	
	public void setLinkTypeId(int typeId) {
		linkTypeId = typeId;
	}

	public String toString() {
			return ""+getFirst()+"-"+getSecond()+"-"+linkTypeId+"-"+linkStatusString+" hashCode:"+this.hashCode();
	}
	
    public String getId() {
		return id;
	}

    public int getFirstNodeid() {
        return nodeid1;
    }

    public void setFirstNodeid(int nodeid) {
        this.nodeid1 =nodeid;
    }

    public int getSecondNodeid() {
        return nodeid2;
    }

    public void setSecondNodeid(int nodeid) {
        this.nodeid2 =nodeid;
    }

}
