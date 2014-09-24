/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import org.eclipse.osgi.util.NLS;

/**
 * Messages class
 *
 * @since 3.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.core.synchronization.messages"; //$NON-NLS-1$
    public static String SyncAlgorithmFullyIncremental_absent;
    public static String SyncAlgorithmFullyIncremental_alpha;
    public static String SyncAlgorithmFullyIncremental_beta;
    public static String SyncAlgorithmFullyIncremental_T_;
    public static String SyncAlgorithmFullyIncremental_otherformula;
    public static String SyncAlgorithmFullyIncremental_mult;
    public static String SyncAlgorithmFullyIncremental_add;
    public static String SyncAlgorithmFullyIncremental_ub;
    public static String SyncAlgorithmFullyIncremental_lb;
    public static String SyncAlgorithmFullyIncremental_accuracy;
    public static String SyncAlgorithmFullyIncremental_accurate;
    public static String SyncAlgorithmFullyIncremental_approx;
    public static String SyncAlgorithmFullyIncremental_fail;
    public static String SyncAlgorithmFullyIncremental_incomplete;
    public static String SyncAlgorithmFullyIncremental_nbmatch;
    public static String SyncAlgorithmFullyIncremental_nbacc;
    public static String SyncAlgorithmFullyIncremental_refhost;
    public static String SyncAlgorithmFullyIncremental_otherhost;
    public static String SyncAlgorithmFullyIncremental_refformula;
    public static String SyncAlgorithmFullyIncremental_NA;
    public static String SyncAlgorithmFullyIncremental_quality;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
