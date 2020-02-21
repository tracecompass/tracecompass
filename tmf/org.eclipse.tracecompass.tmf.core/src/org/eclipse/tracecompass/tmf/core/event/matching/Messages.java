/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.matching;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized strings for this plugin
 *
 * @author Geneviève Bastien
 * @since 1.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.core.event.matching.messages"; //$NON-NLS-1$

    public static String TmfEventMatching_LookingEventsFrom;
    public static String TmfEventMatching_MatchesFound;
    public static String TmfEventMatching_MatchingEvents;
    public static String TmfEventMatching_RequestingEventsFrom;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
