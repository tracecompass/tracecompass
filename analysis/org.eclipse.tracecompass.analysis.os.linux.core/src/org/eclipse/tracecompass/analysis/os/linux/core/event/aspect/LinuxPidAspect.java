/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.event.aspect;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

/**
 * This aspect finds the ID of the process that is running when the event
 * occurred.
 *
 * @author Geneviève Bastien
 * @since 2.5
 */
public abstract class LinuxPidAspect implements ITmfEventAspect<Integer> {

    @Override
    public final String getName() {
        return Messages.getMessage(Messages.AspectName_Pid);
    }

    @Override
    public final String getHelpText() {
        return Messages.getMessage(Messages.AspectHelpText_Pid);
    }

    @Override
    public abstract @Nullable Integer resolve(ITmfEvent event);

}
