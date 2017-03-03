/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.w3c.dom.Element;

/**
 * This Class implements a timestamp condition tree in the XML-defined state
 * system.
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlTimestampCondition implements ITmfXmlCondition {

    private enum TimeRangeOperator {
        IN,
        OUT,
        OTHER
    }

    private enum ElapsedTimeOperator {
        LESS,
        EQUAL,
        MORE,
        NONE
    }

    private final IXmlTimestampsCondition fTimestampsCondition;
    private final IXmlStateSystemContainer fParent;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this timestamp condition
     * @param container
     *            The state system container this timestamp condition belongs to
     */
    public TmfXmlTimestampCondition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        fParent = container;
        String type = node.getNodeName();
        switch (type) {
        case TmfXmlStrings.TIME_RANGE:
            fTimestampsCondition = new TmfXmlTimeRangeCondition(modelFactory, node, fParent);
            break;
        case TmfXmlStrings.ELAPSED_TIME:
            fTimestampsCondition = new TmfXmlElapsedTimeCondition(modelFactory, node, fParent);
            break;
        default:
            throw new IllegalArgumentException("Invalid timestampsChecker declaration in XML : Type should be timeRange or elapsedTime"); //$NON-NLS-1$
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

    /**
     * Test if two long value have the same sign
     *
     * @param i
     *            The first long
     * @param j
     *            The second long
     * @return True if the two long value have the same sign, false otherwise
     */
    private static boolean compareSign(long i, long j) {
        return (i < 0) ^ (j >= 0);
    }

    /**
     * Validate the event
     *
     * @param event
     *            The current event
     * @param scenarioInfo
     *            The active scenario details. The value should be null if there
     *            is no scenario
     * @return True if the test succeed, false otherwise
     */
    @Override
    public boolean test(ITmfEvent event,@Nullable TmfXmlScenarioInfo scenarioInfo) {
        return fTimestampsCondition.test(event, scenarioInfo);
    }

    private interface IXmlTimestampsCondition extends ITmfXmlCondition {
    }

    private class TmfXmlTimeRangeCondition implements IXmlTimestampsCondition {

        private final TimeRangeOperator fType;
        private final String fUnit;
        private final String fBegin;
        private final String fEnd;
        private final IXmlStateSystemContainer fContainer;

        /**
         * Constructor
         *
         * @param modelFactory
         *            The factory used to create XML model elements
         * @param node
         *            The XML root of this time range condition transition
         * @param container
         *            The state system container this time range condition
         *            belongs to
         */
        public TmfXmlTimeRangeCondition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
            fContainer = container;
            String unit = node.getAttribute(TmfXmlStrings.UNIT);
            fUnit = unit;
            List<@Nullable Element> childElements = NonNullUtils.checkNotNull(XmlUtils.getChildElements(node));
            if (childElements.size() != 1) {
                throw new IllegalArgumentException("Invalid timestampsChecker declaration in XML : Only one timing condition is allowed"); //$NON-NLS-1$
            }
            final Element firstElement = NonNullUtils.checkNotNull(childElements.get(0));
            String type = firstElement.getNodeName();
            switch (type) {
            case TmfXmlStrings.IN:
                fType = TimeRangeOperator.IN;
                break;
            case TmfXmlStrings.OUT:
                fType = TimeRangeOperator.OUT;
                break;
            default:
                fType = TimeRangeOperator.OTHER;
                break;
            }

            final String begin = firstElement.getAttribute(TmfXmlStrings.BEGIN);
            final String end = firstElement.getAttribute(TmfXmlStrings.END);
            fBegin = begin;
            fEnd = end;
        }

        @Override
        public boolean test(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) {
            ITmfStateSystem ss = fContainer.getStateSystem();

            long begin;
            begin = valueToNanoseconds(Long.parseLong(fBegin), fUnit);

            long end;
            end = valueToNanoseconds(Long.parseLong(fEnd), fUnit);

            // swap the value if begin > end
            if (begin > end) {
                begin = begin ^ end;
                end = begin ^ end;
                begin = begin ^ end;
            }

            begin = Math.max(ss.getStartTime(), begin);
            end = Math.min(ss.getCurrentEndTime(), end);
            begin = Math.min(begin, end);

            long ts = event.getTimestamp().toNanos();
            switch (fType) {
            case IN:
                return intersects(begin, end, ts);
            case OUT:
                return !intersects(begin, end, ts);
            case OTHER:
            default:
                return false;
            }
        }

        private boolean intersects(long begin, long end, long ts) {
            return ts >= begin && ts <= end;
        }

    }

    private class TmfXmlElapsedTimeCondition implements IXmlTimestampsCondition {

        private final IXmlStateSystemContainer fContainer;
        private final ElapsedTimeOperator fType;
        private final String fUnit;
        private final String fValue;
        private final String fReferenceState;

        /**
         * Constructor
         *
         * @param modelFactory
         *            The factory used to create XML model elements
         * @param node
         *            The XML root of this elapsed time condition
         * @param container
         *            The state system container this elapsed time condition
         *            belongs to
         */
        public TmfXmlElapsedTimeCondition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
            fContainer = container;
            String unit = node.getAttribute(TmfXmlStrings.UNIT);
            fUnit = unit;
            List<@Nullable Element> childElements = XmlUtils.getChildElements(node);
            if (childElements.size() != 1) {
                throw new IllegalArgumentException("Invalid timestampsChecker declaration in XML : Only one timing condition is allowed"); //$NON-NLS-1$
            }
            final Element firstElement = NonNullUtils.checkNotNull(childElements.get(0));
            String type = firstElement.getNodeName();
            switch (type) {
            case TmfXmlStrings.LESS:
                fType = ElapsedTimeOperator.LESS;
                break;
            case TmfXmlStrings.EQUAL:
                fType = ElapsedTimeOperator.EQUAL;
                break;
            case TmfXmlStrings.MORE:
                fType = ElapsedTimeOperator.MORE;
                break;
            default:
                fType = ElapsedTimeOperator.NONE;
                break;
            }
            final String reference = firstElement.getAttribute(TmfXmlStrings.SINCE);
            final String value = firstElement.getAttribute(TmfXmlStrings.VALUE);
            fReferenceState = reference;
            fValue = value;
        }

        @Override
        public boolean test(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) {
            if (scenarioInfo == null) {
                Activator.logError("Elapse time conditions require scenarioInfos and scenarioInfos is null"); //$NON-NLS-1$
                return false;
            }
            boolean success;
            long ts = event.getTimestamp().toNanos();
            long referenceTimestamps = ((XmlPatternStateProvider) fContainer).getHistoryBuilder().getSpecificStateStartTime(fContainer, fReferenceState, scenarioInfo, event);
            if (!compareSign(ts, referenceTimestamps) || ts < referenceTimestamps) {
                throw new IllegalArgumentException("Timestamp is inferior to reference time"); //$NON-NLS-1$
            }
            switch (fType) {
            case LESS:
                success = (ts - referenceTimestamps) < valueToNanoseconds(Long.parseLong(fValue), fUnit);
                break;
            case EQUAL:
                success = (ts - referenceTimestamps) == valueToNanoseconds(Long.parseLong(fValue), fUnit);
                break;
            case MORE:
                success = (ts - referenceTimestamps) > valueToNanoseconds(Long.parseLong(fValue), fUnit);
                break;
            case NONE:
            default:
                success = false;
                break;
            }
            return success;
        }

    }
}
