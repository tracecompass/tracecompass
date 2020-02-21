/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.trace;

import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisEventRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement.PriorityLevel;

import com.google.common.collect.ImmutableList.Builder;

/**
 * This class is a pre-requirement class who will instanciate at runtime the
 * actual requirements depending on a trace's event layout
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public class KernelEventLayoutRequirement {

    /**
     * Functional interface that maps a layout to an event name
     */
    @FunctionalInterface
    public interface ILayoutToEventName {
        /**
         * This method will return the event name mapped by this requirement
         * from the layout. The returned event name may be <code>null</code> in
         * some layouts.
         *
         * @param layout
         *            The event layout of the trace
         * @return The event name
         */
        @Nullable String getEventName(IKernelAnalysisEventLayout layout);
    }

    private final Set<ILayoutToEventName> fEventNames;
    private final PriorityLevel fLevel;

    /**
     * Constructor
     *
     * @param layoutReqs
     *            The layout mappings this requirement represents
     * @param level
     *            Whether the requirement represented by these mapping is
     *            mandatory or optional
     */
    public KernelEventLayoutRequirement(Set<ILayoutToEventName> layoutReqs, PriorityLevel level) {
        fEventNames = layoutReqs;
        fLevel = level;
    }

    /**
     * Build a real requirement from the layout mapping to be matched with a
     * real trace's layout
     *
     * @param layout
     *            The event layout from which to build the requirements.
     * @return The real requirement
     */
    public TmfAbstractAnalysisRequirement instanciateRequirements(IKernelAnalysisEventLayout layout) {
        Builder<String> events = new Builder<>();
        for (ILayoutToEventName eventNameLayout : fEventNames) {
            String eventName = eventNameLayout.getEventName(layout);
            if (eventName != null) {
                events.add(eventName);
            }
        }
        return new TmfAnalysisEventRequirement(events.build(), fLevel);
    }

}
