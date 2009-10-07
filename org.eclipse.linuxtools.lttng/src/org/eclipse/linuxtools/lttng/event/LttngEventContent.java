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
 * <b><u>LttngEventContent</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventContent.<p>
 */
public class LttngEventContent extends TmfEventContent {
    
    /**
     * Constructor with parameters.<p>
     * 
     * Content will be empty as no parsed content is given.
     * 
     * @param thisFormat    The LttngEventFormat relative to the JniEvent
     */
    public LttngEventContent(LttngEventFormat thisFormat) {
        super("", thisFormat);
    }

    /**
     * Constructor with parameters.<p>
     * 
     * @param thisFormat        The LttngEventFormat relative to this JniEvent
     * @param thisParsedContent The string content of the JniEvent, already parsed
     */
    public LttngEventContent(LttngEventFormat thisFormat, String thisParsedContent, TmfEventField[] thisFields)  {
        super(thisParsedContent, thisFormat);
        
        setFields(thisFields);
    }
    
    /**
     * Copy Constructor.<p>
     * 
     * @param oldContent  Content to copy from
     */
    public LttngEventContent(LttngEventContent oldContent) {
        this( (LttngEventFormat)oldContent.getFormat(), oldContent.getContent(), oldContent.getFields() );
    }
    
    /**
     * <b>*DEPRECATED*</b><br>Get all fields of the content.<p>
     * 
     * <b><u>DO NOT USE!</b></u> <br>
     * This function will disapears soon! Use getFields(LttngEvent) instead.<p>
     * 
     * If the content was not parsed, format.parse() will get called.<p>
     * 
     * @return An array of TmfEventFields
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

    /**
     * <b>*DEPRECATED*</b><br>Get a single field of the content, from its id (position).<p>
     * 
     * <b><u>DO NOT USE!</b></u> <br>
     * This function will disapears soon! Use getFields(LttngEvent) instead.<p>
     * 
     * If the content was not parsed, format.parse() will get called.<p>
     * 
     * @param id    The id of the field we want to obtain.
     * 
     * @return A single LttngEventField
     */
    @Override
    public LttngEventField getField(int id) {
        LttngEventField returnedField = null;
        
        if ( (id >= 0) && (id < this.getNbFields()) )
        {
            // Call getFields() of this object. Will parse if fields were'nt parse before.
            TmfEventField[] allFields = this.getFields();
            
            if ( allFields != null ) {
            	returnedField = (LttngEventField)allFields[id];
            }
        }
        return returnedField;
    }
    
    /**
     * Get all fields of the content.<p>
     * 
     * If the content was not parsed, format.parse(LttngEvent) will get called.<p>
     * 
     * @param thisEvent     The event to get the fields from
     * 
     * @return An array of TmfEventFields
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
     * Get a single field of the content, from its id (position).<p>
     * 
     * If the content was not parsed, format.parse(LttngEvent) will get called.<p>
     * 
     * @param thisEvent     The event to get the fields from
     * @param id    The id of the field we want to obtain.
     * 
     * @return A single LttngEventField
     */
    public LttngEventField getField(int id, LttngEvent thisEvent) {
        LttngEventField returnedField = null;
        
        if ( (id >= 0) && (id < this.getNbFields()) ) {
            // Call getFields() of this object. Will parse if fields were'nt parse before.
            TmfEventField[] allFields = this.getFields(thisEvent);
            
            if ( allFields != null ) {
            	returnedField = (LttngEventField)allFields[id];
            }
        }
        return returnedField;
    }
    

    /**
     * toString() method.
     * 
     * @return Attributes of the object concatenated in String
     */
    @Override
	public String toString() {
        return getContent().toString();
    }
}