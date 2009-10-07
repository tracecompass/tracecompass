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
 * <b><u>LttngEventFormat</u></b><p>
 * 
 * Lttng specific implementation of the TmfEventFormat.<p>
 */
public class LttngEventFormat extends TmfEventFormat {

    /**
     * Constructor with parameters.<p>
     * 
     * @param labels    The fields labels of the content. 
     */
    public LttngEventFormat( String[] labels) {
        super(labels);
    }
    
    /**
     * Copy constructor.<p>
     * 
     * @param oldFormat     The format to copy from
     */
    public LttngEventFormat( LttngEventFormat oldFormat ) {
        this(oldFormat.getLabels());
    }
    
    /**
     * Parse the given LttngEvent.<p>
     * 
     * @param thisEvent   The LttngEvent to parse
     * 
     * @return            An array of LttngEventField that contain the parsed data
     */
    public LttngEventField[] parse(LttngEvent thisEvent) {
        // Obtain the parsed content from the JNI
        HashMap<String, Object> parsedMap = thisEvent.convertEventTmfToJni().parseAllFields();
        LttngEventField[] returnedField = new LttngEventField[parsedMap.size()];
        
        String fieldName = null;
        int position = 0;
        Iterator<String> iterator = parsedMap.keySet().iterator();
        // Loop to create the LttngEventField from the parsedContent
        while (iterator.hasNext()) {
            fieldName = iterator.next();
            returnedField[position] = new LttngEventField( fieldName, parsedMap.get(fieldName) );
            position++;
        }
        
        return returnedField;
    }
    
    
    /**
     * <b>*FIXME*</b><br>Parse from a HashMap of content.<p>
     * 
     * This function will hopefully disapears soon!<br>
     * We need to parse WHILE CREATING LttngEvent, so we cannot always use parse(LttngEvent).<br>
     * This function is ugly but should be 100% safe to use.<p>
     * 
     * @param  parsedEvents     HashMap of parsed content, as returned by JniParser.parseAllFields()
     * 
     * @return An array of TmfEventFields
     */
    public LttngEventField[] parse(HashMap<String, Object> parsedEvents) {
        LttngEventField[] returnedField = new LttngEventField[parsedEvents.size()];
        
        String fieldName = null;
        int position = 0;
        Iterator<String> iterator = parsedEvents.keySet().iterator();
        // Loop to create the LttngEventField from the parsedContent in the map
        while (iterator.hasNext()) {
            fieldName = iterator.next();
            returnedField[position] = new LttngEventField( fieldName, parsedEvents.get(fieldName) );
            position++;
        }
        return returnedField;
    }
    
    /**
     * <b>*DEPRECATED*</b><br>Parse from the content string.<p>
     * 
     * <b><u>DO NOT USE!</b></u> <br>
     * This function will disapears soon! Use parse(LttngEvent) instead.<br>
     * It is evil to parse content string directly as we <u>do not control the content</u>.<br>
     * So this would theorically works but it could break at any moment if kernel developpers changes their usual format.<p>
     * 
     * This is provided because we need to implement this function since we inherit TmfFormat
     * 
     * @param  uselessContent    Content to parse as String
     * 
     * @return An array of TmfEventFields
     */
    public LttngEventField[] parse(String uselessContent) {
        
        // This function is _under commented_ because you should not use it
        //  (this imply you shouldn't read it as well).
        
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
        //      Content is the NAME:VALUE format with space separators
        //
        //      We search for 3 possible case : 
        //          - Field name
        //          - Value (AKA payload or parsed content)
        //          - "Out of range" (EOF found)
        //
        //
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
            // : symbol mean we found the end of "NAME" 
            if ( uselessContent.substring(pos, pos+1).equals(":") ) {
              
              if ( isSearchingFieldname==false ) {
                  fieldValue = uselessContent.substring(lastDoubleDottedPos+1, lastSpacePos);
                  tmpFields[fieldPosition] = new LttngEventField( fieldName, fieldValue );
                  fieldPosition++;
                  isSearchingFieldname = true;
              }
              
              lastDoubleDottedPos = pos;
            }
            // space mean we found the end of VALUE
            else if ( uselessContent.substring(pos, pos+1).equals(" ") ) {
                
                if ( isSearchingFieldname==true ) {
                    fieldName = uselessContent.substring(lastFieldnamePos, lastDoubleDottedPos);
                    lastFieldnamePos = pos+1;
                    isSearchingFieldname = false;
                }
                
                lastSpacePos = pos;
            }
            // pos+2 > length mean EOF
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
