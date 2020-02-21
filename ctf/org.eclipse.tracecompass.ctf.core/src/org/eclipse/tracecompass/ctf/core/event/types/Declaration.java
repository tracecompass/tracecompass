/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;
import org.eclipse.tracecompass.ctf.core.event.scope.LexicalScope;

/**
 * Declaration base, it helps for basic functionality that is often called, so
 * performance is often a high priority in this class
 *
 * @author Matthew Khouzam
 */
public abstract class Declaration implements IDeclaration {

    /**
     * @since 1.0
     */
    @Override
    public ILexicalScope getPath(IDefinitionScope definitionScope, @NonNull String fieldName) {
        if (definitionScope != null) {
            final ILexicalScope parentPath = definitionScope.getScopePath();
            if (parentPath != null) {
                ILexicalScope myScope = parentPath.getChild(fieldName);
                if (myScope == null) {
                    myScope = new LexicalScope(parentPath, fieldName);
                }
                return myScope;
            }
        }
        ILexicalScope child = ILexicalScope.ROOT.getChild(fieldName);
        if (child != null) {
            return child;
        }
        return new LexicalScope(ILexicalScope.ROOT, fieldName);
    }

    /**
     * Offset the buffer position wrt the current alignment.
     *
     * @param input
     *            The bitbuffer that is being read
     * @throws CTFException
     *             Happens when there is an out of bounds exception
     */
    protected final void alignRead(BitBuffer input) throws CTFException {
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
