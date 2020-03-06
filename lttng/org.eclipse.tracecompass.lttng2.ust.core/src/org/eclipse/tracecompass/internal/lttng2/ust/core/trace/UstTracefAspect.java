/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.ust.core.trace;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * Message resolving aspect for <a
 * href=http://man7.org/linux/man-pages/man3/tracef.3.html>tracef</a>.
 *
 * @author Matthew Khouzam
 *
 */
public class UstTracefAspect implements ITmfEventAspect<String> {

    /**
     * Instance
     */
    private static ITmfEventAspect<String> INSTANCE = null;

    /**
     * Get the instance of the aspect
     *
     * @return the instance of the aspect
     */
    public static synchronized ITmfEventAspect<String> getInstance() {
        ITmfEventAspect<String> instance = INSTANCE;
        if (instance == null) {
            instance = new UstTracefAspect();
            INSTANCE = instance;
        }
        return instance;
    }

    private UstTracefAspect() {
        // Do nothing
    }

    @Override
    public @NonNull String getName() {
        return Objects.requireNonNull(Messages.UstTracefAspect_Name);
    }

    @Override
    public @NonNull String getHelpText() {
        return Objects.requireNonNull(Messages.UstTracefAspect_Description);
    }

    @Override
    public @Nullable String resolve(@NonNull ITmfEvent event) {
        return event.getContent().getFieldValue(String.class, "msg"); //$NON-NLS-1$
    }

}
