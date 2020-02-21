/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;

/**
 * A CTF data type declaration.
 *
 * An example: <br>
 * int i = 0; <br>
 * <b>int</b> is the declaration.<br>
 * <b>i</b> is the definition.<br>
 * <b>0</b> is the value assigned to the definition, not the declaration.<br>
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public interface IDeclaration {

    /**
     * Create a definition from this declaration
     *
     * @param definitionScope
     *            the definition scope, the parent where the definition will be
     *            placed
     * @param fieldName
     *            the name of the definition
     * @param input
     *            a bitbuffer to read from
     * @return a reference to the definition
     * @throws CTFException
     *             error in reading
     */
    @NonNull Definition createDefinition(IDefinitionScope definitionScope, @NonNull String fieldName, @NonNull BitBuffer input) throws CTFException;

    /**
     * Get the path of a definition
     *
     * @param definitionScope
     *            the scope of the definition
     * @param fieldName
     *            the name of the definition
     * @return the path of the definition
     * @since 1.0
     */
    @NonNull ILexicalScope getPath(IDefinitionScope definitionScope, @NonNull String fieldName);

    /**
     * The minimum alignment. if the field is 32 bits, the definition will pad
     * all the data up to (position%32==0)
     *
     * @return the alignment in bits
     */
    long getAlignment();

    /**
     * The MAXIMUM size of this declaration (in bits).
     *
     * @return the maximum size
     */
    int getMaximumSize();

    @Override
    int hashCode();

    @Override
    boolean equals(Object other);

    /**
     * Are the two declarations equivalent on a binary level. eg: an 8 bit
     * little endian and big endian int.
     *
     * @param other
     *            the other {@link IDeclaration}
     * @return true if the binary CTF stream will generate the same value with
     *         the two streams
     */
    boolean isBinaryEquivalent(IDeclaration other);

}
