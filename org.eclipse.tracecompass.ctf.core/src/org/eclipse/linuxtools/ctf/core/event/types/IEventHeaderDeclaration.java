/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event header declaration abstract class
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
@NonNullByDefault
public interface IEventHeaderDeclaration extends IDeclaration {
    /**
     * The id of an event
     */
    String ID = "id"; //$NON-NLS-1$
    /**
     * The name of a timestamp field
     */
    String TIMESTAMP = "timestamp"; //$NON-NLS-1$
    /**
     * Extended header
     */
    String EXTENDED = "extended"; //$NON-NLS-1$
    /**
     * Compact header (not to be confused with compact vs large)
     */
    String COMPACT = "compact"; //$NON-NLS-1$
}
