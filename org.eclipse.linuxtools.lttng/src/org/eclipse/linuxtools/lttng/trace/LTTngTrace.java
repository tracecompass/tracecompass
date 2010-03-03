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
import org.eclipse.linuxtools.lttng.event.LttngLocation;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniMarker;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.lttng.jni.JniTracefile;
import org.eclipse.linuxtools.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.lttng.jni.factory.JniTraceFactory;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;


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
public class LTTngTrace extends TmfTrace<LttngEvent> {
	
	public static boolean joie = false;
	
    private final static boolean SHOW_LTT_DEBUG_DEFAULT = false;
	private final static boolean IS_PARSING_NEEDED_DEFAULT = false;
	private final static int     CHECKPOINT_PAGE_SIZE      = 1000;
    
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
    
    // The current location
    LttngLocation					currentLocation  = null;
    
    
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
        super(LttngEvent.class, path, CHECKPOINT_PAGE_SIZE);
        try {
    		currentJniTrace = JniTraceFactory.getJniTrace(path, SHOW_LTT_DEBUG_DEFAULT);
        }
        catch (Exception e) {
            throw new LTTngTraceException(e.getMessage());
        }
        
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
        
        // Create a new current location
        currentLocation = new LttngLocation();
        
        
        // Set the currentEvent to the eventContent
        eventContent.setEvent(currentLttngEvent);
        
        // Bypass indexing if asked
        if ( bypassIndexing == false ) {
            indexTrace(true);
        }
        else {
        	// Even if we don't have any index, set ONE checkpoint
        	fCheckpoints.add(new TmfCheckpoint(new LttngTimestamp(0L) , new LttngLocation() ) );
        	
        	// Set the start time of the trace
        	setTimeRange( new TmfTimeRange( new LttngTimestamp(currentJniTrace.getStartTime().getTime()), 
        			  				    	new LttngTimestamp(currentJniTrace.getEndTime().getTime())
                                      	  ) );
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
    	super(LttngEvent.class, null);
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
        currentLttngEvent.updateJniEventReference(jniEvent);
        
        // Parse now if was asked
        // Warning : THIS IS SLOW
        if (isParsingNeeded == true ) {
            eventContent.getFields();
        }
        
        return currentLttngEvent;
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
    
    /**
     * Get the major version number for the current trace
     * 
     * @return Version major or -1 if unknown
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     * 
     */
    public short getVersionMajor() {
    	if ( currentJniTrace!= null ) {
    		return currentJniTrace.getLttMajorVersion();
    	}
    	else {
    		return -1;
    	}
    }
    
    /**
     * Get the minor version number for the current trace
     * 
     * @return Version minor or -1 if unknown
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     * 
     */
    public short getVersionMinor() {
    	if ( currentJniTrace!= null ) {
    		return currentJniTrace.getLttMinorVersion();
    	}
    	else {
    		return -1;
    	}
    }
    
    /**
     * Get the number of CPU for this trace
     * 
     * @return Number of CPU or -1 if unknown
     * 
     * @see org.eclipse.linuxtools.org.eclipse.linuxtools.lttng.jni.JniTrace
     * 
     */
    public int getCpuNumber() {
    	if ( currentJniTrace!= null ) {
    		return currentJniTrace.getCpuNumber();
    	}
    	else {
    		return -1;
    	}
    }
    
    
    @Override
	public String toString() {
    	String returnedData="";
    	
    	returnedData += "Path :" + getPath() + " ";
    	returnedData += "Trace:" + currentJniTrace + " ";
    	returnedData += "Event:" + currentLttngEvent;
    	
    	return returnedData;
    }
    
    
    
    
    
    
    
    
    
    
    public void bidon_function() {
    	
    	System.out.println("START : " + getTimeRange().getStartTime().getValue());
    	System.out.println("END   : " + getTimeRange().getEndTime().getValue());
    	
        for ( int pos=0; pos < fCheckpoints.size(); pos++) {
            //for ( int pos=0; pos < 100; pos++) {
            System.out.print(pos + ": " + "\t");
            System.out.print( fCheckpoints.get(pos).getTimestamp() + "\t" );
            System.out.println( fCheckpoints.get(pos).getLocation() );
        }
    }
    
    @Override
    public synchronized void indexTrace(boolean useless) {
    	
        // Position the trace at the beginning
        TmfContext context = seekEvent( new LttngTimestamp(0L) );
        
        long nbEvents=0L;
//        joie = false;
        
        LttngTimestamp startTime = null;
        LttngTimestamp lastTime  = new LttngTimestamp();
        
        LttngEvent tmpEvent = (LttngEvent)getNextEvent(context);
        LttngLocation tmpLocation = (LttngLocation)context.getLocation();
        
    	
        while ( tmpEvent != null) {
        		tmpLocation = (LttngLocation)context.getLocation();

                if ( startTime == null ) {
                        startTime = new LttngTimestamp( tmpLocation.getCurrentTime() );
                }
                
                lastTime.setValue(tmpEvent.getTimestamp().getValue());
                
                if ((nbEvents % getCacheSize()) == 0) {
                        LttngTimestamp tmpTimestamp = new LttngTimestamp( tmpLocation.getCurrentTime() );
                        LttngLocation  newLocation  = new LttngLocation(  tmpLocation.getCurrentTime() );
                        
                        fCheckpoints.add(new TmfCheckpoint(tmpTimestamp, newLocation ) );
                }

                nbEvents++;

                tmpEvent = (LttngEvent)getNextEvent(context);
        }

        if (startTime != null) {
            setTimeRange( new TmfTimeRange(startTime, lastTime) );
            notifyListeners(getTimeRange() );
        }
        
        fNbEvents = nbEvents;
        //bidon_function();
//        joie = true;
        
    }
    
    @Override
	public ITmfLocation getCurrentLocation() {
        return currentLocation;
    }
    
    @Override
    public synchronized TmfContext seekLocation(ITmfLocation location) {
    	
    	if ( joie == true ) {
    		System.out.println("seekLocation(location) location -> " + location);
    	}
    	
    	LttngTimestamp tmpTime = null;
    	
    	if ( location == null ) {
    		tmpTime = (LttngTimestamp)getStartTime();
    	}
    	else {
    		// *** FIXME ***
    		// NEED TO AVOID TIMESTAMP CREATION
    		tmpTime = new LttngTimestamp( ((LttngLocation)location).getCurrentTime() );
    	}
    	
    	// The only seek valid in LTTng is with the time, we call seekEvent(timestamp)
    	return seekEvent( tmpTime );
    }
    
    @Override
    public synchronized TmfContext seekEvent(TmfTimestamp timestamp) {
    	
    	if ( joie == true ) {
    		System.out.println("seekEvent(timestamp) timestamp -> " + timestamp);
    	}
    	
    	currentJniTrace.seekToTime(new JniTime(timestamp.getValue()));
		
		// Update the current time for the location
		currentLocation.setCurrentTime( timestamp.getValue() );
    	
    	return new TmfContext(currentLocation);
    }
    
    @Override
    public synchronized TmfContext seekEvent(long position) {
    	
    	if ( joie == true ) {
    		System.out.println("seekEvent(position) position -> " + position);
    	}
    	
    	TmfTimestamp timestamp = null;
        long index = position / getCacheSize();
        
        if (fCheckpoints.size() > 0) {
                if (index >= fCheckpoints.size()) {
                        index = fCheckpoints.size() - 1;
                }
                timestamp = (TmfTimestamp)fCheckpoints.elementAt((int)index).getTimestamp();
        }
        else {
            timestamp = getStartTime();
        }
        
        TmfContext 		tmpContext  = seekEvent(timestamp);
        LttngLocation 	tmpLocation = (LttngLocation)tmpContext.getLocation();
        
        Long currentPosition = index * getCacheSize();
        
        JniEvent tmpJniEvent = currentJniTrace.findNextEvent();
        while ( (tmpJniEvent != null) && ( currentPosition < position ) ) {
                tmpJniEvent = currentJniTrace.readNextEvent();
                currentPosition++;
                tmpLocation.setCurrentTime( tmpJniEvent.getEventTime().getTime() );
        }
        
        return tmpContext;
    }
    
    
    @Override
    public synchronized LttngEvent getNextEvent(TmfContext context) {
    	
    	if ( joie == true ) {
    		System.out.println("getNextEvent(context) context.getLocation() -> " + context.getLocation());
    	}
    	
    	LttngEvent 	returnedEvent = null;
    	LttngLocation tmpLocation = null;
    	
    	if ( context.getLocation() == null ) {
    		tmpLocation = new LttngLocation();
    		context.setLocation(tmpLocation);
    	}
    	else {
    		tmpLocation = (LttngLocation)context.getLocation();
    	}
    	
    	if ( tmpLocation.getCurrentTime() != currentJniTrace.getCurrentEventTimestamp().getTime() ) {
    		seekLocation( tmpLocation );
    	}
    	
    	returnedEvent = readEvent(tmpLocation);
    	
    	// No matter what happens, save the location
    	currentLocation = tmpLocation;
    	
    	return returnedEvent;
    	
    }
    
    
    @Override
	public synchronized LttngEvent parseEvent(TmfContext context) {
    	
    	if ( joie == true ) {
    		System.out.println("parseEvent(context) context.getLocation() -> " + context.getLocation());
    	}
    	
    	LttngEvent 	returnedEvent = null;
    	LttngLocation tmpLocation = null;
    	
    	if ( context.getLocation() == null ) {
    		tmpLocation = new LttngLocation();
    		context.setLocation(tmpLocation);
    	}
    	else {
    		tmpLocation = (LttngLocation)context.getLocation();
    	}
    	
    	if ( tmpLocation.getCurrentTime() != currentJniTrace.getCurrentEventTimestamp().getTime() ) {
    		seekLocation( tmpLocation );
    	}
    	
    	if ( currentLttngEvent.getTimestamp().getValue() != currentJniTrace.getCurrentEventTimestamp().getTime() ) {
    		returnedEvent = readEvent(tmpLocation);
    	}
    	else {
			returnedEvent = currentLttngEvent;
		}
    	
    	// No matter what happens, save the location
    	currentLocation = tmpLocation;
    	
    	return returnedEvent;
    	
    }
    
    
    private synchronized LttngEvent readEvent(LttngLocation location) {
    	LttngEvent 	returnedEvent = null;
    	JniEvent tmpEvent = null;
    	
    	tmpEvent = currentJniTrace.readNextEvent();
		
		if ( tmpEvent != null ) {
			// *** NOTE
			// Convert will update the currentLttngEvent
            returnedEvent = convertJniEventToTmf(tmpEvent);
            
            // *** NOTE 
            // Set Last and Current time from the event read, as it could be different from the time requested 
            location.setLastReadTime(returnedEvent.getTimestamp().getValue() );
            location.setCurrentTime( returnedEvent.getTimestamp().getValue() );
        }
		// *** NOTE
		// If the read failed (likely the last event in the trace), set the LastReadTime to the JNI time
		// That way, even if we try to read again, we will step over the bogus seek and read
		else {
//			location.setLastReadTime( currentJniTrace.getEndTime().getTime() + 1 );
//			location.setCurrentTime(  currentJniTrace.getEndTime().getTime() + 1 );
			location.setLastReadTime( getEndTime().getValue() + 1 );
			location.setCurrentTime(  getEndTime().getValue() + 1 );
//			location.setLastReadTime( Long.MAX_VALUE );
//			location.setCurrentTime(  Long.MAX_VALUE );
//			location.setLastReadTime( currentJniTrace.getEndTime().getNanoSeconds() + 1 );
//			location.setCurrentTime(  currentJniTrace.getEndTime().getNanoSeconds() + 1 );
//			System.out.println("TMF End time " + getEndTime().getValue() );
//			System.out.println("JNI End time " + currentJniTrace.getEndTime().getTime() );
//			System.out.println("JNI Partial time " + currentJniTrace.getEndTime().getNanoSeconds() );
		}
		
		return returnedEvent;
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