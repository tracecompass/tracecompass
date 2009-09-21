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

package org.eclipse.linuxtools.lttng.jni;

import java.util.HashMap;

/**
 * <b><u>JniEvent</u></b>
 * <p>
 * A JniEvent has the actual content that got traced by Lttng It provides access to the LttEvent C structure in java.
 * <p>
 * Most important fields in the JniEvent are :
 * <ul>
 * <li>an event time, which is a digested timestamp
 * </ul>
 * Note that the JniEvent content is not directly accessibe and should be obtained
 * using the requestEventContent() or parse() method.
 */
public final class JniEvent extends Jni_C_Common {
    // Variables to detect if the event have been filled at least once
    // this make possible the detection of "uninitialized" struct in Ltt
    // Can be "EOK", "ERANGE" or "EPERM" (defined in Jaf_C_Common)
    private int eventState = EPERM; // Start with EPERM to ensure sanity

    // Internal C pointer of the JniEvent used in LTT
    private C_Pointer thisEventPtr = new C_Pointer();

    // Reference to the parent tracefile
    private JniTracefile parentTracefile = null;

    // This map hold marker relative to the parent tracefile of this event
    // They are "our" marker in this event
    private HashMap<Integer, JniMarker> markersMap;

    // Data we should populate from ltt
    // Note that all type have been scaled up as there is no "unsigned" in java
    // This might be a problem about "unsigned long" as there is no equivalent
    // in java
    private C_Pointer tracefilePtr = new C_Pointer();;
    private int eventMarkerId = 0;
    private JniTime eventTime = null;
    private long eventDataSize = 0;

    // These methods need a tracefile pointer, instead of a event pointer
    private native int      ltt_readNextEvent(long tracefilePtr);
    private native int      ltt_seekEvent(long tracefilePtr, JniTime givenTime);
    private native int      ltt_positionToFirstEvent(long tracefilePtr);
        
    // Native access functions
    private native long     ltt_getTracefilePtr(long eventPtr);
    @SuppressWarnings("unused")
    private native long     ltt_getBlock(long eventPtr);
    @SuppressWarnings("unused")
    private native long     ltt_getOffset(long eventPtr);
    @SuppressWarnings("unused")
    private native long     ltt_getCurrentTimestampCounter(long eventPtr);
    @SuppressWarnings("unused")
    private native long     ltt_getTimestamp(long eventPtr);
    private native int      ltt_getEventMarkerId(long eventPtr);
    private native void     ltt_feedEventTime(long eventPtr, JniTime eventTime);
    private native long     ltt_getEventDataSize(long eventPtr);
    @SuppressWarnings("unused")
    private native long     ltt_getEventSize(long eventPtr);
    @SuppressWarnings("unused")
    private native int      ltt_getCount(long eventPtr);
    @SuppressWarnings("unused")
    private native long     ltt_getOverflowNanoSeconds(long eventPtr);
        
    // This method an event pointer
    private native void     ltt_getDataContent(long eventPtr, long dataSize, byte[] returnedContent);
        
    // Debug native function, ask LTT to print event structure
    private native void     ltt_printEvent(long eventPtr);

    static {
        System.loadLibrary("lttvtraceread");
    }

    
    /**
     * Default constructor is forbidden
     */
    @SuppressWarnings("unused")
    private JniEvent() {
    };

    /**
     * Copy constructor.
     * 
     * @param oldEvent
     *            A reference to the JniEvent you want to copy. 
     */
    public JniEvent(JniEvent oldEvent) {
        thisEventPtr = oldEvent.thisEventPtr;
        markersMap = oldEvent.markersMap;
        parentTracefile = oldEvent.parentTracefile;
        eventState = oldEvent.eventState;

        tracefilePtr = oldEvent.tracefilePtr;
        eventMarkerId = oldEvent.eventMarkerId;
        eventTime = oldEvent.eventTime;
        eventDataSize = oldEvent.eventDataSize;
    }
    
    /**
     * Constructor with parameters<br>
     * <br>
     * This constructor could throw. It will happen if an event can not be populated on first read.<br>
     * In that case, the parent tracefile is probably useless and should be deleted.
     * 
     * @param newEventPtr         C pointer (converted in long) of the LttEvent C structure.
     * @param newMarkersMap       Reference an already populated HashMap of JniMarker objects 
     * @param newParentTracefile  Reference to the parent JniTracefile of this JniEvent
     *            
     * @exception JniException           
     */
    public JniEvent(C_Pointer newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException {

        // Basic test to make sure we didn't get null/empty value 
        if ((newEventPtr.getPointer() == NULL)
                || (newMarkersMap == null) 
                || (newMarkersMap.size() == 0)
                || (newParentTracefile == null)) {
            throw new JniEventException("Null or empty value passed to constructor, object is invalid! (JniEvent)");
        }

        thisEventPtr = newEventPtr;
        tracefilePtr = newParentTracefile.getTracefilePtr();
        markersMap = newMarkersMap;
        parentTracefile = newParentTracefile;

        eventTime = new JniTime();

        // Try to move to the first event
        // If the event is Out of Range (ERANGE) at the first range, this event
        // type will never been usable
        // In that case, throw JafEventOutOfRangeException to warn the parent
        // tracefile
        //
        // Position ourself on the next (first?) event
        eventState = positionToFirstEvent();
        if (eventState != EOK) {
            throw new JniNoSuchEventException("Object not populated, unusable. There is probably no event of that type in the trace. (JniEvent)");
        }
        else {
            populateEventInformation();
        }
    }

    /**
     * Move to the next event and populate LttEvent structure into the java object.<br>
     * <br>
     * If the move fails, the event will not get populated and the last event's data will still be available.
     * 
     * @return The read status, as defined in Jni_C_Common
     * @see org.eclipse.linuxtools.lttng.jni.Jni_C_Common
     */
     public int readNextEvent() {
        // Ask Ltt to read the next event for this particular tracefile
        eventState = ltt_readNextEvent(tracefilePtr.getPointer() );

        // If the event state is sane populate it
        if (eventState == EOK) {
            populateEventInformation();
        }

        return eventState;
    }

    /**
     * Seek to a certain time.<br>
     * <br>
     * Seek to a certain time and read event at this exact time or the next one if there is no event there.<br>
     * <br>
     * Note that this function could end in an invalid position if we seek after the last event. <br>
     * In that case, a seek back would be required to get back to a consistent state.<br>
     * <br>
     * If the seek fails, the event will not get populated and the last event's data will still be available.
     * 
     * @return The read status, as defined in Jni_C_Common
     * @see org.eclipse.linuxtools.lttng.jni.Jni_C_Common
     */
    public int seekToTime(JniTime seekTime) {
        // Ask Ltt to read the next event for this particular tracefile
        eventState = ltt_seekEvent(tracefilePtr.getPointer(), seekTime);

        // If the event state is sane populate it
        if (eventState == EOK) {
            populateEventInformation();
        }

        return eventState;
    }

    /**
     * Seek to a certain time or seek back if it fails.<br>
     * <br>
     * Seek to a certain time and read event at this exact time or the next one if there is no event there.<br>
     * If the seek fails, we will seek back to the previous position, so the event will stay in a consistent state. 
     * 
     * @return The read status, as defined in Jni_C_Common
     * @see org.eclipse.linuxtools.lttng.jni.Jni_C_Common
     */
    public int seekOrFallBack(JniTime seekTime) {
        // Save the old time
        JniTime oldTime = eventTime;

        // Call seek to move ahead
        seekToTime(seekTime);
        // Save the state for the return
        int returnState = eventState;

        // If the event state is sane populate it
        if (eventState == EOK) {
            populateEventInformation();
        }

        // Move to Next event only if the state of the event is sane
        if (eventState != EOK) {
            seekToTime(oldTime);
        }

        return returnState;
    }

    /**
     * Position on the first event in the tracefile.<br>
     * <br>
     * The function will return the read status of the event.<br>
     * An erronous status probably means there is no event of that type associated to the tracefile.
     * 
     * @return The read status, as defined in Jni_C_Common
     * @see org.eclipse.linuxtools.lttng.jni.Jni_C_Common
     */
    public int positionToFirstEvent() {
        eventState = ltt_positionToFirstEvent(tracefilePtr.getPointer());
        
        return eventState;
    }
    
    /**
     * Method to obtain a marker associated with this particular event.
     * 
     * @return  Reference to the JniMarker object for this event or null if none. 
     * @see org.eclipse.linuxtools.lttng.jni.JniMarker
     */
    public JniMarker requestEventMarker() {
        return markersMap.get(eventMarkerId);
    }

    /**
     * Method to obtain the raw data of a LttEvent object.<br>
     * <br>
     * Note : The data will be in raw C bytes, not java bytes. 
     * 
     * @return  bytes array of raw data.
     */
    public byte[] requestEventContent() {

        byte dataContent[] = new byte[(int) eventDataSize];

        ltt_getDataContent(thisEventPtr.getPointer(), eventDataSize, dataContent);

        return dataContent;
    }

    // *** TODO ***
    // No "Source" of event exist in Ltt so far
    // It would be a good addition to have a way to detect where an event come
    // from, like "kernel" or "userspace"
    // 
    /**
     * Method to obtain an event source.<br>
     * <br>
     * This is not implemented yet and will always return "Kernel core" for now.
     * 
     * @return  Reference to the JniMarker object for this event or null if none. 
     * @see org.eclipse.linuxtools.lttng.jni.JniMarker
     */
    public String requestEventSource() {
        return "Kernel Core";
    }
    
    /**
     * Method to parse a particular field in the event payload, identified by its id (position).
     * 
     * @return Object that contain the parsed payload
     */
    public Object parseFieldById(int fieldId) {
        return JniParser.parseField(this, fieldId);
    }
    
    /**
     * Method to parse a particular field in the event payload, identified by its name.
     * 
     * @return Object that contain the parsed payload
     */
    public Object parseFieldByName(String fieldName) {
        return JniParser.parseField(this, fieldName);
    }
    
    /**
     * Method to parse all the event payload.
     * 
     * @return  ArrayList of ParsedContent objects. 
     */
    public HashMap<String, Object> parseAllFields() {
        return JniParser.parseAllFields(this);
    }

    /* 
     * This function populates the event data with data from LTT
     * 
     */
    private void populateEventInformation() {
        if (thisEventPtr.getPointer() == NULL) {
            printlnC("Pointer is NULL, trace closed? (populateEventInformation)");
        }
        else {
            tracefilePtr = new C_Pointer( ltt_getTracefilePtr(thisEventPtr.getPointer()) );
            eventMarkerId = ltt_getEventMarkerId(thisEventPtr.getPointer());

            // Creation of time is a bit different, we need to pass the object
            // reference to C
            ltt_feedEventTime(thisEventPtr.getPointer(), eventTime);

            eventDataSize = ltt_getEventDataSize(thisEventPtr.getPointer());
        }
    }
    
    public int getEventMarkerId() {
        return eventMarkerId;
    }

    public JniTime getEventTime() {
        return eventTime;
    }

    public long getEventDataSize() {
        return eventDataSize;
    }

    public HashMap<Integer, JniMarker> getMarkersMap() {
        return markersMap;
    }

    
    /**
     * Pointer to the parent LttEvent C structure<br>
     * <br>
     * The pointer should only be used INTERNALY, do not use these unless you
     * know what you are doing.
     * 
     * @return The actual (long converted) pointer or NULL
     */
    public C_Pointer getTracefilePtr() {
        return tracefilePtr;
    }

    /**
     * Pointer to the LttEvent C structure<br>
     * <br>
     * The pointer should only be used INTERNALY, do not use these unless you
     * know what you are doing.
     * 
     * @return The actual (long converted) pointer or NULL
     */
    public C_Pointer getEventPtr() {
        return thisEventPtr;
    }

    public int getEventState() {
        return eventState;
    }

    /**
     * Getter to the parent tracefile for this event.
     *
     * 
     * @return  the parent tracefile 
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
    public JniTracefile getParentTracefile() {
        return parentTracefile;
    }

    /**
     * toString() method. <u>Intended to debug</u><br>
     * 
     * @return String Attributes of the object concatenated in String
     */
    @Override
	public String toString() {
        String returnData = "";

        returnData += "tracefilePtr            : " + tracefilePtr + "\n";
        returnData += "eventMarkerId           : " + eventMarkerId + "\n";
        returnData += "eventTime               : " + eventTime.getReferenceToString() + "\n";
        returnData += "   seconds              : " + eventTime.getSeconds() + "\n";
        returnData += "   nanoSeconds          : " + eventTime.getNanoSeconds() + "\n";
        returnData += "eventDataSize           : " + eventDataSize + "\n";
        returnData += "markersMap              : " + markersMap.keySet() + "\n"; // Hack to avoid ending up with markersMap.toString()

        return returnData;
    }

    /**
     * Print information for this event. <u>Intended to debug</u><br>
     * 
     * This function will call Ltt to print, so information printed will be the one from the C structure<br>
     * <br>
     * This function will not throw but will complain loudly if pointer is NULL
     */
    public void printEventInformation() {

        // If null pointer, print a warning!
        if (thisEventPtr.getPointer() == NULL) {
            printlnC("Pointer is NULL, cannot print. (printEventInformation)");
        }
        else {
            ltt_printEvent(thisEventPtr.getPointer());
        }
    }
}
