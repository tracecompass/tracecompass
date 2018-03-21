/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;

/**
 * Simple Datatype definition is a datatype that allows the addition of
 * getIntegerValue and getStringValue to a class.
 *
 * @author Matthew Khouzam
 */
public abstract class SimpleDatatypeDefinition extends Definition {

    /**
     * Create a new SimpleDatatypeDefinition
     *
     * @param declaration
     *            definition's declaration
     * @param definitionScope
     *            The scope of this definition
     * @param fieldName
     *            The name of the field matching this definition in the parent scope
     */
    public SimpleDatatypeDefinition(@NonNull IDeclaration declaration, IDefinitionScope definitionScope,
            @NonNull String fieldName) {
        super(declaration, definitionScope, fieldName);
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

    /**
     * Gets the bytes that make this definition
     *
     * @return the byte array
     * @since 2.4
     */
    public byte[] getBytes() {
        throw new UnsupportedOperationException("getBytes not implemented for " + this.getClass().getCanonicalName()); //$NON-NLS-1$
    }
}
