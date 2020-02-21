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
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event header declaration abstract class
 *
 * @author Matthew Khouzam
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
    /**
     * Name of the variant according to the spec
     */
    String VARIANT_NAME = "v"; //$NON-NLS-1$
}
