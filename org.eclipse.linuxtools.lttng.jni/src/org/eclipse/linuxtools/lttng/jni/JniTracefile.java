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
import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.exception.JniNoSuchEventException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTracefileException;
import org.eclipse.linuxtools.lttng.jni.exception.JniTracefileWithoutEventException;

/**
 * <b><u>JniTracefile</u></b>
 * <p>
 * A tracefile own an event of a certain type.<br>
 * Provides access to the LttTracefile C structure in java.
 * <p>
 * Most important fields in the JniTracefile are :
 * <ul>
 * <li> a JniTracefile path (a tracefile <b>file</b> within a JniTrace directory)
 * <li> a name (basically the name without the directory part)
 * <li> a reference to a single event object
 * <li> a HashMap of marker associated with this tracefile
 * </ul>
 */
public abstract class JniTracefile extends Jni_C_Common {
        
    // Internal C pointer of the JniTracefile used in LTT
    private Jni_C_Pointer thisTracefilePtr = new Jni_C_Pointer();
    
    // Reference to the parent trace
    private JniTrace parentTrace = null;
    
    // Data we should populate from LTT
    // Note that all type have been scaled up as there is no "unsigned" in java
    // This might be a problem about "unsigned long" as there is no equivalent in java
    private boolean isCpuOnline = false;
    private String  tracefilePath = "";
    private String  tracefileName = "";
    private long    cpuNumber = 0;
    private long    tid = 0;
    private long    pgid = 0;
    private long    creation = 0;
    
    // Internal C pointer for trace and marker
    private Jni_C_Pointer tracePtr = null;
    private Jni_C_Pointer markerDataPtr = null;
    
    private int     CFileDescriptor = 0;
    private long    fileSize = 0;
    private long    blocksNumber = 0;
    private boolean isBytesOrderReversed = false;
    private boolean isFloatWordOrdered = false;
    private long    alignement = 0;
    private long    bufferHeaderSize = 0;
    private int     bitsOfCurrentTimestampCounter = 0;
    private int     bitsOfEvent = 0;
    private long    currentTimestampCounterMask = 0;
    private long    currentTimestampCounterMaskNextBit = 0;
    private long    eventsLost = 0;
    private long    subBufferCorrupt = 0;
    private JniEvent   currentEvent = null;
    
    // Internal C pointer for trace and marker
    private Jni_C_Pointer bufferPtr = null;
    
    private long    bufferSize = 0;

    // This map will hold markers_info owned by this tracefile
    private HashMap<Integer, JniMarker> tracefileMarkersMap = null;        

    // Native access functions
    protected native boolean  ltt_getIsCpuOnline(long tracefilePtr);
    protected native String   ltt_getTracefilepath(long tracefilePtr);
    protected native String   ltt_getTracefilename(long tracefilePtr);
    protected native long     ltt_getCpuNumber(long tracefilePtr);
    protected native long     ltt_getTid(long tracefilePtr);
    protected native long     ltt_getPgid(long tracefilePtr);
    protected native long     ltt_getCreation(long tracefilePtr);
    protected native long     ltt_getTracePtr(long tracefilePtr);
    protected native long     ltt_getMarkerDataPtr(long tracefilePtr);
    protected native int      ltt_getCFileDescriptor(long tracefilePtr);
    protected native long     ltt_getFileSize(long tracefilePtr);
    protected native long     ltt_getBlockNumber(long tracefilePtr);
    protected native boolean  ltt_getIsBytesOrderReversed(long tracefilePtr);
    protected native boolean  ltt_getIsFloatWordOrdered(long tracefilePtr);
    protected native long     ltt_getAlignement(long tracefilePtr);
    protected native long     ltt_getBufferHeaderSize(long tracefilePtr);
    protected native int      ltt_getBitsOfCurrentTimestampCounter(long tracefilePtr);
    protected native int      ltt_getBitsOfEvent(long tracefilePtr);
    protected native long     ltt_getCurrentTimestampCounterMask(long tracefilePtr);
    protected native long     ltt_getCurrentTimestampCounterMaskNextBit(long tracefilePtr);
    protected native long     ltt_getEventsLost(long tracefilePtr);
    protected native long     ltt_getSubBufferCorrupt(long tracefilePtr);
    protected native long     ltt_getEventPtr(long tracefilePtr);
    protected native long     ltt_getBufferPtr(long tracefilePtr);
    protected native long     ltt_getBufferSize(long tracefilePtr);

    // Method to fill a map with marker object
    protected native void ltt_feedAllMarkers(long tracefilePtr);
    
    // Debug native function, ask LTT to print tracefile structure
    protected native void ltt_printTracefile(long tracefilePtr);
    
    // *** FIXME ***
    // To uncomment as soon as the library will be able to load multiple version at once
	// static {
	//	System.loadLibrary("lttvtraceread_loader");
	//}
        
    /*
     * Default constructor is forbidden
     */
    protected JniTracefile() {
    }

    /**
     * Copy constructor.<p>
     * 
     * @param oldTracefile      Reference to the JniTracefile you want to copy. 
     */
    public JniTracefile(JniTracefile oldTracefile) {
        thisTracefilePtr    = oldTracefile.thisTracefilePtr;
        parentTrace         = oldTracefile.parentTrace;
        tracefileMarkersMap = oldTracefile.tracefileMarkersMap;
        isCpuOnline         = oldTracefile.isCpuOnline;
        tracefilePath       = oldTracefile.tracefilePath;
        tracefileName       = oldTracefile.tracefileName;
        cpuNumber           = oldTracefile.cpuNumber;
        tid                 = oldTracefile.tid;
        pgid                = oldTracefile.pgid;
        creation            = oldTracefile.creation;
        tracePtr            = oldTracefile.tracePtr;
        markerDataPtr       = oldTracefile.markerDataPtr;
        CFileDescriptor     = oldTracefile.CFileDescriptor;
        fileSize            = oldTracefile.fileSize;
        blocksNumber        = oldTracefile.blocksNumber;
        isBytesOrderReversed = oldTracefile.isBytesOrderReversed;
        isFloatWordOrdered  = oldTracefile.isFloatWordOrdered;
        alignement          = oldTracefile.alignement;
        bufferHeaderSize    = oldTracefile.bufferHeaderSize;
        bitsOfCurrentTimestampCounter = oldTracefile.bitsOfCurrentTimestampCounter;
        bitsOfEvent         = oldTracefile.bitsOfEvent;
        currentTimestampCounterMask = oldTracefile.currentTimestampCounterMask;
        currentTimestampCounterMaskNextBit = oldTracefile.currentTimestampCounterMaskNextBit;
        eventsLost          = oldTracefile.eventsLost;
        subBufferCorrupt    = oldTracefile.subBufferCorrupt;
        currentEvent        = oldTracefile.currentEvent;
        bufferPtr           = oldTracefile.bufferPtr;
        bufferSize          = oldTracefile.bufferSize;
    }

    /**
     * Constructor, using C pointer.<p>
     * 
     * @param newPtr  The pointer of an already opened LttTracefile C Structure
     * 
     * @exception JniException
     * 
     * @see org.eclipse.linuxtools.lttng.jni.eclipse.linuxtools.lttng.jni.JniTrace
     * @see org.eclipse.linuxtools.lttng.jni.common.eclipse.linuxtools.lttng.jni.Jni_C_Pointer
     */
    public JniTracefile(Jni_C_Pointer newPtr, JniTrace newParentTrace) throws JniException {
        thisTracefilePtr = newPtr;
        parentTrace = newParentTrace;
        tracefileMarkersMap = new HashMap<Integer, JniMarker>();

        // Retrieve the trace file information and load the first event.
        try {
            populateTracefileInformation();
        } catch (JniNoSuchEventException e) {
            throw new JniTracefileWithoutEventException(
                    "JniEvent constructor reported that no event of this type are usable. (Jaf_Tracefile)");
        }
    }        

    /**
     * Read the next event of this tracefile.<p>
     * 
     * Note : If the read succeed, the event will be populated.<p>
     *      
     * @return LTT read status, as defined in Jni_C_Common
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.eclipse.linuxtools.lttng.jni.Jni_C_Common
     */
    public int readNextEvent() {
        return currentEvent.readNextEvent();
    }        

    /**
     * Seek to the given time.<p>
     * 
     * Note : If the seek succeed, the event will be populated.
     * 
     * @param seekTime      The timestamp where to seek.
     * 
     * @return LTT read status, as defined in Jni_C_Common
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.eclipse.linuxtools.lttng.jni.Jni_C_Common
     */
    public int seekToTime(JniTime seekTime) {
        return currentEvent.seekToTime(seekTime);
    }

    /* 
     * This function populates the tracefile data with data from LTT
     * 
     * @throws JniException
     */
    private void populateTracefileInformation() throws JniException {
        if (thisTracefilePtr.getPointer() == NULL) {
            throw new JniTracefileException(
                    "Pointer is NULL, trace closed? (populateTracefileInformation)");
        }

        isCpuOnline = ltt_getIsCpuOnline( thisTracefilePtr.getPointer() );
        tracefilePath = ltt_getTracefilepath( thisTracefilePtr.getPointer() );
        tracefileName = ltt_getTracefilename( thisTracefilePtr.getPointer() );
        cpuNumber = ltt_getCpuNumber( thisTracefilePtr.getPointer() );
        tid = ltt_getTid( thisTracefilePtr.getPointer() );
        pgid = ltt_getPgid( thisTracefilePtr.getPointer() );
        creation = ltt_getCreation( thisTracefilePtr.getPointer() );
        tracePtr = new Jni_C_Pointer(ltt_getTracePtr( thisTracefilePtr.getPointer()) );
        markerDataPtr = new Jni_C_Pointer(ltt_getMarkerDataPtr( thisTracefilePtr.getPointer()) );
        CFileDescriptor = ltt_getCFileDescriptor( thisTracefilePtr.getPointer() );
        fileSize = ltt_getFileSize( thisTracefilePtr.getPointer() );
        blocksNumber = ltt_getBlockNumber( thisTracefilePtr.getPointer() );
        isBytesOrderReversed = ltt_getIsBytesOrderReversed( thisTracefilePtr.getPointer() );
        isFloatWordOrdered = ltt_getIsFloatWordOrdered( thisTracefilePtr.getPointer() );
        alignement = ltt_getAlignement( thisTracefilePtr.getPointer() );
        bufferHeaderSize = ltt_getBufferHeaderSize( thisTracefilePtr.getPointer() );
        bitsOfCurrentTimestampCounter = ltt_getBitsOfCurrentTimestampCounter( thisTracefilePtr.getPointer() );
        bitsOfEvent = ltt_getBitsOfEvent( thisTracefilePtr.getPointer() );
        currentTimestampCounterMask = ltt_getCurrentTimestampCounterMask( thisTracefilePtr.getPointer() );
        currentTimestampCounterMaskNextBit = ltt_getCurrentTimestampCounterMaskNextBit( thisTracefilePtr.getPointer() );
        eventsLost = ltt_getEventsLost( thisTracefilePtr.getPointer() );
        subBufferCorrupt = ltt_getSubBufferCorrupt( thisTracefilePtr.getPointer() );
        bufferPtr = new Jni_C_Pointer(ltt_getBufferPtr( thisTracefilePtr.getPointer()) );
        bufferSize = ltt_getBufferSize( thisTracefilePtr.getPointer() );

        // To fill the map is a bit different
        ltt_feedAllMarkers( thisTracefilePtr.getPointer() );

        Jni_C_Pointer tmpEventPointer = new Jni_C_Pointer(ltt_getEventPtr(thisTracefilePtr.getPointer()));
        currentEvent = allocateNewJniEvent(tmpEventPointer , tracefileMarkersMap, this);
    }        
    
    /* 
     * Fills a map of all the markers associated with this tracefile.
     * 
     * Note: This function is called from C and there is no way to propagate
     * exception back to the caller without crashing JNI. Therefore, it MUST
     * catch all exceptions.
     * 
     * @param markerId          Id of the marker (int)
     * @param markerInfoPtr     C Pointer to a marker_info C structure 
     */
    @SuppressWarnings("unused")
    private void addMarkersFromC(int markerId, long markerInfoPtr) {
        // Create a new tracefile object and insert it in the map
        // the tracefile fill itself with LTT data while being constructed
        try {
            JniMarker newMarker = allocateNewJniMarker( new Jni_C_Pointer(markerInfoPtr) );

            tracefileMarkersMap.put(markerId, newMarker);
        } catch (Exception e) {
            printlnC("Failed to add marker to tracefileMarkersMap!(addMarkersFromC)\n\tException raised : " + e.toString());
        }
    }
    
    // Access to class variable. Most of them doesn't have setter
    public boolean getIsCpuOnline() {
        return isCpuOnline;
    }
    
    public String getTracefilePath() {
        return tracefilePath;
    }

    public String getTracefileName() {
        return tracefileName;
    }

    public long getCpuNumber() {
        return cpuNumber;
    }

    public long getTid() {
        return tid;
    }

    public long getPgid() {
        return pgid;
    }

    public long getCreation() {
        return creation;
    }

    public Jni_C_Pointer getTracePtr() {
        return tracePtr;
    }

    public Jni_C_Pointer getMarkerDataPtr() {
        return markerDataPtr;
    }

    public int getCFileDescriptor() {
        return CFileDescriptor;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getBlocksNumber() {
        return blocksNumber;
    }

    public boolean getIsBytesOrderReversed() {
        return isBytesOrderReversed;
    }

    public boolean getIsFloatWordOrdered() {
        return isFloatWordOrdered;
    }

    public long getAlignement() {
        return alignement;
    }

    public long getBufferHeaderSize() {
        return bufferHeaderSize;
    }

    public int getBitsOfCurrentTimestampCounter() {
        return bitsOfCurrentTimestampCounter;
    }

    public int getBitsOfEvent() {
        return bitsOfEvent;
    }

    public long getCurrentTimestampCounterMask() {
        return currentTimestampCounterMask;
    }

    public long getCurrentTimestampCounterMaskNextBit() {
        return currentTimestampCounterMaskNextBit;
    }

    public long getEventsLost() {
        return eventsLost;
    }

    public long getSubBufferCorrupt() {
        return subBufferCorrupt;
    }

    public JniEvent getCurrentEvent() {
        return currentEvent;
    }

    public Jni_C_Pointer getBufferPtr() {
        return bufferPtr;
    }

    public long getBufferSize() {
        return bufferSize;
    }

    public HashMap<Integer, JniMarker> getTracefileMarkersMap() {
        return tracefileMarkersMap;
    }

    /**
     * Parent trace of this tracefile.<p>
     *
     * @return The parent trace
     * 
     * @see org.eclipse.linuxtools.lttng.jni.eclipse.linuxtools.lttng.jni.JniTrace
     */
    public JniTrace getParentTrace() {
        return parentTrace;
    }
    
    /**
     * Pointer to the LttTracefile C structure<p>
     * 
     * The pointer should only be used <u>INTERNALY</u>, do not use unless you
     * know what you are doing.<p>
     * 
     * @return The actual (long converted) pointer or NULL.
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.eclipse.linuxtools.lttng.jni.Jni_C_Pointer
     */
    public Jni_C_Pointer getTracefilePtr() {
        return thisTracefilePtr;
    }
    
    /**
     * Print information for this tracefile. 
     * <u>Intended to debug</u><p>
     * 
     * This function will call Ltt to print, so information printed will be the
     * one from the C structure, not the one populated in java.<p>
     * 
     * This function will not throw but will complain loudly if pointer is NULL.
     */
    public void printTracefileInformation() {

        // If null pointer, print a warning!
        if (thisTracefilePtr.getPointer() == NULL) {
            printlnC("Pointer is NULL, cannot print. (printTracefileInformation)");
        } 
        else {
            ltt_printTracefile( thisTracefilePtr.getPointer() );
        }
    }
    
    /**
     * toString() method. 
     * <u>Intended to debug</u><p>
     * 
     * @return Attributes of the object concatenated in String
     */
	@Override
	public String toString() {
        String returnData = "";
                
        returnData += "isCpuOnline                        : " + isCpuOnline + "\n";
        returnData += "tracefilePath                      : " + tracefilePath + "\n";
        returnData += "tracefileName                      : " + tracefileName + "\n";
        returnData += "cpuNumber                          : " + cpuNumber + "\n";
        returnData += "tid                                : " + tid + "\n";
        returnData += "pgid                               : " + pgid + "\n";
        returnData += "creation                           : " + creation + "\n";
        returnData += "tracePtr                           : " + tracePtr + "\n";
        returnData += "markerDataPtr                      : " + markerDataPtr + "\n";
        returnData += "CFileDescriptor                    : " + CFileDescriptor + "\n";
        returnData += "fileSize                           : " + fileSize + "\n";
        returnData += "blocksNumber                       : " + blocksNumber + "\n";
        returnData += "isBytesOrderReversed               : " + isBytesOrderReversed + "\n";
        returnData += "isFloatWordOrdered                 : " + isFloatWordOrdered + "\n";
        returnData += "alignement                         : " + alignement + "\n";
        returnData += "bufferHeaderSize                   : " + bufferHeaderSize + "\n";
        returnData += "bitsOfCurrentTimestampCounter      : " + bitsOfCurrentTimestampCounter + "\n";
        returnData += "bitsOfEvent                        : " + bitsOfEvent + "\n";
        returnData += "currentTimestampCounterMask        : " + currentTimestampCounterMask + "\n";
        returnData += "currentTimestampCounterMaskNextBit : " + currentTimestampCounterMaskNextBit + "\n";
        returnData += "eventsLost                         : " + eventsLost + "\n";
        returnData += "subBufferCorrupt                   : " + subBufferCorrupt + "\n";
        returnData += "currentEvent                       : " + currentEvent.getReferenceToString() + "\n"; // Hack to avoid ending up with event.toString()
        returnData += "bufferPtr                          : " + bufferPtr + "\n";
        returnData += "bufferSize                         : " + bufferSize + "\n";
        returnData += "tracefileMarkersMap                : " + tracefileMarkersMap.keySet() + "\n"; // Hack to avoid ending up with tracefileMarkersMap.toString()

        return returnData;
    }
	
	
	public abstract JniEvent allocateNewJniEvent(Jni_C_Pointer newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException;
    public abstract JniMarker allocateNewJniMarker(Jni_C_Pointer newMarkerPtr) throws JniException;
	
}

