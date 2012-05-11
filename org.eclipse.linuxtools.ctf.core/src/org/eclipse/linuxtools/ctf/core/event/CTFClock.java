/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event;

import java.util.HashMap;

/**
 */
public class CTFClock {

    /**
     * Field properties.
     */
    final private HashMap<String, Object> properties = new HashMap<String, Object>();
    /**
     * Field name.
     */
    private String name;

    /**
     * Method addAttribute.
     * @param key String
     * @param value Object
     */
    public void addAttribute(String key, Object value) {
        this.properties.put(key, value);
        if (key.equals("name")) { //$NON-NLS-1$
            this.name = (String) value;
        }
    }

    /**
     * Method getName.
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Method getProperty.
     * @param key String
     * @return Object
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

}
