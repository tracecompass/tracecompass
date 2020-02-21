/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsm;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmSimpleState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmStateTransition;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * A compilation unit for XML finite state machine
 *
 * @author Geneviève Bastien
 * @author Jean-Christian Kouamé
 */
public abstract class TmfXmlFsmStateCu implements IDataDrivenCompilationUnit {

    /**
     * Represents an FSM
     */
    public static class TmfXmlFsmCu extends TmfXmlFsmStateCu {

        private final TmfXmlFsmStateCu fInitialState;
        private final Collection<TmfXmlFsmSimpleStateCu> fStates;
        private final TmfXmlConditionCu fPreCondition;
        private final boolean fConsuming;
        private final boolean fMultipleInstances;

        /**
         * Constructor
         *
         * @param id
         *            The ID of this FSM
         * @param initialStateCu
         *            The initial state compilation unit
         * @param states
         *            The list of states
         * @param consuming
         *            Whether this fsm is consuming (ie, once a transition is
         *            taken in one scenario, it won't be taken on others)
         * @param instanceMultipleEnabled
         *            Whether multiple scenarios of this FSM can be run in
         *            parallel
         * @param preCondition
         *            The preconditions for this FSM
         */
        private TmfXmlFsmCu(String id, TmfXmlFsmStateCu initialStateCu, Collection<TmfXmlFsmSimpleStateCu> states, boolean consuming, boolean instanceMultipleEnabled, TmfXmlConditionCu preCondition) {
            super(id);
            fInitialState = initialStateCu;
            fStates = states;
            fPreCondition = preCondition;
            fConsuming = consuming;
            fMultipleInstances = instanceMultipleEnabled;
        }

        @Override
        public DataDrivenFsm generate() {
            Map<String, DataDrivenFsmSimpleState> states = fStates.stream()
                    .map(TmfXmlFsmSimpleStateCu::generate)
                    .collect(Collectors.toMap(DataDrivenFsmState::getId, state -> state));

            return new DataDrivenFsm(getId(), fInitialState.generate(), states, fPreCondition.generate(), fConsuming, fMultipleInstances);
        }

    }

    /**
     * Represent a final state, without transitions
     */
    private static class TmfXmlFsmFinalStateCu extends TmfXmlFsmSimpleStateCu {

        public TmfXmlFsmFinalStateCu(String id) {
            super(id, Collections.emptyList(), Collections.emptyList());
        }

        @Override
        public DataDrivenFsmSimpleState generate() {
            return DataDrivenFsmSimpleState.createFinalState(getId());
        }

    }

    /**
     * Package-private class so other compilation units can see it
     */
    static class TmfXmlFsmSimpleStateCu extends TmfXmlFsmStateCu {

        private final List<TmfXmlFsmStateTransitionCu> fTransitions = new ArrayList<>();
        private final TmfXmlActionCu fOnEntryActions;
        private final TmfXmlActionCu fOnExitActions;

        private TmfXmlFsmSimpleStateCu(String id, List<TmfXmlActionCu> onEntryActions, List<TmfXmlActionCu> onExitActions) {
            super(id);
            fOnEntryActions = TmfXmlActionCu.createActionList(onEntryActions);
            fOnExitActions = TmfXmlActionCu.createActionList(onExitActions);
        }

        private void addTransitions(List<TmfXmlFsmStateTransitionCu> transitions) {
            fTransitions.addAll(transitions);
        }

        @Override
        public DataDrivenFsmSimpleState generate() {
            List<DataDrivenFsmStateTransition> transitions = fTransitions.stream()
                    .map(TmfXmlFsmStateTransitionCu::generate)
                    .collect(Collectors.toList());
            return new DataDrivenFsmSimpleState(getId(), transitions, fOnEntryActions.generate(), fOnExitActions.generate());
        }

    }

    private final String fId;

    private TmfXmlFsmStateCu(String id) {
        fId = id;
    }

    @Override
    public abstract DataDrivenFsmState generate();

    /**
     * Get the ID of this FSM state
     *
     * @return The ID of the FSM state
     */
    protected String getId() {
        return fId;
    }

    /**
     * Compile a finite state machine from an XML element
     *
     * FIXME: Return the FSM directly when legacy code is gone
     *
     * @param analysisData
     *            The analysis data already compiled
     * @param element
     *            the XML element corresponding to the fsm
     * @return The FSM
     */
    public static @Nullable TmfXmlFsmCu compileFsm(AnalysisCompilationData analysisData, Element element) {
        String id = element.getAttribute(TmfXmlStrings.ID);
        if (id.isEmpty()) {
            // TODO: Validation message here
            Activator.logError("FSM: The FSM should have a non-empty 'id' parameter"); //$NON-NLS-1$
            return null;
        }
        String consumingStr = element.getAttribute(TmfXmlStrings.CONSUMING);
        String multipleInstancesStr = element.getAttribute(TmfXmlStrings.MULTIPLE);
        boolean consuming = consumingStr.isEmpty() ? true : Boolean.parseBoolean(consumingStr);
        boolean instanceMultipleEnabled = multipleInstancesStr.isEmpty() ? true : Boolean.parseBoolean(multipleInstancesStr);

        Map<String, TmfXmlFsmSimpleStateCu> states = new HashMap<>();
        // Create the FSM states, without transitions yet
        // FSMs can be cyclic, so we need all states to be able to create the
        // transitions
        List<Element> stateElements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.STATE);
        TmfXmlFsmStateCu firstState = null;
        for (Element stateElement : stateElements) {
            TmfXmlFsmStateCu state = compileSimpleState(analysisData, stateElement, states, false);
            if (state == null) {
                return null;
            }
            // Save the first state in case there is no initial state
            if (firstState == null) {
                firstState = state;
            }
        }

        // Get the final state
        List<Element> finalElements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.FINAL);
        if (!finalElements.isEmpty()) {
            if (finalElements.size() > 1) {
                // TODO: Validation message here
                Activator.logWarning("Fsm " + id + ": there should be only one final state"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            TmfXmlFsmSimpleStateCu state = compileSimpleState(analysisData, finalElements.get(0), states, true);
            if (state == null) {
                return null;
            }
        }

        // Compile the initial state
        String initialStr = element.getAttribute(TmfXmlStrings.INITIAL);
        List<Element> nodesInitialElement = TmfXmlUtils.getChildElements(element, TmfXmlStrings.INITIAL);
        List<Element> nodesInitialStateElement = TmfXmlUtils.getChildElements(element, TmfXmlStrings.INITIAL_STATE);
        TmfXmlFsmStateCu initialStateCu = null;
        if (!nodesInitialStateElement.isEmpty()) {
            if (!initialStr.isEmpty() || !nodesInitialElement.isEmpty()) {
                // TODO: Validation message here
                Activator.logWarning("Fsm " + id + ": the 'initial' attribute was set or an <initial> element was defined. Only one of the 3 should be used. The " + TmfXmlStrings.INITIAL_STATE + " element will have precedence"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            initialStateCu = compileInitialState(analysisData, nodesInitialStateElement.get(0), states);
            if (initialStateCu == null) {
                return null;
            }
        } else if (initialStr.isEmpty() && nodesInitialElement.isEmpty()) {
            // Take the first state as initial state
            if (firstState == null) {
                // TODO: Validation message here
                Activator.logError("FSM " + id + ": No state was defined."); //$NON-NLS-1$ //$NON-NLS-2$
                return null;
            }
            initialStateCu = firstState;
        } else {
            if (!initialStr.isEmpty() && !nodesInitialElement.isEmpty()) {
                // TODO: Validation message here
                Activator.logWarning("Fsm " + id + ": Both 'initial' attribute and <initial> element were declared. Only the 'initial' attribute will be used"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!initialStr.isEmpty()) {
                // Take the initial attribute as initial state, but make sure
                // the state exists
                initialStateCu = states.get(initialStr);
                if (initialStateCu == null) {
                    // TODO: Validation message here
                    Activator.logError("FSM " + id + ": Undefined initial state " + initialStr); //$NON-NLS-1$ //$NON-NLS-2$
                    return null;
                }
            } else {
                // The initial element is the equivalent of the initial
                // attribute except the starting state is the name of the target
                List<Element> transitionElements = TmfXmlUtils.getChildElements(nodesInitialElement.get(0), TmfXmlStrings.TRANSITION);
                if (transitionElements.isEmpty()) {
                    // TODO: Validation message here
                    Activator.logError("FSM " + id + ": No transition defined for 'initial' element"); //$NON-NLS-1$ //$NON-NLS-2$
                    return null;
                } else if (transitionElements.size() > 1) {
                    // TODO: Validation message here
                    Activator.logWarning("FSM " + id + ": Too many transitions defined for 'initial' element. Only 1 needed"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                initialStateCu = TmfXmlFsmStateTransitionCu.compileInitialTransition(analysisData, transitionElements.get(0), states);
                if (initialStateCu == null) {
                    return null;
                }
            }
        }

        // For each state, compile the transitions
        for (Element stateElement : stateElements) {
            TmfXmlFsmSimpleStateCu simpleState = getSimpleState(stateElement, states);
            List<TmfXmlFsmStateTransitionCu> transitions = compileTransitionElements(analysisData, stateElement, states);
            if (transitions == null) {
                return null;
            }
            if (transitions.isEmpty()) {
                // TODO: Validation message here
                Activator.logWarning("Fsm " + id + ": a state was defined without transition. You may get stuck there"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            simpleState.addTransitions(transitions);
        }

        // Compile the preconditions
        List<Element> preCondElements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.PRECONDITION);
        List<TmfXmlConditionCu> preConditions = new ArrayList<>();
        for (Element preCondElement : preCondElements) {
            TmfXmlConditionCu preCond = TmfXmlFsmStateTransitionCu.compileAsCondition(analysisData, preCondElement);
            if (preCond == null) {
                return null;
            }
            preConditions.add(preCond);
        }
        TmfXmlConditionCu preCondition = TmfXmlConditionCu.createOrCondition(preConditions);

        TmfXmlFsmCu fsm = new TmfXmlFsmCu(id, initialStateCu, states.values(), consuming, instanceMultipleEnabled, preCondition);
        analysisData.addFsm(id, fsm);
        return fsm;
    }

    private static @Nullable List<TmfXmlFsmStateTransitionCu> compileTransitionElements(AnalysisCompilationData analysisData, Element element, Map<String, TmfXmlFsmSimpleStateCu> states) {
        List<Element> childElements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.TRANSITION);
        List<TmfXmlFsmStateTransitionCu> transitions = new ArrayList<>();
        for (Element childElement : childElements) {
            TmfXmlFsmStateTransitionCu transition = TmfXmlFsmStateTransitionCu.compile(analysisData, childElement, states);
            if (transition == null) {
                return null;
            }
            transitions.add(transition);
        }
        return transitions;
    }

    private static @Nullable TmfXmlFsmSimpleStateCu compileInitialState(AnalysisCompilationData analysisData, Element element, Map<String, TmfXmlFsmSimpleStateCu> states) {
        List<TmfXmlFsmStateTransitionCu> transitions = compileTransitionElements(analysisData, element, states);
        if (transitions == null) {
            return null;
        }
        TmfXmlFsmSimpleStateCu initialState = new TmfXmlFsmSimpleStateCu(TmfXmlStrings.INITIAL_STATE, Collections.emptyList(), Collections.emptyList());
        initialState.addTransitions(transitions);
        return initialState;
    }

    private static @Nullable TmfXmlFsmSimpleStateCu compileSimpleState(AnalysisCompilationData analysisData, Element element, Map<String, TmfXmlFsmSimpleStateCu> states, boolean isFinalState) {
        String stateId = element.getAttribute(TmfXmlStrings.ID);
        if (stateId.isEmpty()) {
            // TODO: Validation message here
            Activator.logError("FSM State: The state should have a non-empty 'id' parameter"); //$NON-NLS-1$
            return null;
        }
        TmfXmlFsmStateCu currentStateCu = states.get(stateId);
        if (currentStateCu != null) {
            // TODO: Validation message here
            Activator.logError("FSM State: Redefinition of state " + stateId); //$NON-NLS-1$
            return null;
        }

        // Get the onEntry and onExit actions
        List<Element> elements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.ONENTRY);
        List<TmfXmlActionCu> onEntryActions = Collections.emptyList();
        if (!elements.isEmpty()) {
            onEntryActions = getActionList(analysisData, elements.get(0));
            if (onEntryActions == null) {
                return null;
            }
        }
        elements = TmfXmlUtils.getChildElements(element, TmfXmlStrings.ONEXIT);
        List<TmfXmlActionCu> onExitActions = Collections.emptyList();
        if (!elements.isEmpty()) {
            onExitActions = getActionList(analysisData, elements.get(0));
            if (onExitActions == null) {
                return null;
            }
        }

        TmfXmlFsmSimpleStateCu stateCu;
        if (isFinalState) {
            if (!onExitActions.isEmpty() || !onEntryActions.isEmpty()) {
                // TODO: Validation message here
                Activator.logWarning("FSM State: Final state should not have any onEntry and onExit actions"); //$NON-NLS-1$
            }
            stateCu = new TmfXmlFsmFinalStateCu(stateId);
        } else {
            stateCu = new TmfXmlFsmSimpleStateCu(stateId, onEntryActions, onExitActions);
        }
        states.put(stateId, stateCu);
        return stateCu;
    }

    private static @Nullable List<TmfXmlActionCu> getActionList(AnalysisCompilationData analysisData, Element element) {
        List<TmfXmlActionCu> actions = new ArrayList<>();
        String onEntryActionStr = element.getAttribute(TmfXmlStrings.ACTION);
        if (onEntryActionStr.isEmpty()) {
            return actions;
        }
        @NonNull String[] actionIds = onEntryActionStr.split(TmfXmlStrings.AND_SEPARATOR);
        for (String actionId : actionIds) {
            TmfXmlActionCu action = analysisData.getAction(actionId);
            if (action == null) {
                // TODO: Validation message here
                Activator.logError("FSM State: Undefined action " + actionId); //$NON-NLS-1$
                return null;
            }
            actions.add(action);
        }
        return actions;
    }

    private static TmfXmlFsmSimpleStateCu getSimpleState(Element stateElement, Map<String, TmfXmlFsmSimpleStateCu> states) {
        String stateId = stateElement.getAttribute(TmfXmlStrings.ID);
        TmfXmlFsmStateCu state = states.get(stateId);
        if (!(state instanceof TmfXmlFsmSimpleStateCu)) {
            throw new NullPointerException("The requested state is not of the right type: " + stateId); //$NON-NLS-1$
        }
        return (TmfXmlFsmSimpleStateCu) state;
    }

}
