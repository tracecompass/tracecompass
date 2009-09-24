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

import java.io.FileNotFoundException;

import org.eclipse.linuxtools.lttng.event.LttngEvent;
import org.eclipse.linuxtools.lttng.event.LttngEventContent;
import org.eclipse.linuxtools.lttng.event.LttngEventField;
import org.eclipse.linuxtools.lttng.event.LttngEventFormat;
import org.eclipse.linuxtools.lttng.event.LttngEventReference;
import org.eclipse.linuxtools.lttng.event.LttngEventSource;
import org.eclipse.linuxtools.lttng.event.LttngEventType;
import org.eclipse.linuxtools.lttng.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.lttng.jni.JniException;
import org.eclipse.linuxtools.lttng.jni.JniTime;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;

/**
 * <b><u>LTTngTrace</u></b>
 * <p>
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
     * Default Constructor
     * <p>
     * @param path Path to a <b>directory</b> that contain an LTTng trace.
     * 
     * @exception Exception Trace opening failed (most likely FileNotFoundException)
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public LTTngTrace(String path) throws Exception {
        this(path, true);
    }
    
    /**
     * Default constructor, with control over the indexing
     * <p>
     * @param path 					Path to a <b>directory</b> that contain an LTTng trace.
     * @param waitForCompletion  	Should we wait for indexign to complete before moving on.
     * 
     * @exception Exception Trace opening failed (most likely FileNotFoundException)
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public LTTngTrace(String path, boolean waitForCompletion) throws Exception {
        super(path, CHECKPOINT_PAGE_SIZE, waitForCompletion);
        try {
            currentJniTrace = new JniTrace(path);
        }
        catch (JniException e) {
            throw new FileNotFoundException();
        }
        TmfTimestamp startTime = new LttngTimestamp(currentJniTrace.getStartTimeFromTimestampCurrentCounter().getTime());
        setTimeRange(new TmfTimeRange(startTime, startTime));
        indexStream();
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#parseNextEvent()
	 */
	@Override
	public synchronized TmfEvent parseEvent(TmfTraceContext context) {
		seekLocation(context.location);
    	JniEvent jniEvent = currentJniTrace.readNextEvent();
    	currentLttngEvent = convertJniEventToTmf(jniEvent);
        return currentLttngEvent;
	}

    /**
     * Return a reference to the current LttngTrace we are reading from.
     * 
     * @return JniTrace
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public JniTrace getCurrentJniTrace() {
        return currentJniTrace;
    }
    
    
    /**
     * Return a reference to the current LttngEvent we are reading.
     * 
     * @return LttngEvent
     * @see org.eclipse.linuxtools.lttng.event.LttngEvent
     */
    public LttngEvent getCurrentEvent() {
        return currentLttngEvent;
    }
    
    
    /**
     * Method to convert a JniEvent into a LttngEvent.<br>
     * <br>
     * Note : This method will call LttngEvent convertEventJniToTmf(JniEvent, boolean)
     * with a default value for isParsingNeeded
     * 
     * @param   newEvent     The JniEvent to convert
     * @return  LttngEvent   The converted event
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    private LttngEvent convertJniEventToTmf(JniEvent newEvent) {
    	LttngEvent event = null;
    	if (newEvent != null)
    		event = convertJniEventToTmf(newEvent, IS_PARSING_NEEDED_DEFAULT);
    	return event;
    }
    
    /**
     * Method tp convert a JniEvent into a LttngEvent
     * 
     * @param   jniEvent        The JniEvent to convert
     * @param   isParsingNeeded A boolean value telling if the event should be parsed or not.
     * @return  LttngEvent   The converted event
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public LttngEvent convertJniEventToTmf(JniEvent jniEvent, boolean parsingIsNeeded) {
        LttngEventFormat eventFormat = new LttngEventFormat();
        String content = "";
        LttngEventField[] fields = null;

        if (parsingIsNeeded) {
            fields = eventFormat.parse(jniEvent.parseAllFields());
            for (int y = 0; y < fields.length; y++) {
                content += fields[y].toString() + " ";
            }
        }
        
        LttngEvent event = new LttngEvent(
        		new LttngTimestamp(jniEvent.getEventTime().getTime()),
                new LttngEventSource(jniEvent.requestEventSource() ), 
                new LttngEventType(jniEvent.getParentTracefile().getTracefileName(),
                                   jniEvent.getParentTracefile().getCpuNumber(),
                                   jniEvent.requestEventMarker().getName(),
                                   eventFormat),
                new LttngEventContent(eventFormat, content, fields), 
                new LttngEventReference(jniEvent.getParentTracefile().getTracefilePath(), this.getName()),
                jniEvent);
        
        return event;
    }
    
    
    /**
     * Return location (timestamp) of our current position in the trace.
     * 
     * @return LttngTimestamp The current LTT timestamp, in long. Unit is nanosecond.
     */
	@Override
	public Object getCurrentLocation() {
        LttngTimestamp returnedLocation = null;
        JniEvent tmpJniEvent = currentJniTrace.findNextEvent();
        
        if (tmpJniEvent != null) {
            returnedLocation = new LttngTimestamp(tmpJniEvent.getEventTime().getTime());
        }
        
        return returnedLocation;
    }
    
    /**
     * Seek (move) to a certain location in the trace.
     * 
     * @param location  a LttngTimestamp of a position in the trace
     * @return StreamContext pointing to the current (after seek) position in the trace
     */
	public synchronized TmfTraceContext seekLocation(Object location) {
        
        // If location is null, interpret this as a request to get back to the beginning of the trace
        // Change the location, the seek will happen below
    	if (location == null) {
    		location = getStartTime();
    	}
    	
    	if (location instanceof LttngTimestamp) {
    		long value = ((LttngTimestamp) location).getValue();
    		if (value != currentJniTrace.getCurrentEventTimestamp().getTime()) {
    			currentJniTrace.seekToTime(new JniTime(value));
    		}
    	}
    	else {
    	    System.out.println("ERROR : Location not instance of TmfTimestamp");
    	}

    	LttngTimestamp ts = (LttngTimestamp) getCurrentLocation();
        return new TmfTraceContext(ts, ts, 0);
	}

	   @Override
	public String toString() {
	    	String result="";

	    	result += "Path :" + getPath() + " ";
	    	result += "Trace:" + currentJniTrace + " ";
	    	result += "Event:" + currentLttngEvent;
	    	
	    	return result;
	    }
	    
//    // !!! THIS MAIN IS FOR TESTING ONLY !!!
//    public static void main(String[] args) {
//
//        LttngEventStream testStream = null;
//        try {
//            testStream = new LttngEventStream("/home/william/trace1");
//            Thread.sleep(5000);
//            
//            System.out.println("NB Events : " + testStream.getNbEvents());
//            System.out.println("Beginning test run parsing event");
//            
//            LttngEvent tmpEvent = null;
//            Random generator = new Random();
//            
//            int number = 0;
//            long execStartTime = System.currentTimeMillis();
//            for (int x = 0; x < 10; x++) {
//                number = generator.nextInt(testStream.getNbEvents());
//                
//                tmpEvent = (LttngEvent) testStream.getEvent(new TmfTraceContext(null), number);
//                
//                
//                System.out.println("GETTING EVENT #" + number);
//                
//                // *****************
//                // ***  OLD EVIL WAY  
//                TmfEventField[] tmpJoieFields = tmpEvent.getContent().getFields();
//                for ( int pos = 0; pos< tmpJoieFields.length; pos++  )
//                {
//                    System.out.print(tmpJoieFields[pos].toString() + " ");
//                }
//                System.out.println("");
//                // ***
//                // *****************
//                
//                
//                // *****************
//                // ***  NEW CAST-O-RAMA WAY  
//                TmfEventField[] tmpJoieFields2 = ( (LttngEventContent)tmpEvent.getContent()).getFields(tmpEvent);
//                for ( int pos = 0; pos< tmpJoieFields2.length; pos++  )
//                {
//                    System.out.print(tmpJoieFields2[pos].toString() + " ");
//                }
//                System.out.println("");
//                // ***
//                // *****************
//            }
//            long execEndTime = System.currentTimeMillis();
//            System.out.println("Execution time: " + (execEndTime - execStartTime) + "ms");
//            
//
//        } catch (Exception e) {
//            System.out.println("FAILED WITH : " + e.getMessage() + "\n");
//        }
//
//    }

}
