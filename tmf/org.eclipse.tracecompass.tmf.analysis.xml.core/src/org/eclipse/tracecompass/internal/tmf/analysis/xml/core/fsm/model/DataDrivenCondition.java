/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
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
public interface DataDrivenCondition extends IDataDrivenRuntimeObject {

    /**
     * A condition that returns always true
     */
    public static final DataDrivenCondition TRUE_CONDITION = (e, s, c) -> true;

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
     * Condition operators used to determine the position of a value wrt to a
     * given time range. It tests the operator with whether the value intersects
     * a time range or not
     */
    public enum TimeRangeOperator implements Predicate<Boolean> {
        /** value is inside the time range */
        IN(i -> i),
        /** Value is outside the time range */
        OUT(i -> !i);

        private final Function<Boolean, Boolean> fResultFunction;

        private TimeRangeOperator(Function<Boolean, Boolean> cmpFunction) {
            fResultFunction = cmpFunction;
        }

        @Override
        public boolean test(Boolean t) {
            return Objects.requireNonNull(fResultFunction.apply(t));
        }
    }

    /**
     * A condition comparing 2 values together
     */
    public static class DataDrivenComparisonCondition implements DataDrivenCondition {

        private final ConditionOperator fOperator;
        private final DataDrivenValue fFirstValue;
        private final DataDrivenValue fSecondValue;

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

        @Override
        public int hashCode() {
            return Objects.hash(fOperator, fFirstValue, fSecondValue);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof DataDrivenComparisonCondition)) {
                return false;
            }
            DataDrivenComparisonCondition other = (DataDrivenComparisonCondition) obj;
            return Objects.equals(fOperator, other.fOperator) &&
                    Objects.equals(fFirstValue, other.fFirstValue) &&
                    Objects.equals(fSecondValue, other.fSecondValue);
        }

        @Override
        public String toString() {
            return fFirstValue.toString() + ' ' + fOperator.toString() + ' ' + fSecondValue.toString();
        }

    }

    /**
     * A condition that verifies the relation of a time value with a given time
     * range
     */
    public static class DataDrivenTimeRangeCondition implements DataDrivenCondition {

        private final TimeRangeOperator fOperator;
        private final long fBegin;
        private final long fEnd;

        /**
         * Constructor
         *
         * @param operator
         *            The operator linking a time value to the time range
         * @param begin
         *            The start of the time range
         * @param end
         *            The end of the time range
         */
        public DataDrivenTimeRangeCondition(TimeRangeOperator operator, long begin, long end) {
            fOperator = operator;
            fBegin = begin;
            fEnd = end;
        }

        @Override
        public boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
            long ts = event.getTimestamp().toNanos();
            return fOperator.test(ts >= fBegin && ts <= fEnd);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fOperator, fBegin, fEnd);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof DataDrivenTimeRangeCondition)) {
                return false;
            }
            DataDrivenTimeRangeCondition other = (DataDrivenTimeRangeCondition) obj;
            return Objects.equals(fOperator, other.fOperator) &&
                    fBegin == other.fBegin &&
                    fEnd == other.fEnd;
        }

        @Override
        public String toString() {
            return "Time range conditions: " + fOperator.toString() + ' ' + fBegin + ',' + fEnd; //$NON-NLS-1$
        }

    }

    /**
     * A condition that verifies the value of an elapsed time according to an
     * operator
     */
    public static class DataDrivenElapsedTimeCondition implements DataDrivenCondition {

        private final ConditionOperator fOperator;
        private final String fReference;
        private final long fValue;

        /**
         * Constructor
         *
         * @param operator
         *            The operator to compare the elapsed time with
         * @param reference
         *            The reference state from which to start
         * @param value
         *            The value to compare elapsed time to
         */
        public DataDrivenElapsedTimeCondition(ConditionOperator operator, String reference, long value) {
            fOperator = operator;
            fReference = reference;
            fValue = value;
        }

        @Override
        public boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
            long ts = event.getTimestamp().toNanos();
            long referenceTs = scenarioInfo.getStateStartTime(container, fReference);
            if (referenceTs < 0) {
                // No elapsed time for the state, return false
                return false;
            }
            return fOperator.test(Long.compare(ts - referenceTs, fValue));
        }

        @Override
        public int hashCode() {
            return Objects.hash(fOperator, fReference, fValue);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof DataDrivenElapsedTimeCondition)) {
                return false;
            }
            DataDrivenElapsedTimeCondition other = (DataDrivenElapsedTimeCondition) obj;
            return Objects.equals(fOperator, other.fOperator) &&
                    Objects.equals(fReference, other.fReference) &&
                    fValue == other.fValue;
        }

        @Override
        public String toString() {
            return "Elapsed time condition: " + fOperator.toString() + ' ' + fReference + ',' + fValue; //$NON-NLS-1$
        }

    }

    /**
     * A condition negating another condition
     */
    public static class DataDrivenNotCondition implements DataDrivenCondition {

        private final DataDrivenCondition fCondition;

        /**
         * Constructor
         *
         * @param condition
         *            The condition to verify
         */
        public DataDrivenNotCondition(DataDrivenCondition condition) {
            fCondition = condition;
        }

        @Override
        public boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
            return !fCondition.test(event, scenarioInfo, container);
        }

        @Override
        public int hashCode() {
            return Objects.hash(DataDrivenNotCondition.class, fCondition);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof DataDrivenNotCondition)) {
                return false;
            }
            DataDrivenNotCondition other = (DataDrivenNotCondition) obj;
            return Objects.equals(fCondition, other.fCondition);
        }

        @Override
        public String toString() {
            return "NOT: " + fCondition.toString(); //$NON-NLS-1$
        }

    }

    /**
     * A condition verifying if all conditions are true
     */
    public static class DataDrivenAndCondition implements DataDrivenCondition {

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

        @Override
        public int hashCode() {
            return Objects.hash(DataDrivenAndCondition.class, fConditions);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof DataDrivenAndCondition)) {
                return false;
            }
            DataDrivenAndCondition other = (DataDrivenAndCondition) obj;
            return Objects.equals(fConditions, other.fConditions);
        }

        @Override
        public String toString() {
            return "AND: " + fConditions.toString(); //$NON-NLS-1$
        }

    }

    /**
     * A condition verifying if any conditions are true
     */
    public static class DataDrivenOrCondition implements DataDrivenCondition {

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

        @Override
        public int hashCode() {
            return Objects.hash(DataDrivenOrCondition.class, fConditions);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof DataDrivenOrCondition)) {
                return false;
            }
            DataDrivenOrCondition other = (DataDrivenOrCondition) obj;
            return Objects.equals(fConditions, other.fConditions);
        }

        @Override
        public String toString() {
            return "OR: " + fConditions.toString(); //$NON-NLS-1$
        }

    }

    /**
     * A condition that validates if a value corresponds to a regex pattern
     */
    public static class DataDrivenRegexCondition implements DataDrivenCondition {

        private final Pattern fPattern;
        private final DataDrivenValue fValue;

        /**
         * Constructor
         *
         * @param pattern
         *            The regex pattern to match
         * @param value
         *            The value to match with the pattern
         */
        public DataDrivenRegexCondition(Pattern pattern, DataDrivenValue value) {
            fPattern = pattern;
            fValue = value;
        }

        @Override
        public boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container) {
            Object value = fValue.getValue(event, ITmfStateSystem.ROOT_ATTRIBUTE, scenarioInfo, container);
            if (value == null) {
                return false;
            }
            return fPattern.matcher(String.valueOf(value)).matches();
        }

        @Override
        public int hashCode() {
            return Objects.hash(String.valueOf(fPattern), fValue);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof DataDrivenRegexCondition)) {
                return false;
            }
            DataDrivenRegexCondition other = (DataDrivenRegexCondition) obj;
            // Compare the equality of the pattern's string, as the pattern
            // object are not equal if the string is equal
            return Objects.equals(String.valueOf(fPattern), String.valueOf(other.fPattern)) &&
                    Objects.equals(fValue, other.fValue);
        }

        @Override
        public String toString() {
            return fValue.toString() + " matches " + fPattern; //$NON-NLS-1$
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
     * @return The result of this condition, so <code>true</code> if the
     *         condition validates, <code>false</code> otherwise
     */
    boolean test(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container);

}
