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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlActionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlConditionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlFsmStateCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsm;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenRuntimeData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * This Class implements a pattern handler tree in the XML-defined state system.
 * It receives events and dispatches it to Active finite state machines.
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlPatternEventHandler {

    /** The save stored fields action label */
    public static final String SAVE_STORED_FIELDS_STRING = "saveStoredFields"; //$NON-NLS-1$
    /** The clear stored fields action label */
    public static final String CLEAR_STORED_FIELDS_STRING = "clearStoredFields"; //$NON-NLS-1$

    /* list of states changes */
    private final XmlPatternStateProvider fParent;

    private final List<DataDrivenFsm> fInitialFsm = new ArrayList<>();
    private final Map<String, DataDrivenCondition> fTestMap;
    private final Map<String, DataDrivenAction> fActionMap;
    private final Map<String, DataDrivenFsm> fFsmMap = new HashMap<>();
    private final List<DataDrivenFsm> fActiveFsmList = new ArrayList<>();

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this event handler
     * @param parent
     *            The state system container this event handler belongs to
     */
    public TmfXmlPatternEventHandler(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fParent = (XmlPatternStateProvider) parent;

        Map<String, DataDrivenCondition> testMap = new HashMap<>();
        // Compile the test conditions
        NodeList nodesTest = node.getElementsByTagName(TmfXmlStrings.TEST);
        /* load transition input */
        for (int i = 0; i < nodesTest.getLength(); i++) {
            Element element = (Element) nodesTest.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            String testId = modelFactory.createTransitionValidator(element, fParent);
            TmfXmlConditionCu test = Objects.requireNonNull(fParent.getAnalysisCompilationData().getTest(testId));
            testMap.put(testId, test.generate());
        }
        fTestMap = Collections.unmodifiableMap(testMap);

        @NonNull
        Builder<String, DataDrivenAction> builder = ImmutableMap.builder();
        NodeList nodesAction = node.getElementsByTagName(TmfXmlStrings.ACTION);
        /* load actions */
        for (int i = 0; i < nodesAction.getLength(); i++) {
            Element element = (Element) nodesAction.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            String actionId = modelFactory.createAction(element, fParent);
            TmfXmlActionCu action = Objects.requireNonNull(fParent.getAnalysisCompilationData().getAction(actionId));
            builder.put(actionId, action.generate());
        }
        fActionMap = builder.build();

        NodeList nodesFsm = node.getElementsByTagName(TmfXmlStrings.FSM);
        /* load fsm */
        for (int i = 0; i < nodesFsm.getLength(); i++) {
            Element element = (Element) nodesFsm.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            String fsmId = Objects.requireNonNull(TmfXmlFsmStateCu.compileFsm(fParent.getAnalysisCompilationData(), element), "The FSM did not compile properly"); //$NON-NLS-1$
            TmfXmlFsmStateCu fsmCu = Objects.requireNonNull(fParent.getAnalysisCompilationData().getFsm(fsmId));
            DataDrivenFsmState fsm = fsmCu.generate();
            if (!(fsm instanceof DataDrivenFsm)) {
                throw new NullPointerException("fsm not of the right type"); //$NON-NLS-1$
            }
            fFsmMap.put(fsmId, (DataDrivenFsm) fsm);
        }

        String initialFsm = node.getAttribute(TmfXmlStrings.INITIAL);
        if (!initialFsm.isEmpty()) {
            for (String initial : initialFsm.split(TmfXmlStrings.AND_SEPARATOR)) {
                DataDrivenFsm fsm = fFsmMap.get(initial);
                if (fsm != null) {
                    fInitialFsm.add(fsm);
                }
            }
        }

    }

    /**
     * Start a new scenario for this specific fsm id. If the fsm support only a
     * single instance and this instance already exist, no new scenario is then
     * started. If the scenario is created we handle the current event directly.
     *
     * @param fsms
     *            The FSMs to start
     * @param event
     *            The current event
     * @param force
     *            True to force the creation of the scenario, false otherwise
     * @param executionData
     *            The execution data
     */
    public void startScenario(List<DataDrivenFsm> fsms, ITmfEvent event, boolean force, DataDrivenRuntimeData executionData) {
        for (DataDrivenFsm fsm : fsms) {
            if (!fActiveFsmList.contains(fsm)) {
                fActiveFsmList.add(fsm);
            }
            fsm.createScenario(event, force, executionData, fParent);
        }
    }

    /**
     * Get all the defined transition tests
     *
     * @return The tests in a map
     */
    public Map<String, DataDrivenCondition> getTestMap() {
        return fTestMap;
    }

    /**
     * Get all the defined actions
     *
     * @return The actions
     */
    public Map<String, DataDrivenAction> getActionMap() {
        return fActionMap;
    }

    /**
     * If the pattern handler can handle the event, it send the event to all
     * finite state machines with ongoing scenarios
     *
     * @param event
     *            The trace event to handle
     */
    public void handleEvent(ITmfEvent event) {
        /*
         * Order is important so cannot be parallelized
         */
        final List<DataDrivenFsm> activeFsmList = fActiveFsmList;
        final Map<String, DataDrivenFsm> fsmMap = fFsmMap;
        if (activeFsmList.isEmpty()) {
            List<DataDrivenFsm> fsms = fInitialFsm;
            if (fInitialFsm.isEmpty()) {
                // Add all FSMs to the list to create scenarios
                fsms = new ArrayList<>();
                for (DataDrivenFsm fsm : fsmMap.values()) {
                    fsms.add(fsm);
                }
            }
            if (!fsms.isEmpty()) {
                startScenario(fsms, event, true, fParent.getExecutionData());
            }
        } else {
            List<DataDrivenFsm> fsmToStart = new ArrayList<>();
            for (DataDrivenFsm fsm : fsmMap.values()) {
                if (fsm.isNewScenarioAllowed(fParent.getExecutionData().getRuntimeForFsm(fsm))) {
                    fsmToStart.add(fsm);
                }
            }
            if (!fsmToStart.isEmpty()) {
                startScenario(fsmToStart, event, false, fParent.getExecutionData());
            }
        }
        for (DataDrivenFsm fsm : activeFsmList) {
            fsm.handleEvent(event, fParent.getExecutionData(), fParent);
        }
    }

    /**
     * Abandon all the ongoing scenarios
     */
    public void dispose() {
        for (DataDrivenFsm fsm : fActiveFsmList) {
            fsm.dispose(fParent.getExecutionData());
        }
    }

}
