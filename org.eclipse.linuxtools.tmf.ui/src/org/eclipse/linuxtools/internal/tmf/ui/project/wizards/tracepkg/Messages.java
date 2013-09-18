/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg;

import org.eclipse.osgi.util.NLS;

/**
 * Messages common to trace package operations
 *
 * @author Marc-Andre Laperle
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.messages"; //$NON-NLS-1$

    /**
     * Text for supplementary files in the element viewer
     */
    public static String TracePackage_SupplementaryFiles;

    /**
     * Text for trace in the element viewer
     */
    public static String TracePackage_TraceElement;

    /**
     * Text for bookmarks in the element viewer
     */
    public static String TracePackage_Bookmarks;

    /**
     * Text for browse button in the wizard pages
     */
    public static String TracePackage_Browse;

    /**
     * Title for the file dialog
     */
    public static String TracePackage_FileDialogTitle;

    /**
     * Text for browse select all button in the wizard pages
     */
    public static String TracePackage_SelectAll;

    /**
     * Text for browse deselect all button in the wizard pages
     */
    public static String TracePackage_DeselectAll;

    /**
     * Generic error message for wizard operations
     */
    public static String TracePackage_ErrorOperation;

    /**
     * Generic error when multiple problems occur (MultiStatus)
     */
    public static String TracePackage_ErrorMultipleProblems;

    /**
     * Generic dialog message for error in wizard operations
     */
    public static String TracePackage_InternalErrorTitle;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
