/*******************************************************************************
 * Copyright (c) 2017, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.callstack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.base.IProfilingElement;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.InstrumentedProfilingElement;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Contains interfaces and classes to resolve the host ID in instrumented
 * callstack analyses.
 *
 * @author Geneviève Bastien
 * @since 1.1
 */
public final class CallStackHostUtils {

    private CallStackHostUtils() {
        // Nothing to do
    }

    /**
     * Interface for classes that provide a host ID at time t for a callstack. The
     * host ID is used to identify the machine on which the callstack was taken and
     * can be correlated with the model of a system to obtain additional data.
     */
    public interface IHostIdProvider extends Function<Long, @NonNull String> {

    }

    /**
     * The host ID is always the same, so return that string
     */
    private static class TraceHostIdProvider implements IHostIdProvider {

        private final String fHostId;

        public TraceHostIdProvider(String hostId) {
            fHostId = hostId;
        }

        @Override
        public @NonNull String apply(Long time) {
            return fHostId;
        }

    }

    /**
     * This class uses the value of an attribute as a host ID.
     */
    private static final class AttributeValueHostProvider implements IHostIdProvider {

        private final ITmfStateSystem fSs;
        private final int fQuark;
        private @Nullable ITmfStateInterval fInterval;
        private String fLastHostId = StringUtils.EMPTY;

        public AttributeValueHostProvider(ITmfStateSystem ss, int quark) {
            fSs = ss;
            fQuark = quark;
        }

        @Override
        public @NonNull String apply(Long time) {
            ITmfStateInterval interval = fInterval;
            String hostId = fLastHostId;
            if (interval != null && interval.intersects(time)) {
                return hostId;
            }
            try {
                interval = fSs.querySingleState(time, fQuark);
                hostId = String.valueOf(interval.getValue());
            } catch (StateSystemDisposedException e) {
                interval = null;
                hostId = StringUtils.EMPTY;
            }
            fInterval = interval;
            fLastHostId = hostId;
            return hostId;
        }

    }

    /**
     * This class uses the value of an attribute as a host ID.
     */
    private static final class AttributeNameHostProvider implements IHostIdProvider {

        private final String fHostId;

        public AttributeNameHostProvider(ITmfStateSystem ss, int quark) {
            String host = StringUtils.EMPTY;
            try {
                host = ss.getAttributeName(quark);
            } catch (IndexOutOfBoundsException e) {

            }
            fHostId = host;
        }

        @Override
        public @NonNull String apply(Long time) {
            return fHostId;
        }

    }

    /**
     * Interface for describing how a callstack will get the host ID, it will return
     * the host ID provider for a callstack element
     */
    public interface IHostIdResolver extends Function<IProfilingElement, @NonNull IHostIdProvider> {

    }

    /**
     * A host ID resolver that gets the host ID from the trace
     */
    public static final class TraceHostIdResolver implements IHostIdResolver {

        private final String fHostId;

        /**
         * @param trace
         *            The trace to use to provide the host ID
         */
        public TraceHostIdResolver(@Nullable ITmfTrace trace) {
            fHostId = trace == null ? StringUtils.EMPTY : trace.getHostId();
        }

        @Override
        public @NonNull IHostIdProvider apply(IProfilingElement element) {
            return new TraceHostIdProvider(fHostId);
        }

    }

    /**
     * This class will resolve the host ID provider by the value of a attribute at
     * a given depth
     */
    public static final class AttributeValueHostResolver implements IHostIdResolver {

        private int fLevel;

        /**
         * Constructor
         *
         * @param level
         *            The depth of the element whose value will be used to retrieve the
         *            host ID
         */
        public AttributeValueHostResolver(int level) {
            fLevel = level;
        }

        @Override
        public @NonNull IHostIdProvider apply(IProfilingElement element) {
            if (!(element instanceof InstrumentedProfilingElement)) {
                throw new IllegalArgumentException();
            }
            InstrumentedProfilingElement insElement = (InstrumentedProfilingElement) element;

            List<InstrumentedProfilingElement> elements = new ArrayList<>();
            InstrumentedProfilingElement el = insElement;
            while (el != null) {
                elements.add(el);
                el = el.getParentElement();
            }
            Collections.reverse(elements);
            if (elements.size() <= fLevel) {
                throw new NullPointerException("The host should never resolve to null, level " + fLevel + " is not available"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            InstrumentedProfilingElement stackElement = elements.get(fLevel);
            return new AttributeValueHostProvider(stackElement.getStateSystem(), stackElement.getQuark());
        }

    }

    /**
     * This class will resolve the thread ID provider by the value of a attribute at
     * a given depth
     */
    public static final class AttributeNameHostResolver implements IHostIdResolver {

        private int fLevel;

        /**
         * Constructor
         *
         * @param level
         *            The depth of the element whose value will be used to retrieve the
         *            thread ID
         */
        public AttributeNameHostResolver(int level) {
            fLevel = level;
        }

        @Override
        public @NonNull IHostIdProvider apply(IProfilingElement element) {
            if (!(element instanceof InstrumentedProfilingElement)) {
                throw new IllegalArgumentException();
            }
            InstrumentedProfilingElement insElement = (InstrumentedProfilingElement) element;

            List<InstrumentedProfilingElement> elements = new ArrayList<>();
            InstrumentedProfilingElement el = insElement;
            while (el != null) {
                elements.add(el);
                el = el.getParentElement();
            }
            Collections.reverse(elements);
            if (elements.size() <= fLevel) {
                throw new NullPointerException("The host should never resolve to null, level " + fLevel + " is not available"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            InstrumentedProfilingElement stackElement = elements.get(fLevel);
            return new AttributeNameHostProvider(stackElement.getStateSystem(), stackElement.getQuark());
        }

    }

}
