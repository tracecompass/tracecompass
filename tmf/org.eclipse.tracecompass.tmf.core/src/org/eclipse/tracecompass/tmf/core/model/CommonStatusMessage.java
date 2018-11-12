/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.core.model;

import java.util.Objects;

/**
 * Data providers will return a response and it may contains a detailed message
 * if necessary. This class regroup common status messages that data providers
 * can send.
 *
 * Since we don't want to expose Messages class, CommonStatusMessage is a
 * wrapper. And it can be used by any data providers from any package/plugin.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public final class CommonStatusMessage {

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status#RUNNING}
     * status
     */
    public static final String RUNNING = Objects.requireNonNull(Messages.CommonStatusMessage_Running);

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status#COMPLETED}
     * status
     */
    public static final String COMPLETED = Objects.requireNonNull(Messages.CommonStatusMessage_Completed);

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status#CANCELLED}
     * status
     */
    public static final String TASK_CANCELLED = Objects.requireNonNull(Messages.CommonStatusMessage_TaskCancelled);

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status#FAILED}
     * status
     */
    public static final String ANALYSIS_INITIALIZATION_FAILED = Objects.requireNonNull(Messages.CommonStatusMessage_AnalysisInitializationFailed);

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status#FAILED}
     * status
     */
    public static final String STATE_SYSTEM_FAILED = Objects.requireNonNull(Messages.CommonStatusMessage_StateSystemFailed);

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status#FAILED}
     * status
     */
    public static final String INCORRECT_QUERY_INTERVAL = Objects.requireNonNull(Messages.CommonStatusMessage_IncorrectQueryInterval);

    /**
     * A possible detailed message for a
     * {@link org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status#FAILED}
     * status
     * @since 4.3
     */
    public static final String INCORRECT_QUERY_PARAMETERS = Objects.requireNonNull(Messages.CommonStatusMessage_IncorrectQueryParameters);

    /**
     * Constructor
     */
    private CommonStatusMessage() {

    }
}
