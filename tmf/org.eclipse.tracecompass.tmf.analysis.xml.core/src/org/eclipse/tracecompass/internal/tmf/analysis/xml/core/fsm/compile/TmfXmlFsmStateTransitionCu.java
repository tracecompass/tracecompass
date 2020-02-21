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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlConditionCu.TmfXmlRegexConditionCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlFsmStateCu.TmfXmlFsmSimpleStateCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionResetStoredFields;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionUpdateStoredFields;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmStateTransition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueEventName;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;

/**
 * A compilation unit for state transitions
 *
 * @author Geneviève Bastien
 * @author Jean-Christian Kouamé
 */
public class TmfXmlFsmStateTransitionCu implements IDataDrivenCompilationUnit {

    private static final TmfXmlStateValueCu EVENT_NAME_VALUE = new TmfXmlStateValueCu(() -> new DataDrivenValueEventName(null)) {

        private @Nullable DataDrivenValue fValue = null;

        @Override
        public DataDrivenValue generate() {
            DataDrivenValue value = fValue;
            if (value == null) {
                value = super.generate();
                fValue = value;
            }
            return value;
        }

    };

    private static final Pattern WILDCARD_PATTERN = Pattern.compile("\\*"); //$NON-NLS-1$
    private static final Pattern ALL_ACCEPT_PATTERN = Pattern.compile(".*"); //$NON-NLS-1$

    private final TmfXmlConditionCu fEvents;
    private final TmfXmlConditionCu fConditions;
    private final TmfXmlFsmStateCu fTarget;
    private final List<TmfXmlActionCu> fActions;
    private final boolean fSaveFields;
    private final boolean fClearFields;

    private TmfXmlFsmStateTransitionCu(TmfXmlConditionCu eventCond, TmfXmlConditionCu conditions, TmfXmlFsmStateCu target, List<TmfXmlActionCu> actions, boolean saveFields, boolean clearFields) {
        fEvents = eventCond;
        fConditions = conditions;
        fTarget = target;
        fActions = actions;
        fSaveFields = saveFields;
        fClearFields = clearFields;
    }

    @Override
    public DataDrivenFsmStateTransition generate() {
        List<DataDrivenAction> actions = fActions.stream()
                .map(TmfXmlActionCu::generate)
                .collect(Collectors.toList());
        if (fSaveFields) {
            actions.add(DataDrivenActionUpdateStoredFields.getInstance());
        }
        if (fClearFields) {
            actions.add(DataDrivenActionResetStoredFields.getInstance());
        }
        // Do not generate the target, as the FSM may be recursive, it can cause
        // infinite loop
        return new DataDrivenFsmStateTransition(fEvents.generate(), fConditions.generate(), fTarget.getId(), actions);
    }

    /**
     * Compile a state transition
     *
     * @param analysisData
     *            The analysis data gathered so far
     * @param element
     *            The XML element of this transition
     * @param states
     *            The map of states available for the FSM
     * @return The transition compilation unit or <code>null</code> if the
     *         compilation had errors
     */
    public static @Nullable TmfXmlFsmStateTransitionCu compile(AnalysisCompilationData analysisData, Element element, Map<String, TmfXmlFsmSimpleStateCu> states) {
        // Compile the events
        TmfXmlConditionCu eventCond = compileEventsCondition(element);

        // Compile the conditions
        TmfXmlConditionCu conditions = compileConditions(analysisData, element);
        if (conditions == null) {
            return null;
        }

        // Compile the target
        String targetStr = element.getAttribute(TmfXmlStrings.TARGET);
        if (targetStr.isEmpty()) {
            // TODO: Validation message here
            Activator.logError("State Transition: No target defined"); //$NON-NLS-1$
            return null;
        }
        TmfXmlFsmStateCu target = states.get(targetStr);
        if (target == null) {
            // TODO: Validation message here
            Activator.logError("State Transition: Undefined target state: " + targetStr); //$NON-NLS-1$
            return null;
        }

        // Compile the actions
        String actionsStr = element.getAttribute(TmfXmlStrings.ACTION);
        List<TmfXmlActionCu> actions = new ArrayList<>();
        if (!actionsStr.isEmpty()) {
            for (String action : actionsStr.split(TmfXmlStrings.AND_SEPARATOR)) {
                TmfXmlActionCu actionCu = analysisData.getAction(action);
                if (actionCu == null) {
                    // TODO: Validation message here
                    Activator.logError("State Transition: Undefined action: " + action); //$NON-NLS-1$
                    return null;
                }
                actions.add(actionCu);
            }
        }

        String saveSfStr = element.getAttribute(TmfXmlStrings.SAVE_STORED_FIELDS);
        boolean saveFields = false;
        if (!saveSfStr.isEmpty()) {
            saveFields = Boolean.parseBoolean(saveSfStr);
        }

        String clearSfStr = element.getAttribute(TmfXmlStrings.CLEAR_STORED_FIELDS);
        boolean clearFields = false;
        if (!clearSfStr.isEmpty()) {
            clearFields = Boolean.parseBoolean(clearSfStr);
        }

        return new TmfXmlFsmStateTransitionCu(eventCond, conditions, target, actions, saveFields, clearFields);
    }

    /**
     * Compile a transition with no target or action as a condition
     *
     * @param analysisData
     *            The analysis data compiled
     * @param element
     *            the XML element to compile
     * @return The resulting condition or <code>null</code> if there was a
     *         compilation problem
     */
    public static @Nullable TmfXmlConditionCu compileAsCondition(AnalysisCompilationData analysisData, Element element) {
        // Compile the events
        TmfXmlConditionCu event = compileEventsCondition(element);

        // Compile the conditions
        TmfXmlConditionCu conditions = compileConditions(analysisData, element);
        if (conditions == null) {
            return null;
        }

        // Add warnings for other fields
        String targetStr = element.getAttribute(TmfXmlStrings.TARGET);
        String actionsStr = element.getAttribute(TmfXmlStrings.ACTION);
        String saveSfStr = element.getAttribute(TmfXmlStrings.SAVE_STORED_FIELDS);
        String clearSfStr = element.getAttribute(TmfXmlStrings.CLEAR_STORED_FIELDS);
        if (!(targetStr.isEmpty() && actionsStr.isEmpty() && saveSfStr.isEmpty() && clearSfStr.isEmpty())) {
            // TODO: Validation message here
            Activator.logWarning("State Transition: Transition used a condition, there should be only 'events' and 'cond' attributes"); //$NON-NLS-1$
        }

        return TmfXmlConditionCu.createAndCondition(ImmutableList.of(event, conditions));
    }

    private static @Nullable TmfXmlConditionCu compileConditions(AnalysisCompilationData analysisData, Element element) {
        String testsStr = element.getAttribute(TmfXmlStrings.COND);
        List<TmfXmlConditionCu> conditions = new ArrayList<>();
        if (!testsStr.isEmpty()) {
            for (String condition : testsStr.split(TmfXmlStrings.AND_SEPARATOR)) {
                TmfXmlConditionCu test = analysisData.getTest(condition);
                if (test == null) {
                    // TODO: Validation message here
                    Activator.logError("State Transition: Undefined condition: " + condition); //$NON-NLS-1$
                    return null;
                }
                conditions.add(test);
            }
        }
        return TmfXmlConditionCu.createAndCondition(conditions);
    }

    private static TmfXmlConditionCu compileEventsCondition(Element element) {
        String eventsStr = element.getAttribute(TmfXmlStrings.EVENT);
        List<Pattern> events = new ArrayList<>();
        if (!eventsStr.isEmpty()) {
            for (String eventName : eventsStr.split(TmfXmlStrings.OR_SEPARATOR)) {
                String name = WILDCARD_PATTERN.matcher(eventName).replaceAll(".*"); //$NON-NLS-1$
                events.add(Pattern.compile(name));
            }
        } else {
            events.add(ALL_ACCEPT_PATTERN);
        }
        List<TmfXmlConditionCu> eventConditions = new ArrayList<>();
        for (Pattern pattern : events) {
            eventConditions.add(new TmfXmlRegexConditionCu(pattern, EVENT_NAME_VALUE));
        }
        return TmfXmlConditionCu.createOrCondition(eventConditions);
    }

    /**
     * Return the target state of this element. It will warn if any other
     * attribute was found on this transition, as only the target is needed
     *
     * @param analysisData
     *            The analysis data gathered so far
     * @param element
     *            The XML element of this transition
     * @param states
     *            The map of states available for the FSM
     * @return The name of the target state if it exists
     */
    public static @Nullable TmfXmlFsmStateCu compileInitialTransition(AnalysisCompilationData analysisData, Element element, Map<String, TmfXmlFsmSimpleStateCu> states) {
        String eventsStr = element.getAttribute(TmfXmlStrings.EVENT);
        String condStr = element.getAttribute(TmfXmlStrings.COND);
        String actionsStr = element.getAttribute(TmfXmlStrings.ACTION);
        String saveSfStr = element.getAttribute(TmfXmlStrings.SAVE_STORED_FIELDS);
        String clearSfStr = element.getAttribute(TmfXmlStrings.CLEAR_STORED_FIELDS);
        if (!(eventsStr.isEmpty() && condStr.isEmpty() && actionsStr.isEmpty() && saveSfStr.isEmpty() && clearSfStr.isEmpty())) {
            // TODO: Validation message here
            Activator.logWarning("Initial Transition: Initial transition needs the 'target' attribute to point to the real initial state. All other attributes will be ignored. If this state is meant to be the real initial state, use the 'initialState' element instead."); //$NON-NLS-1$
        }

        // Compile the target
        String targetStr = element.getAttribute(TmfXmlStrings.TARGET);
        if (targetStr.isEmpty()) {
            // TODO: Validation message here
            Activator.logError("Initial Transition: No target defined"); //$NON-NLS-1$
            return null;
        }
        TmfXmlFsmStateCu target = states.get(targetStr);
        if (target == null) {
            // TODO: Validation message here
            Activator.logError("Initial Transition: Undefined target state: " + targetStr); //$NON-NLS-1$
            return null;
        }

        return target;

    }

}
