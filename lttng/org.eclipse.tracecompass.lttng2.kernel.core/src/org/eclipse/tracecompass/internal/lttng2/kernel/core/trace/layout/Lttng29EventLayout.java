/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventField;

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.9 and above.
 *
 * @author Geneviève Bastien
 */
public class Lttng29EventLayout extends Lttng28EventLayout {

    private static final String[] TCP_SEQ_FIELD = { "network_header", CtfTmfEventField.FIELD_VARIANT_SELECTED, "transport_header", "tcp", "seq" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private static final String[] TCP_ACK_FIELD = { "network_header", CtfTmfEventField.FIELD_VARIANT_SELECTED, "transport_header", "tcp", "ack_seq" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    private static final String[] TCP_FLAGS_FIELD = { "network_header", CtfTmfEventField.FIELD_VARIANT_SELECTED, "transport_header", "tcp", "flags" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    /**
     * Constructor
     */
    protected Lttng29EventLayout() {
    }

    private static final Lttng29EventLayout INSTANCE = new Lttng29EventLayout();

    public static Lttng29EventLayout getInstance() {
        return INSTANCE;
    }

    @Override
    public String @NonNull [] fieldPathTcpSeq() {
        return TCP_SEQ_FIELD;
    }

    @Override
    public String @NonNull [] fieldPathTcpAckSeq() {
        return TCP_ACK_FIELD;
    }

    @Override
    public String @NonNull [] fieldPathTcpFlags() {
        return TCP_FLAGS_FIELD;
    }

}
