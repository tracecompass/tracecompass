/*******************************************************************************
 * Copyright (c) 2016, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callstack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.base.IProfilingElement;
import org.eclipse.tracecompass.analysis.profiling.core.base.IProfilingGroupDescriptor;
import org.eclipse.tracecompass.analysis.profiling.core.base.ProfilingElement;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStack;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackHostUtils.IHostIdProvider;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackHostUtils.IHostIdResolver;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackSeries.IThreadIdProvider;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackSeries.IThreadIdResolver;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue.Type;

/**
 * A callstack element corresponding to an attribute in the state system
 *
 * @author Geneviève Bastien
 */
public class InstrumentedProfilingElement extends ProfilingElement {

    private static final String INSTRUMENTED = "instrumented"; //$NON-NLS-1$

    private final ITmfStateSystem fStateSystem;
    private final int fQuark;
    private final IHostIdResolver fHostResolver;
    private final @Nullable IThreadIdResolver fThreadIdResolver;
    private final Map<Integer, IProfilingElement> fNextElements = new HashMap<>();

    private @Nullable CallStack fCallstack = null;

    /**
     * Constructor
     *
     * @param hostResolver
     *            The resolver for the host ID for the callstack
     * @param stateSystem
     *            The state system containing the callstack
     * @param quark
     *            The quark corresponding to this element
     * @param group
     *            The group descriptor of this element
     * @param nextGroup
     *            The group descriptor of the next group of elements
     * @param threadIdResolver
     *            The object describing how to resolve the thread ID
     * @param parent
     *            The parent element or <code>null</code> if this is the root
     *            element
     */
    public InstrumentedProfilingElement(IHostIdResolver hostResolver, ITmfStateSystem stateSystem, Integer quark,
            InstrumentedGroupDescriptor group,
            @Nullable InstrumentedGroupDescriptor nextGroup,
            @Nullable IThreadIdResolver threadIdResolver,
            @Nullable InstrumentedProfilingElement parent) {
        super(INSTRUMENTED, group, nextGroup, parent);
        fStateSystem = stateSystem;
        fQuark = quark;
        fHostResolver = hostResolver;
        fThreadIdResolver = threadIdResolver;
    }

    @Override
    public Collection<IProfilingElement> getChildren() {
        // Get the elements from the next group in the hierarchy
        @Nullable IProfilingGroupDescriptor nextGroup = getNextGroup();
        if (!(nextGroup instanceof InstrumentedGroupDescriptor)) {
            return Collections.emptyList();
        }
        return getNextGroupElements((InstrumentedGroupDescriptor) nextGroup);
    }

    @Override
    public @Nullable InstrumentedProfilingElement getParentElement() {
        return (InstrumentedProfilingElement) super.getParentElement();
    }

    /**
     * Create the root elements from a root group and its thread ID resolver
     *
     * @param rootGroup
     *            The root group descriptor
     * @param hostResolver
     *            The host ID resolver
     * @param resolver
     *            the thread ID resolver
     * @param cache
     *            A cache of elements already built. It maps a quark to an element
     *            and the element will be returned if it has already been computed
     * @return A collection of elements that are roots of the given callstack
     *         grouping
     */
    public static Collection<IProfilingElement> getRootElements(InstrumentedGroupDescriptor rootGroup, IHostIdResolver hostResolver, @Nullable IThreadIdResolver resolver, Map<Integer, IProfilingElement> cache) {
        return getNextElements(rootGroup, rootGroup.getStateSystem(), ITmfStateSystem.ROOT_ATTRIBUTE, hostResolver, resolver, null, cache);
    }

    private Collection<IProfilingElement> getNextGroupElements(InstrumentedGroupDescriptor nextGroup) {
        return getNextElements(nextGroup, fStateSystem, fQuark, fHostResolver, fThreadIdResolver, this, fNextElements);
    }

    private static Collection<IProfilingElement> getNextElements(InstrumentedGroupDescriptor nextGroup, ITmfStateSystem stateSystem, int baseQuark, IHostIdResolver hostResolver, @Nullable IThreadIdResolver threadIdProvider, @Nullable InstrumentedProfilingElement parent, Map<Integer, IProfilingElement> cache) {
        // Get the elements from the base quark at the given pattern
        List<Integer> quarks = stateSystem.getQuarks(baseQuark, nextGroup.getSubPattern());
        if (quarks.isEmpty()) {
            return Collections.emptyList();
        }

        InstrumentedGroupDescriptor nextLevel = nextGroup.getNextGroup();
        // If the next level is null, then this is a callstack final element
        List<IProfilingElement> elements = new ArrayList<>(quarks.size());
        for (Integer quark : quarks) {
            IProfilingElement element = cache.get(quark);
            if (element == null) {
                element = new InstrumentedProfilingElement(hostResolver, stateSystem, quark,
                        nextGroup, nextLevel, threadIdProvider, parent);
                if (nextGroup.isSymbolKeyGroup()) {
                    element.setSymbolKeyElement(element);
                }
                cache.put(quark, element);
            }
            elements.add(element);
        }
        return elements;
    }

    /**
     * Get the thread ID resolver, the object that will retrieve the thread ID of a
     * given stack
     *
     * @return The thread ID provider or <code>null</code> if unavailable
     */
    protected @Nullable IThreadIdResolver getThreadIdResolver() {
        return fThreadIdResolver;
    }

    @Override
    public @NonNull String getName() {
        if (fQuark == ITmfStateSystem.ROOT_ATTRIBUTE) {
            return StringUtils.EMPTY;
        }
        return fStateSystem.getAttributeName(fQuark);
    }

    @Override
    public int retrieveSymbolKeyAt(long startTime) {
        int processId = ProfilingElement.DEFAULT_SYMBOL_KEY;
        if (fQuark != ITmfStateSystem.ROOT_ATTRIBUTE) {
            try {
                // Query a time that is within the bounds of the state system
                long start = Math.max(fStateSystem.getStartTime(), startTime);
                start = Math.max(start, fStateSystem.getCurrentEndTime());

                // Query the value of the quark at the requested time
                ITmfStateInterval interval = fStateSystem.querySingleState(start, fQuark);
                ITmfStateValue processStateValue = interval.getStateValue();
                // If the state value is an integer, assume it is the symbol we
                // are looking for
                if (processStateValue.getType() == Type.INTEGER) {
                    processId = processStateValue.unboxInt();
                } else {
                    try {
                        // Otherwise, try to take the attribute name as the key
                        String processName = fStateSystem.getAttributeName(fQuark);
                        processId = Integer.parseInt(processName);
                    } catch (NumberFormatException e) {
                        /* use default processId */
                    }
                }
            } catch (StateSystemDisposedException e) {
                // ignore
            }
        }
        return processId;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": [" + fQuark + ']'; //$NON-NLS-1$
    }

    /**
     * Get the state system containing the callstack data
     *
     * @return The state system
     */
    public ITmfStateSystem getStateSystem() {
        return fStateSystem;
    }

    /**
     * Get the quark corresponding to this element
     *
     * @return The quark
     */
    public int getQuark() {
        return fQuark;
    }

    /**
     * Get the callstack associated with this element if this is a leaf element. If
     * it is not a leaf, it throw a {@link NoSuchElementException}
     *
     * @return The call stack
     */
    public CallStack getCallStack() {
        CallStack callstack  = fCallstack;
        List<Integer> subAttributes = getStackQuarks();
        if (callstack == null) {
            IHostIdProvider hostProvider = fHostResolver.apply(this);
            IThreadIdResolver threadIdResolver = fThreadIdResolver;
            IThreadIdProvider threadIdProvider = threadIdResolver == null ? null : threadIdResolver.resolve(hostProvider, this);
            callstack =  new CallStack(getStateSystem(), subAttributes, this, hostProvider, threadIdProvider);
            fCallstack = callstack;
        } else {
            synchronized (callstack) {
                // Update the callstack if attributes were added
                if (callstack.getMaxDepth() < subAttributes.size()) {
                    callstack.updateAttributes(subAttributes);
                }
            }
        }
        return Objects.requireNonNull(callstack);
    }

    /**
     * Get the stack quarks that contain the data of the function calls
     *
     * This is meant to remain internal. It is public only so that the segment store
     * from the CallStackSeries can access this
     *
     * @return The list of quarks containing the data
     */
    public List<Integer> getStackQuarks() {
        if (!isLeaf()) {
            throw new NoSuchElementException();
        }
        int stackQuark = getStateSystem().optQuarkRelative(getQuark(), CallStackAnalysis.CALL_STACK);
        if (stackQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            // No CallStack element underneath, assume a flat chart: the current quark has
            // the status to show
            return Collections.singletonList(getQuark());
        }
        List<Integer> subAttributes = getStateSystem().getSubAttributes(stackQuark, false);
        return subAttributes;
    }

}
