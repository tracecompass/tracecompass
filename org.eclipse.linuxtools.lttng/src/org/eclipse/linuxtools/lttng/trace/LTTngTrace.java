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

import org.eclipse.linuxtools.lttng.LttngException;
import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.event.LttngEventField;
import org.eclipse.linuxtools.lttng.event.LttngEventFormat;
import org.eclipse.linuxtools.lttng.event.LttngEventReference;
import org.eclipse.linuxtools.lttng.event.LttngEventType;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniTime;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
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

	private final static boolean IS_PARSING_NEEDED_DEFAULT = true;
	private final static int     CHECKPOINT_PAGE_SIZE = 1000;
	
    // Reference to the current LttngEvent
    private LttngEvent currentLttngEvent = null;
    
    // Reference to our JNI trace
    private JniTrace currentJniTrace = null;
    
    /**
     * Default Constructor.<p>
     * 
     * @param path  Path to a <b>directory</b> that contain an LTTng trace.
     * 
     * @exception Exception (most likely LTTngTraceException or FileNotFoundException)
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
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
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
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
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public LTTngTrace(String path, boolean waitForCompletion, boolean bypassIndexing) throws Exception {
        super(path, CHECKPOINT_PAGE_SIZE, true);
        try {
    		currentJniTrace = new JniTrace(path);
        }
        catch (Exception e) {
            throw new LTTngTraceException(e.getMessage());
        }
        TmfTimestamp startTime = new LttngTimestamp(currentJniTrace.getStartTimeFromTimestampCurrentCounter().getTime());
        setTimeRange(new TmfTimeRange(startTime, startTime));
        
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
    
    /** 
     * Parse the next event in the trace.<p>
     * 
     * @param context   The actual context of the trace
     * 
     * @return The parsed event, or null if none available
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
     */ 
    @Override
	public TmfEvent parseEvent(TmfTraceContext context) {
		JniEvent jniEvent;
		LttngTimestamp timestamp = null;
		
    	synchronized (currentJniTrace) {
    	    // Seek to the context's location
    		seekLocation(context.getLocation());
    		
    		// Read an event from the JNI and convert it into a LttngEvent
    		jniEvent = currentJniTrace.readNextEvent();
    		currentLttngEvent = (jniEvent != null) ? convertJniEventToTmf(jniEvent, true) : null;
    		
    		// Save timestamp
    		timestamp = (LttngTimestamp) getCurrentLocation();
    	}
   		context.setLocation(timestamp);
   		context.setTimestamp(timestamp);
   		context.incrIndex();

   		return currentLttngEvent;
    }
    
    /**
     * Method to convert a JniEvent into a LttngEvent.<p>
     * 
     * Note : This method will call LttngEvent convertEventJniToTmf(JniEvent, boolean)
     * with a default value for isParsingNeeded
     * 
     * @param   newEvent     The JniEvent to convert into LttngEvent
     * 
     * @return  The converted event
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
	public LttngEvent convertJniEventToTmf(JniEvent newEvent) {
    	LttngEvent event = null;
    	if (newEvent != null)
    		event = convertJniEventToTmf(newEvent, IS_PARSING_NEEDED_DEFAULT);
    	return event;
    }
    
    /**
     * Method tp convert a JniEvent into a LttngEvent
     * 
     * @param   jniEvent        The JniEvent to convert into LttngEvent
     * @param   isParsingNeeded A boolean value telling if the event should be parsed or not.
     * 
     * @return  The converted event
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public LttngEvent convertJniEventToTmf(JniEvent jniEvent, boolean isParsingNeeded) {
    	
    	// *** FIXME ***
    	// Format seems weird to me... we need to revisit Format/Fields/Content to find a better ways
    	//
    	// Generate fields
    	String[] labels = new String[jniEvent.requestEventMarker().getMarkerFieldsHashMap().size()];
    	labels = jniEvent.requestEventMarker().getMarkerFieldsHashMap().keySet().toArray( labels );
    	
    	// We need a format for content and fields
        LttngEventFormat eventFormat = new LttngEventFormat(labels);
        String content = "";
        LttngEventField[] fields = null;

        if (isParsingNeeded == true) {
            fields = eventFormat.parse(jniEvent.parseAllFields());
            for (int y = 0; y < fields.length; y++) {
                content += fields[y].toString() + " ";
            }
        }
        
        LttngEvent event = null;
        try {
        	event = new LttngEvent(
	        			new LttngTimestamp(jniEvent.getEventTime().getTime()),
	        			new TmfEventSource(jniEvent.requestEventSource() ), 
	        			new LttngEventType(jniEvent.getParentTracefile().getTracefileName(),
	                                       jniEvent.getParentTracefile().getCpuNumber(),
	                                       jniEvent.requestEventMarker().getName(),
	                                       eventFormat),
	                    new LttngEventContent(eventFormat, content, fields), 
	                    new LttngEventReference(jniEvent.getParentTracefile().getTracefilePath(), this.getName()),
	                    jniEvent);
        }
        catch (LttngException e) {
        	System.out.println("ERROR : Event creation returned :" + e.getMessage() );
        }
        
        return event;
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
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
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