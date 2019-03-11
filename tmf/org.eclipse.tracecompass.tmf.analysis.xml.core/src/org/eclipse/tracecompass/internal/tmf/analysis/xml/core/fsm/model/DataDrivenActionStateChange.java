/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * An action that assigns a value to a path in a state system
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 */
public class DataDrivenActionStateChange implements DataDrivenAction {

    /**
     * An enumeration of possible actions on stack
     */
    public enum StackAction {
        /** No action */
        NONE,
        /** Push a value to the stack */
        PUSH,
        /** Peek the top of a stack */
        PEEK,
        /** Pop a value from a stack */
        POP,
        /** Pops all the values from the stack */
        POP_ALL;

        /**
         * Get the type stack value corresponding to a string
         *
         * @param input
         *            The string to match to a value
         * @return The ValueTypeStack value
         */
        public static StackAction getTypeFromString(String input) {
            switch (input) {
            case TmfXmlStrings.STACK_PUSH:
                return PUSH;
            case TmfXmlStrings.STACK_POP:
                return POP;
            case TmfXmlStrings.STACK_POPALL:
                return POP_ALL;
            case TmfXmlStrings.STACK_PEEK:
                return PEEK;
            default:
                return NONE;
            }
        }
    }

    private final DataDrivenValue fRightOperand;
    private final DataDrivenStateSystemPath fLeftOperand;
    // private final List<DataDrivenValue> fLeftOperand;
    private final boolean fUpdate;
    private final boolean fIncrement;
    private final StackAction fStackAction;
    private final @Nullable DataDrivenValue fFutureTime;

    /**
     * Constructor
     *
     * @param leftOperand
     *            The left operands (path in the state system)
     * @param rightOperand
     *            The right operand, ie the value to assign
     * @param increment
     *            If <code>true</code>, the value of the right operand will be added
     *            to the preceding value
     * @param update
     *            If <code>true</code>, the resulting value will be updated and will
     *            not create a state change, otherwise, the value will be modified
     *            and a new state will start at the time of the event.
     * @param stackAction
     *            The action to perform on a stack
     * @param futureTime
     *            The state value for the future time. If the state change happens
     *            at the time of the event, this value should be null
     */
    public DataDrivenActionStateChange(DataDrivenStateSystemPath leftOperand, DataDrivenValue rightOperand, boolean increment, boolean update, StackAction stackAction, @Nullable DataDrivenValue futureTime) {
        fRightOperand = rightOperand;
        fLeftOperand = leftOperand;
        fUpdate = update;
        fIncrement = increment;
        fStackAction = stackAction;
        fFutureTime = futureTime;
    }

    @Override
    public void eventHandle(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
        int quark = ITmfStateSystem.ROOT_ATTRIBUTE;
        ITmfStateSystem stateSystem = container.getStateSystem();
        if (!(stateSystem instanceof ITmfStateSystemBuilder)) {
            throw new IllegalStateException("With state changes, the state system should be in building mode"); //$NON-NLS-1$
        }
        ITmfStateSystemBuilder ssb = (ITmfStateSystemBuilder) stateSystem;

        // Get the quark that will be changed
        quark = fLeftOperand.getQuark(event, quark, scenarioInfo, container);
        if (quark < 0) {
            return;
        }

        // Get the value
        Object assignVal = fRightOperand.getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);

        long timestamp = event.getTimestamp().toNanos();
        // Perform the stack action if applicable and return
        switch (fStackAction) {
        case POP:
            ssb.popAttribute(timestamp, quark);
            return;
        case PUSH:
            ssb.pushAttribute(timestamp, assignVal, quark);
            return;
        case POP_ALL:
            // The stack state will contain the number of elements on the stack
            Object stackDepth = ssb.queryOngoing(quark);
            if (stackDepth instanceof Integer) {
                int nbElements = ((Integer) stackDepth).intValue();
                for (int i = 0; i < nbElements; i++) {
                    ssb.popAttribute(timestamp, quark);
                }
            }
            return;
        case NONE:
        case PEEK:
        default:
            break;
        }

        // Increment if necessary
        if (fIncrement && assignVal != null) {
            assignVal = incrementByType(quark, ssb, assignVal);
        }
        // Should this state change be done at a future time?
        long scTime = timestamp;
        if (fFutureTime != null) {
            Object futureTimeObj = fFutureTime.getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);
            if (futureTimeObj instanceof Number) {
                scTime = ((Number) futureTimeObj).longValue();
            }
        }

        // If update is requested, do update and return
        if (fUpdate) {
            ssb.updateOngoingState(TmfXmlUtils.newTmfStateValueFromObject(assignVal), quark);
        } else if (scTime == timestamp) {
            ssb.modifyAttribute(timestamp, assignVal, quark);
        } else if (scTime > timestamp) {
            container.addFutureState(scTime, assignVal, quark);
        } else {
            Activator.logWarning("No state change occurred for event " + event + " and state change " + this + ". The time of the change is invalid: " + scTime);  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private static @Nullable Object incrementByType(int quark, ITmfStateSystem ss, Object stateValue) {
        if (stateValue instanceof Long) {
            Long incrementLong = (Long) stateValue;
            Object currentState = ss.queryOngoing(quark);
            Long currentValue = (currentState == null ? 0 : (Long) currentState);
            return incrementLong + currentValue;
        }
        if (stateValue instanceof Integer) {
            Integer incrementLong = (Integer) stateValue;
            Object currentState = ss.queryOngoing(quark);
            Integer currentValue = (currentState == null ? 0 : (Integer) currentState);
            return incrementLong + currentValue;
        }
        return stateValue;
    }

}
