/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;

/**
 * Null aspect for LAMI tables, normally printing nothing in the corresponding
 * cells. Should be access using {@link #INSTANCE}.
 *
 * @author Alexandre Montplaisir
 */
public final class LamiEmptyAspect extends LamiTableEntryAspect {

    /** Singleton instance */
    public static final LamiEmptyAspect INSTANCE = new LamiEmptyAspect();

    private LamiEmptyAspect() {
        super("", null); //$NON-NLS-1$
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        return null;
    }

    @Override
    public boolean isTimeStamp() {
        return false;
    }

    @Override
    public @Nullable Number resolveNumber(@NonNull LamiTableEntry entry) {
        return null;
    }

    @Override
    public Comparator<LamiTableEntry> getComparator() {
        return (o1, o2) -> 0;
    }

}
