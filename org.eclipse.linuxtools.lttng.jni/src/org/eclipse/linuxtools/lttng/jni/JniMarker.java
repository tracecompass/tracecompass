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

import org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniException;
import org.eclipse.linuxtools.internal.lttng.jni.exception.JniMarkerException;

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
 * 
 * <b>NOTE</b><p>
 * This class is ABSTRACT, you need to extends it to support your specific LTTng version.<br>
 * Please look at the abstract functions to override at the bottom of this file.<p>
 * 
 */
public abstract class JniMarker extends Jni_C_Common
{
    // Internal C pointer of the JniEvent used in LTT
    private Jni_C_Pointer_And_Library_Id thisMarkerPtr = new Jni_C_Pointer_And_Library_Id();

    private String name = ""; //$NON-NLS-1$
    private String formatOverview = ""; //$NON-NLS-1$
    
    // These two contains hold references to the same MarkerField object
    //  The ArrayList can be used to efficiently find a field by its position
    //  The HashMap can be used to find a field by its name
    private HashMap<String, JniMarkerField> markerFieldsHashMap = null;
    private ArrayList<JniMarkerField> markerFieldsArrayList = null;

    // Native access method
    protected native String ltt_getName(int libId, long markerPtr);   
    protected native String ltt_getFormatOverview(int libId, long markerPtr);
    protected native long ltt_getSize(int libId, long markerPtr);
    protected native short ltt_getLargestAlign(int libId, long markerPtr);
    protected native short ltt_getIntSize(int libId, long markerPtr);
    protected native short ltt_getLongSize(int libId, long markerPtr);
    protected native short ltt_getPointerSize(int libId, long markerPtr);
    protected native short ltt_getSize_tSize(int libId, long markerPtr);
    protected native void ltt_getAllMarkerFields(int libId, long tracePtr);
    protected native short ltt_getAlignement(int libId, long markerPtr);
    protected native long ltt_getNextMarkerPtr(int libId, long markerPtr);

    // Debug native function, ask LTT to print marker structure
    protected native void ltt_printMarker(int libId, long markerPtr);

    /*
     * Default constructor is forbidden
     */
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
    public JniMarker(Jni_C_Pointer_And_Library_Id newMarkerPtr) throws JniException {
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
            throw new JniMarkerException("Pointer is NULL, trace closed? (populateMarkerInformatOverviewion)"); //$NON-NLS-1$
        } else {
            name = ltt_getName(thisMarkerPtr.getLibraryId(), thisMarkerPtr.getPointer());
            formatOverview = ltt_getFormatOverview(thisMarkerPtr.getLibraryId(),  thisMarkerPtr.getPointer());
            // To fill the markerFieldArray is a bit different
            ltt_getAllMarkerFields(thisMarkerPtr.getLibraryId(),  thisMarkerPtr.getPointer());
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
	private void addMarkerFieldFromC(String markerFieldName, long markerFieldPtr) {
        // Create a new Jni_markerField object and insert it in the map
        // the maker field fill itself with LTT data while being constructed
        try {
            JniMarkerField newMarkerField = allocateNewJniMarkerField( new Jni_C_Pointer_And_Library_Id(thisMarkerPtr.getLibraryId(), markerFieldPtr));
            markerFieldsArrayList.add(newMarkerField);
            markerFieldsHashMap.put(markerFieldName, newMarkerField);
            
        } catch (JniException e) {
            printlnC(thisMarkerPtr.getLibraryId(), "Failed to add marker field " + markerFieldName + " to marker fields list!(addMarkerFieldFromC)\n\tException raised : " + e.toString() ); //$NON-NLS-1$ //$NON-NLS-2$
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
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     */
    public Jni_C_Pointer_And_Library_Id getMarkerPtr() {
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
        ltt_printMarker(thisMarkerPtr.getLibraryId(), thisMarkerPtr.getPointer());
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
            printlnC(thisMarkerPtr.getLibraryId(), allMarkersField[pos].toString());
        }
    }
    
    /**
     * toString() method. 
     * <u>Intended to debug</u><br>
     * 
     * @return Attributes of the object concatenated in String
     */
    @Override
    @SuppressWarnings("nls")
	public String toString() {
        String returnData = "";

        returnData += "name                    : " + name + "\n";
        returnData += "formatOverview          : " + formatOverview + "\n";
        returnData += "markerFieldArrayList    : " + markerFieldsArrayList.hashCode() + " (size : " + markerFieldsArrayList.size() + " )" + "\n";
        
        return returnData;
    }
    
    
    // ****************************
    // **** ABSTRACT FUNCTIONS ****
    // You MUST override those in your version specific implementation
	
	
	/**
     * Function place holder to allocate a new JniMarkerField.<p>
     * <br>
     * JniMarkerField constructor is non overridable so we need another overridable function to return the correct version of JniMarkerField.<br>
     * Effect of this function should be the same (allocate a fresh new JniMarkerField).<br>
     * <br>
     * <b>!! Override this with you version specific implementation.</b><br>
     * 
     * @param newMarkerFieldPtr		The pointer and library id of an already opened marker_field C Structure
     * 
     * @return						The newly allocated JniMarkerField of the correct version
     * 
     * @throws JniException			The construction (allocation) failed.
     * 
     * @see org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer_And_Library_Id
     * @see org.eclipse.linuxtools.lttng.jni.JniMarkerField
     */
    public abstract JniMarkerField allocateNewJniMarkerField(Jni_C_Pointer_And_Library_Id newMarkerFieldPtr) throws JniException;
    
}
