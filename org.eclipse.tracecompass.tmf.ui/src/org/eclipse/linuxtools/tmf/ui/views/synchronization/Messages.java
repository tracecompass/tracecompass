/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.synchronization;

import org.eclipse.osgi.util.NLS;

/**
 * Message file for the synchronization view
 * @since 3.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.synchronization.messages"; //$NON-NLS-1$
    public static String TmfSynchronizationView_NameColumn;
    public static String TmfSynchronizationView_ValueColumn;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
