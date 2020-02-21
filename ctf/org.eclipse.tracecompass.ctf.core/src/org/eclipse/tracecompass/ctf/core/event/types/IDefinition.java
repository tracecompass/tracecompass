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
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.ctf.core.event.scope.ILexicalScope;

/**
 * Interface for data definitions. A definition is when a value is given to a
 * declaration
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public interface IDefinition {

    /**
     * Get the complete path of this field.
     *
     * @return The path
     * @since 1.0
     */
    ILexicalScope getScopePath();

    /**
     * Get the declaration of this definition
     *
     * @return the declaration of a datatype
     */
    IDeclaration getDeclaration();

    /**
     * Get the size in bits
     *
     * @return the size in bits. Long#MAX_VALUE means unset.
     * @since 3.0
     */
    default long size() {
        return Long.MAX_VALUE;
    }

}