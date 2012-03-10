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
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;

/**
 * <b><u>LttngEventContent</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventContent.<p>
 */
public class LttngEventContent extends TmfEventField {
    
    private LttngEvent fParentEvent;
    
    // Hash map that contain the (parsed) fields. This is the actual payload of the event.
    private HashMap<String, LttngEventField> fFieldsMap = new HashMap<String, LttngEventField>();
    
    /**
     * Default constructor.<p>
     * 
     */
    public LttngEventContent() {
        super(ITmfEventField.ROOT_FIELD_ID, null);
    }
    
    /**
     * Constructor with parameters.<p>
     * 
     * @param thisParent    Parent event for this content.
     * 
     * @see org.eclipse.linuxtools.lttng.core.event.LttngEvent
     */
    public LttngEventContent(LttngEvent thisParent) {
        super(ITmfEventField.ROOT_FIELD_ID, null);
        fParentEvent = thisParent;
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
        super(ITmfEventField.ROOT_FIELD_ID, null);
        fParentEvent = thisParent;
        fFieldsMap = thisContent;
    }
    
    /**
     * Copy Constructor.<p>
     * 
     * @param oldContent  Content to copy from
     */
    public LttngEventContent(LttngEventContent oldContent) {
        this((LttngEvent) oldContent.getEvent(), oldContent.getMapContent());
    }
    
	public synchronized LttngEvent getEvent() {
        return fParentEvent;
    }
    
    public synchronized void setEvent(LttngEvent newParent) {
        fParentEvent = newParent;
    }
    
    
    // *** VERIFY ***
    // These are not very useful, are they?

//    public LttngEventType getType() {
//        return (LttngEventType)fParentEvent.getType();
//    }
    
//    public void setType(LttngEventType newType) {
//        ((LttngEvent)fParentEvent).setType(newType);
//    }
    
    
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
    public Object[] getRawContent() {
        Object[] returnedContent = fFieldsMap.values().toArray(new Object[fFieldsMap.size()]);
        return returnedContent;
    }
    
    /**
     * Return currently parsed fields in the internal hashmap format.<p>
     * 
     * @return  Currently parsed fields.
     */
    public HashMap<String, LttngEventField> getMapContent() {
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
        int nbFields = fParentEvent.getType().getFieldNames().length;
        
        if (fFieldsMap.size() < nbFields) {
        	LttngEventField tmpField = null;
        	LttngEventType tmpType = (LttngEventType)fParentEvent.getType();
        	
	        for (int pos=0; pos < nbFields; pos++) {
	            String name = null;
	            LttngEvent lttngTmpEvent = (LttngEvent)getEvent(); //added for easier debugging
				JniEvent tmpEvent = (lttngTmpEvent).convertEventTmfToJni();
				
				// tmpEvent == null probably mean there is a discrepancy between Eclipse and C library
				// An error was probably printed in convertEventTmfToJni() already, but keep in mind this is SERIOUS
				if ( tmpEvent != null ) {
//					try {
						name = tmpType.getFieldName(pos);
					
						Object newValue = tmpEvent.parseFieldByName(name);
                        tmpField = new LttngEventField(name, newValue, null);
						fFieldsMap.put(name, tmpField);
//					}
//					catch (TmfNoSuchFieldException e) {
//						System.out.println("Invalid field position requested : " + pos + ", ignoring (getFields).");  //$NON-NLS-1$//$NON-NLS-2$
//					}
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
    public synchronized LttngEventField getField(int position) {
        LttngEventField returnedField = null;
        String label = null;
//		try {
			label = fParentEvent.getType().getFieldName(position);
			returnedField = (LttngEventField) this.getField(label);
//		} 
//		catch (TmfNoSuchFieldException e) {
//			System.out.println("Invalid field position requested : " + position + ", ignoring (getField).");  //$NON-NLS-1$//$NON-NLS-2$
//		}
        
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
    public synchronized LttngEventField getField(String name) {

        // Check for generic table header fields
        if (name.equals(LttngEventType.CONTENT_LABEL) || name.equals(ITmfEvent.EVENT_FIELD_CONTENT)) {
            return new LttngEventField(toString());
        } else if (name.equals(LttngEventType.MARKER_LABEL) || name.equals(ITmfEvent.EVENT_FIELD_TYPE)) {
            return new LttngEventField(fParentEvent.getType().getName());
        } else if (name.equals(LttngEventType.TRACE_LABEL) || name.equals(ITmfEvent.EVENT_FIELD_REFERENCE)) {
            return new LttngEventField(fParentEvent.getReference());
        } else if (name.equals(LttngEventType.TIMESTAMP_LABEL) || name.equals(ITmfEvent.EVENT_FIELD_TIMESTAMP)) {
            return new LttngEventField(fParentEvent.getTimestamp().toString());
        } else if (name.equals(ITmfEvent.EVENT_FIELD_SOURCE)) {
            return new LttngEventField(fParentEvent.getSource());
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
                    returnedField = new LttngEventField(name, newValue);
	                fFieldsMap.put(name, returnedField );
	            }
        	}
        }
        
        return returnedField;
    }
    
//    // *** VERIFY ***
//    // *** Is this even useful?
//    protected void parseContent() {
//        fSubfields = getFields();
//    }
    
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
		LttngEventField[] subfields = new LttngEventField[fields.length];
		for (int i = 0; i < fields.length; i++) {
		    subfields[i] = (LttngEventField) fields[i].clone();
		}
		clone.setValue(getValue(), subfields);
		clone.fFieldsMap = new HashMap<String, LttngEventField>();
		for (String key : fFieldsMap.keySet()) {
			clone.fFieldsMap.put(key, ((LttngEventField) fFieldsMap.get(key)).clone());
		}
		return clone;
	}

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fFieldsMap == null) ? 0 : fFieldsMap.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof LttngEventContent)) {
            return false;
        }
        LttngEventContent other = (LttngEventContent) obj;
        if (fFieldsMap == null) {
            if (other.fFieldsMap != null) {
                return false;
            }
        } else if (!fFieldsMap.equals(other.fFieldsMap)) {
            return false;
        }
        return true;
    }

}