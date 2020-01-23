/*******************************************************************************
 * Copyright (c) 2015, 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.base;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Packages external string files
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.graph.core.base.messages"; //$NON-NLS-1$

    /** Label for edge block device */
    public static @Nullable String TmfEdge_BlockDevice;
    /** Label for edge unknown blocked */
    public static @Nullable String TmfEdge_Blocked;
    /** Label for group blocked */
    public static @Nullable String TmfEdge_GroupBlocked;
    /** Label for group running */
    public static @Nullable String TmfEdge_GroupRunning;
    /** Label for edge interrupted */
    public static @Nullable String TmfEdge_Interrupted;
    /** Label for edge IPI */
    public static @Nullable String TmfEdge_IPI;
    /** Label for edge network */
    public static @Nullable String TmfEdge_Network;
    /** Label for edge preempted */
    public static @Nullable String TmfEdge_Preempted;
    /** Label for edge running */
    public static @Nullable String TmfEdge_Running;
    /** Label for edge timer */
    public static @Nullable String TmfEdge_Timer;
    /** Label for edge unknown */
    public static @Nullable String TmfEdge_Unknown;
    /** Label for edge user input */
    public static @Nullable String TmfEdge_UserInput;

    public static @Nullable String TmfGraph_FromNotInGraph;

    public static @Nullable String TmfVertex_ArgumentTimestampLower;

    public static @Nullable String TmfVertex_CannotLinkToSelf;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Do nothing
    }
}
