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

import org.eclipse.linuxtools.tmf.event.*;

/**
 * <b><u>LttngEventField</u></b>
 * <p>
 * Lttng specific implementation of the TmfEventField
 * <p>
 * Lttng LttngEventField add a "Name" attribute to the Tmf implementation
 * This mean the fields will have a name and a value.
 */
public class LttngEventField extends TmfEventField 
{
        private String fieldName = "";
        
        /**
         * Constructor with parameters<br>
         * 
         * @param newContent    The parsedContent we want to populate the field with. 
         * 
         * @see org.eclipse.linuxtools.lttng.jni.ParsedContent   
         */
        public LttngEventField(String name, Object newContent) {
                super( newContent );
        
                fieldName = name;
        }
        
        /**
         * getter for the name attribute.
         */
        public String getName() {
                return fieldName;
        }
        
        /**
         * overrided toString() method.<br>
         * <br>
         * Print both field name and value.
         */
        @Override
        public String toString() {
                return fieldName + " : " + getValue().toString();
        }
}

