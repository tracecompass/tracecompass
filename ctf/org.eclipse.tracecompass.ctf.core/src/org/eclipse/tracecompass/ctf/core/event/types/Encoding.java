/*******************************************************************************
 * Copyright (c) 2011-2014 Ericsson, Ecole Polytechnique de Montreal and others
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

package org.eclipse.tracecompass.ctf.core.event.types;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * CTF encoding types
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
@NonNullByDefault
public enum Encoding {
    /** UTF-8 encoding */
    UTF8,
    /** Ascii encoding */
    ASCII,
    /** No encoding, maybe not even text */
    NONE
}
