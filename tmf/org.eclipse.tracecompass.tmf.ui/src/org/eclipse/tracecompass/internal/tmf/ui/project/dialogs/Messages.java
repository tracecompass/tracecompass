/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Francois Chouinard - Initial API and implementation
 *     Marc-Andre Laperle - Add select/deselect all
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.dialogs;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for dialog messages.
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ui.project.dialogs.messages"; //$NON-NLS-1$

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
