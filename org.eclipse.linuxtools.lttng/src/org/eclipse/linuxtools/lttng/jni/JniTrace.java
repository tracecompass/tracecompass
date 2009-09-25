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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * <b><u>JniTrace</u></b>
 * <p>
 * This is the top level class in the JNI. It provides access to the 
 * LttTrace C structure in java.
 * <p>
 * Most important fields in the JniTrace are :
 * <ul>
 * <li>a JniTrace path (a trace <b>directory</b>)
 * <li>a HashMap of tracefiles that exists in this trace
 * </ul>
 */
public class JniTrace extends Jni_C_Common {
        
    // Internal C pointer of the JniTrace used in LTT
    private C_Pointer thisTracePtr = new C_Pointer();

    // Data we should populate from LTT
    // Note that all type have been scaled up as there is no "unsigned" in java
    // This might be a problem about "unsigned long" as there is no equivalent
    // in java

    private String tracepath = ""; // Path of the trace. Should be a directory (like : /tmp/traceX)
    private int    cpuNumber = 0;
    private long   archType = 0;
    private long   archVariant = 0;
    private short  archSize = 0;
    private short  lttMajorVersion = 0;
    private short  lttMinorVersion = 0;
    private short  flightRecorder = 0;
    private long   freqScale = 0;
    private long   startFreq = 0;
    private long   startTimestampCurrentCounter = 0;
    private long   startMonotonic = 0;
    private JniTime   startTime = null;
    private JniTime   startTimeFromTimestampCurrentCounter = null;

    // This Map holds a reference to the tracefiles owned by this trace
    private HashMap<String, JniTracefile> tracefilesMap = null;
    // The priority queue (similar to heap) hold events 
    private PriorityQueue<JniEvent> eventsHeap = null;
    
    // This variable will hold the content of the "last" event we read
    private JniTime currentEventTimestamp = new JniTime();
    
    // Comparator we will need for the heap construction
    private Comparator<JniEvent> eventComparator = new Comparator<JniEvent>() {
    	public int compare(JniEvent left, JniEvent right ){
    		return ( left.getEventTime().compare( right.getEventTime() ) );
    	}
    };
    
    
    // Open/close native functions
    private native long ltt_openTrace(String pathname);
    private native void ltt_closeTrace(long tracePtr);

    // Native access functions
    private native String ltt_getTracepath(long tracePtr);
    private native int    ltt_getCpuNumber(long tracePtr);
    private native long   ltt_getArchType(long tracePtr);
    private native long   ltt_getArchVariant(long tracePtr);
    private native short  ltt_getArchSize(long tracePtr);
    private native short  ltt_getLttMajorVersion(long tracePtr);
    private native short  ltt_getLttMinorVersion(long tracePtr);
    private native short  ltt_getFlightRecorder(long tracePtr);
    private native long   ltt_getFreqScale(long tracePtr);
    private native long   ltt_getStartFreq(long tracePtr);
    private native long   ltt_getStartTimestampCurrentCounter(long tracePtr);
    private native long   ltt_getStartMonotonic(long tracePtr);

    // Native function to fill out startTime
    private native void ltt_feedStartTime(long tracePtr, JniTime startTime);

    // Native function to fill out startTimeFromTimestampCurrentCounter
    private native void ltt_feedStartTimeFromTimestampCurrentCounter(long tracePtr, JniTime startTime);

    // Native function to fill out tracefilesMap
    private native void ltt_getAllTracefiles(long tracePtr);

    // Debug native function, ask LTT to print trace structure
    private native void ltt_printTrace(long tracePtr);

    static {
        System.loadLibrary("lttvtraceread");
    }
        
    /**
     * Default constructor is forbidden
     */
    @SuppressWarnings("unused")
    private JniTrace() {
    }

    /**
     * Copy constructor.
     * 
     * @param oldTrace
     *            A reference to the JniTrace you want to copy.           
     */
    public JniTrace(JniTrace oldTrace) {
        thisTracePtr  = oldTrace.thisTracePtr;
        
        tracepath       = oldTrace.tracepath;
        cpuNumber       = oldTrace.cpuNumber;
        archType        = oldTrace.archType;
        archVariant     = oldTrace.archVariant;
        archSize        = oldTrace.archSize;
        lttMajorVersion = oldTrace.lttMajorVersion;
        lttMinorVersion = oldTrace.lttMinorVersion;
        flightRecorder  = oldTrace.flightRecorder;
        freqScale       = oldTrace.freqScale;
        startFreq       = oldTrace.startFreq;
        startTimestampCurrentCounter = oldTrace.startTimestampCurrentCounter;
        startMonotonic  = oldTrace.startMonotonic;
        startTime       = oldTrace.startTime;
        startTimeFromTimestampCurrentCounter = oldTrace.startTimeFromTimestampCurrentCounter;

        tracefilesMap = new HashMap<String, JniTracefile>(oldTrace.tracefilesMap.size());
        tracefilesMap = oldTrace.tracefilesMap;
        
        eventsHeap = new PriorityQueue<JniEvent>( oldTrace.eventsHeap.size(), eventComparator );
        eventsHeap = oldTrace.eventsHeap;
    }        
        
    /**
     * Copy constructor, using pointer.
     * 
     * @param newPtr The pointer to an already opened LttTrace C structure
     *            
     * @exception JniException
     */
    public JniTrace(C_Pointer newPtr) throws JniException {
        thisTracePtr = newPtr;
        
        // Populate our trace
        populateTraceInformation();
    }
        
    /**
     * Constructor that takes a tracepath parameter
     * <br>
     * This constructor also opens the trace
     * 
     * @param newpath The <b>directory</b> of the trace to be opened
     * 
     * @exception JniException
     */
    public JniTrace(String newpath) throws JniException {
        tracepath = newpath;
        thisTracePtr = new C_Pointer();
        
        openTrace(newpath);
    }
        
    /**
     * Open an existing trace<br>
     * <br>
     * The tracepath is a directory and needs to exist, otherwise
     * a JafOpenTraceFailedException is raised.
     * 
     * @param newPath
     *            The <b>directory</b> of the trace to be opened
     * @exception JafOpenTraceFailedException
     *                Thrown if the open failed
     */
    public void openTrace(String newPath) throws JniException {
        // If open is called while a trace is already opened, we will try to close it first
        if (thisTracePtr.getPointer() != NULL) {
            closeTrace();
        }

        // Set the tracepath and open it
        tracepath = newPath;
        openTrace();
    }
        
    /**
     * Open an existing trace<br>
     * <br>
     * The tracepath should have been set already,
     * 
     * @exception JafOpenTraceFailedException
     *                Thrown if the open failed
     */
    public void openTrace() throws JniException {
        
        // Raise an exception if the tracepath is empty, otherwise open the trace
        if (tracepath == "") {
            throw new JniTraceException("Tracepath is not set. (openTrace)");
        }
        
        // If the file is already opened, close it first
        if (thisTracePtr.getPointer() != NULL) {
            closeTrace();
        }

        // Call the LTT to open the trace
        long newPtr = ltt_openTrace(tracepath);
        if (newPtr == NULL) {
            throw new JniOpenTraceFailedException("Error while opening trace. Is the tracepath correct? (openTrace)");
        }

        // This is OUR pointer
        thisTracePtr = new C_Pointer(newPtr);

        // Populate the trace with LTT information
        populateTraceInformation();
    }
        
    /**
     * Close a trace
     * 
     * If the trace is already closed, will silently do nothing.
     */
    public void closeTrace() {
        if (thisTracePtr.getPointer() != NULL) {
            ltt_closeTrace(thisTracePtr.getPointer());
            thisTracePtr = new C_Pointer(NULL);

            // Clear the tracefile map
            tracefilesMap.clear();
            tracefilesMap = null;
            
            // Clear the eventsHeap and make it points to null
            eventsHeap.clear();
            eventsHeap = null;

            // Ask the garbage collector to make a little pass here, as we could
            // be left with 100's of unreferenced objects
            System.gc();
        }
    }

    /* 
     * This function populates the trace data with data from LTT
     * 
     * @throws JafException
     */
    private void populateTraceInformation() throws JniException {
        if (thisTracePtr.getPointer() == NULL) {
            throw new JniTraceException("Pointer is NULL, trace not opened/already closed? (populateTraceInformation)");
        }

        // Populate from the LTT library
        tracepath   = ltt_getTracepath( thisTracePtr.getPointer() );
        cpuNumber   = ltt_getCpuNumber( thisTracePtr.getPointer() );
        archType    = ltt_getArchType( thisTracePtr.getPointer() );
        archVariant = ltt_getArchVariant( thisTracePtr.getPointer() );
        archSize    = ltt_getArchSize( thisTracePtr.getPointer() );
        lttMajorVersion = ltt_getLttMajorVersion( thisTracePtr.getPointer() );
        lttMinorVersion = ltt_getLttMinorVersion( thisTracePtr.getPointer() );
        flightRecorder  = ltt_getFlightRecorder( thisTracePtr.getPointer() );
        freqScale   = ltt_getFreqScale( thisTracePtr.getPointer() );
        startFreq   = ltt_getStartFreq( thisTracePtr.getPointer() );
        startTimestampCurrentCounter = ltt_getStartTimestampCurrentCounter( thisTracePtr.getPointer() );
        startMonotonic = ltt_getStartMonotonic( thisTracePtr.getPointer() );

        // Creation of time is a bit different, we need to pass the object reference to C
        startTime = new JniTime();
        ltt_feedStartTime( thisTracePtr.getPointer(), startTime );

        startTimeFromTimestampCurrentCounter = new JniTime();
        ltt_feedStartTimeFromTimestampCurrentCounter( thisTracePtr.getPointer(), startTimeFromTimestampCurrentCounter );

        // Call the fill up function for the tracefiles map
        if ( tracefilesMap== null ) {
            tracefilesMap = new HashMap<String, JniTracefile>();
        }
        
        ltt_getAllTracefiles( thisTracePtr.getPointer() );
        
        if (eventsHeap == null) {
            eventsHeap = new PriorityQueue<JniEvent>(tracefilesMap.size(), eventComparator);
        }
        
        // Populate the heap with events
        populateEventHeap();
    }
    
    /* 
     * This function populates the event heap with one event from each tracefile
     * It should be called after each seek or when the object is constructed
     */
    private void populateEventHeap() {
        currentEventTimestamp = new JniTime();
        eventsHeap.clear();
        
        Object new_key = null;
        JniTracefile tmpTracefile = null;
        
        Iterator<String> iterator = tracefilesMap.keySet().iterator();
        while( iterator.hasNext() ) {
            new_key = iterator.next();
            
            tmpTracefile = tracefilesMap.get(new_key);
            if ( tmpTracefile.getCurrentEvent().getEventState() == EOK ) {
                eventsHeap.add( tmpTracefile.getCurrentEvent() );
            }
        }
    }

    /* 
     * Fills a map of all the trace files.
     * 
     * Note: This function is called from C and there is no way to propagate
     * exception back to the caller without crashing JNI. Therefore, it MUST
     * catch all exceptions.
     * 
     * @param tracefileName
     * @param tracefilePtr
     */
    @SuppressWarnings("unused")
    private void addTracefileFromC(String tracefileName, long tracefilePtr) {
        
        JniTracefile newTracefile = null;
            
        // Create a new tracefile object and insert it in the map
        //    the tracefile fill itself with LTT data while being constructed
        try {
            newTracefile = new JniTracefile( new C_Pointer(tracefilePtr), this );
            tracefilesMap.put(tracefileName + newTracefile.getCpuNumber(), newTracefile);
        }
        catch(JniTracefileWithoutEventException e) {
            printlnC("JniTracefile " + tracefileName + " has no event (addTracefileFromC). Ignoring.");
        }
        catch(Exception e) {
            printlnC("Failed to add tracefile " + tracefileName + " to tracefilesMap!(addTracefileFromC)\n\tException raised : " + e.toString() );
        }
    }
        
        
    /**
     * Return the next event, determined by timestamp, among the trace files.
     * The event content is populated.
     *  
     * Returns  null in case of error or if we reach end of trace.
     * 
     * @return The next event in the trace or null
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public JniEvent readNextEvent() {
        // Get the "next" event on the top of the heap but DO NOT remove it
        JniEvent tmpEvent = eventsHeap.peek();
        
        // If the event is null, it was the last one in the trace we can leave the function
        if (tmpEvent == null) {
            return null;
        }
        
        // Otherwise, we need to make sure the timestamp of the event we got is not the same as the last "NextEvent" we requested 
        if (tmpEvent.getEventTime().getTime() == currentEventTimestamp.getTime() ) {
            // Remove the event on top as it is the same currentEventTimestamp
            eventsHeap.poll();
            
            // Read the next event for this particular event type
            tmpEvent.readNextEvent();
            
            // If the event state is sane (not Out of Range), put it back in the heap
            if ( tmpEvent.getEventState() == EOK ) {
                eventsHeap.add(tmpEvent);
            }
            
            // Pick the top event again
            tmpEvent = eventsHeap.peek();
            
            // Save the timestamp if the event is not null (null mean we reached the last event in the trace)
            if (tmpEvent != null) {
                currentEventTimestamp = tmpEvent.getEventTime();
            }
        }
        // If the event on top has differetn timestamp than the currentTimestamp, just save this timestamp as current
        else {
            currentEventTimestamp = tmpEvent.getEventTime();
        }
            
        return tmpEvent;
    }
    
    /**
     * Return the next event, determined by timestamp, among the trace files.
     * The event content is NOT populated (requires a call to readNextEvent()).
     *  
     * Returns null in case of error or EOF.
     * 
     * @return The next event, or null if none is available
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public JniEvent findNextEvent() {
        return eventsHeap.peek();
    }
    
    /**
     * Seek to a certain time and read the next event from that time.<br>
     * <br>
     * If no more events are available or an error happen, null will be returned
     * 
     * @param seekTime  The time where we want to seek to
     * @return JniEvent    The next event after the seek time or null
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
     public JniEvent seekAndRead(JniTime seekTime) { 
          JniEvent returnedEvent = null;
          seekToTime(seekTime);
              
          // The trace should be correctly positionned, let's get the event
          returnedEvent = readNextEvent();
               
          return returnedEvent;
     }
    
     /**
      * Seek to a certain time but <b>do not</b> read the next event.<br>
      * <br>
      * This only position the trace, it will not return anything.
      * 
      * @param seekTime     The time where we want to seek to
      */
      public void seekToTime(JniTime seekTime) {
           Object tracefile_name = null;
           Iterator<String> iterator = tracefilesMap.keySet().iterator();
           
           while (iterator.hasNext() ) {
               // We seek to the given event for ALL tracefiles
               tracefile_name = iterator.next();
               tracefilesMap.get(tracefile_name).seekToTime(seekTime);
           }
           
           populateEventHeap();
      }
     
    /**
     * Get a certain tracefile from its given name<br>
     * 
     * @param tracefileName The name of the tracefile
     * @return JniTracefile The tracefile found or null if none
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
    public JniTracefile requestTracefileByName(String tracefileName) {
        return tracefilesMap.get(tracefileName);
    }        
        
    /**
     * Get a certain event associated to a trace file from the trace file name<br>
     * 
     * @param tracefileName The name of the trace file
     * @return The JniEvent found or null if none
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    public JniEvent requestEventByName(String tracefileName) {
        JniEvent returnValue = null;

        JniTracefile tmpTracefile = tracefilesMap.get(tracefileName);

        // If the tracefile is found, return the current event
        // There should always be an event linked to a tracefile
        if (tmpTracefile != null) {
            returnValue = tmpTracefile.getCurrentEvent();
        }

        return returnValue;
    }   
        
    // Access to class variable. Most of them doesn't have setter
    public String getTracepath() {
        return tracepath;
    }

    public int getCpuNumber() {
        return cpuNumber;
    }

    public long getArchType() {
        return archType;
    }

    public long getArchVariant() {
        return archVariant;
    }

    public short getArchSize() {
        return archSize;
    }

    public short getLttMajorVersion() {
        return lttMajorVersion;
    }

    public short getLttMinorVersion() {
        return lttMinorVersion;
    }

    public short getFlightRecorder() {
        return flightRecorder;
    }

    public long getFreqScale() {
        return freqScale;
    }

    public long getStartFreq() {
        return startFreq;
    }

    public long getStartTimestampCurrentCounter() {
        return startTimestampCurrentCounter;
    }

    public long getStartMonotonic() {
        return startMonotonic;
    }

    public JniTime getStartTime() {
        return startTime;
    }

    public JniTime getStartTimeFromTimestampCurrentCounter() {
        return startTimeFromTimestampCurrentCounter;
    }

    public HashMap<String, JniTracefile> getTracefilesMap() {
        return tracefilesMap;
    }        
    
    /**
     * Getter for the last read event timestamp<br>
     * 
     * @return The time of the last event read
     */
    public JniTime getCurrentEventTimestamp() {
        return currentEventTimestamp;
    }
    
    /**
     * Pointer to the LttTrace C structure<br>
     * <br>
     * The pointer should only be used INTERNALY, do not use these unless you
     * know what you are doing.
     * 
     * @return The actual (long converted) pointer or NULL
     */
    public C_Pointer getTracePtr() {
        return thisTracePtr;
    }        
        
    /**
     * Print information for all the tracefiles associated with this trace.
     * <u>Intended to debug</u><br>
     * 
     * This function will call Ltt to print, so information printed will be the
     * one from the C structure
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
    public void printAllTracefilesInformation() {

        Object new_key = null;
        JniTracefile tracefile;

        Iterator<String> iterator = tracefilesMap.keySet().iterator();
        while (iterator.hasNext()) {
            new_key = iterator.next();

            tracefile = tracefilesMap.get(new_key);

            tracefile.printTracefileInformation();
        }
    }        
        
    /**
     * Print information for this trace. <u>Intended to debug</u><br>
     * 
     * This function will call Ltt to print, so information printed will be the
     * one from the C structure<br>
     * <br>
     * This function will not throw but will complain loudly if pointer is NULL
     */
    public void printTraceInformation() {

        // If null pointer, print a warning!
        if (thisTracePtr.getPointer() == NULL) {
            printlnC("Pointer is NULL, cannot print. (printTraceInformation)");
        } else {
            ltt_printTrace( thisTracePtr.getPointer() );
        }
    }
        
    /**
     * toString() method. <u>Intended to debug</u><br>
     * 
     * @return String Attributes of the object concatenated in String
     */
    @Override
	public String toString() {
        String returnData = "";
        returnData += "tracepath                            : " + tracepath + "\n";
        returnData += "cpuNumber                            : " + cpuNumber + "\n";
        returnData += "archType                             : " + archType + "\n";
        returnData += "archVariant                          : " + archVariant + "\n";
        returnData += "archSize                             : " + archSize + "\n";
        returnData += "lttMajorVersion                      : " + lttMajorVersion + "\n";
        returnData += "lttMinorVersion                      : " + lttMinorVersion + "\n";
        returnData += "flightRecorder                       : " + flightRecorder + "\n";
        returnData += "freqScale                            : " + freqScale + "\n";
        returnData += "startFreq                            : " + startFreq + "\n";
        returnData += "startTimestampCurrentCounter         : " + startTimestampCurrentCounter + "\n";
        returnData += "startMonotonic                       : " + startMonotonic + "\n";
        returnData += "startTime                            : " + startTime.getReferenceToString() + "\n";
        returnData += "   seconds                           : " + startTime.getSeconds() + "\n";
        returnData += "   nanoSeconds                       : " + startTime.getNanoSeconds() + "\n";
        returnData += "startTimeFromTimestampCurrentCounter : " + startTimeFromTimestampCurrentCounter.getReferenceToString() + "\n";
        returnData += "   seconds                           : " + startTimeFromTimestampCurrentCounter.getSeconds() + "\n";
        returnData += "   nanoSeconds                       : " + startTimeFromTimestampCurrentCounter.getNanoSeconds() + "\n";
        returnData += "tracefilesMap                        : " + tracefilesMap.keySet() + "\n";      // Hack to avoid ending up with tracefilesMap.toString()

        return returnData;
    }
     
     /* 
     *  MAIN : For testing only!
     */
     public static void main(String[] args) {
         JniTrace testTrace = null;
         JniEvent tmpEvent = null;
         
         try {
             //testTrace = new JniTrace("/home/william/trace1");
             testTrace = new JniTrace("/home/william/trace5");
             //testTrace = new JniTrace("/home/william/trace1_numcpu");
         }
         catch (JniException e) {
             System.out.println(e.getMessage() );
             return;
         }
         
//         testTrace.printAllTracefilesInformation();
//         
//         
//         testTrace.printlnC( testTrace.toString() );
         
         long nbEvent = 0;
         
         testTrace.printlnC("Beginning test run on all events");
         tmpEvent = testTrace.readNextEvent();
         while (tmpEvent != null) {
             nbEvent++;
             tmpEvent = testTrace.readNextEvent();
             
             if ( tmpEvent != null ) {
                 tmpEvent.parseAllFields();
             }
         }
         testTrace.printlnC("We read " + nbEvent + " total events (JAF)\n");
         
         
         /*
         
         tmpEvent = testTrace.readNextEvent();
         
         JniTime test_time = new JniTime(960386633737L);
         tmpEvent = testTrace.seekAndRead(test_time);
         
         testTrace.printlnC(tmpEvent.getParentTracefile().getTracefileName().toString() );
         testTrace.printlnC(tmpEvent.toString() );
         
         
         test_time = new JniTime(960386638531L);
         tmpEvent = testTrace.seekAndRead(test_time);
         
         testTrace.printlnC(tmpEvent.getParentTracefile().getTracefileName().toString() );
         testTrace.printlnC(tmpEvent.toString() );
         
         
         tmpEvent = testTrace.readNextEvent();
         if ( tmpEvent == null ) {
             testTrace.printlnC("NULL NULL NULL1");
         }
         else {
             testTrace.printlnC(tmpEvent.getParentTracefile().getTracefileName().toString() );
             testTrace.printlnC(tmpEvent.toString() );
         }
         
         tmpEvent = testTrace.readNextEvent();
         if ( tmpEvent == null ) {
             testTrace.printlnC("NULL NULL NULL2");
         }
         else {
             testTrace.printlnC(tmpEvent.getParentTracefile().getTracefileName().toString() );
             testTrace.printlnC(tmpEvent.toString() );
         }
         */
         
         
         
         
         
         
         /*
         testTrace.printlnC("Beginning test run seek time");        
         JniTime test_time = new JniTime(953, 977711854);
         testTrace.seekToTime(test_time);
         tmpEvent = testTrace.findNextEvent();
         testTrace.printlnC(tmpEvent.toString() );
         */
         
         /*
         testTrace.printlnC("Beginning test run parsing event"); 
         Object[] parsedName = null;
         HashMap<String,Object> parsedData = null;
         for ( int x = 0; x<30; x++) {
             tmpEvent = testTrace.readNextEvent();
                 
             testTrace.printlnC(tmpEvent.getParentTracefile().getTracefileName().toString() );
             testTrace.printC(tmpEvent.toString() );
                 
             testTrace.printlnC("Format                  : " + tmpEvent.requestEventMarker().getFormatOverview().toString() );
             parsedData = tmpEvent.parse();
             parsedName = parsedData.keySet().toArray();
             
             testTrace.printC("                          ");
             for ( int pos=0; pos<parsedName.length; pos++) {
                 testTrace.printC( parsedName[pos].toString() + " " + parsedData.get(parsedName[pos]).toString() + " ");
             }
             testTrace.printlnC("\n");
         }*/
         
     }
     
}