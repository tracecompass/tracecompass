/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core;

import org.eclipse.tracecompass.tmf.core.util.Pair;

/**
 * Pair of Enum value name and its long value.
 *
 * @author Bernd Hufmann
 */
public class CtfEnumPair extends Pair<String, Long> {

    /**
     * Constructs a CtfEnumPair
     *
     * @param strValue
     *                  The first parameter of the pair (String)
     * @param longValue
     *                  The second parameter of the pair (Long)
     */
    public CtfEnumPair(String strValue, Long longValue) {
        super(strValue, longValue);
    }

    /**
     * Returns the String value of the Enum.
     *
     * @return the string value
     */
    public String getStringValue() {
        return getFirst();
    }

    /**
     * Returns the long value of the Enum.
     *
     * @return the Long value
     */
    public Long getLongValue() {
        return getSecond();
    }

    @Override
    public String toString() {
        return getFirst();
    }
}
