/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionConditional;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionList;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionSegment;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionStateChange;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionStateChange.StackAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValueConstant;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.w3c.dom.Element;

/**
 * The compilation unit for an XML state change element
 *
 * FIXME: When porting the XML pattern to these classes, see if state change can
 * be an action
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 */
public abstract class TmfXmlActionCu implements IDataDrivenCompilationUnit {

    /** A state change assigning a value to a path in the state system */
    private static class TmfXmlStateChangeAssignationCu extends TmfXmlActionCu {

        private TmfXmlStateSystemPathCu fPath;
        private TmfXmlStateValueCu fRightOperand;
        private boolean fIncrement;
        private boolean fUpdate;
        private StackAction fStackAction;
        private @Nullable TmfXmlStateValueCu fFutureTime;

        public TmfXmlStateChangeAssignationCu(TmfXmlStateSystemPathCu path,
                TmfXmlStateValueCu rightOperandCu, boolean increment, boolean update, StackAction stackAction, @Nullable TmfXmlStateValueCu futureTime) {
            fPath = path;
            fRightOperand = rightOperandCu;
            fIncrement = increment;
            fUpdate = update;
            fStackAction = stackAction;
            fFutureTime = futureTime;
        }

        @Override
        public DataDrivenAction generate() {
            DataDrivenStateSystemPath path = fPath.generate();
            DataDrivenValue rightOperand = fRightOperand.generate();
            DataDrivenValue futureTime = null;
            if (fFutureTime != null) {
                futureTime = fFutureTime.generate();
            }
            return new DataDrivenActionStateChange(path, rightOperand, fIncrement, fUpdate, fStackAction, futureTime);
        }

    }

    /** Conditional state change */
    private static class TmfXmlConditionalStateChangeCu extends TmfXmlActionCu {

        private final TmfXmlConditionCu fCondition;
        private final TmfXmlActionCu fThen;
        private final @Nullable TmfXmlActionCu fElse;

        public TmfXmlConditionalStateChangeCu(TmfXmlConditionCu condition, TmfXmlActionCu thenChange, @Nullable TmfXmlActionCu elseChange) {
            fCondition = condition;
            fThen = thenChange;
            fElse = elseChange;
        }

        @Override
        public DataDrivenAction generate() {
            DataDrivenCondition condition = fCondition.generate();
            DataDrivenAction thenChange = fThen.generate();
            DataDrivenAction elseChange = (fElse != null ? fElse.generate() : null);
            return new DataDrivenActionConditional(condition, thenChange, elseChange);
        }

    }

    /** Action list action */
    private static class TmfXmlActionListCu extends TmfXmlActionCu {

        private final List<TmfXmlActionCu> fActions;

        public TmfXmlActionListCu(List<TmfXmlActionCu> actions) {
            fActions = actions;
        }

        @Override
        public DataDrivenAction generate() {
            List<DataDrivenAction> actions = fActions.stream()
                    .map(TmfXmlActionCu::generate)
                    .collect(Collectors.toList());
            return new DataDrivenActionList(actions);
        }

    }

    /** No action */
    private static final TmfXmlActionCu NO_ACTION_CU = new TmfXmlActionCu() {

        @Override
        public DataDrivenAction generate() {
            return DataDrivenAction.NO_ACTION;
        }
    };

    /** Segment creation action */
    private static class TmfXmlActionSegmentCu extends TmfXmlActionCu {

        private final TmfXmlStateValueCu fType;
        private final @Nullable TmfXmlStateValueCu fStart;
        private final @Nullable TmfXmlStateValueCu fDuration;
        private final @Nullable TmfXmlStateValueCu fEnd;
        private final Map<String, TmfXmlStateValueCu> fFieldMap;

        public TmfXmlActionSegmentCu(TmfXmlStateValueCu segmentTypeValue, @Nullable TmfXmlStateValueCu beginTimeValue, @Nullable TmfXmlStateValueCu durationValue, @Nullable TmfXmlStateValueCu endTimeValue, Map<String, TmfXmlStateValueCu> fieldMap) {
            fType = segmentTypeValue;
            fStart = beginTimeValue;
            fDuration = durationValue;
            fEnd = endTimeValue;
            fFieldMap = fieldMap;
        }

        @Override
        public DataDrivenAction generate() {
            DataDrivenValue type = fType.generate();
            DataDrivenValue start = fStart != null ? fStart.generate() : null;
            DataDrivenValue end = fEnd != null ? fEnd.generate() : null;
            DataDrivenValue duration = fDuration != null ? fDuration.generate() : null;
            Map<String, DataDrivenValue> fields = new HashMap<>();
            for (Entry<String, TmfXmlStateValueCu> fieldEntry : fFieldMap.entrySet()) {
                fields.put(fieldEntry.getKey(), fieldEntry.getValue().generate());
            }
            return new DataDrivenActionSegment(type, start, duration, end, fields);
        }

    }

    @Override
    public abstract DataDrivenAction generate();

    /**
     * Compile a state change XML element
     *
     * @param analysisData
     *            The analysis data already compiled
     * @param stateChange
     *            The state change XML element to compile
     * @return The state change compilation unit or <code>null</code> if there was a
     *         compilation error
     */
    public static @Nullable TmfXmlActionCu compile(AnalysisCompilationData analysisData, Element stateChange) {
        /*
         * child nodes is either a list of TmfXmlStateAttributes and TmfXmlStateValues,
         * or an if-then-else series of nodes.
         */
        List<Element> childElements = TmfXmlUtils.getChildElements(stateChange, TmfXmlStrings.IF);
        if (childElements.size() == 1) {
            return compileConditionalChange(analysisData, stateChange, childElements.get(0));
        } else if (childElements.size() > 1) {
            // TODO: Validation message here
            Activator.logError("Conditional State Change: There should be only 1 if node"); //$NON-NLS-1$
            return null;
        }
        return compileAssignationChange(analysisData, stateChange);
    }

    /** Compile a conditional state change */
    private static @Nullable TmfXmlActionCu compileConditionalChange(AnalysisCompilationData analysisData, Element stateChange, Element ifNode) {
        // Compile the child of the IF node
        List<@Nullable Element> childElements = XmlUtils.getChildElements(ifNode);
        if (childElements.size() != 1) {
            // TODO: Validation message here
            Activator.logError("There should be only one element under this condition"); //$NON-NLS-1$
            return null;
        }
        Element subCondition = Objects.requireNonNull(childElements.get(0));
        TmfXmlConditionCu condition = TmfXmlConditionCu.compile(analysisData, subCondition);
        if (condition == null) {
            return null;
        }

        // Compile the then element
        List<Element> thenElements = TmfXmlUtils.getChildElements(stateChange, TmfXmlStrings.THEN);
        if (thenElements.size() != 1) {
            // TODO: Validation message here
            Activator.logError("Conditional State Change: There should be 1 and only 1 then element"); //$NON-NLS-1$
            return null;
        }
        TmfXmlActionCu thenChange = compile(analysisData, thenElements.get(0));
        if (thenChange == null) {
            return null;
        }

        // Compile the else element
        List<Element> elseElements = TmfXmlUtils.getChildElements(stateChange, TmfXmlStrings.ELSE);
        if (elseElements.isEmpty()) {
            return new TmfXmlConditionalStateChangeCu(condition, thenChange, null);
        }
        if (thenElements.size() != 1) {
            // TODO: Validation message here
            Activator.logError("Conditional State Change: There should be at most 1 else element"); //$NON-NLS-1$
            return null;
        }
        TmfXmlActionCu elseChange = compile(analysisData, elseElements.get(0));
        if (elseChange == null) {
            return null;
        }
        return new TmfXmlConditionalStateChangeCu(condition, thenChange, elseChange);
    }

    /** Compile an assignation state change */
    private static @Nullable TmfXmlActionCu compileAssignationChange(AnalysisCompilationData analysisContent, Element stateChange) {
        List<@NonNull Element> leftOperands = TmfXmlUtils.getChildElements(stateChange, TmfXmlStrings.STATE_ATTRIBUTE);
        List<@NonNull Element> rightOperands = TmfXmlUtils.getChildElements(stateChange, TmfXmlStrings.STATE_VALUE);
        List<@NonNull Element> futureTimes = TmfXmlUtils.getChildElements(stateChange, TmfXmlStrings.FUTURE_TIME);
        if (rightOperands.size() != 1) {
            // TODO: Validation message here
            Activator.logError("There should only be one state Value in this state change"); //$NON-NLS-1$
        }
        Element rightOperand = rightOperands.get(0);

        TmfXmlStateSystemPathCu path = TmfXmlStateSystemPathCu.compile(analysisContent, leftOperands);
        if (path == null) {
            return null;
        }
        if (futureTimes.size() > 1) {
            // TODO: Validation message here
            Activator.logError("There should at most one future time for this state change"); //$NON-NLS-1$
        }
        Element futureTime = futureTimes.isEmpty() ? null : futureTimes.get(0);
        TmfXmlStateValueCu futureTimeCu = null;
        if (futureTime != null) {
            futureTimeCu = TmfXmlStateValueCu.compileValue(analysisContent, futureTime);
            if (futureTimeCu == null) {
                return null;
            }
        }

        TmfXmlStateValueCu rightOperandCu = TmfXmlStateValueCu.compileValue(analysisContent, rightOperand);
        if (rightOperandCu == null) {
            return null;
        }

        /* Check if there is an increment for the value */
        boolean increment = Boolean.parseBoolean(rightOperand.getAttribute(TmfXmlStrings.INCREMENT));

        /* Check if this value is an update of the ongoing state */
        boolean update = Boolean.parseBoolean(rightOperand.getAttribute(TmfXmlStrings.UPDATE));

        /*
         * Stack Actions : allow to define a stack with PUSH/POP/PEEK methods
         */
        String stack = rightOperand.getAttribute(TmfXmlStrings.ATTRIBUTE_STACK);
        StackAction stackAction = DataDrivenActionStateChange.StackAction.getTypeFromString(stack);

        // Extra attribute validation
        // Update and increment are not valid for stack actions
        if (update && stackAction != StackAction.NONE) {
            Activator.logError("State change: Update cannot be done with stack action " + stackAction); //$NON-NLS-1$
            return null;
        }
        if (increment && stackAction != StackAction.NONE) {
            Activator.logError("State change: Increment cannot be done with stack action " + stackAction); //$NON-NLS-1$
            return null;
        }

        return new TmfXmlStateChangeAssignationCu(path, rightOperandCu, increment, update, stackAction, futureTimeCu);
    }

    /**
     * Compile a named action element, ie an action that contains an ID
     *
     * TODO: Return the action itself instead of the string when everything has
     * moved to new code path
     *
     * @param analysisData
     *            The analysis data already compiled
     * @param namedEl
     *            the XML element corresponding to the action
     * @return The action ID, or <code>null</code> if there was compilation
     *         errors
     */
    public static @Nullable String compileNamedAction(AnalysisCompilationData analysisData, Element namedEl) {
        String actionId = namedEl.getAttribute(TmfXmlStrings.ID);
        if (actionId.isEmpty()) {
            // TODO: Validation message here
            Activator.logError("The action should have an ID attribute"); //$NON-NLS-1$
            return null;
        }
        List<@Nullable Element> childElements = XmlUtils.getChildElements(namedEl);
        List<TmfXmlActionCu> actionList = new ArrayList<>();
        for (Element child : childElements) {
            final @NonNull Element nonNullChild = NonNullUtils.checkNotNull(child);
            switch (nonNullChild.getNodeName()) {
            case TmfXmlStrings.STATE_CHANGE:
            {
                TmfXmlActionCu action = compile(analysisData, nonNullChild);
                if (action == null) {
                    return null;
                }
                actionList.add(action);
            }
                break;
            case TmfXmlStrings.FSM_SCHEDULE_ACTION:
                // Do not do anything, this action is not supported yet
                // TODO: Validation message here
                Activator.logWarning("Action " + TmfXmlStrings.FSM_SCHEDULE_ACTION + " is not supported yet, it will have no effect"); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            case TmfXmlStrings.SEGMENT:
            {
                TmfXmlActionCu action = compileSegmentAction(analysisData, nonNullChild);
                if (action == null) {
                    return null;
                }
                actionList.add(action);
            }
                break;
            case TmfXmlStrings.ACTION:
            {
                // Compile the subaction
                String subActionId = compileNamedAction(analysisData, nonNullChild);
                if (subActionId == null) {
                    return null;
                }
                TmfXmlActionCu action = analysisData.getAction(subActionId);
                if (action == null) {
                    return null;
                }
                actionList.add(action);
            }
                break;
            default:
                // TODO: Validation message here
                Activator.logError("Invalid action type : " + nonNullChild.getNodeName()); //$NON-NLS-1$
            }
        }
        TmfXmlActionCu actionCu = createActionList(actionList);
        analysisData.addAction(actionId, actionCu);
        return actionId;
    }

    private static @Nullable TmfXmlActionCu compileSegmentAction(AnalysisCompilationData analysisData, Element node) {
        // Compile the segment type
        List<@NonNull Element> segmentType = TmfXmlUtils.getChildElements(node, TmfXmlStrings.SEGMENT_TYPE);
        if (segmentType.size() != 1) {
            // TODO: Validation message here
            Activator.logWarning("Segment action: There should be one and only one " + TmfXmlStrings.SEGMENT_TYPE + " element."); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }
        Element typeElement = Objects.requireNonNull(segmentType.get(0));
        String segmentName = typeElement.getAttribute(TmfXmlStrings.SEGMENT_NAME);
        TmfXmlStateValueCu segmentTypeValue = null;
        if (!segmentName.isEmpty()) {
            // Constant name for the segment type
            segmentTypeValue = new TmfXmlStateValueCu(() -> new DataDrivenValueConstant(null, ITmfStateValue.Type.NULL, segmentName));
        } else {
            List<Element> nameElements = TmfXmlUtils.getChildElements(typeElement, TmfXmlStrings.SEGMENT_NAME);
            if (nameElements.size() != 1) {
                // TODO: Validation message here
                Activator.logError("Segment type: You need to either give a segName attribute or define one and only one <segName> element with child <stateValue> to get the type name"); //$NON-NLS-1$
                return null;
            }
            Element segNameElement = Objects.requireNonNull(nameElements.get(0));
            List<Element> nameValueElements = TmfXmlUtils.getChildElements(segNameElement, TmfXmlStrings.STATE_VALUE);
            if (nameValueElements.size() != 1) {
                // TODO: Validation message here
                Activator.logError("Segment type: the <segName> element should have one and only one <stateValue> child"); //$NON-NLS-1$
                return null;
            }
            Element segNameValueElement = Objects.requireNonNull(nameValueElements.get(0));
            segmentTypeValue = TmfXmlStateValueCu.compileValue(analysisData, segNameValueElement);
        }
        if (segmentTypeValue == null) {
            return null;
        }

        // Compile the optional time element
        TmfXmlStateValueCu beginTimeValue = null;
        TmfXmlStateValueCu durationValue = null;
        TmfXmlStateValueCu endTimeValue = null;
        List<Element> timeElements = TmfXmlUtils.getChildElements(node, TmfXmlStrings.SEGMENT_TIME);
        if (timeElements.size() > 1) {
            // TODO: Validation message here
            Activator.logWarning("Segment time: there should be only one <segTime> element"); //$NON-NLS-1$
        }
        if (timeElements.size() > 0) {
            Element segTimeElement = Objects.requireNonNull(timeElements.get(0));
            List<Element> beginElements = TmfXmlUtils.getChildElements(segTimeElement, TmfXmlStrings.BEGIN);
            if (beginElements.isEmpty()) {
                // TODO: Validation message here
                Activator.logError("Segment time: there should be one <begin> element to describe segment start time"); //$NON-NLS-1$
                return null;
            }
            if (beginElements.size() > 1) {
                // TODO: Validation message here
                Activator.logWarning("Segment time: there should be only one <begin> element"); //$NON-NLS-1$
            }
            beginTimeValue = TmfXmlStateValueCu.compileValue(analysisData, Objects.requireNonNull(beginElements.get(0)));
            if (beginTimeValue == null) {
                return null;
            }
            beginTimeValue = new TmfXmlStateValueCu(new TmfXmlStateValueCu.ValueWrapperGenerator(beginTimeValue, ITmfStateValue.Type.LONG));
            // Validate end time or duration
            List<Element> durationElements = TmfXmlUtils.getChildElements(segTimeElement, TmfXmlStrings.DURATION);
            List<Element> endElements = TmfXmlUtils.getChildElements(segTimeElement, TmfXmlStrings.END);
            if (durationElements.isEmpty() && endElements.isEmpty()) {
                // TODO: Validation message here
                Activator.logError("Segment time: there should be either a <duration> or <end> element"); //$NON-NLS-1$
                return null;
            }
            if (!durationElements.isEmpty() && !endElements.isEmpty()) {
                // TODO: Validation message here
                Activator.logError("Segment time: only one of <duration> or <end> shoud be present"); //$NON-NLS-1$
                return null;
            }
            if (durationElements.size() > 1) {
                // TODO: Validation message here
                Activator.logWarning("Segment time: there should be only one <duration> element"); //$NON-NLS-1$
            }
            if (endElements.size() > 1) {
                // TODO: Validation message here
                Activator.logWarning("Segment time: there should be only one <end> element"); //$NON-NLS-1$
            }
            if (!durationElements.isEmpty()) {
                durationValue = TmfXmlStateValueCu.compileValue(analysisData, Objects.requireNonNull(durationElements.get(0)));
                if (durationValue == null) {
                    return null;
                }
                durationValue = new TmfXmlStateValueCu(new TmfXmlStateValueCu.ValueWrapperGenerator(durationValue, ITmfStateValue.Type.LONG));
            } else {
                endTimeValue = TmfXmlStateValueCu.compileValue(analysisData, Objects.requireNonNull(endElements.get(0)));
                if (endTimeValue == null) {
                    return null;
                }
                endTimeValue = new TmfXmlStateValueCu(new TmfXmlStateValueCu.ValueWrapperGenerator(endTimeValue, ITmfStateValue.Type.LONG));
            }
        }

        // Compile the segment fields
        List<Element> contentElements = TmfXmlUtils.getChildElements(node, TmfXmlStrings.SEGMENT_CONTENT);
        Map<String, TmfXmlStateValueCu> fieldMap = new HashMap<>();
        for (Element contentEl : contentElements) {
            List<Element> fieldElements = TmfXmlUtils.getChildElements(contentEl, TmfXmlStrings.SEGMENT_FIELD);
            for (Element fieldEl : fieldElements) {
                String name = fieldEl.getAttribute(TmfXmlStrings.NAME);
                if (name.isEmpty()) {
                    // TODO: Validation message here
                    Activator.logError("Segment field does not have a name"); //$NON-NLS-1$
                    return null;
                }
                if (fieldMap.containsKey(name)) {
                    // TODO: Validation message here
                    Activator.logError("Redefinition of field " + name); //$NON-NLS-1$
                    return null;
                }
                TmfXmlStateValueCu fieldCu = TmfXmlStateValueCu.compileSegmentField(analysisData, fieldEl);
                if (fieldCu == null) {
                    return null;
                }
                fieldMap.put(name, fieldCu);
            }
        }

        return new TmfXmlActionSegmentCu(segmentTypeValue, beginTimeValue, durationValue, endTimeValue, fieldMap);
    }

    /**
     * Create an action from a list
     *
     * @param actions
     *            The list of actions
     * @return The new action
     */
    public static TmfXmlActionCu createActionList(List<TmfXmlActionCu> actions) {
        if (actions.size() == 0) {
            // There are unsupported actions, so the action list may be empty
            return NO_ACTION_CU;
        }
        return actions.size() == 1 ? actions.get(0) : new TmfXmlActionListCu(actions);
    }

}
