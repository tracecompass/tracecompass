/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Francois Chouinard - Initial API and implementation
 *     Marc-Andre Laperle - Add select/deselect all
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.dialogs;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for dialog messages.
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.messages"; //$NON-NLS-1$

    public static String SelectSpplementaryResources_DialogTitle;
    public static String SelectSpplementaryResources_ResourcesGroupTitle;
    public static String Dialog_SelectAll;
    public static String Dialog_DeselectAll;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
