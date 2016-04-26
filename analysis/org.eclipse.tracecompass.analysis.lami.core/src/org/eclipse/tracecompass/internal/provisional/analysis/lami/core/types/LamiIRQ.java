/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Lami IRQ data type.
 *
 * @author Alexandre Montplaisir
 */
public class LamiIRQ extends LamiData {

    /**
     * IRQ type
     */
    public enum Type {
        /** Hardware IRQ */
        HARD,
        /** Software IRQ */
        SOFT
    }

    private final Type fType;
    private final int fNumber;
    private final @Nullable String fName;

    /**
     * Constructor
     *
     * @param irqType
     *            IRQ type
     * @param nb
     *            IRQ number
     * @param name
     *            IRQ name, null if not available
     */
    public LamiIRQ(Type irqType, int nb, @Nullable String name) {
        fType = irqType;
        fNumber = nb;
        fName = name;
    }

    /**
     * Get this IRQ's name. May be null if unavailable.
     *
     * @return The IRQ name
     */
    public @Nullable String getName() {
        return fName;
    }

    /**
     * Get this IRQ's type
     *
     * @return The IRQ type
     */
    public Type getType() {
        return fType;
    }

    /**
     * Get this IRQ's number.
     *
     * @return The IRQ number
     */
    public int getNumber() {
        return fNumber;
    }

    @Override
    public @Nullable String toString() {
        StringBuilder sb = new StringBuilder();
        switch (fType) {
        case SOFT:
            sb.append(Messages.LamiIRQ_SoftIRQ).append(' ');
            break;
        case HARD:
        default:
            sb.append(Messages.LamiIRQ_HardwareIRQ).append(' ');
            break;
        }

        sb.append(String.valueOf(fNumber));

        if (fName != null) {
            sb.append(" (" + fName + ')'); //$NON-NLS-1$
        }
        return sb.toString();
    }
}
