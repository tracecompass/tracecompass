package org.eclipse.linuxtools.lttng.jni;
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


import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.lttng.jni.exception.JniEventException;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.exception.JniNoSuchEventException;

/**
 * <b><u>JniEvent</u></b> <p>
 * 
 * A JniEvent has the actual content that got traced by Lttng.<br>
 * Provides access to the LttEvent C structure in java. <p>
 * 
 * Most important fields in the JniEvent are :
 * <ul>
 * <li>an event time, which is a digested timestamp.
 * </ul>
 * Note that the JniEvent content is not directly accessibe and should be obtained
 * using the parseAllFields() or parseFieldBy...() methods.
 * 
 * <b>NOTE</b><p>
 * This class is ABSTRACT, you need to extends it to support your specific LTTng version.<p>
 * 
 */
public abstract class JniEvent extends Jni_C_Common implements Comparable<JniEvent> 
{
    // Variables to detect if the event have been filled at least once
    // this make possible the detection of "uninitialized" struct in Ltt
    // Can be "EOK", "ERANGE" or "EPERM" (defined in Jni_C_Common)
    private int eventState = EPERM; // Start with EPERM to ensure sanity

    // Internal C pointer of the JniEvent used in LTT
    private Jni_C_Pointer_And_Library_Id thisEventPtr = new Jni_C_Pointer_And_Library_Id();

    // Reference to the parent tracefile
    private JniTracefile parentTracefile = null;

    // This map hold marker relative to the parent tracefile of this event
    // They are "our" marker in this event
    private HashMap<Integer, JniMarker> markersMap = null;

    // Data we should populate from ltt
    // Note that all type have been scaled up as there is no "unsigned" in java
    // This might be a problem about "unsigned long" as there is no equivalent
    // in java
    private Jni_C_Pointer_And_Library_Id tracefilePtr = new Jni_C_Pointer_And_Library_Id();
    private JniTime eventTime = null;

    // These methods need a tracefile pointer, instead of a event pointer
    protected native int      ltt_readNextEvent(int libId, long tracefilePtr);
    protected native int      ltt_seekEvent(int libId, long tracefilePtr, JniTime givenTime);
    protected native int      ltt_positionToFirstEvent(int libId, long tracefilePtr);
        
    // Native access functions
    protected native long     ltt_getTracefilePtr(int libId, long eventPtr);
    protected native long     ltt_getBlock(int libId, long eventPtr);
    protected native long     ltt_getOffset(int libId, long eventPtr);
    protected native long     ltt_getCurrentTimestampCounter(int libId, long eventPtr);
    protected native long     ltt_getTimestamp(int libId, long eventPtr);
    protected native int      ltt_getEventMarkerId(int libId, long eventPtr);
    protected native long     ltt_getNanosencondsTime(int libId, long eventPtr);
    protected native void     ltt_feedEventTime(int libId, long eventPtr, JniTime eventTime);
    protected native long     ltt_getEventDataSize(int libId, long eventPtr);
    protected native long     ltt_getEventSize(int libId, long eventPtr);
    protected native int      ltt_getCount(int libId, long eventPtr);
    protected native long     ltt_getOverflowNanoSeconds(int libId, long eventPtr);
        
    // This method can be use to obtain the content as byte array
    // Warning : untested!
    protected native void     ltt_getDataContent(int libId, long eventPtr, long dataSize, byte[] returnedContent);
        
    // Debug native function, ask LTT to print event structure
    protected native void     ltt_printEvent(int libId, long eventPtr);
    
    /**
     * Default constructor is forbidden
     */
    protected JniEvent() {
    }

    /**
     * Copy constructor.<p>
     * 
     * @param oldEvent      Reference to the JniEvent you want to copy. 
     */
    public JniEvent(JniEvent oldEvent) {
        thisEventPtr = oldEvent.thisEventPtr;
        markersMap = oldEvent.markersMap;
        parentTracefile = oldEvent.parentTracefile;
        eventState = oldEvent.eventState;

        tracefilePtr = oldEvent.tracefilePtr;
        eventTime = oldEvent.eventTime;
    }
    
    /**
     * Constructor with parameters<p>
     * 
     * This constructor could throw. It will happen if an event can not be populated on <u>first read</u>.<br>
     * In that case, the parent tracefile is probably useless and should be deleted.
     * 
     * @param newEventPtr         C pointer (converted in long) of the LttEvent C structure.
     * @param newMarkersMap       Reference an already populated HashMap of JniMarker objects 
     * @param newParentTracefile  Reference to the parent JniTracefile of this JniEvent
     *            
     * @exception JniException
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     * @see org.eclipse.linuxtools.lttng.jni.JniMarker
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
    public JniEvent(Jni_C_Pointer_And_Library_Id newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException {
    	
        // Basic test to make sure we didn't get null/empty value 
        if ((newEventPtr.getPointer() == NULL)
                || (newMarkersMap == null) 
                || (newMarkersMap.size() == 0)
                || (newParentTracefile == null)) {
            throw new JniEventException("Null or empty value passed to constructor, object is invalid! (JniEvent)"); //$NON-NLS-1$
        }
        
        thisEventPtr = newEventPtr;
        tracefilePtr = newParentTracefile.getTracefilePtr();
        markersMap = newMarkersMap;
        parentTracefile = newParentTracefile;

        eventTime = new JniTime();
        
        // Try to move to the first event
        // If the event is Out of Range (ERANGE) at the first read, 
        //  this event type will never be usable.
        // In that case, throw JniNoSuchEventException to warn the tracefile.
        eventState = positionToFirstEvent();
        if (eventState != EOK)  {
            throw new JniNoSuchEventException("Object not populated, unusable. There is probably no event of that type in the trace. (JniEvent)"); //$NON-NLS-1$
        }
        else {
            populateEventInformation();
        }
    }

    /**
     * Move to the next event and populate the java object with LttEvent structure.<p>
     * 
     * If the move fails, the event will not get populated and the last event data will still be available.
     * 
     * @return LTT read status, as defined in Jni_C_Constant.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Constant
     */
     public int readNextEvent() {
        // Ask Ltt to read the next event for this particular tracefile
        eventState = ltt_readNextEvent(tracefilePtr.getLibraryId(), tracefilePtr.getPointer() );
        // If the event state is sane populate it
        if (eventState == EOK) {
            populateEventInformation();
        }
        
        return eventState;
    }

    /**
     * Seek to a certain time.<p>
     * 
     * Seek to a certain time and read event at this exact time or the next one if there is no event there.<p>
     * 
     * Note that this function can seek in an invalid position if the timestamp is after the last event.<br>
     * In that case, a seek back would be required to get back to a consistent state.<p>
     * 
     * If the seek fails, the event will not get populated and the last event data will still be available.<p>
     * 
     * @return LTT read status, as defined in Jni_C_Constant
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Constant
     */
    public int seekToTime(JniTime seekTime) {
        // Ask Ltt to read the next event for this particular tracefile
        eventState = ltt_seekEvent(tracefilePtr.getLibraryId(), tracefilePtr.getPointer(), seekTime);
        
        // If the event state is sane populate it
        if (eventState == EOK) {
            populateEventInformation();
        }

        return eventState;
    }

    /**
     * Try to seek to a certain time and seek back if it failed.<p>
     * 
     * Seek to a certain time and read event at this exact time or the next one if there is no event there.<p>
     * 
     * If the seek fails, we will seek back to the previous position, so the event will stay in a consistent state.<p> 
     * 
     * @return LTT read status, as defined in Jni_C_Constant
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Constant
     */
    public int seekOrFallBack(JniTime seekTime) {
        // Save the old time
        JniTime oldTime = new JniTime(eventTime);

        // Call seek to move ahead
        // Save the state for the return (eventState will be modified if we seek back)
        int returnState = seekToTime(seekTime);

        // If the event state is sane populate it
        if (returnState == EOK) {
            populateEventInformation();
        }
        else {
            seekToTime(oldTime);
        }

        return returnState;
    }

    /**
     * Position on the first event in the tracefile.<p>
     * 
     * The function return the read status after the first event.<p>
     * 
     * A status different of EOK probably means there is no event associated to this tracefile.
     * 
     * @return LTT read status, as defined in Jni_C_Constant
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Constant
     */
    protected int positionToFirstEvent() {
        eventState = ltt_positionToFirstEvent(tracefilePtr.getLibraryId(), tracefilePtr.getPointer());
        
        return eventState;
    }
    
    /**
     * Obtain a marker associated with this tracefile's event.
     * 
     * @return Reference to the marker for this tracefile's event or null if none.
     *  
     * @see org.eclipse.linuxtools.lttng.jni.JniMarker
     */
    public JniMarker requestEventMarker() {
        return markersMap.get(getEventMarkerId());
    }

    /**
     * Obtain the raw data of a LttEvent object.<p>
     * 
     * The data will be in raw C bytes, not java bytes.<br>
     * Note : This function is mostly untested and provided "as is".
     * 
     * @return  Bytes array of raw data (contain raw C bytes).
     */
    public byte[] requestEventContent() {
        byte dataContent[] = new byte[(int) getEventDataSize()];

        ltt_getDataContent(thisEventPtr.getLibraryId(), thisEventPtr.getPointer(), getEventDataSize(), dataContent);

        return dataContent;
    }
    
    /**
     * Obtain an event source.<p>
     * 
     * This is not implemented yet and will always return "Kernel core" for now.
     * 
     * @return  Reference to the JniMarker object for this event or null if none. 
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniMarker
     */
    public String requestEventSource() {
        // *** TODO ***
        // No "Source" of event exists in Ltt so far
        // It would be a good addition to have a way to detect where an event come
        // from, like "kernel" or "userspace"
        // 
        return "Kernel Core"; //$NON-NLS-1$
    }
    
    /**
     * Parse a particular field in the event payload, identified by its id (position).<p>
     * 
     * Note : Position are relative to an event marker (i.e. requestEventMarker().getMarkerFieldsArrayList() )
     * 
     * @param fieldId   Position of the field to parse.
     * 
     * @return Object that contain the parsed payload
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniParser
     */
    public Object parseFieldById(int fieldId) {
        return JniParser.parseField(this, fieldId);
    }
    
    /**
     * Parse a particular field in the event payload, identified by its name.<p>
     * 
     * Note : Name are relative to an event marker (i.e. requestEventMarker().getMarkerFieldsHashMap() )
     * 
     * @param fieldName   Position of the field to parse.
     * 
     * @return Object that contain the parsed payload
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniParser
     */
    public Object parseFieldByName(String fieldName) {
        return JniParser.parseField(this, fieldName);
    }
    
    /**
     * Method to parse all the event payload.<p>
     * 
     * @return HashMap<String, Object> which is the parsedContent objects and their name as key.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniParser 
     */
    public HashMap<String, Object> parseAllFields() {
        return JniParser.parseAllFields(this);
    }

    /* 
     * This function populates the event data with data from LTT
     * 
     * NOTE : To get better performance, we copy very few data into memory here
     * 
     */
    private void populateEventInformation() {
    	// We need to save the time, as it is not a primitive (can't be dynamically called in getter)
    	eventTime.setTime(ltt_getNanosencondsTime(thisEventPtr.getLibraryId(), thisEventPtr.getPointer()));
    }
    
    public JniTime getEventTime() {
        return eventTime;
    }
    
    // *** To get better performance, all getter belows call LTT directly ****
    //     That way, we can avoid copying data into memory
    public int getEventMarkerId() {
        return ltt_getEventMarkerId(thisEventPtr.getLibraryId(), thisEventPtr.getPointer());
    }

    public long getEventDataSize() {
        return ltt_getEventDataSize(thisEventPtr.getLibraryId(), thisEventPtr.getPointer());
    }

    public HashMap<Integer, JniMarker> getMarkersMap() {
        return markersMap;
    }

    /**
     * Pointer to the parent LTTTracefile C structure.<br>
     * <br>
     * The pointer should only be used <u>INTERNALY</u>, do not use unless you
     * know what you are doing.
     * 
     * @return The actual (long converted) pointer or NULL.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
    public Jni_C_Pointer_And_Library_Id getTracefilePtr() {
        return new Jni_C_Pointer_And_Library_Id(thisEventPtr.getLibraryId(), ltt_getTracefilePtr(thisEventPtr.getLibraryId(), thisEventPtr.getPointer()) );
    }

    /**
     * Pointer to the LttEvent C structure.<br>
     * <br>
     * The pointer should only be used <u>INTERNALY</u>, do not use unless you
     * know what you are doing.
     * 
     * @return The actual (long converted) pointer or NULL.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
    public Jni_C_Pointer_And_Library_Id getEventPtr() {
        return thisEventPtr;
    }

    public int getEventState() {
        return eventState;
    }

    /**
     * Getter to the parent tracefile for this event.
     *
     * @return  The parent tracefile 
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
    public JniTracefile getParentTracefile() {
        return parentTracefile;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((eventTime == null) ? 0 : eventTime.hashCode());
        result = prime * result + ((parentTracefile == null) ? 0 : parentTracefile.hashCode());
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JniEvent)) {
            return false;
        }
        JniEvent other = (JniEvent) obj;
        if (eventTime == null) {
            if (other.eventTime != null) {
                return false;
            }
        } else if (!eventTime.equals(other.eventTime)) {
            return false;
        }
        if (parentTracefile == null) {
            if (other.parentTracefile != null) {
                return false;
            }
        } else if (!parentTracefile.equals(other.parentTracefile)) {
            return false;
        }
        return true;
    }

    /**
     * Compare fonction for JNIEvent.<p>
     * <p>
     * This will compare the current JNIEvent with a passed one by timestamp AND tracefile ("type").<br>
     * If both are equal but type differs, current event is considered to be older (-1 returned).
     * 
     * @return -1 if given event happens before, 0 if equal, 1 if passed event happens after.
     */
    @Override
	public int compareTo(JniEvent other) {

		// By default, we consider the current event to be older.
		int eventComparaison = -1;

		// Test against null before performing anything
		if (other != null) {
			// Compare the timestamp first
			eventComparaison = this.getEventTime().compareTo(other.getEventTime());

			// If timestamp is equal, compare the parent trace file ("event type")
			if ((eventComparaison == 0)
					&& (!this.parentTracefile.equals(other.parentTracefile))) {
				eventComparaison = 1;
			}
		}
		return eventComparaison;
	}
    
    /**
     * Print information for this event. 
     * <u>Intended to debug</u><br>
     * 
     * This function will call Ltt to print, so information printed will be 
     * the one from the C structure, not the one populated in java.<p>
     */
    public void printEventInformation() {
        ltt_printEvent(thisEventPtr.getLibraryId(), thisEventPtr.getPointer());
    }
    
    /**
     * toString() method. 
     * <u>Intended to debug.</u><p>
     * 
     * @return Attributes of the object concatenated in String
     */
    @Override
    @SuppressWarnings("nls")
	public String toString() {
        String returnData = "";

        returnData += "tracefilePtr            : " + tracefilePtr + "\n";
        returnData += "eventMarkerId           : " + getEventMarkerId() + "\n";
        returnData += "eventTime               : " + eventTime.getReferenceToString() + "\n";
        returnData += "   seconds              : " + eventTime.getSeconds() + "\n";
        returnData += "   nanoSeconds          : " + eventTime.getNanoSeconds() + "\n";
        returnData += "eventDataSize           : " + getEventDataSize() + "\n";
        returnData += "markersMap              : " + markersMap.keySet() + "\n"; // Hack to avoid ending up with markersMap.toString()

        return returnData;
    }
    
}
