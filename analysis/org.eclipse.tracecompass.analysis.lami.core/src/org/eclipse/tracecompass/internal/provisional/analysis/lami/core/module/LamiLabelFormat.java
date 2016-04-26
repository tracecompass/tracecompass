/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Map.Entry;


import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.BiMap;

/**
 * Format label based on a given Map<String, Integer>
 *
 * @author Jonathan Rajotte-Julien
 */
public class LamiLabelFormat extends Format {

    private static final long serialVersionUID = 4939553034329681316L;

    private static final String SWTCHART_EMPTY_LABEL = " "; //$NON-NLS-1$
    private static final String UNKNOWN_REPRESENTATION = "?"; //$NON-NLS-1$
    private final BiMap<@Nullable String, Integer> fMap;

    /**
     * Constructor
     *
     * @param map
     *            Map of indices to labels
     */
    public LamiLabelFormat(BiMap<@Nullable String, Integer> map) {
        super();
        fMap = map;
    }

    @Override
    public @Nullable StringBuffer format(@Nullable Object obj, @Nullable StringBuffer toAppendTo, @Nullable FieldPosition pos) {
        if (obj == null || toAppendTo == null) {
            return new StringBuffer(SWTCHART_EMPTY_LABEL);
        }

        Double doubleObj = (Double) obj;

        /*
         * Return a string buffer with a space in it since SWT does not like to
         * draw empty strings.
         */
        if ((doubleObj % 1 != 0) || !fMap.containsValue((doubleObj.intValue()))) {
            return new StringBuffer(SWTCHART_EMPTY_LABEL);
        }

        for (Entry<@Nullable String, Integer> entry : fMap.entrySet()) {
            /*
             * FIXME: Find if the elements are the same, based on their double
             * value, because SWTChart uses double values so we do the same
             * check. The loss of precision could lead to false positives.
             */
            if (Double.compare(entry.getValue().doubleValue(), doubleObj.doubleValue()) == 0) {
                if (entry.getKey() == null) {
                    return new StringBuffer(UNKNOWN_REPRESENTATION);
                }
                return toAppendTo.append(entry.getKey());
            }
        }
        return new StringBuffer(SWTCHART_EMPTY_LABEL);
    }

    @Override
    public @Nullable Object parseObject(@Nullable String source, @Nullable ParsePosition pos) {
        return fMap.get(source);
    }

}
