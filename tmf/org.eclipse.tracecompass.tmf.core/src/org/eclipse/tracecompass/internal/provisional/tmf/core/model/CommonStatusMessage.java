/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.Messages;

/**
 * Data providers will return a response and it may contains a detailed message
 * if necessary. This class regroups common status messages that data providers
 * can send.
 *
 * Since we don't want to expose Messages class, CommonStatusMessage is a
 * wrapper. And it can be used by any data providers from any package/plugin.
 *
 * @author Yonni Chen
 */
public final class CommonStatusMessage {

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse.Status#RUNNING}
     * status
     */
    public static final @Nullable String RUNNING = Messages.CommonStatusMessage_Running;

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse.Status#COMPLETED}
     * status
     */
    public static final @Nullable String COMPLETED = Messages.CommonStatusMessage_Completed;

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse.Status#CANCELLED}
     * status
     */
    public static final @Nullable String PROGRESS_MONITOR_CANCELLED = Messages.CommonStatusMessage_ProgressMonitorCancelled;

    /**
     * Constructor
     */
    private CommonStatusMessage() {

    }
}
