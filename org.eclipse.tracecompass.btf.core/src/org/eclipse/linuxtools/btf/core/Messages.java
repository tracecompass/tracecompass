/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.btf.core;

import org.eclipse.osgi.util.NLS;

/**
 * BTF messages, taken from the spec
 *
 * @author Matthew Khouzam
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.btf.core.messages"; //$NON-NLS-1$
    public static String BtfTypeId_CDescr;
    public static String BtfTypeId_CName;
    public static String BtfTypeId_ECUDescr;
    public static String BtfTypeId_ECUName;
    public static String BtfTypeId_IBDescr;
    public static String BtfTypeId_IBName;
    public static String BtfTypeId_ISRDescr;
    public static String BtfTypeId_ISRName;
    public static String BtfTypeId_PDescr;
    public static String BtfTypeId_PName;
    public static String BtfTypeId_RDescr;
    public static String BtfTypeId_RName;
    public static String BtfTypeId_SCHEDDescr;
    public static String BtfTypeId_SCHEDName;
    public static String BtfTypeId_SEMDescr;
    public static String BtfTypeId_SEMName;
    public static String BtfTypeId_SIGDescr;
    public static String BtfTypeId_SIGName;
    public static String BtfTypeId_SIMDescr;
    public static String BtfTypeId_SIMName;
    public static String BtfTypeId_STIDescr;
    public static String BtfTypeId_STIName;
    public static String BtfTypeId_TDescr;
    public static String BtfTypeId_TName;
    public static String BTFPayload_Activate;
    public static String BTFPayload_BoundedMigration;
    public static String BTFPayload_EnforcedMigration;
    public static String BTFPayload_FullMigration;
    public static String BTFPayload_MapLimitExceeded;
    public static String BTFPayload_Park;
    public static String BTFPayload_PhaseMigration;
    public static String BTFPayload_Poll;
    public static String BTFPayload_PollParking;
    public static String BTFPayload_Preempt;
    public static String BTFPayload_Release;
    public static String BTFPayload_ReleaseParking;
    public static String BTFPayload_Resume;
    public static String BTFPayload_Run;
    public static String BTFPayload_Start;
    public static String BTFPayload_Terminate;
    public static String BTFPayload_Wait;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
