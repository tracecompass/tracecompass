/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import java.util.List;
import java.util.Objects;

import org.eclipse.tracecompass.internal.tmf.core.aspect.Messages;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;

/**
 * This aspect finds the call sites of the code that are running when the event
 * occurred.
 *
 * @author Matthew Khouzam
 * @since 5.0
 */
public abstract class TmfCallsiteAspect implements ITmfEventAspect<List<ITmfCallsite>> {

    @Override
    public final String getName() {
        return Objects.requireNonNull(Messages.TmfCallsiteAspect_name);
    }

    @Override
    public final String getHelpText() {
        return Objects.requireNonNull(Messages.TmfCallsiteAspect_description);
    }

}