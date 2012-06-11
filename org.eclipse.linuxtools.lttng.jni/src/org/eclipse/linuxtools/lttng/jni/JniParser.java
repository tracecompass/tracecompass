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
import java.util.Iterator;

import org.eclipse.linuxtools.internal.lttng.jni.common.Jni_C_Pointer;

/**
 * <b><u>JniParser</u></b><p>
 *
 * JniParser is used to parse an event payload into something usable.<p>
 *
 * All methods are static, the parser shouldn't be instantiated.
 *
 * <b>NOTE</b><p>
 * This class is ABSTRACT, you need to extends it to support your specific LTTng version.<p>
 *
 */
public abstract class JniParser extends Jni_C_Common
{
    protected static native void ltt_getParsedData(int libId, ParsedObjectContent parseddata, long eventPtr, long markerFieldPtr);

    // *** HACK ***
    // We cannot use "Object" directly as java does not support swapping primitive value
    //    We either need to create a new object type or to use a "non-primitive" type that have "Setter()" functions
    //    Another (ugly) hack would be to pass an array to modify the reference's reference.
    // ***
    private static ParsedObjectContent parsedData = new ParsedObjectContent();

    /*
     * Default constructor is forbidden
     */
    protected JniParser() {
    }


    /**
     * Method to parse a single field identified by its id.<p>
     *
     * All parsing will be done on C side as we need LTT functions.
     *
     * @param   eventToParse    The jni event we want to parse.
     * @param   fieldPosition   The position (or id) of the field we want to parse
     *
     * @return                  An Object that contain the JniEvent payload parsed by the C, or null, if if was impossible to parse (i.e., wrong position)
     *
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    static public Object parseField(JniEvent eventToParse, int fieldPosition) {

        // Sanity check
        if ( (fieldPosition < 0) || ( fieldPosition >= eventToParse.requestEventMarker().getMarkerFieldsArrayList().size() ) ){
            return null;
        }

        JniMarkerField tmpField = eventToParse.requestEventMarker().getMarkerFieldsArrayList().get(fieldPosition);

        // Call the parsing function in C. The result will be put in parsedData object
        ltt_getParsedData(eventToParse.getEventPtr().getLibraryId(), parsedData, eventToParse.getEventPtr().getPointer(), tmpField.getMarkerFieldPtr().getPointer());

        return parsedData.getData();
    }


    /**
     * Method to parse a single field identified by its name.<p>
     *
     * All parsing will be done on C side as we need LTT functions.
     *
     * @param   eventToParse    The jni event we want to parse.
     * @param   fieldName       The name of the field we want to parse.
     *
     * @return                  An Object that contain the JniEvent payload parsed by the C, or null, if if was impossible to parse (i.e., wrong position)
     *
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    static public Object parseField(JniEvent eventToParse, String fieldName) {

        JniMarkerField tmpField = eventToParse.requestEventMarker().getMarkerFieldsHashMap().get(fieldName);

        // return immediately if there is no field by that name
        if ( tmpField == null ) {
            return null;
        }

        ltt_getParsedData(eventToParse.getEventPtr().getLibraryId(), parsedData, eventToParse.getEventPtr().getPointer(), tmpField.getMarkerFieldPtr().getPointer());

        return parsedData.getData();
    }



    /**
     * Method to parse all fields at once.<p>
     *
     * All parsing will be done on C side as we need LTT functions.
     *
     * @param   eventToParse    The jni event we want to parse.
     * @return                  An HashMap of Object that contain the is the JniEvent's payload parsed by the C
     *
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    static public HashMap<String, Object> parseAllFields(JniEvent eventToParse) {
        HashMap<String,JniMarkerField> markerFieldData = eventToParse.requestEventMarker().getMarkerFieldsHashMap();

		// This hashmap will contain the parsed content.
		// ParsedContent is a local class defined at the end of this file

		// *** HACK ***
		// We want (need?) the map that contain the parsed data to be in the same order as markerField map
		// The "instinctive way" would be to use :
		//       HashMap<String, Object> parsedDataMap = new HashMap<String, Object>(nbMarkerField);
		//
		// However, we cannot ensure that the newly created hashmap will use the same order.
		// The hard way would be to override the default hash function for both hashmap
		// However, this is way easier to abuse the fact that both hashmap are of type <String, something...>
		// Therefore we can abuse the java-cast with clone() :
		//       HashMap<String, Object> parsedDataMap = (HashMap<String, Object>)markerFieldData.clone();
		// Or even safer, use HashMap constructor to do so :
        HashMap<String, Object> parsedDataMap = new HashMap<String, Object>(markerFieldData);

        JniMarkerField      newMarkerField  = null;
        Iterator<String>    iterator        = markerFieldData.keySet().iterator();

        while ( iterator.hasNext() ) {
            newMarkerField = markerFieldData.get(iterator.next());
            // Call the C to parse the data
            ltt_getParsedData(eventToParse.getEventPtr().getLibraryId(), parsedData, eventToParse.getEventPtr().getPointer(), newMarkerField.getMarkerFieldPtr().getPointer());
            // Save the result into the HashMap
            parsedDataMap.put(newMarkerField.getField(), parsedData.getData() );
        }

        return parsedDataMap;
    }


    /*
     * Add a parsed String value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     *
     * @param parsedArray   Array where to store the value
     * @param fieldName     The name of the parsed field
     * @param stringToAdd   The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
	static private void addStringToParsingFromC(Object contentHolder, String stringToAdd) {
        ((ParsedObjectContent)contentHolder).setData( stringToAdd);
    }

    /*
     * Add a parsed 64 bits Pointer value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     *
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param pointerToAdd  The parsed data to add (in 64 bits long!)
     * @param formatToAdd   The format of the raw data
     */
	static private void addLongPointerToParsingFromC(Object contentHolder, long pointerToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new Jni_C_Pointer(pointerToAdd));
    }

    /*
     * Add a parsed 32 bits Pointer value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     *
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param pointerToAdd  The parsed data to add (converted in 64 bits long!)
     * @param formatToAdd   The format of the raw data
     */
	static private void addIntPointerToParsingFromC(Object contentHolder, long pointerToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new Jni_C_Pointer((int) pointerToAdd));
    }

    /*
     * Add a parsed short value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     *
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param shortToAdd    The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
	static private void addShortToParsingFromC(Object contentHolder, short shortToAdd) {
        ((ParsedObjectContent)contentHolder).setData( Short.valueOf(shortToAdd));
    }

    /*
     * Add a parsed integer value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     *
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param intToAdd      The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
	static private void addIntegerToParsingFromC(Object contentHolder, int intToAdd) {
        ((ParsedObjectContent)contentHolder).setData( Integer.valueOf(intToAdd));
    }

    /*
     * Add a parsed long value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     *
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param longToAdd     The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
	static private void addLongToParsingFromC(Object contentHolder, long longToAdd) {
        ((ParsedObjectContent)contentHolder).setData( Long.valueOf(longToAdd));
    }

    /*
     * Add a parsed float value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     *
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param floatToAdd    The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
	static private void addFloatToParsingFromC(Object contentHolder, float floatToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new Float(floatToAdd));
    }

    /*
     * Add a parsed double value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     *
     *
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param doubleToAdd   The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
	static private void addDoubleToParsingFromC(Object contentHolder, double doubleToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new Double(doubleToAdd));
    }

}


/**
 * <b><u>ParsedObjectContent</u></b><p>
 *
 * ParsedObjectContent class.
 * Only be used locally in this object to parse event data more efficiently in the C.
 */
class ParsedObjectContent {
    private Object parsedData = null;

    public Object getData() {
        return parsedData;
    }

    public void setData(Object newData) {
        parsedData = newData;
    }
}
