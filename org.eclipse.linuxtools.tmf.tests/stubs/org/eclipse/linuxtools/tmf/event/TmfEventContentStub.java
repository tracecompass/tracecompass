/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

/**
 * <b><u>TmfEventContentStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventContentStub extends TmfEventContent {

	public TmfEventContentStub(TmfEvent parent, Object content) {
		super(parent, content);
	}

	public TmfEventContentStub(TmfEventContentStub other) {
		super(other);
	}

	@Override
	protected void parseContent() {
        Object field1 = new Integer(1);
        Object field2 = new Integer(-10);
        Object field3 = new Boolean(true);
        Object field4 = new String("some string");
        Object field5 = new TmfTimestamp(1, (byte) 2, 3);
        fFields = new Object[] { field1, field2, field3, field4, field5 };
	}
	
    @Override
    public TmfEventContent clone() {
    	TmfEventContentStub content = new TmfEventContentStub(this);
    	content.fRawContent = "Some content";
    	content.fFields = null;
		return content;
    }

    @Override
	public String toString() {
    	Object[] fields = getFields();
    	String result = "[TmfEventContentStub(";
    	for (int i = 0; i < fields.length; i++) {
    		result += fields[i].toString() + ",";
    	}
    	result += ")]";

    	return result;
    }
}
