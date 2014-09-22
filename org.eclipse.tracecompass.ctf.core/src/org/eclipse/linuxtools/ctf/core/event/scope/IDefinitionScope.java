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

package org.eclipse.linuxtools.ctf.core.event.scope;

import org.eclipse.linuxtools.ctf.core.event.types.Definition;

/**
 * The scope of a CTF definition. Used for compound types.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 * @since 3.0
 */
public interface IDefinitionScope {

    /**
     * Gets the path in a C style for the scope.
     *
     * @return the path
     * @since 3.0
     */
    LexicalScope getScopePath();

    /**
     * Looks up in this definition scope.
     *
     * @param lookupPath
     *            The path to look up
     * @return The Definition that was read
     */
    // TODO: change to IDefintion
    Definition lookupDefinition(String lookupPath);
}
