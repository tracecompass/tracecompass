/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.event;

import org.eclipse.linuxtools.tmf.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.event.TmfEventField;

/**
 * <b><u>LttngEventContent</u></b>
 * <p>
 * Lttng specific implementation of the TmfEventContent
 * <p>
 * Lttng LttngEventContent is very similar from TMF basic one. <br>
 */
public class LttngEventContent extends TmfEventContent {
    
    /**
     * Constructor with parameters<br>
     * <br>
     * Content will be null as no parsed content is given.
     * 
     * @param thisFormat    The LttngEventFormat relative to the JniEvent
     */
    public LttngEventContent(LttngEventFormat thisFormat) {
        super(null, thisFormat);
    }

    /**
     * Constructor with parameters<br>
     * 
     * @param thisFormat        The LttngEventFormat relative to this JniEvent
     * @param thisParsedContent The string content of the JniEvent, already parsed
     * 
     */
    public LttngEventContent(LttngEventFormat thisFormat, String thisParsedContent, LttngEventField[] thisFields) {
        super(thisParsedContent, thisFormat);
        setFields(thisFields);
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.event.TmfEventContent#getFields()
     */
    @Override
	public TmfEventField[] getFields() {
    	
    	// Request the field variable from the inherited class
		TmfEventField[] fields = super.getFields();
        
        // Field may be null if the content hasn't been parse yet
        // If that's the case, call the parsing function
        if (fields == null) {
            fields = ((LttngEventFormat) this.getFormat()).parse(this.getContent());
            setFields(fields);
        }
        return fields;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.event.TmfEventContent#getField(int)
     */
    @Override
    public LttngEventField getField(int id) {
        assert id >= 0 && id < this.getNbFields();
        
        LttngEventField returnedField = null;
		TmfEventField[] allFields = this.getFields();
        
        if ( allFields != null ) {
			returnedField = (LttngEventField) allFields[id];
        }
        
        return returnedField;
    }
    
    /**
     * @param thisEvent
     * @return
     */
	public TmfEventField[] getFields(LttngEvent thisEvent) {
    	
    	// Request the field variable from the inherited class
		TmfEventField[] fields = super.getFields();
        
        // Field may be null if the content hasn't been parse yet
        // If that's the case, call the parsing function
        if (fields == null) {
            fields = ((LttngEventFormat)this.getFormat()).parse(thisEvent);
            setFields(fields);
        }
        return fields;
    }
    
    /**
     * @param id
     * @param thisEvent
     * @return
     */
    public LttngEventField getField(int id, LttngEvent thisEvent) {
        assert id >= 0 && id < this.getNbFields();
        
        LttngEventField returnedField = null;
		TmfEventField[] allFields = this.getFields(thisEvent);
        
        if ( allFields != null ) {
			returnedField = (LttngEventField) allFields[id];
        }
        
        return returnedField;
    }
    

    /**
     * basic toString() method.
     * 
     * @return Attributes of the object concatenated in String
     */
    @Override
	public String toString() {
        return getContent().toString();
    }
}