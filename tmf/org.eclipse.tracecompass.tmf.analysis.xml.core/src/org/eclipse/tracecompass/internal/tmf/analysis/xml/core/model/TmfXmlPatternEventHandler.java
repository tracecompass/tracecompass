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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlConditionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition;
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

    /* list of states changes */
    private final XmlPatternStateProvider fParent;

    private final List<String> fInitialFsm;
    private final Map<String, DataDrivenCondition> fTestMap;
    private final Map<String, ITmfXmlAction> fActionMap;
    private final Map<String, TmfXmlFsm> fFsmMap = new LinkedHashMap<>();
    private final List<TmfXmlFsm> fActiveFsmList = new ArrayList<>();

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
        String initialFsm = node.getAttribute(TmfXmlStrings.INITIAL);
        fInitialFsm = initialFsm.isEmpty() ? Collections.emptyList() : Arrays.asList(initialFsm.split(TmfXmlStrings.AND_SEPARATOR));

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

        @NonNull Builder<String, ITmfXmlAction> builder = ImmutableMap.builder();
        NodeList nodesAction = node.getElementsByTagName(TmfXmlStrings.ACTION);
        /* load actions */
        for (int i = 0; i < nodesAction.getLength(); i++) {
            Element element = (Element) nodesAction.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlAction action = modelFactory.createAction(element, fParent);
            builder.put(action.getId(), action);
        }
        builder.put(TmfXmlStrings.CONSTANT_PREFIX + ITmfXmlAction.CLEAR_STORED_FIELDS_STRING, new ResetStoredFieldsAction(fParent));
        builder.put(TmfXmlStrings.CONSTANT_PREFIX + ITmfXmlAction.SAVE_STORED_FIELDS_STRING, new UpdateStoredFieldsAction(fParent));
        fActionMap = builder.build();

        NodeList nodesFsm = node.getElementsByTagName(TmfXmlStrings.FSM);
        /* load fsm */
        for (int i = 0; i < nodesFsm.getLength(); i++) {
            Element element = (Element) nodesFsm.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlFsm fsm = modelFactory.createFsm(element, fParent);
            fFsmMap.put(fsm.getId(), fsm);
        }
    }

    /**
     * Start a new scenario for this specific fsm id. If the fsm support only a
     * single instance and this instance already exist, no new scenario is then
     * started. If the scenario is created we handle the current event directly.
     *
     * @param fsmIds
     *            The IDs of the fsm to start
     * @param event
     *            The current event
     * @param force
     *            True to force the creation of the scenario, false otherwise
     */
    public void startScenario(List<String> fsmIds, @Nullable ITmfEvent event, boolean force) {
        for (String fsmId : fsmIds) {
            TmfXmlFsm fsm = NonNullUtils.checkNotNull(fFsmMap.get(fsmId));
            if (!fActiveFsmList.contains(fsm)) {
                fActiveFsmList.add(fsm);
            }
            fsm.createScenario(event, this, force);
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
    public Map<String, ITmfXmlAction> getActionMap() {
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
        final @NonNull List<@NonNull TmfXmlFsm> activeFsmList = fActiveFsmList;
        final @NonNull Map<@NonNull String, @NonNull TmfXmlFsm> fsmMap = fFsmMap;
        if (activeFsmList.isEmpty()) {
            List<String> fsmIds = fInitialFsm;
            if (fsmIds.isEmpty()) {
                fsmIds = new ArrayList<>();
                for (TmfXmlFsm fsm : fsmMap.values()) {
                    fsmIds.add(fsm.getId());
                }
            }
            if (!fsmIds.isEmpty()) {
                startScenario(fsmIds, null, true);
            }
        } else {
            List<String> fsmToStart = new ArrayList<>();
            for (Map.Entry<String, TmfXmlFsm> entry : fsmMap.entrySet()) {
                if (entry.getValue().isNewScenarioAllowed()) {
                    fsmToStart.add(entry.getKey());
                }
            }
            if (!fsmToStart.isEmpty()) {
                startScenario(fsmToStart, null, false);
            }
        }
        for (TmfXmlFsm fsm : activeFsmList) {
            fsm.handleEvent(event, fTestMap, fParent);
        }
    }

    /**
     * Abandon all the ongoing scenarios
     */
    public void dispose() {
        for (TmfXmlFsm fsm : fActiveFsmList) {
            fsm.dispose();
        }
    }

    /**
     * Get the fsm corresponding to the specified id
     *
     * @param fsmId
     *            The id of the fsm
     * @return The fsm found, null if nothing found
     */
    public @Nullable TmfXmlFsm getFsm(String fsmId) {
        return fFsmMap.get(fsmId);
    }
}
