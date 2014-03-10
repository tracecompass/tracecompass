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

package org.eclipse.linuxtools.tmf.analysis.xml.ui.views.timegraph;

import org.eclipse.osgi.util.NLS;

/**
 * Message for the XML state system view
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.analysis.xml.ui.views.timegraph.messages"; //$NON-NLS-1$

    public static String XmlPresentationProvider_MultipleStates;

    /* Default text messages */
    public static String XmlTimeGraphView_ColumnId;
    public static String XmlTimeGraphView_ColumnName;
    public static String XmlTimeGraphView_ColumnParentId;
    public static String XmlTimeGraphView_DefaultTitle;

    /* Text and tooltips of the view */
    public static String XmlTimeGraphView_NextText;
    public static String XmlTimeGraphView_NextTooltip;
    public static String XmlTimeGraphView_PreviousInterval;
    public static String XmlTimeGraphView_PreviousText;

    /* Errors and warnings messages */
    public static String XmlTimeGraphView_UselessEndPath;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
