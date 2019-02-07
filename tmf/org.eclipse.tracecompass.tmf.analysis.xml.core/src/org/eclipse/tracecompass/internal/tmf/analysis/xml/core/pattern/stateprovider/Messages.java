/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized messages for the XML analysis state provider package
 *
 * @author Jean-Christian Kouame
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.messages"; //$NON-NLS-1$
    /** State system property prefix */
    public static String PatternAnalysis_StateSystemPrefix;
    /** Segment store property prefix */
    public static String PatternAnalysis_SegmentStorePrefix;
    /**
     * The string content
     */
    public static String PatternSegmentContentAspect_Content;
    /**
     * Help text of the segment content aspect
     */
    public static String PatternSegmentContentAspect_HelpText;
    /**
     * Help text of the segment name aspect
     */
    public static String PatternSegmentNameAspect_HelpText;
    /**
     * The string name
     */
    public static String PatternSegmentNameAspect_Name;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
