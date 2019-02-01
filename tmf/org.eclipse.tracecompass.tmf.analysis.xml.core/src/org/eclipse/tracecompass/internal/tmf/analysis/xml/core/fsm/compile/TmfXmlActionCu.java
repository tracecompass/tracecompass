/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionConditional;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionStateChange;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenActionStateChange.StackAction;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenStateSystemPath;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
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

}
