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


import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.linuxtools.lttng.jni.common.Jni_C_Pointer;
import org.eclipse.linuxtools.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.lttng.jni.exception.JniMarkerException;

/**
 * <b><u>JniMarker</u></b><p>
 * 
 * A JniMarker contain information how to interpret the unparsed content (payload) of an event.<br>
 * Each JniMarker contains several MarkerFields for each fields in the event's payload.
 * 
 * Provides access to the marker_info C structure (from LTT) in java. 
 * 
 * Most important fields in the JniMarker are :
 * <ul>
 * <li> the name of the marker in String
 * <li> an overview of the marker format (in C style printf format)
 * <li> a reference to an ArrayList that contains MarkerFields object of this JniMarker
 * </ul>
 */
public abstract class JniMarker extends Jni_C_Common
{
    // Internal C pointer of the JniEvent used in LTT
    private Jni_C_Pointer thisMarkerPtr = new Jni_C_Pointer();

    private String name = "";
    private String formatOverview = "";
    
    // These two contains hold references to the same MarkerField object
    //  The ArrayList can be used to efficiently find a field by its position
    //  The HashMap can be used to find a field by its name
    private HashMap<String, JniMarkerField> markerFieldsHashMap = null;
    private ArrayList<JniMarkerField> markerFieldsArrayList = null;

    // Native access method
    protected native String ltt_getName(long markerPtr);   
    protected native String ltt_getFormatOverview(long markerPtr);
    @SuppressWarnings("unused")
    protected native long ltt_getSize(long markerPtr);
    @SuppressWarnings("unused")
    protected native short ltt_getLargestAlign(long markerPtr);
    @SuppressWarnings("unused")
    protected native short ltt_getIntSize(long markerPtr);
    @SuppressWarnings("unused")
    protected native short ltt_getLongSize(long markerPtr);
    @SuppressWarnings("unused")
    protected native short ltt_getPointerSize(long markerPtr);
    @SuppressWarnings("unused")
    protected native short ltt_getSize_tSize(long markerPtr);
    protected native void ltt_getAllMarkerFields(long tracePtr);
    @SuppressWarnings("unused")
    protected native short ltt_getAlignement(long markerPtr);
    @SuppressWarnings("unused")
    protected native long ltt_getNextMarkerPtr(long markerPtr);

    // Debug native function, ask LTT to print marker structure
    protected native void ltt_printMarker(long markerPtr);

	static {
		System.loadLibrary("lttvtraceread_loader");
	}

    /*
     * Default constructor is forbidden
     */
    @SuppressWarnings("unused")
    protected JniMarker() {
    }
    
    /**
     * Copy constructor.<p>
     * 
     * @param oldMarker Reference to the JniMarker you want to copy. 
     */
    public JniMarker(JniMarker oldMarker) {
        thisMarkerPtr = oldMarker.thisMarkerPtr;
        name = oldMarker.name;
        formatOverview = oldMarker.formatOverview;
        markerFieldsHashMap = oldMarker.markerFieldsHashMap;
        markerFieldsArrayList = oldMarker.markerFieldsArrayList;

    }

    /**
     * Constructor, using pointer.<p>
     * 
     * @param newMarkerPtr  Pointer to a C marker_info structure
     * 
     * @exception JniException
     */
    public JniMarker(Jni_C_Pointer newMarkerPtr) throws JniException {
        thisMarkerPtr = newMarkerPtr;
        markerFieldsArrayList = new ArrayList<JniMarkerField>();
        markerFieldsHashMap = new HashMap<String, JniMarkerField>();

        // Populate the marker
        populateMarkerInformation();
    }

    
    /* 
     * This function populates the marker data with data from LTT
     * 
     */
    private void populateMarkerInformation() throws JniException {
        if (thisMarkerPtr.getPointer() == NULL) {
            throw new JniMarkerException("Pointer is NULL, trace closed? (populateMarkerInformatOverviewion)");
        } else {
            name = ltt_getName( thisMarkerPtr.getPointer() );
            formatOverview = ltt_getFormatOverview( thisMarkerPtr.getPointer() );
            // To fill the markerFieldArray is a bit different
            ltt_getAllMarkerFields( thisMarkerPtr.getPointer() );
        }
    }

    /* 
     * Fills a map of all the JniMarkerField associated with this JniMarker.
     * 
     * Note: This function is called from C and there is no way to propagate
     * exception back to the caller without crashing JNI. Therefore, it MUST
     * catch all exceptions.
     * 
     * @param markerName        Name of the parent marker
     * @param markerFieldPtr    C Pointer (converted in long) to marker_field C Structure
     */
    @SuppressWarnings("unused")
    private void addMarkerFieldFromC(String markerFieldName, long markerFieldPtr) {
        // Create a new Jaf_markerField object and insert it in the map
        // the maker field fill itself with LTT data while being constructed
        try {
            JniMarkerField newMarkerField = allocateNewJniMarkerField( new Jni_C_Pointer(markerFieldPtr) );
            markerFieldsArrayList.add(newMarkerField);
            markerFieldsHashMap.put(markerFieldName, newMarkerField);
            
        } catch (JniException e) {
            printlnC("Failed to add marker field " + markerFieldName + " to marker fields list!(addMarkerFieldFromC)\n\tException raised : " + e.toString() );
        }
    }

    // Access to class variable. Most of them doesn't have setter
    public String getName() {
        return name;
    }

    public String getFormatOverview() {
        return formatOverview;
    }

    public HashMap<String,JniMarkerField> getMarkerFieldsHashMap() {
        return markerFieldsHashMap;
    }
    
    public ArrayList<JniMarkerField> getMarkerFieldsArrayList() {
        return markerFieldsArrayList;
    }
    
    /**
     * Pointer to the marker_info C structure.<p>
     * 
     * The pointer should only be used <u>INTERNALY</u>, do not use unless you
     * know what you are doing.<p>
     * 
     * @return The actual (long converted) pointer or NULL
     * 
     * @see org.eclipse.linuxtools.lttng.jni.common.eclipse.linuxtools.lttng.jni.Jni_C_Pointer
     */
    public Jni_C_Pointer getMarkerPtr() {
        return thisMarkerPtr;
    }
    
    
    /**
     * Print information for this JniMarker. 
     * <u>Intended to debug</u><br>
     * 
     * This function will call Ltt to print, so information printed will be the one from 
     * the C structure, not the one populated in java.<p>
     * 
     * This function will not throw but will complain loudly if pointer is NULL
     */
    public void printMarkerInformation() {

        // If null pointer, print a warning!
        if (thisMarkerPtr.getPointer() == NULL) {
            printlnC("Pointer is NULL, cannot print. (printMarkerInformation)");
        } else {
            ltt_printMarker(thisMarkerPtr.getPointer());
        }
    }
    
    /**
     * Print information for ALL marker fields for this marker. 
     * <u>Intended to debug</u><br>
     * 
     * This function will call Ltt to print, so information printed will be the one from 
     * the C structure, not the one populated in java.
     */
    public void printAllMarkerFieldsInformation() {
        Object[] allMarkersField = markerFieldsArrayList.toArray();

        for (int pos = 0; pos < allMarkersField.length; pos++) {
            printlnC(allMarkersField[pos].toString());
        }
    }
    
    /**
     * toString() method. 
     * <u>Intended to debug</u><br>
     * 
     * @return Attributes of the object concatenated in String
     */
    @Override
	public String toString() {
        String returnData = "";

        returnData += "name                    : " + name + "\n";
        returnData += "formatOverview          : " + formatOverview + "\n";
        returnData += "markerFieldArrayList    : " + markerFieldsArrayList.toArray() + "\n";

        return returnData;
    }
    
    
    public abstract JniMarkerField allocateNewJniMarkerField(Jni_C_Pointer newMarkerFieldPtr) throws JniException;
    
}
