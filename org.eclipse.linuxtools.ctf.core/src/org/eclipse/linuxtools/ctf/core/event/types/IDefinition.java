/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;

/**
 * Interface for data definitions. A definition is when a value is given to a
 * declaration
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public interface IDefinition {

    /**
     * Get the complete path of this field.
     *
     * @return The path
     */
    LexicalScope getScopePath();

    /**
     * Get the declaration of this definition
     *
     * @return the declaration of a datatype
     */
    IDeclaration getDeclaration();

}