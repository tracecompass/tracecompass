/*******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.model.OsStrings;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Thread Status entry model.
 *
 * @author Simon Delisle
 */
public class ThreadEntryModel extends TimeGraphEntryModel {

    /**
     * {@link ThreadEntryModel} builder, we use this to be able to reassign
     * parentIds to be able to rebuild the PS tree even when inactive threads are
     * filtered.
     */
    public static final class Builder {
        private final long fId;
        private @NonNull List<@NonNull String> fLabels;
        private final long fStartTime;
        private long fEndTime;
        private final int fPid;
        private int fPpid;

        /**
         * Constructor
         *
         * @param id
         *            The unique ID for this Entry model for its trace
         * @param labels
         *            the thread labels
         * @param start
         *            the thread's start time
         * @param end
         *            the thread's end time
         * @param pid
         *            the thread's PID
         * @param ppid
         *            the thread's parent TID
         */
        public Builder(long id, @NonNull List<@NonNull String> labels, long start, long end, int pid, int ppid) {
            fId = id;
            fLabels = labels;
            fStartTime = start;
            fEndTime = end;
            fPid = pid;
            fPpid = ppid;
        }

        /**
         * Get the unique ID for this entry / builder
         *
         * @return this entry's unique ID
         */
        public long getId() {
            return fId;
        }

        /**
         * Get this entry / builder's start time
         *
         * @return the start time
         */
        public long getStartTime() {
            return fStartTime;
        }

        /**
         * Get this entry/builder's end time
         *
         * @return the end time
         */
        public long getEndTime() {
            return fEndTime;
        }

        /**
         * Get this entry/builder's parent PID
         *
         * @return the PPID
         */
        public int getPpid() {
            return fPpid;
        }

        /**
         * Update this entry / builder's name
         *
         * @param name
         *            the new name
         */
        public void setName(@NonNull List<@NonNull String> name) {
            fLabels = name;
        }

        /**
         * Update this entry / builder's end time
         *
         * @param endTime
         *            the new end time
         */
        public void setEndTime(long endTime) {
            fEndTime = Long.max(fEndTime, endTime);
        }

        /**
         * Update this entry / builder's PPID
         *
         * @param ppid
         *            the new PPID
         */
        public void setPpid(int ppid) {
            fPpid = ppid;
        }

        /**
         * Build the {@link ThreadEntryModel} from the builder, specify the parent id
         * here to avoid race conditions
         *
         * @param parentId
         *            parent ID to use when building this entry
         * @return the relevant {@link ThreadEntryModel} or throw a
         *         {@link NullPointerException} if the parent Id is not set.
         */
        public ThreadEntryModel build(long parentId) {
            return new ThreadEntryModel(fId, parentId, fLabels, fStartTime, fEndTime, fPid, fPpid);
        }
    }

    private final int fThreadId;
    private final int fParentThreadId;
    private final @NonNull Multimap<@NonNull String, @NonNull Object> fAspects;

    /**
     * Constructor
     *
     * @param id
     *            The unique ID for this Entry model for its trace
     * @param parentId
     *            this Entry model's ID
     * @param labels
     *            the thread labels
     * @param start
     *            the thread's start time
     * @param end
     *            the thread's end time
     * @param pid
     *            the thread's PID
     * @param ppid
     *            the thread's parent thread ID
     */
    public ThreadEntryModel(long id, long parentId, @NonNull List<@NonNull String> labels, long start, long end, int pid, int ppid) {
        super(id, parentId, labels, start, end);
        fThreadId = pid;
        fParentThreadId = ppid;
        fAspects = HashMultimap.create();
        fAspects.put(OsStrings.tid(), pid);
        fAspects.put(OsStrings.ptid(), ppid);
        if (!labels.isEmpty()) {
            fAspects.put(OsStrings.execName(), String.valueOf(labels.get(0)));
        }
    }

    /**
     * Gets the entry thread ID
     *
     * @return Thread ID
     */
    public int getThreadId() {
        return fThreadId;
    }

    /**
     * Gets the parent entry thread ID
     *
     * @return Parent thread ID
     */
    public int getParentThreadId() {
        return fParentThreadId;
    }

    @Override
    public @NonNull String toString() {
        return "<name=" + getLabels() + " id=" + getId() + " parentId=" + getParentId() //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + " start=" + getStartTime() + " end=" + getEndTime() //$NON-NLS-1$ //$NON-NLS-2$
                + " TID=" + fThreadId + " PTID=" + fParentThreadId + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public Multimap<@NonNull String, @NonNull Object> getMetadata() {
        return fAspects;
    }

    @Override
    public boolean hasRowModel() {
        // parent level entries do not have row models
        return getParentId() != -1L;
    }

}
