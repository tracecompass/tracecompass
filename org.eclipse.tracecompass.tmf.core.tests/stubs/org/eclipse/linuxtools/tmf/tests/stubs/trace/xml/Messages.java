/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace.xml;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized messages for the
 * {@link org.eclipse.linuxtools.tmf.tests.stubs.trace.xml} package
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.tests.stubs.trace.xml.messages"; //$NON-NLS-1$

    public static String TmfDevelopmentTrace_FileNotFound;
    public static String TmfDevelopmentTrace_IoError;
    public static String TmfDevelopmentTrace_ValidationError;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
