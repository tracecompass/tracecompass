/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.views.xychart;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings for the xml xy chart view package
 *
 * @author Geneviève Bastien
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.views.xychart.messages"; //$NON-NLS-1$
    /** Default view title */
    public static @Nullable String XmlXYView_DefaultTitle;

    /** Default Viewer title */
    public static @Nullable String XmlXYViewer_DefaultViewerTitle;
    /** Default X axix text */
    public static @Nullable String XmlXYViewer_DefaultXAxis;
    /** Default Y axis text */
    public static @Nullable String XmlXYViewer_DefaultYAxis;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
