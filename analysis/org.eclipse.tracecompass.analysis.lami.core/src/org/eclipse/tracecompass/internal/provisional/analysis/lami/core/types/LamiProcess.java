/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class defining a LAMI 'process' type.
 *
 * This is the representation of an operating system process.
 *
 * @author Alexandre Montplaisir
 */
public class LamiProcess extends LamiData {

    private final @Nullable String fName;
    private final @Nullable Long fPid;
    private final @Nullable Long fTid;

    private final String fString;

    /**
     * Constructor
     *
     * All parameters are optional, but realistically at least one should be
     * provided!
     *
     * @param name
     *            The process name
     * @param pid
     *            The process's PID
     * @param tid
     *            The process's TID
     */
    public LamiProcess(@Nullable String name, @Nullable Long pid, @Nullable Long tid) {
        fName = name;
        fPid = pid;
        fTid = tid;

        fString = generateString();
    }

    /**
     * Get this process's name, null if unavailable.
     *
     * @return The process name
     */
    public @Nullable String getName() {
        return fName;
    }

    /**
     * Get this process's PID, null if unavailable.
     *
     * @return The process PID
     */
    public @Nullable Long getPID() {
        return fPid;
    }

    /**
     * Get this process's TID, null if unavailable.
     *
     * @return The process TID
     */
    public @Nullable Long getTID() {
        return fTid;
    }

    private String generateString() {
        Long pid = fPid;
        Long tid = fTid;

        StringBuilder sb = new StringBuilder();
        if (fName != null) {
            sb.append(fName);
        }

        if (pid != null || tid != null) {
            sb.append(' ');
            StringJoiner sj = new StringJoiner(", ", "(", ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            if (pid != null) {
                sj.add("pid=" + pid.toString()); //$NON-NLS-1$
            }
            if (tid != null) {
                sj.add("tid=" + tid.toString()); //$NON-NLS-1$
            }
            sb.append(sj.toString());
        }

        return sb.toString();
    }

    @Override
    public @NonNull String toString() {
        return fString;
    }

}
