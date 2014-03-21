/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * Declaration base, it helps for basic functionality that is often called, so
 * performance is often a high priority in this class
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public abstract class Declaration implements IDeclaration {

    @Override
    public LexicalScope getPath(IDefinitionScope definitionScope, @NonNull String fieldName) {
        if (definitionScope != null) {
            final LexicalScope parentPath = definitionScope.getScopePath();
            if (parentPath != null) {
                LexicalScope myScope = parentPath.getChild(fieldName);
                if (myScope == null) {
                    myScope = new LexicalScope(parentPath, fieldName);
                }
                return myScope;
            }
        }
        LexicalScope child = LexicalScope.ROOT.getChild(fieldName);
        if (child != null) {
            return child;
        }
        return new LexicalScope(LexicalScope.ROOT, fieldName);
    }

    /**
     * Offset the buffer position wrt the current alignment.
     *
     * @param input
     *            The bitbuffer that is being read
     * @throws CTFReaderException
     *             Happens when there is an out of bounds exception
     * @since 3.0
     */
    protected final void alignRead(BitBuffer input) throws CTFReaderException {
        long mask = getAlignment() - 1;
        /*
         * The alignment is a power of 2
         */
        long pos = input.position();
        if ((pos & mask) == 0) {
            return;
        }
        pos = (pos + mask) & ~mask;
        input.position(pos);
    }
}
