/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions;

import org.eclipse.osgi.util.NLS;

/**
 * Action messages
 *
 * @author Matthew Khouzam
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions.messages"; //$NON-NLS-1$
    /**
     * Follow message
     */
    public static String FollowThreadAction_follow;
    /**
     * Unfollow message
     */
    public static String FollowThreadAction_unfollow;
    /**
     * Follow CPU message
     */
    public static String CpuSelectionAction_followCpu;
    /**
     * Stop following CPU
     */
    public static String CpuSelectionAction_unfollowCpu;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
