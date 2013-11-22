/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

/**
 * Simple Datatype definition is a datatype that allows the addition of
 * getIntegerValue and getStringValue to a class.
 *
 * @author Matthew Khouzam
 * @since 1.2
 */
public abstract class SimpleDatatypeDefinition extends Definition {

    /**
     * Create a new SimpleDatatypeDefinition
     *
     * @param definitionScope
     *            The scope of this definition
     * @param fieldName
     *            The name of the field matching this definition in the parent
     *            scope
     */
    public SimpleDatatypeDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        super(definitionScope, fieldName);
    }

    /**
     * Gets the value in integer form
     *
     * @return the integer in a Long, can be null
     */
    public Long getIntegerValue() {
        return null;
    }

    /**
     * Gets the value in string form
     *
     * @return the integer in a String, can be null
     */
    public String getStringValue() {
        return null;
    }

}
