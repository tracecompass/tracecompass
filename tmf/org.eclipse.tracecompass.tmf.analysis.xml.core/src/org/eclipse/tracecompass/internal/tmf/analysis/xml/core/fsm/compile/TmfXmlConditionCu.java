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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition.ConditionOperator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenCondition.TimeRangeOperator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.w3c.dom.Element;

/**
 * The base compilation unit for XML tests and conditions
 *
 * @author Geneviève Bastien
 * @author Florian Wininger
 */
public abstract class TmfXmlConditionCu implements IDataDrivenCompilationUnit {

    /** Compararison condition */
    private static class TmfXmlCompareConditionCu extends TmfXmlConditionCu {

        private final TmfXmlStateValueCu fFirstValue;
        private final TmfXmlStateValueCu fSecondValue;
        private final ConditionOperator fOperator;

        TmfXmlCompareConditionCu(TmfXmlStateValueCu firstValue, TmfXmlStateValueCu secondValue, ConditionOperator conditionOperator) {
            fFirstValue = firstValue;
            fSecondValue = secondValue;
            fOperator = conditionOperator;
        }

        @Override
        public DataDrivenCondition generate() {
            return new DataDrivenCondition.DataDrivenComparisonCondition(fFirstValue.generate(), fSecondValue.generate(), fOperator);
        }

    }

    /** Compare time range */
    private static class TmfXmlTimeRangeConditionCu extends TmfXmlConditionCu {

        private final TimeRangeOperator fOperator;
        private final long fBegin;
        private final long fEnd;

        TmfXmlTimeRangeConditionCu(TimeRangeOperator operator, long begin, long end) {
            fOperator = operator;
            fBegin = begin;
            fEnd = end;
        }

        @Override
        public DataDrivenCondition generate() {
            return new DataDrivenCondition.DataDrivenTimeRangeCondition(fOperator, fBegin, fEnd);
        }

    }

    /** Compare time range */
    private static class TmfXmlElapsedTimeConditionCu extends TmfXmlConditionCu {

        private final ConditionOperator fOperator;
        private final String fReference;
        private final long fValue;

        TmfXmlElapsedTimeConditionCu(ConditionOperator operator, String reference, long end) {
            fOperator = operator;
            fReference = reference;
            fValue = end;
        }

        @Override
        public DataDrivenCondition generate() {
            return new DataDrivenCondition.DataDrivenElapsedTimeCondition(fOperator, fReference, fValue);
        }

    }

    /** NOT condition */
    private static class TmfXmlNotConditionCu extends TmfXmlConditionCu {

        private final TmfXmlConditionCu fCondition;

        TmfXmlNotConditionCu(TmfXmlConditionCu compile) {
            fCondition = compile;
        }

        @Override
        public DataDrivenCondition generate() {
            return new DataDrivenCondition.TmfDdNotCondition(fCondition.generate());
        }

    }

    /** AND condition */
    private static class TmfXmlAndConditionCu extends TmfXmlConditionCu {

        private final List<TmfXmlConditionCu> fConditions;

        TmfXmlAndConditionCu(List<TmfXmlConditionCu> childConditions) {
            fConditions = childConditions;
        }

        @Override
        public DataDrivenCondition generate() {
            List<DataDrivenCondition> conditions = fConditions.stream()
                    .map(TmfXmlConditionCu::generate)
                    .collect(Collectors.toList());
            return new DataDrivenCondition.DataDrivenAndCondition(conditions);
        }

    }

    /** OR condition */
    private static class TmfXmlOrConditionCu extends TmfXmlConditionCu {

        private final List<TmfXmlConditionCu> fConditions;

        TmfXmlOrConditionCu(List<TmfXmlConditionCu> childConditions) {
            fConditions = childConditions;
        }

        @Override
        public DataDrivenCondition generate() {
            List<DataDrivenCondition> conditions = fConditions.stream()
                    .map(TmfXmlConditionCu::generate)
                    .collect(Collectors.toList());
            return new DataDrivenCondition.DataDrivenOrCondition(conditions);
        }

    }

    @Override
    public abstract DataDrivenCondition generate();

    /**
     * @param analysisData
     *            The analysis data already compiled
     * @param conditionEl
     *            the XML element corresponding to the condition
     * @return The condition compilation unit or <code>null</code> if there was
     *         compilation error
     */
    public static @Nullable TmfXmlConditionCu compile(AnalysisCompilationData analysisData, Element conditionEl) {
        switch (conditionEl.getNodeName()) {
        case TmfXmlStrings.CONDITION:
            return compileSingleCondition(analysisData, conditionEl);
        case TmfXmlStrings.NOT: {
            List<@Nullable Element> childElements = XmlUtils.getChildElements(conditionEl);
            if (childElements.size() != 1) {
                Activator.logError("Compiling condition: NOT condition must have 1 and only 1 child"); //$NON-NLS-1$
                return null;
            }
            Element element = Objects.requireNonNull(childElements.get(0));
            TmfXmlConditionCu compile = compile(analysisData, element);
            return (compile == null ? null : new TmfXmlNotConditionCu(compile));
        }
        case TmfXmlStrings.AND: {
            List<TmfXmlConditionCu> childConditions = getCompiledChildConditions(analysisData, conditionEl);
            return (childConditions == null ? null : new TmfXmlAndConditionCu(childConditions));
        }
        case TmfXmlStrings.OR: {
            List<TmfXmlConditionCu> childConditions = getCompiledChildConditions(analysisData, conditionEl);
            return (childConditions == null ? null : new TmfXmlOrConditionCu(childConditions));
        }
        default:
            Activator.logError("Xml condition: Unsupported condition type: " + conditionEl.getNodeName()); //$NON-NLS-1$
        }
        return null;
    }

    private static @Nullable List<TmfXmlConditionCu> getCompiledChildConditions(AnalysisCompilationData analysisData, Element conditionEl) {
        List<@Nullable Element> childElements = XmlUtils.getChildElements(conditionEl);
        if (childElements.isEmpty()) {
            Activator.logError("Compiling condition: AND and OR condition must have at least 1 element"); //$NON-NLS-1$
            return null;
        }
        List<TmfXmlConditionCu> childConditions = new ArrayList<>();
        for (Element element : childElements) {
            TmfXmlConditionCu condition = compile(analysisData, Objects.requireNonNull(element));
            if (condition == null) {
                return null;
            }
            childConditions.add(condition);
        }
        return childConditions;
    }

    private static @Nullable TmfXmlConditionCu compileSingleCondition(AnalysisCompilationData analysisData, Element conditionEl) {
        // Is the condition a comparison condition?
        if (conditionEl.getElementsByTagName(TmfXmlStrings.STATE_VALUE).getLength() > 0) {
            return compileValueCondition(analysisData, conditionEl);
        }
        // Compile a time range condition
        List<Element> childElements = TmfXmlUtils.getChildElements(conditionEl, TmfXmlStrings.TIME_RANGE);
        if (childElements.size() == 1) {
            return compileTimeRangeCondition(childElements.get(0));
        }
        // Compile an elapsed time condition
        childElements = TmfXmlUtils.getChildElements(conditionEl, TmfXmlStrings.ELAPSED_TIME);
        if (childElements.size() == 1) {
            return compileElapsedTimeCondition(childElements.get(0));
        }
        return null;
    }

    private static @Nullable TmfXmlConditionCu compileElapsedTimeCondition(Element element) {
        String unit = element.getAttribute(TmfXmlStrings.UNIT);
        List<@Nullable Element> childElements = XmlUtils.getChildElements(element);
        if (childElements.size() != 1) {
            Activator.logError("Invalid timestampsChecker declaration in XML : Only one timing condition is allowed"); //$NON-NLS-1$
            return null;
        }
        final Element firstElement = NonNullUtils.checkNotNull(childElements.get(0));
        String type = firstElement.getNodeName();
        ConditionOperator operator;
        switch (type) {
        case TmfXmlStrings.LESS:
            operator = ConditionOperator.LT;
            break;
        case TmfXmlStrings.EQUAL:
            operator = ConditionOperator.EQ;
            break;
        case TmfXmlStrings.MORE:
            operator = ConditionOperator.GT;
            break;
        default:
            Activator.logError("ElapsedTimeChecker: Invalid operator: " + type); //$NON-NLS-1$
            return null;
        }
        final String reference = firstElement.getAttribute(TmfXmlStrings.SINCE);
        final String valueStr = firstElement.getAttribute(TmfXmlStrings.VALUE);
        try {
            long value = valueToNanoseconds(Long.parseLong(valueStr), unit);
            return new TmfXmlElapsedTimeConditionCu(operator, reference, value);
        } catch (NumberFormatException e) {
            Activator.logError("Invalid value for elapsed time: " + e.getMessage()); //$NON-NLS-1$
            return null;
        }
    }

    private static @Nullable TmfXmlConditionCu compileTimeRangeCondition(Element element) {
        String unit = element.getAttribute(TmfXmlStrings.UNIT);

        List<@Nullable Element> childElements = NonNullUtils.checkNotNull(XmlUtils.getChildElements(element));
        if (childElements.size() != 1) {
            Activator.logError("Invalid timestampsChecker declaration in XML : Only one timing condition is allowed"); //$NON-NLS-1$
            return null;
        }
        final Element firstElement = NonNullUtils.checkNotNull(childElements.get(0));
        TimeRangeOperator operator;
        String type = firstElement.getNodeName();
        switch (type) {
        case TmfXmlStrings.IN:
            operator = TimeRangeOperator.IN;
            break;
        case TmfXmlStrings.OUT:
            operator = TimeRangeOperator.OUT;
            break;
        default:
            Activator.logError("TimeRangeChecker: Invalid operator: " + type); //$NON-NLS-1$
            return null;
        }

        final String beginStr = firstElement.getAttribute(TmfXmlStrings.BEGIN);
        final String endStr = firstElement.getAttribute(TmfXmlStrings.END);
        try {
            long begin = valueToNanoseconds(Long.parseLong(beginStr), unit);
            long end = valueToNanoseconds(Long.parseLong(endStr), unit);

            return new TmfXmlTimeRangeConditionCu(operator, begin, end);
        } catch (NumberFormatException e) {
            Activator.logError("Invalid value for time range: " + e.getMessage()); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Normalize the value into a nanosecond time value
     *
     * @param timestamp
     *            The timestamp value
     * @param unit
     *            The initial unit of the timestamp
     * @return The value of the timestamp in nanoseconds
     */
    public static long valueToNanoseconds(long timestamp, String unit) {
        switch (unit) {
        case TmfXmlStrings.NS:
            return timestamp;
        case TmfXmlStrings.US:
            return TmfTimestamp.create(timestamp, ITmfTimestamp.MICROSECOND_SCALE).toNanos();
        case TmfXmlStrings.MS:
            return TmfTimestamp.create(timestamp, ITmfTimestamp.MILLISECOND_SCALE).toNanos();
        case TmfXmlStrings.S:
            return TmfTimestamp.create(timestamp, ITmfTimestamp.SECOND_SCALE).toNanos();
        default:
            throw new IllegalArgumentException("The time unit is not yet supporting."); //$NON-NLS-1$
        }
    }

    private static @Nullable TmfXmlConditionCu compileValueCondition(AnalysisCompilationData analysisData, Element conditionEl) {
        ConditionOperator conditionOperator = getConditionOperator(conditionEl);
        // Compile the case with 2 state values
        List<Element> childElements = TmfXmlUtils.getChildElements(conditionEl, TmfXmlStrings.STATE_VALUE);
        if (childElements.size() == 2) {
            TmfXmlStateValueCu firstValue = TmfXmlStateValueCu.compileValue(analysisData, childElements.get(0));
            TmfXmlStateValueCu secondValue = TmfXmlStateValueCu.compileValue(analysisData, childElements.get(1));
            if (firstValue == null || secondValue == null) {
                return null;
            }
            return new TmfXmlCompareConditionCu(firstValue, secondValue, conditionOperator);
        }
        // Compile the case with first element being a stateAttribute or event field
        if (childElements.size() == 1) {
            TmfXmlStateValueCu secondValue = TmfXmlStateValueCu.compileValue(analysisData, childElements.get(0));
            TmfXmlStateValueCu firstValue = null;
            List<Element> attributes = TmfXmlUtils.getChildElements(conditionEl, TmfXmlStrings.STATE_ATTRIBUTE);
            if (!attributes.isEmpty()) {
                // The first value is an array of state attributes
                TmfXmlStateSystemPathCu path = TmfXmlStateSystemPathCu.compile(analysisData, attributes);
                if (path == null) {
                    return null;
                }
                firstValue = TmfXmlStateValueCu.compileAsQuery(path);
            } else {
                // The first value is an event field
                attributes = TmfXmlUtils.getChildElements(conditionEl, TmfXmlStrings.ELEMENT_FIELD);
                if (attributes.size() != 1) {
                    Activator.logError("Condition: There should be either 2 state values or 1 attribute or field and 1 state value"); //$NON-NLS-1$
                    return null;
                }
                firstValue = TmfXmlStateValueCu.compileField(analysisData, attributes.get(0));
            }
            if (firstValue == null || secondValue == null) {
                return null;
            }
            return new TmfXmlCompareConditionCu(firstValue, secondValue, conditionOperator);
        }
        return null;
    }

    private static DataDrivenCondition.ConditionOperator getConditionOperator(Element rootNode) {
        String equationType = rootNode.getAttribute(TmfXmlStrings.OPERATOR);
        switch (equationType) {
        case TmfXmlStrings.EQ:
            return ConditionOperator.EQ;
        case TmfXmlStrings.NE:
            return ConditionOperator.NE;
        case TmfXmlStrings.GE:
            return ConditionOperator.GE;
        case TmfXmlStrings.GT:
            return ConditionOperator.GT;
        case TmfXmlStrings.LE:
            return ConditionOperator.LE;
        case TmfXmlStrings.LT:
            return ConditionOperator.LT;
        case TmfXmlStrings.NULL:
            return ConditionOperator.EQ;
        default:
            throw new IllegalArgumentException("TmfXmlCondition: invalid comparison operator."); //$NON-NLS-1$
        }
    }

}
