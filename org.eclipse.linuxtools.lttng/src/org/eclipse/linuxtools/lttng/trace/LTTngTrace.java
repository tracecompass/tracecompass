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

package org.eclipse.linuxtools.lttng.trace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.linuxtools.lttng.LttngException;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.event.LttngEventReference;
import org.eclipse.linuxtools.lttng.event.LttngEventSource;
import org.eclipse.linuxtools.lttng.event.LttngEventType;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTime;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;


class LTTngTraceException extends LttngException {
	static final long serialVersionUID = -1636648737081868146L;

	public LTTngTraceException(String errMsg) {
        super(errMsg);
    }
}

/**
 * <b><u>LTTngTrace</u></b><p>
 * 
 * LTTng trace implementation. It accesses the C trace handling library
 * (seeking, reading and parsing) through the JNI component.
 */
public class LTTngTrace extends TmfTrace {
    private final static boolean SHOW_LTT_DEBUG_DEFAULT = false;
	private final static boolean IS_PARSING_NEEDED_DEFAULT = false;
	private final static int     CHECKPOINT_PAGE_SIZE      = 1;
    
    // Reference to our JNI trace
    private JniTrace currentJniTrace = null;
    
    // *** HACK ***
    // To save time, we will declare all component of the LttngEvent during the construction of the trace
    //  Then, while reading the trace, we will just SET the values instead of declaring new object
    LttngTimestamp                  eventTimestamp   = null;
    LttngEventSource                eventSource      = null;
    LttngEventType                  eventType        = null;
    LttngEventContent               eventContent     = null;
    LttngEventReference             eventReference   = null;
    // The actual event
    LttngEvent                      currentLttngEvent = null;             
    
    // Hashmap of the possible types of events (Tracefile/CPU/Marker in the JNI)
    HashMap<String, LttngEventType> traceTypes       = null;
    // This vector will be used to quickly find a marker name from a position 
    Vector<String>                  traceTypeNames   = null;
    
    /**
     * Default Constructor.<p>
     * 
     * @param path  Path to a <b>directory</b> that contain an LTTng trace.
     * 
     * @exception Exception (most likely LTTngTraceException or FileNotFoundException)
     */
    public LTTngTrace(String path) throws Exception {
        // Call with "wait for completion" true and "skip indexing" false
        this(path, true, false);
    }
    
    /**
     * Constructor, with control over the indexing.
     * <p>
     * @param path                  Path to a <b>directory</b> that contain an LTTng trace.
     * @param waitForCompletion     Should we wait for indexign to complete before moving on.
     * 
     * @exception Exception (most likely LTTngTraceException or FileNotFoundException)
     */
    public LTTngTrace(String path, boolean waitForCompletion) throws Exception {
        // Call with "skip indexing" false
        this(path, waitForCompletion, false);
    }
    
    /**
     * Default constructor, with control over the indexing and possibility to bypass indexation
     * <p>
     * @param path 					Path to a <b>directory</b> that contain an LTTng trace.
     * @param waitForCompletion  	Should we wait for indexign to complete before moving on.
     * @param bypassIndexing        Should we bypass indexing completly? This is should only be useful for unit testing.
     * 
     * @exception Exception (most likely LTTngTraceException or FileNotFoundException)
     * 
     */
    public LTTngTrace(String path, boolean waitForCompletion, boolean bypassIndexing) throws Exception {
        super(path, CHECKPOINT_PAGE_SIZE, true);
        try {
    		currentJniTrace = new JniTrace(path, SHOW_LTT_DEBUG_DEFAULT);
        }
        catch (Exception e) {
            throw new LTTngTraceException(e.getMessage());
        }
        
        // Set the start time of the trace
        LttngTimestamp startTime = new LttngTimestamp(currentJniTrace.getStartTimeFromTimestampCurrentCounter().getTime());
        setTimeRange(new TmfTimeRange(startTime, startTime));
        
        // Export all the event types from the JNI side 
        traceTypes      = new HashMap<String, LttngEventType>();
        traceTypeNames  = new Vector<String>();
        initialiseEventTypes(currentJniTrace);
        
        // *** VERIFY ***
        // Verify that all those "default constructor" are safe to use
        eventTimestamp        = new LttngTimestamp();
        eventSource           = new LttngEventSource();
        eventType             = new LttngEventType();
        eventContent          = new LttngEventContent(currentLttngEvent);
        eventReference        = new LttngEventReference(this.getName());
        
        // Create the skeleton event
        currentLttngEvent = new LttngEvent(eventTimestamp, eventSource, eventType, eventContent, eventReference, null);
        
        // Set the currentEvent to the eventContent
        eventContent.setEvent(currentLttngEvent);
        
        // Bypass indexing if asked
        if ( bypassIndexing == false ) {
            indexStream();
        }
    }
    
    /*
     * Copy constructor is forbidden for LttngEvenmStream
     * 
     * Events are only valid for a very limited period of time and
     *   JNI library does not support multiple access at once (yet?).
     * For this reason, copy constructor should NEVER be used.
     */
    private LTTngTrace(LTTngTrace oldStream) throws Exception { 
    	super(null);
    	throw new Exception("Copy constructor should never be use with a LTTngTrace!");
    }
    
    /*
     * Fill out the HashMap with "Type" (Tracefile/Marker)
     * 
     * This should be called at construction once the trace is open
     */
    private void initialiseEventTypes(JniTrace trace) {
        // Work variables
        LttngEventType  tmpType             = null;
        String[]        markerFieldsLabels  = null;
        
        String          newTracefileKey     = null;
        Integer         newMarkerKey        = null;
        
        JniTracefile    newTracefile    = null;
        JniMarker       newMarker       = null;
        
        // First, obtain an iterator on TRACEFILES of owned by the TRACE
        Iterator<String>    tracefileItr = trace.getTracefilesMap().keySet().iterator();
        while ( tracefileItr.hasNext() ) {
            newTracefileKey = tracefileItr.next();
            newTracefile    = trace.getTracefilesMap().get(newTracefileKey);
            
            // From the TRACEFILE read, obtain its MARKER
            Iterator<Integer> markerItr = newTracefile.getTracefileMarkersMap().keySet().iterator();
            while ( markerItr.hasNext() ) {
                newMarkerKey = markerItr.next();
                newMarker = newTracefile.getTracefileMarkersMap().get(newMarkerKey);
                
                // From the MARKER we can obtain the MARKERFIELDS keys (i.e. labels)
                markerFieldsLabels = newMarker.getMarkerFieldsHashMap().keySet().toArray( new String[newMarker.getMarkerFieldsHashMap().size()] );
                tmpType = new LttngEventType(newTracefile.getTracefileName(), newTracefile.getCpuNumber(), newMarker.getName(), markerFieldsLabels );
                
                // Add the type to the map/vector
                addEventTypeToMap(tmpType);
            }
        }
    }
    
    /*
     * Add a new type to the HashMap
     * 
     * As the hashmap use a key format that is a bit dangerous to use, we should always add using this function.
     */
    private void addEventTypeToMap(LttngEventType newEventType) {
        String newTypeKey = EventTypeKey.getEventTypeKey(newEventType);
        
        this.traceTypes.put(newTypeKey, newEventType);
        this.traceTypeNames.add(newTypeKey);
    }
    
    /** 
     * Return the next event in the trace.<p>
     * 
     * @param context   The actual context of the trace
     * 
     * @return The next event, or null if none available
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngEvent
     */ 
    @Override
	public LttngEvent parseEvent(TmfTraceContext context) {
		JniEvent jniEvent;
		LttngTimestamp timestamp = null;
		LttngEvent returnedEvent = null;
		
    	synchronized (currentJniTrace) {
    	    // Seek to the context's location
    		seekLocation(context.getLocation());
    		
    		// Read an event from the JNI and convert it into a LttngEvent
    		jniEvent = currentJniTrace.readNextEvent();
    		
    		//currentLttngEvent = (jniEvent != null) ? convertJniEventToTmf(jniEvent, true) : null;
    		if ( jniEvent != null ) {
    		    currentLttngEvent = convertJniEventToTmf(jniEvent);
    		    returnedEvent = currentLttngEvent;
    		}
    		
    		// Save timestamp
    		timestamp = (LttngTimestamp) getCurrentLocation();
    	}
   		context.setLocation(timestamp);
   		context.setTimestamp(timestamp);
   		context.incrIndex();
   		
   		return returnedEvent;
    }
    
    /**
     * Method to convert a JniEvent into a LttngEvent.<p>
     * 
     * Note : This method will call LttngEvent convertEventJniToTmf(JniEvent, boolean)
     * with a default value for isParsingNeeded
     * 
     * @param   newEvent     The JniEvent to convert into LttngEvent
     * 
     * @return  The converted LttngEvent
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     * @see org.eclipse.linuxtools.lttng.event.LttngEvent
     */
	public LttngEvent convertJniEventToTmf(JniEvent newEvent) {
	    currentLttngEvent = convertJniEventToTmf(newEvent, IS_PARSING_NEEDED_DEFAULT);
	    
	    return currentLttngEvent;
    }
    
    /**
     * Method to convert a JniEvent into a LttngEvent
     * 
     * @param   jniEvent        The JniEvent to convert into LttngEvent
     * @param   isParsingNeeded A boolean value telling if the event should be parsed or not.
     * 
     * @return  The converted LttngEvent
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniEvent
     * @see org.eclipse.linuxtools.lttng.event.LttngEvent
     */
    public LttngEvent convertJniEventToTmf(JniEvent jniEvent, boolean isParsingNeeded) {
        // *** HACK *** 
        // To save time here, we only set value instead of allocating new object
        // This give an HUGE performance improvement
        // all allocation done in the LttngTrace constructor
        
        eventTimestamp.setValue(jniEvent.getEventTime().getTime());
        eventSource.setSourceId(jniEvent.requestEventSource());
        
        eventType = traceTypes.get( EventTypeKey.getEventTypeKey(jniEvent) );
        
        eventReference.setValue(jniEvent.getParentTracefile().getTracefilePath());
        eventReference.setTracepath(this.getName());
        
//        eventContent.setEvent(currentLttngEvent);
//        eventContent.setType(eventType);
        eventContent.emptyContent();
        
//        currentLttngEvent.setContent(eventContent);
        currentLttngEvent.setType(eventType);
        // Save the jni reference
        currentLttngEvent.setJniEventReference(jniEvent);
        
        // Parse now if was asked
        // Warning : THIS IS SLOW
        if (isParsingNeeded == true ) {
            eventContent.getFields();
        }
        
        return currentLttngEvent;
    }
    
    /**
     * Seek (move) to a certain location in the trace.<p>
     * 
     * WARNING : No event is read by this function, it just seek to a certain time.<br>
     * Use "parseEvent()" or "getNextEvent()" to get the event we seeked to. 
     * 
     * @param location  a TmfTimestamp of a position in the trace
     * 
     * @return TmfTraceContext pointing the position in the trace at the seek location 
     */
    public TmfTraceContext seekLocation(Object location) {
        
    	LttngTimestamp timestamp = null;

    	// If location is null, interpret this as a request to get back to the beginning of the trace
        //      in that case, just change the location, the seek will happen below
    	if (location == null) {
    		location = getStartTime();
    	}

    	if (location instanceof TmfTimestamp) {
    		long value = ((TmfTimestamp) location).getValue();
    		if (value != currentJniTrace.getCurrentEventTimestamp().getTime()) {
    			synchronized (currentJniTrace) {
    				currentJniTrace.seekToTime(new JniTime(value));
    				timestamp = (LttngTimestamp) getCurrentLocation();
    			}
    		}
    	}
    	else {
    	    System.out.println("ERROR : Location not instance of TmfTimestamp");
    	}

    	// FIXME: LTTng hack - start
    	// return new TmfTraceContext(timestamp, timestamp, 0);	// Original
        return new TmfTraceContext(timestamp, timestamp, -1);	// Hacked
    	// FIXME: LTTng hack - end
    }
    
    /**
     * Location (timestamp) of our current position in the trace.<p>
     * 
     * @return The time (in LttngTimestamp format) of the current event or AFTER endTime if no more event is available.
     */
    @Override
	public Object getCurrentLocation() {
        
        LttngTimestamp returnedLocation = null;
        JniEvent tmpJniEvent = currentJniTrace.findNextEvent();
        
        if ( tmpJniEvent != null  ) {
            returnedLocation = new LttngTimestamp(tmpJniEvent.getEventTime().getTime());
        }
        else {
            returnedLocation = new LttngTimestamp( getEndTime().getValue() + 1 );
        }
        
        return returnedLocation;
    }
    
    /**
     * Reference to the current LttngTrace we are reading from.<p>
     * 
     * Note : This bypass the framework and should not be use, except for testing!
     * 
     * @return Reference to the current LttngTrace 
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public JniTrace getCurrentJniTrace() {
        return currentJniTrace;
    }
    
    
    /**
     * Return a reference to the current LttngEvent we have in memory.
     * 
     * @return The current (last read) LttngEvent
     * 
     * @see org.eclipse.linuxtools.lttng.event.LttngEvent
     */
    public LttngEvent getCurrentEvent() {
        return currentLttngEvent;
    }
    
    
    @Override
	public String toString() {
    	String returnedData="";
    	
    	returnedData += "Path :" + getPath() + " ";
    	returnedData += "Trace:" + currentJniTrace + " ";
    	returnedData += "Event:" + currentLttngEvent;
    	
    	return returnedData;
    }
}

/*
 * EventTypeKey inner class
 * 
 * This class is used to make the process of generating the HashMap key more transparent and so less error prone to use
 * 
 */
class EventTypeKey {
    //*** WARNING ***
    // These two getEventTypeKey() functions should ALWAYS construct the key the same ways! 
    // Otherwise, every type search will fail!
    
    static public String getEventTypeKey(LttngEventType newEventType) {
        String key = newEventType.getTracefileName() + "/" + newEventType.getCpuId().toString() + "/" + newEventType.getMarkerName();
        
        return key;
    }
    
    static public String getEventTypeKey(JniEvent newEvent) {
        String key = newEvent.getParentTracefile().getTracefileName() + "/" + newEvent.getParentTracefile().getCpuNumber() + "/" + newEvent.requestEventMarker().getName();
        
        return key;
    }
    
}
