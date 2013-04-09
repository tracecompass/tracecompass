/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.util.Pair;

/**
 * Pair of Enum value name and its long value.
 *
 * @author Bernd Hufmann
 * @since 2.0
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
