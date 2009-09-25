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

package org.eclipse.linuxtools.lttng.event;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.linuxtools.tmf.event.TmfEventFormat;

/**
 * <b><u>LttngEventFormat</u></b>
 * <p>
 * Lttng specific implementation of the TmfEventFormat
 * <p>
 * The Lttng implementation of format override parse but require a LttngEvent for most functions so it can call Jni
 */
public class LttngEventFormat extends TmfEventFormat {

    /**
     * Default constructor
     */
    public LttngEventFormat( String[] labels) {
        super(labels);
    }
    
    /**
     * Copy constructor
     */
    public LttngEventFormat( LttngEventFormat oldFormat ) {
        this(oldFormat.getLabels());
    }
    
    /**
     * Parse the LttngEvent linked to this LttngEventFormat.<br>
     * 
     * @return LttngEventField[]     An array of LttngEventField that contain the parsed data
     */
    public LttngEventField[] parse(LttngEvent thisEvent) {
        HashMap<String, Object> parsedMap = thisEvent.convertEventTmfToJni().parseAllFields();
        LttngEventField[] returnedField = new LttngEventField[parsedMap.size()];
        
        String fieldName = null;
        int position = 0;
        Iterator<String> iterator = parsedMap.keySet().iterator();
        while (iterator.hasNext()) {
            fieldName = iterator.next();
            returnedField[position] = new LttngEventField( fieldName, parsedMap.get(fieldName) );
            position++;
        }
        
        return returnedField;
    }
    
    
    
    /* *** FIXME
     * TEMPORARY IMPLEMENTATION
     * THIS WILL DISAPEAR ( ?? )
     */
    public LttngEventField[] parse(HashMap<String, Object> parsedEvents) {
        LttngEventField[] returnedField = new LttngEventField[parsedEvents.size()];
        
        String fieldName = null;
        int position = 0;
        Iterator<String> iterator = parsedEvents.keySet().iterator();
        while (iterator.hasNext()) {
            fieldName = iterator.next();
            returnedField[position] = new LttngEventField( fieldName, parsedEvents.get(fieldName) );
            position++;
        }
        
        return returnedField;
    }
    
    /* *** FIXME ***
     * Evil "do at least something" parse function
     * THIS IS JUST FOR COMPATIBILITY! DO NOT USE!
     * Content/Format/Fields interelation need to be revisited ASAP
     */ 
    public LttngEventField[] parse(String uselessContent) {
        // *** Begining of the evil "parse String" function
        // 
        // - 1st : Find the number of ":" in the String. This will be the number of fields in the String. 
        //      Highly unreliable, as we depend on String content that we don't control!
        int nbFields = 0;
        for ( int pos = 0; pos < uselessContent.length(); pos++ ) {
            if ( uselessContent.substring(pos, pos+1).equals(":") ) {
                nbFields++;
            }
        }
        
        // - 2nd : Create the fields array
        LttngEventField[] tmpFields = new LttngEventField[nbFields];
        
        // - 3rd : Fill the array
        int fieldPosition = 0;
        
        int lastFieldnamePos = 0;
        int lastDoubleDottedPos = 0;
        
        int lastSpacePos = 0;
        
        boolean isSearchingFieldname = true;
        
        String  fieldName   = "";
        String  fieldValue  = "";
        
        int pos = 0;
        while ( (pos < uselessContent.length()) && (fieldPosition<nbFields) )
        {
            if ( uselessContent.substring(pos, pos+1).equals(":") ) {
              
              if ( isSearchingFieldname==false ) {
                  fieldValue = uselessContent.substring(lastDoubleDottedPos+1, lastSpacePos);
                  tmpFields[fieldPosition] = new LttngEventField( fieldName, fieldValue );
                  fieldPosition++;
                  isSearchingFieldname = true;
              }
              
              lastDoubleDottedPos = pos;
            }
            else if ( uselessContent.substring(pos, pos+1).equals(" ") ) {
                
                if ( isSearchingFieldname==true ) {
                    fieldName = uselessContent.substring(lastFieldnamePos, lastDoubleDottedPos);
                    lastFieldnamePos = pos+1;
                    isSearchingFieldname = false;
                }
                
                lastSpacePos = pos;
            }
            else if ( pos+2 >= uselessContent.length() ) {
                fieldName = uselessContent.substring(lastSpacePos+1, lastDoubleDottedPos);
                fieldValue = uselessContent.substring(lastDoubleDottedPos+1, pos+1);
                
                tmpFields[fieldPosition] = new LttngEventField( fieldName, fieldValue );
                fieldPosition++;
            }
            
            pos++;
        }
        
        return tmpFields;
    }

}
