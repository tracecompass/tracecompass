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

package org.eclipse.linuxtools.lttng.core.event;

import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.core.event.TmfNoSuchFieldException;

/**
 * <b><u>LttngEventContent</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventContent.<p>
 */
public class LttngEventContent extends TmfEventContent {
    
    // Hash map that contain the (parsed) fields. This is the actual payload of the event.
    HashMap<String, LttngEventField> fFieldsMap = new HashMap<String, LttngEventField>();
    
    /**
     * Default constructor.<p>
     * 
     * 
     */
    public LttngEventContent() {
        super(null, null);
    }
    
    /**
     * Constructor with parameters.<p>
     * 
     * @param thisParent    Parent event for this content.
     * 
     * @see org.eclipse.linuxtools.lttng.core.event.LttngEvent
     */
    public LttngEventContent(LttngEvent thisParent) {
        super(thisParent, null);
    }
    
    /**
     * Constructor with parameters, with optional content.<p>
     * 
     * @param thisParent    Parent event for this content.
     * @param thisContent   Already parsed content.
     * 
     * @see org.eclipse.linuxtools.lttng.core.event.LttngEvent
     */
    public LttngEventContent(LttngEvent thisParent, HashMap<String, LttngEventField> thisContent) {
        super(thisParent, null);
        
        fFieldsMap = thisContent;
    }
    
    /**
     * Copy Constructor.<p>
     * 
     * @param oldContent  Content to copy from
     */
    public LttngEventContent(LttngEventContent oldContent) {
        this((LttngEvent)oldContent.getEvent(), oldContent.getRawContent() );
    }
    
    
    @Override
	public LttngEvent getEvent() {
        return (LttngEvent)fParentEvent;
    }
    
    public void setEvent(LttngEvent newParent) {
        fParentEvent = newParent;
    }
    
    
    // *** VERIFY ***
    // These are not very useful, are they?
    @Override
	public LttngEventType getType() {
        return (LttngEventType)fParentEvent.getType();
    }
    public void setType(LttngEventType newType) {
        ((LttngEvent)fParentEvent).setType(newType);
    }
    
    
    // ***TODO***
    // Find a better way to ensure content is sane!!
    public void emptyContent() {
        fFieldsMap.clear();
    }
    
    // ***VERIFY***
    // A bit weird to return the _currently_parsed fields (unlike all fields like getFields() )
    // Should we keep this?
    /**
     * Return currently parsed fields in an object array format.<p>
     * 
     * @return  Currently parsed fields.
     */
    @Override
    public Object[] getContent() {
        Object[] returnedContent = fFieldsMap.values().toArray( new Object[fFieldsMap.size()] );
        
        return returnedContent;
    }
    
    /**
     * Return currently parsed fields in the internal hashmap format.<p>
     * 
     * @return  Currently parsed fields.
     */
    public HashMap<String, LttngEventField> getRawContent() {
        return fFieldsMap;
    }
    
//    @SuppressWarnings("unchecked")
//    @Override
//    public LttngEventField[] getFields() {
//        LttngEventField tmpField = null;
//        
//        // *** TODO ***
//        // SLOW! SLOW! SLOW! We should prevent the user to use this!!
//        HashMap<String, Object> parsedContent = parseContent();
//        
//        String contentKey = null;
//        Iterator<String> contentItr = parsedContent.keySet().iterator();
//        while ( contentItr.hasNext() ) {
//            contentKey = contentItr.next();
//            
//            tmpField = new LttngEventField(this, contentKey, parsedContent.get(contentKey));
//            ((HashMap<String, LttngEventField>)fFields).put(contentKey, tmpField);
//        }
//        
//        return fFields.values().toArray(new LttngEventField[fFields.size()]);
//    }
    
    /**
     * Parse all fields and return them as an array of LttngFields.<p>
     * 
     * Note : This function is heavy and should only be called if all fields are really needed.
     * 
     * @return  All fields.
     * 
     * @see @see org.eclipse.linuxtools.lttng.event.LttngEventField
     */
    @Override
    public synchronized LttngEventField[] getFields() {
        if ( fFieldsMap.size() < fParentEvent.getType().getNbFields() ) {
        	LttngEventField tmpField = null;
        	LttngEventType tmpType = (LttngEventType)fParentEvent.getType();
        	
	        for ( int pos=0; pos<tmpType.getNbFields(); pos++ ) {
	            String name = null;
	            LttngEvent lttngTmpEvent = (LttngEvent)getEvent(); //added for easier debugging
				JniEvent tmpEvent = (lttngTmpEvent).convertEventTmfToJni();
				
				// tmpEvent == null probably mean there is a discrepancy between Eclipse and C library
				// An error was probably printed in convertEventTmfToJni() already, but keep in mind this is SERIOUS
				if ( tmpEvent != null ) {
					try {
						name = tmpType.getFieldLabel(pos);
					
						Object newValue = tmpEvent.parseFieldByName(name);
						tmpField = new LttngEventField(this, name, newValue );
						fFieldsMap.put(name, tmpField);
					}
					catch (TmfNoSuchFieldException e) {
						System.out.println("Invalid field position requested : " + pos + ", ignoring (getFields).");  //$NON-NLS-1$//$NON-NLS-2$
					}
	            }
	        }
        }
        return fFieldsMap.values().toArray(new LttngEventField[fFieldsMap.size()]);
    }
    
    /**
     * Parse a single field from its given position.<p>
     * 
     * @return  The parsed field or null.
     * 
     * @see @see org.eclipse.linuxtools.lttng.event.LttngEventField
     */
    @Override
    public LttngEventField getField(int position) {
        LttngEventField returnedField = null;
        String label = null;
		try {
			label = fParentEvent.getType().getFieldLabel(position);
			
			returnedField = (LttngEventField) this.getField(label);
		} 
		catch (TmfNoSuchFieldException e) {
			System.out.println("Invalid field position requested : " + position + ", ignoring (getField).");  //$NON-NLS-1$//$NON-NLS-2$
		}
        
        return returnedField;
    }
    
    /**
     * Parse a single field from its given name.<p>
     * 
     * @return  The parsed field or null.
     * 
     * @see @see org.eclipse.linuxtools.lttng.event.LttngEventField
     */
    @Override
    public synchronized Object getField(String name) {

        // Check for generic table header fields
        if (name.equals(LttngEventType.CONTENT_LABEL) || name.equals(FIELD_ID_CONTENT)) {
            return fParentEvent.getContent().toString();
        } else if (name.equals(LttngEventType.MARKER_LABEL) || name.equals(FIELD_ID_TYPE)) {
            return fParentEvent.getType().getId();
        } else if (name.equals(LttngEventType.TRACE_LABEL) || name.equals(FIELD_ID_REFERENCE)) {
            return fParentEvent.getReference();
        } else if (name.equals(LttngEventType.TIMESTAMP_LABEL) || name.equals(FIELD_ID_TIMESTAMP)) {
            return new Long(fParentEvent.getTimestamp().getValue()).toString();
        } else if (name.equals(FIELD_ID_SOURCE)) {
            return fParentEvent.getSource();
        }

    	// *** VERIFY ***
        // Should we check if the field exists in LttngType before parsing? 
        // It could avoid calling parse for non-existent fields but would waste some cpu cycle on check?
        LttngEventField returnedField = fFieldsMap.get(name);
        
        if ( returnedField == null ) {
            // *** VERIFY ***
            // Should we really make sure we didn't get null before creating/inserting a field?
        	JniEvent tmpEvent = ((LttngEvent)getEvent()).convertEventTmfToJni();
        	
        	if ( tmpEvent != null) {
	        	Object newValue =  tmpEvent.parseFieldByName(name);
	            
	            if ( newValue!= null ) {
	                returnedField = new LttngEventField(this, name, newValue);
	                fFieldsMap.put(name, returnedField );
	            }
        	}
        }
        
        return returnedField;
    }
    
    // *** VERIFY ***
    // *** Is this even useful?
    @Override
    protected void parseContent() {
        fFields = getFields();
    }
    
    /**
     * toString() method to print the content
     * 
     * Note : this function parse all fields and so is very heavy to use.
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        LttngEventField[] allFields = getFields();
        
        StringBuffer strBuffer = new StringBuffer();
        for ( int pos=0; pos < allFields.length; pos++) {
        	if (pos != 0) strBuffer.append(",");
        	strBuffer.append(allFields[pos].toString());
        }
        
        return strBuffer.toString();
    }

	@Override
	public LttngEventContent clone() {
		LttngEventContent clone = (LttngEventContent) super.clone();
		LttngEventField[] fields = getFields();
		clone.fFields = new LttngEventField[fields.length];
		for (int i = 0; i < fields.length; i++) {
			clone.fFields[i] = fields[i].clone();
		}
		clone.fFieldsMap = new HashMap<String, LttngEventField>();
		for (String key : fFieldsMap.keySet()) {
			clone.fFieldsMap.put(new String(key), ((LttngEventField) fFieldsMap.get(key)).clone());
		}
		return clone;
	}

}