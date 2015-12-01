/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.types;

import java.nio.ByteOrder;

/**
 * Common interface for simple CTF data types (which do not contain sub-fields).
 *
 * @author Matthew Khouzam
 */
public interface ISimpleDatatypeDeclaration {

    /**
     * Is the byte order set
     *
     * @return If the byte order was set
     * @since 2.0
     */
    public boolean isByteOrderSet();

    /**
     * Get the byte order
     *
     * @return the byte order, or @link {@link ByteOrder#nativeOrder()} if not
     *         set
     * @since 2.0
     */
    public ByteOrder getByteOrder();

}
