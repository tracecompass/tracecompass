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

import org.eclipse.linuxtools.internal.lttng.jni.common.JniTime;
import org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniNoSuchEventException;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniTracefileException;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniTracefileWithoutEventException;

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
 * <b>NOTE</b><p>
 * This class is ABSTRACT, you need to extends it to support your specific LTTng version.<br>
 * Please look at the abstract functions to override at the bottom of this file.<p>
 *
 */
public abstract class JniTracefile extends Jni_C_Common
{
    // Internal C pointer of the JniTracefile used in LTT
    private Jni_C_Pointer_And_Library_Id thisTracefilePtr = new Jni_C_Pointer_And_Library_Id();

    // Reference to the parent trace
    private JniTrace parentTrace = null;

    // Data we should populate from LTT
    // Note that all type have been scaled up as there is no "unsigned" in java
    // This might be a problem about "unsigned long" as there is no equivalent in java
    private boolean isCpuOnline = false;
    private String  tracefilePath = ""; //$NON-NLS-1$
    private String  tracefileName = ""; //$NON-NLS-1$
    private long    cpuNumber = 0;
    private long    tid = 0;
    private long    pgid = 0;
    private long    creation = 0;

    // Internal C pointer for trace and marker
    // Note : These are real Jni_C_Pointer, not Jni_C_Pointer_And_Library_Id
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
    // Note : This one is a real Jni_C_Pointer, not Jni_C_Pointer_And_Library_Id
    private Jni_C_Pointer bufferPtr = null;

    private long    bufferSize = 0;

    // This map will hold markers_info owned by this tracefile
    private HashMap<Integer, JniMarker> tracefileMarkersMap = null;

    // Native access functions
    protected native boolean  ltt_getIsCpuOnline(int libId, long tracefilePtr);
    protected native String   ltt_getTracefilepath(int libId, long tracefilePtr);
    protected native String   ltt_getTracefilename(int libId, long tracefilePtr);
    protected native long     ltt_getCpuNumber(int libId, long tracefilePtr);
    protected native long     ltt_getTid(int libId, long tracefilePtr);
    protected native long     ltt_getPgid(int libId, long tracefilePtr);
    protected native long     ltt_getCreation(int libId, long tracefilePtr);
    protected native long     ltt_getTracePtr(int libId, long tracefilePtr);
    protected native long     ltt_getMarkerDataPtr(int libId, long tracefilePtr);
    protected native int      ltt_getCFileDescriptor(int libId, long tracefilePtr);
    protected native long     ltt_getFileSize(int libId, long tracefilePtr);
    protected native long     ltt_getBlockNumber(int libId, long tracefilePtr);
    protected native boolean  ltt_getIsBytesOrderReversed(int libId, long tracefilePtr);
    protected native boolean  ltt_getIsFloatWordOrdered(int libId, long tracefilePtr);
    protected native long     ltt_getAlignement(int libId, long tracefilePtr);
    protected native long     ltt_getBufferHeaderSize(int libId, long tracefilePtr);
    protected native int      ltt_getBitsOfCurrentTimestampCounter(int libId, long tracefilePtr);
    protected native int      ltt_getBitsOfEvent(int libId, long tracefilePtr);
    protected native long     ltt_getCurrentTimestampCounterMask(int libId, long tracefilePtr);
    protected native long     ltt_getCurrentTimestampCounterMaskNextBit(int libId, long tracefilePtr);
    protected native long     ltt_getEventsLost(int libId, long tracefilePtr);
    protected native long     ltt_getSubBufferCorrupt(int libId, long tracefilePtr);
    protected native long     ltt_getEventPtr(int libId, long tracefilePtr);
    protected native long     ltt_getBufferPtr(int libId, long tracefilePtr);
    protected native long     ltt_getBufferSize(int libId, long tracefilePtr);

    // Method to fill a map with marker object
    protected native void ltt_feedAllMarkers(int libId, long tracefilePtr);

    // Debug native function, ask LTT to print tracefile structure
    protected native void ltt_printTracefile(int libId, long tracefilePtr);

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
     * Constructor, using C pointer.
     * <p>
     *
     * @param newPtr
     *            The pointer of an already opened LttTracefile C Structure
     * @param newParentTrace
     *            The JniTrace parent of this tracefile.
     * @exception JniException
     *                If the JNI call fails
     *
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
    public JniTracefile(Jni_C_Pointer_And_Library_Id newPtr, JniTrace newParentTrace) throws JniException {
    	thisTracefilePtr = newPtr;
        parentTrace = newParentTrace;
        tracefileMarkersMap = new HashMap<Integer, JniMarker>();

        // Retrieve the trace file information and load the first event.
        try {
            populateTracefileInformation();
        }
        catch (JniNoSuchEventException e) {
            throw new JniTracefileWithoutEventException("JniEvent constructor reported that no event of this type are usable. (Jni_Tracefile)"); //$NON-NLS-1$
        }
    }

    /**
     * Read the next event of this tracefile.<p>
     *
     * Note : If the read succeed, the event will be populated.<p>
     *
     * @return LTT read status, as defined in Jni_C_Constant
     *
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Constant
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
     * @return LTT read status, as defined in Jni_C_Constant
     *
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Constant
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
            throw new JniTracefileException("Pointer is NULL, trace closed? (populateTracefileInformation)"); //$NON-NLS-1$
        }

        isCpuOnline = ltt_getIsCpuOnline(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer());
        tracefilePath = ltt_getTracefilepath(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer());
        tracefileName = ltt_getTracefilename(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer());
        cpuNumber = ltt_getCpuNumber(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer());
        tid = ltt_getTid(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        pgid = ltt_getPgid(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        creation = ltt_getCreation(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        tracePtr = new Jni_C_Pointer(ltt_getTracePtr(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer()) );
        markerDataPtr = new Jni_C_Pointer(ltt_getMarkerDataPtr(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer()) );
        CFileDescriptor = ltt_getCFileDescriptor(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        fileSize = ltt_getFileSize(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        blocksNumber = ltt_getBlockNumber(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        isBytesOrderReversed = ltt_getIsBytesOrderReversed(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        isFloatWordOrdered = ltt_getIsFloatWordOrdered(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        alignement = ltt_getAlignement(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        bufferHeaderSize = ltt_getBufferHeaderSize(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        bitsOfCurrentTimestampCounter = ltt_getBitsOfCurrentTimestampCounter(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        bitsOfEvent = ltt_getBitsOfEvent(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        currentTimestampCounterMask = ltt_getCurrentTimestampCounterMask(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        currentTimestampCounterMaskNextBit = ltt_getCurrentTimestampCounterMaskNextBit(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        eventsLost = ltt_getEventsLost(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        subBufferCorrupt = ltt_getSubBufferCorrupt(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
        bufferPtr = new Jni_C_Pointer(ltt_getBufferPtr(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer()) );
        bufferSize = ltt_getBufferSize(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer());

        // To fill the map is a bit different
        ltt_feedAllMarkers(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );

        Jni_C_Pointer_And_Library_Id tmpEventPointer = new Jni_C_Pointer_And_Library_Id(thisTracefilePtr.getLibraryId(), ltt_getEventPtr(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer()));
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
	private void addMarkersFromC(int markerId, long markerInfoPtr) {
        // Create a new tracefile object and insert it in the map
        // the tracefile fill itself with LTT data while being constructed
        try {
            JniMarker newMarker = allocateNewJniMarker( new Jni_C_Pointer_And_Library_Id(thisTracefilePtr.getLibraryId(), markerInfoPtr) );

            tracefileMarkersMap.put(markerId, newMarker);
        } catch (Exception e) {
            printlnC(thisTracefilePtr.getLibraryId(), "Failed to add marker to tracefileMarkersMap!(addMarkersFromC)\n\tException raised : " + e.toString()); //$NON-NLS-1$
        }
    }

    // Access to class variable. Most of them don't have setters

    /**
     * Return if the CPU corresponding to this trace file is online or not.
     *
     * @return If the CPU is online, true/false
     */
    public boolean getIsCpuOnline() {
        return isCpuOnline;
    }

    /**
     * Get the complete path to this trace file
     *
     * @return The file path
     */
    public String getTracefilePath() {
        return tracefilePath;
    }

    /**
     * Get the base name of this trace file
     *
     * @return The file name
     */
    public String getTracefileName() {
        return tracefileName;
    }

    /**
     * Get the CPU number corresponding to this file
     *
     * @return The CPU number. Yes, as a long. Don't ask.
     */
    public long getCpuNumber() {
        return cpuNumber;
    }

    /**
     * Get the TID of this trace file (for UST traces)
     *
     * @return The TID
     */
    public long getTid() {
        return tid;
    }

    /**
     * Get the PGID of this trace file (for UST traces)
     *
     * @return The PGID
     */
    public long getPgid() {
        return pgid;
    }

    /**
     * Get the creation time of this process (for UST traces)
     *
     * @return The creation timestamp
     */
    public long getCreation() {
        return creation;
    }

    /**
     * Get the C pointer to the trace
     *
     * @return The Jni_C_Pointer to the trace
     */
    public Jni_C_Pointer getTracePtr() {
        return tracePtr;
    }

    /**
     * Get the C pointer to the marker data object
     *
     * @return The Jni_C_Pointer to the marker data
     */
    public Jni_C_Pointer getMarkerDataPtr() {
        return markerDataPtr;
    }

    /**
     * Get the file descriptor number used for this trace file
     *
     * @return The file descriptor index
     */
    public int getCFileDescriptor() {
        return CFileDescriptor;
    }

    /**
     * Get the size of this file, in bytes
     *
     * @return The file size
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Get the number of blocks in this trace file
     *
     * @return The number of blocks
     */
    public long getBlocksNumber() {
        return blocksNumber;
    }

    /**
     * Return if the byte order is reversed in this trace
     *
     * @return If the byte order is reversed, Y/N
     */
    public boolean getIsBytesOrderReversed() {
        return isBytesOrderReversed;
    }

    /**
     * Return if floats are aligned to words in this trace
     *
     * @return If floats are aligned, Y/N
     */
    public boolean getIsFloatWordOrdered() {
        return isFloatWordOrdered;
    }

    /**
     * Get the byte alignment of this trace
     *
     * @return The byte alignment
     */
    public long getAlignement() {
        return alignement;
    }

    /**
     * Get the size of the buffer headers, in bytes
     *
     * @return The buffer header size
     */
    public long getBufferHeaderSize() {
        return bufferHeaderSize;
    }

    /**
     * Get the number of bits for the current timestamp counter
     *
     * @return The number of bits for the TS counter
     */
    public int getBitsOfCurrentTimestampCounter() {
        return bitsOfCurrentTimestampCounter;
    }

    /**
     * Get the number of bits for the current event
     *
     * @return The number of bits for the event
     */
    public int getBitsOfEvent() {
        return bitsOfEvent;
    }

    /**
     * Get the mask for the current timestamp counter
     *
     * @return The TS counter mask
     */
    public long getCurrentTimestampCounterMask() {
        return currentTimestampCounterMask;
    }

    /**
     * Get the mask of the next bit for the current timestamp counter
     *
     * @return The mask of the next bit
     */
    public long getCurrentTimestampCounterMaskNextBit() {
        return currentTimestampCounterMaskNextBit;
    }

    /**
     * Get the amount of lost events in this trace file
     *
     * @return The amount of lost events
     */
    public long getEventsLost() {
        return eventsLost;
    }

    /**
     * Return if the current subbuffer is corrupted. Or maybe the number of
     * corrupted subbuffers? I have no idea...
     *
     * @return The number of subbuffers
     */
    public long getSubBufferCorrupt() {
        return subBufferCorrupt;
    }

    /**
     * Get the current event this trace is pointed at.
     *
     * @return The current event, as a JniEvent
     */
    public JniEvent getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Get a pointer to the current subbuffer
     *
     * @return Pointer to the current subbuffer
     */
    public Jni_C_Pointer getBufferPtr() {
        return bufferPtr;
    }

    /**
     * Get the buffer size for this trace, in bytes
     *
     * @return The buffer size
     */
    public long getBufferSize() {
        return bufferSize;
    }

    /**
     * Get the map of markers in this trace file
     *
     * @return The map of markers
     */
    public HashMap<Integer, JniMarker> getTracefileMarkersMap() {
        return tracefileMarkersMap;
    }

    /**
     * Parent trace of this tracefile.<p>
     *
     * @return The parent trace
     *
     * @see org.eclipse.linuxtools.lttng.jni.JniTrace
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
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
    public Jni_C_Pointer_And_Library_Id getTracefilePtr() {
        return thisTracefilePtr;
    }

    /**
     * Print information for this tracefile.
     * <u>Intended to debug</u><p>
     *
     * This function will call Ltt to print, so information printed will be the
     * one from the C structure, not the one populated in java.<p>
     */
    public void printTracefileInformation() {
        ltt_printTracefile(thisTracefilePtr.getLibraryId(), thisTracefilePtr.getPointer() );
    }

    /**
     * toString() method.
     * <u>Intended to debug</u><p>
     *
     * @return Attributes of the object concatenated in String
     */
	@Override
    @SuppressWarnings("nls")
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


	// ****************************
    // **** ABSTRACT FUNCTIONS ****
    // You MUST override those in your version specific implementation


	/**
     * Function place holder to allocate a new JniEvent.<p>
     * <br>
     * JniEvent constructor is non overridable so we need another overridable function to return the correct version of JniEvent.<br>
     * Effect of this function should be the same (allocate a fresh new JniEvent).<br>
     * <br>
     * <b>!! Override this with you version specific implementation.</b><br>
     *
     * @param newEventPtr			The pointer of an already opened LttEvent C Structure
     * @param newMarkersMap			An already populated HashMap of JniMarker objects for this new event
     * @param newParentTracefile	The JniTrace parent of this tracefile.
     *
     * @return						The newly allocated JniEvent of the correct version
     *
     * @throws JniException			The construction (allocation) failed.
     *
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     * @see org.eclipse.linuxtools.lttng.jni.JniMarker
     * @see org.eclipse.linuxtools.lttng.jni.JniTracefile
     */
	public abstract JniEvent allocateNewJniEvent(Jni_C_Pointer_And_Library_Id newEventPtr, HashMap<Integer, JniMarker> newMarkersMap, JniTracefile newParentTracefile) throws JniException;


	/**
     * Function place holder to allocate a new JniMarker.<p>
     * <br>
     * JniMarker constructor is non overridable so we need another overridable function to return the correct version of JniMarker.<br>
     * Effect of this function should be the same (allocate a fresh new JniMarker).<br>
     * <br>
     * <b>!! Override this with you version specific implementation.</b><br>
     *
     * @param newMarkerPtr			The pointer of an already opened marker_info C Structure
     *
     * @return						The newly allocated JniMarker of the correct version
     *
     * @throws JniException			The construction (allocation) failed.
     *
     * @see org.eclipse.linuxtools.lttng.jni.JniMarker
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
	public abstract JniMarker allocateNewJniMarker(Jni_C_Pointer_And_Library_Id newMarkerPtr) throws JniException;

}
