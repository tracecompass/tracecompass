/*******************************************************************************
 * Copyright (c) 2013, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *   Xavier Raynaud - Support filters tracking
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

import com.google.common.collect.ImmutableMap;

/**
 * Context of a trace, which is the representation of the "view" the user
 * currently has on this trace (window time range, selected time or time range).
 *
 * TODO could be extended to support the notion of current location too.
 *
 * FIXME Is this really the right place for the Editor File ?
 *
 * @author Alexandre Montplaisir
 * @since 1.0
 */
@NonNullByDefault
public class TmfTraceContext implements ITraceContextSignalHandler {

    static final TmfTraceContext NULL_CONTEXT = new TmfTraceContext(new TmfTimeRange(TmfTimestamp.BIG_CRUNCH, TmfTimestamp.BIG_CRUNCH),
            TmfTimeRange.NULL_RANGE, null, null);

    private final TmfTimeRange fSelection;
    private final TmfTimeRange fWindowRange;
    /**
     * This field indicates whether the trace is time synchronized in selection and
     * window range with other traces
     */
    private final boolean fIsSynchronized;
    private final int fInstanceNumber;
    private final @Nullable IFile fEditorFile;
    private final @Nullable ITmfFilter fFilter;
    private final Map<@NonNull String, @NonNull Object> fData;

    /**
     * Build a new trace context.
     *
     * @param selection
     *            The selected time range
     * @param windowRange
     *            The visible window's time range
     * @param editorFile
     *            The file representing the selected editor
     * @param filter
     *            The currently applied filter. 'null' for none.
     */
    public TmfTraceContext(TmfTimeRange selection, TmfTimeRange windowRange,
            @Nullable IFile editorFile, @Nullable ITmfFilter filter) {
        fSelection = selection;
        fWindowRange = windowRange;
        fIsSynchronized = false;
        fInstanceNumber = 1;
        fEditorFile = editorFile;
        fFilter = filter;
        fData = new HashMap<>();
    }

    /**
     * Constructs a new trace context with data taken from a builder.
     *
     * @param builder
     *            the builder
     * @since 2.3
     */
    public TmfTraceContext(Builder builder) {
        fSelection = builder.selection;
        fWindowRange = builder.windowRange;
        fIsSynchronized = builder.isSynchronized;
        fInstanceNumber = builder.instanceNumber;
        fEditorFile = builder.editorFile;
        fFilter = builder.filter;
        fData = new HashMap<>(builder.data);
    }

    /**
     * Return the time range representing the current active selection.
     *
     * @return The selected time range
     */
    public TmfTimeRange getSelectionRange() {
        return fSelection;
    }

    /**
     * Return the current window time range.
     *
     * @return The current window time range
     */
    public TmfTimeRange getWindowRange() {
        return fWindowRange;
    }

    /**
     * Gets whether the trace is time synchronized in selection and window range
     * with other traces.
     *
     * @return true if the trace should be time synchronized, false otherwise
     * @since 3.2
     */
    public boolean isSynchronized() {
        return fIsSynchronized;
    }

    /**
     * Return the instance number of this trace.
     *
     * @return The instance number
     * @since 3.2
     */
    public int getInstanceNumber() {
        return fInstanceNumber;
    }

    /**
     * Get the editor's file
     *
     * @return The editor file
     */
    public @Nullable IFile getEditorFile() {
        return fEditorFile;
    }

    /**
     * Gets the filter applied to the current trace
     *
     * @return The current filter, or <code>null</code> if there is none
     */
    public @Nullable ITmfFilter getFilter() {
        return fFilter;
    }

    /**
     * Get the data for the specific key
     *
     * @param key
     *            The id of the data
     * @return The data or null if the key do not exist
     * @since 2.1
     */
    public synchronized @Nullable Object getData(String key) {
        return fData.get(key);
    }

    /**
     * Get a copy of the data map
     *
     * @return The data map copy
     * @since 2.1
     */
    public synchronized Map<String, Object> getData() {
        return ImmutableMap.copyOf(fData);
    }

    /**
     * Returns a new builder that is initialized with the data from this trace
     * context.
     *
     * @return the builder
     * @since 2.3
     */
    public Builder builder() {
        return new Builder(this);
    }

    /**
     * A builder for creating trace context instances.
     *
     * @since 2.3
     */
    public static class Builder {
        private TmfTimeRange selection;
        private TmfTimeRange windowRange;
        private boolean isSynchronized;
        private int instanceNumber;
        private @Nullable IFile editorFile;
        private @Nullable ITmfFilter filter;
        private Map<String, Object> data;

        /**
         * Constructor
         *
         * @param ctx
         *            the trace context used to initialize the builder
         */
        public Builder(TmfTraceContext ctx) {
            this.selection = ctx.fSelection;
            this.windowRange = ctx.fWindowRange;
            this.isSynchronized = ctx.fIsSynchronized;
            this.instanceNumber = ctx.fInstanceNumber;
            this.editorFile = ctx.fEditorFile;
            this.filter = ctx.fFilter;
            this.data = new HashMap<>(ctx.fData);
        }

        /**
         * Build the trace context.
         *
         * @return a trace context
         */
        public TmfTraceContext build() {
            return new TmfTraceContext(this);
        }

        /**
         * Sets the selected time range.
         *
         * @param selection
         *            the selected time range
         * @return this {@code Builder} object
         */
        public Builder setSelection(TmfTimeRange selection) {
            this.selection = selection;
            return this;
        }

        /**
         * Sets the window range.
         *
         * @param windowRange
         *            the window range
         * @return this {@code Builder} object
         */
        public Builder setWindowRange(TmfTimeRange windowRange) {
            this.windowRange = windowRange;
            return this;
        }

        /**
         * Sets whether the trace is time synchronized in selection and window range
         * with other traces.
         *
         * @param isSynchronized
         *            true if the trace should be time synchronized, false otherwise
         * @return this {@code Builder} object
         * @since 3.2
         */
        public Builder setSynchronized(boolean isSynchronized) {
            this.isSynchronized = isSynchronized;
            return this;
        }

        /**
         * Sets the instance number of this trace.
         *
         * @param instanceNumber
         *            the instance number
         * @return this {@code Builder} object
         */
        /* Package visibility only so that users can't change it */
        Builder setInstanceNumber(int instanceNumber) {
            this.instanceNumber = instanceNumber;
            return this;
        }

        /**
         * Sets the current filter.
         *
         * @param filter
         *            the current filter
         * @return this {@code Builder} object
         */
        public Builder setFilter(@Nullable ITmfFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Sets a data mapping.
         *
         * @param key
         *            The key of the data
         * @param value
         *            The value of the data
         * @return this {@code Builder} object
         */
        public Builder setData(String key, @Nullable Object value) {
            if (value == null) {
                data.remove(key);
                return this;
            }
            data.put(key, value);
            return this;
        }

        /**
         * Sets data mappings.
         *
         * @param data
         *            The map of data
         * @return this {@code Builder} object
         */
        public Builder setData(Map<String, Object> data) {
            this.data.putAll(data);
            return this;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[fSelection=" + fSelection + //$NON-NLS-1$
                ", fWindowRange=" + fWindowRange + ']'; //$NON-NLS-1$
    }

}
