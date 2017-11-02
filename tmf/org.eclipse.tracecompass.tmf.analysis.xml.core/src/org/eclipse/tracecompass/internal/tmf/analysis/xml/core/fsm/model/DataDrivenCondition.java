/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.values.DataDrivenValue;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A data-driven condition.
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 */
public abstract class DataDrivenCondition implements IDataDrivenRuntimeObject {

    /**
     * Condition operators used to compare 2 values together
     */
    public enum ConditionOperator implements Predicate<Integer> {
        /** equal */
        EQ(i -> i == 0),
        /** not equal */
        NE(i -> i != 0),
        /** Greater or equal */
        GE(i -> i >= 0),
        /** Greater than */
        GT(i -> i > 0),
        /** Less or equal */
        LE(i -> i <= 0),
        /** Less than */
        LT(i -> i < 0);

        private final Function<Integer, Boolean> fCmpFunction;

        ConditionOperator(Function<Integer, Boolean> cmpFunction) {
            fCmpFunction = cmpFunction;
        }

        @Override
        public boolean test(Integer cmpValue) {
            return Objects.requireNonNull(fCmpFunction.apply(cmpValue));
        }
    }

    /**
     * A condition comparing 2 values together
     */
    public static class DataDrivenComparisonCondition extends DataDrivenCondition {

        private final DataDrivenValue fFirstValue;
        private final DataDrivenValue fSecondValue;
        private final ConditionOperator fOperator;

        /**
         * Constructor
         *
         * @param firstValue
         *            The first value to compare
         * @param secondValue
         *            The second value to compare
         * @param operator
         *            The comparison operator
         */
        public DataDrivenComparisonCondition(DataDrivenValue firstValue, DataDrivenValue secondValue, ConditionOperator operator) {
            fFirstValue = firstValue;
            fSecondValue = secondValue;
            fOperator = operator;
        }

        @Override
        public boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
            Object firstValue = fFirstValue.getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);
            Object secondValue = fSecondValue.getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);
            Integer cmpVal = null;
            if ((firstValue instanceof Integer) && (secondValue instanceof Number)) {
                cmpVal = ((Integer) firstValue).compareTo(((Number) secondValue).intValue());
            } else if ((firstValue instanceof Long) && (secondValue instanceof Number)) {
                cmpVal = ((Long) firstValue).compareTo(((Number) secondValue).longValue());
            } else {
                cmpVal = String.valueOf(firstValue).compareTo(String.valueOf(secondValue));
            }

            return fOperator.test(cmpVal);
        }

    }

    /**
     * A condition negating another condition
     */
    public static class TmfDdNotCondition extends DataDrivenCondition {

        private final DataDrivenCondition fCondition;

        /**
         * Constructor
         *
         * @param condition
         *            The condition to verify
         */
        public TmfDdNotCondition(DataDrivenCondition condition) {
            fCondition = condition;
        }

        @Override
        public boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
            return !fCondition.test(event, scenarioInfo, container);
        }

    }

    /**
     * A condition verifying if all conditions are true
     */
    public static class DataDrivenAndCondition extends DataDrivenCondition {

        private final List<DataDrivenCondition> fConditions;

        /**
         * Constructor
         *
         * @param conditions
         *            A list of conditions that must all be true
         */
        public DataDrivenAndCondition(List<DataDrivenCondition> conditions) {
            fConditions = conditions;
        }

        @Override
        public boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
            for (DataDrivenCondition cond : fConditions) {
                if (!cond.test(event, scenarioInfo, container)) {
                    return false;
                }
            }
            return true;
        }

    }

    /**
     * A condition verifying if any conditions are true
     */
    public static class DataDrivenOrCondition extends DataDrivenCondition {

        private final List<DataDrivenCondition> fConditions;

        /**
         * Constructor
         *
         * @param conditions
         *            A list of conditions to test
         */
        public DataDrivenOrCondition(List<DataDrivenCondition> conditions) {
            fConditions = conditions;
        }

        @Override
        public boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
            for (DataDrivenCondition cond : fConditions) {
                if (cond.test(event, scenarioInfo, container)) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * Handle the event, ie execute the actions if the event matches the name
     *
     * @param event
     *            The event to handle
     * @param scenarioInfo
     *            The scenario info
     * @param container
     *            The analysis data container
     * @return The result of this condition, so <code>true</code> if the condition
     *         validates, <code>false</code> otherwise
     */
    public abstract boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container);

}
