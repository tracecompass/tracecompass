/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.w3c.dom.Element;

/**
 * This Class implements an action tree in the XML-defined state system.
 * An action is a collection of {@link ITmfXmlAction} that are executed when necessary.
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlAction implements ITmfXmlAction {

    private final IXmlStateSystemContainer fParent;
    private final String fId;
    private final List<ITmfXmlAction> fActionList = new ArrayList<>();

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this action
     * @param container
     *            The state system container this action belongs to
     */
    public TmfXmlAction(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        fParent = container;
        fId = NonNullUtils.checkNotNull(node.getAttribute(TmfXmlStrings.ID));
        List<@Nullable Element> childElements = XmlUtils.getChildElements(node);
        for (Element child : childElements) {
            final @NonNull Element nonNullChild = NonNullUtils.checkNotNull(child);
            switch (nonNullChild.getNodeName()) {
            case TmfXmlStrings.STATE_CHANGE:
                fActionList.add(new StateChange(modelFactory, nonNullChild, fParent));
                break;
            case TmfXmlStrings.FSM_SCHEDULE_ACTION:
                fActionList.add(new ScheduleNewScenario(modelFactory, nonNullChild, fParent));
                break;
            case TmfXmlStrings.SEGMENT:
                fActionList.add(new GeneratePatternSegment(modelFactory, nonNullChild, fParent));
                break;
            case TmfXmlStrings.ACTION:
                fActionList.add(new TmfXmlAction(modelFactory, nonNullChild, fParent));
                break;
            default:
                Activator.logError("Invalid action type : " + nonNullChild.getNodeName()); //$NON-NLS-1$
            }
        }

    }

    /**
     * Get the ID of this action
     *
     * @return The id of this action
     */
    public String getId() {
        return fId;
    }

    @Override
    public void execute(@NonNull ITmfEvent event, TmfXmlScenarioInfo scenarioInfo) {
        // the order of the actions is important, do not parallelize.
        for (ITmfXmlAction action : fActionList) {
            action.execute(event, scenarioInfo);
        }
    }

    /**
     * Private class for an action that will create a state change in the state
     * system
     */
    private class StateChange implements ITmfXmlAction {

        private final DataDrivenAction fStateChange;
        private final IXmlStateSystemContainer fContainer;

        public StateChange(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
            fStateChange = modelFactory.createStateChange(node, parent);
            fContainer = parent;
        }

        @Override
        public void execute(@NonNull ITmfEvent event, TmfXmlScenarioInfo scenarioInfo) {
            try {
                fStateChange.eventHandle(event, scenarioInfo, fContainer);
            } catch (StateValueTypeException e) {
                Activator.logError("Exception when executing action state change", e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Private class for an action that will instantiate and schedule a new instance of
     * an fsm
     */
    private static class ScheduleNewScenario implements ITmfXmlAction {

        /**
         * Constructor
         *
         * @param modelFactory
         *            The factory used to create XML model elements
         * @param node
         *            The XML root of this action
         * @param container
         *            The state system container this action belongs to
         */
        public ScheduleNewScenario(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        }

        @Override
        public void execute(ITmfEvent event, TmfXmlScenarioInfo scenarioInfo) {
            // TODO This action needs to be implemented
            throw new UnsupportedOperationException("Schedule an FSM is not yet supported"); //$NON-NLS-1$
        }
    }

    /**
     * Private class for an action that will generate pattern segment
     */
    private static class GeneratePatternSegment implements ITmfXmlAction {

        private final TmfXmlPatternSegmentBuilder fSegmentBuilder;
        private final XmlPatternStateProvider fProvider;

        public GeneratePatternSegment(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
            fProvider = ((XmlPatternStateProvider) parent);
            fSegmentBuilder = modelFactory.createPatternSegmentBuilder(node, parent);
        }

        @Override
        public void execute(ITmfEvent event, TmfXmlScenarioInfo scenarioInfo) {
            long ts = fProvider.getHistoryBuilder().getStartTime(fProvider, scenarioInfo, event);
            // FIXME Should the scale always be nanoseconds?
            ITmfTimestamp start = TmfTimestamp.fromNanos(ts);
            ITmfTimestamp end = event.getTimestamp();
            fSegmentBuilder.generatePatternSegment(event, start, end, scenarioInfo);
        }
    }
}
