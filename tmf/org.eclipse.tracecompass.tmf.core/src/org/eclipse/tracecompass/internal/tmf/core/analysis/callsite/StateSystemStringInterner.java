/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.analysis.callsite;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * State System Interner
 *
 * @author Matthew Khouzam
 */
public class StateSystemStringInterner {

    private final Map<Integer, BiMap<String, Long>> fStringPools = new WeakHashMap<>();

    /**
     * Intern a string, get a unique ID, the reciprocal of
     * {@link #resolve(ITmfStateSystem, long, int)}
     *
     * @param ss
     *            state system
     * @param string
     *            the string to intern
     * @param quark
     *            the quark of the threadpool
     * @return the id of the interned String
     * @throws IndexOutOfBoundsException
     *             An error occured getting the quark
     * @throws TimeRangeException
     *             The interned value is out of bounds
     */
    public long intern(ITmfStateSystemBuilder ss, String string, int quark) throws IndexOutOfBoundsException, TimeRangeException {
        Map<String, Long> stringPool = fStringPools.computeIfAbsent(quark, unused -> HashBiMap.<String, Long> create());
        Long index = stringPool.get(string);
        if (index == null) {
            long internValue = stringPool.size() + ss.getStartTime();
            stringPool.put(string, internValue);
            ss.modifyAttribute(internValue, string, quark);
            return internValue;
        }
        return index;
    }

    /**
     * resolve an ID to a string, the reciprocal of
     * {@link #intern(ITmfStateSystemBuilder, String, int)}
     *
     * @param ss
     *            state system
     * @param value
     *            the id to resolve
     * @param quark
     *            the quark of the threadpool
     * @return the string corresponding to the id
     * @throws IndexOutOfBoundsException
     *             An error occured getting the quark
     * @throws TimeRangeException
     *             The interned value is out of bounds
     * @throws StateSystemDisposedException
     *             state system is disposed.
     */
    public @Nullable String resolve(ITmfStateSystem ss, long value, int quark) throws StateSystemDisposedException {
        if (quark != ITmfStateSystem.INVALID_ATTRIBUTE) {
            Map<Long, String> stringPool = fStringPools.computeIfAbsent(quark, unused -> HashBiMap.<String, Long> create()).inverse();
            String string = stringPool.get(value);
            if (string != null) {
                return string;
            }
            Object resolved = ss.querySingleState(value, quark).getValue();
            if (resolved instanceof String) {
                return (String) resolved;
            }
        }
        return null;
    }
}
