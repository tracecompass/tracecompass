/*******************************************************************************
 * Copyright (c) 2013 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.core;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for tmf.core.ctfadaptor
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ctf.core.messages"; //$NON-NLS-1$

    /** Buffer overflow detected
     * @since 2.1*/
    public static String CtfTmfTrace_BufferOverflowErrorMessage;

    /** Major version number not set */
    public static String CtfTmfTrace_MajorNotSet;

    /** Reading error */
    public static String CtfTmfTrace_ReadingError;

    /** No event */
    public static String CtfTmfTrace_NoEvent;

    /** Unsupported field type */
    public static String CtfTmfEventField_UnsupportedType;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
