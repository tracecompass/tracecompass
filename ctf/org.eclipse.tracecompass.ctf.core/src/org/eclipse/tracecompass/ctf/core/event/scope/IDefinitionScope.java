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

package org.eclipse.tracecompass.ctf.core.event.scope;

import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;

/**
 * The scope of a CTF definition. Used for compound types.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public interface IDefinitionScope {

    /**
     * Gets the path in a C style for the scope.
     *
     * @return the path
     * @since 1.0
     */
    ILexicalScope getScopePath();

    /**
     * Looks up in this definition scope.
     *
     * @param lookupPath
     *            The path to look up
     * @return The Definition that was read
     * @since 1.0
     */
    IDefinition lookupDefinition(String lookupPath);
}
