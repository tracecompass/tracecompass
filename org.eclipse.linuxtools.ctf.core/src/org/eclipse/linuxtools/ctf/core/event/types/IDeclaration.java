/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

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
     * @throws CTFReaderException
     *             error in reading
     * @since 3.0
     */
    Definition createDefinition(IDefinitionScope definitionScope, @NonNull String fieldName, @NonNull BitBuffer input) throws CTFReaderException;

    /**
     * Get the path of a definition
     *
     * @param definitionScope
     *            the scope of the definition
     * @param fieldName
     *            the name of the definition
     * @return the path of the definition
     * @since 3.0
     */
    public LexicalScope getPath(IDefinitionScope definitionScope, @NonNull String fieldName);

    /**
     * The minimum alignment. if the field is 32 bits, the definition will pad
     * all the data up to (position%32==0)
     *
     * @return the alignment in bits
     */
    long getAlignment();

    /**
     * The MAXIMUM size of this declaration
     *
     * @return the maximum size
     * @since 3.0
     */
    int getMaximumSize();

}
