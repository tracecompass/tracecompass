/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor;

import java.util.EnumSet;

/**
 * Visitor used for determining the type of multiple descriptors. In order to
 * get the type of a set of descriptors, it must visit all of them.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DescriptorTypeVisitor implements IDescriptorVisitor {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    /**
     * Enumeration for determining what kind of descriptors is inside.
     */
    public enum DescriptorType {
        /**
         * Value when the descriptors are numerical.
         */
        NUMERICAL,
        /**
         * Value when the descriptors are durations.
         */
        DURATION,
        /**
         * Value when the descriptors are timestamps.
         */
        TIMESTAMP,
        /**
         * Value when the descriptors are strings.
         */
        STRING;

        /**
         * This method check if the type is considered numerical of not.
         *
         * @return {@code true} if the type is numerical, else {@code false}
         */
        public boolean isNumerical() {
            return (this == DescriptorType.NUMERICAL ||
                    this == DescriptorType.DURATION ||
                    this == DescriptorType.TIMESTAMP);
        }
    }

    private final EnumSet<DescriptorType> fType = EnumSet.noneOf(DescriptorType.class);

    // ------------------------------------------------------------------------
    // Overriden methods
    // ------------------------------------------------------------------------

    @Override
    public void visit(DataChartStringDescriptor<?> desc) {
        fType.add(DescriptorType.STRING);
    }

    @Override
    public void visit(DataChartNumericalDescriptor<?, ? extends Number> desc) {
        fType.add(DescriptorType.NUMERICAL);
    }

    @Override
    public void visit(DataChartDurationDescriptor<?, ? extends Number> desc) {
        fType.add(DescriptorType.DURATION);
    }

    @Override
    public void visit(DataChartTimestampDescriptor<?> desc) {
        fType.add(DescriptorType.TIMESTAMP);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * This method checks if it has not visited a descriptor yet.
     *
     * @return {@code true} if it has not visited a descriptor, else
     *         {@code false}
     */
    public boolean isEmpty() {
        return fType.isEmpty();
    }

    /**
     * This method checks if it has visited multiple types of descriptor.
     *
     * @return {@code true} if it has visited multiple types of descriptor, else
     *         {@code false}
     */
    public boolean isMixed() {
        return fType.size() > 1;
    }

    /**
     * This method checks if all the visited descriptors are of a certain type.
     *
     * @param type
     *            The type to check
     * @return {@code true} if all the descriptors are of the same type, else
     *         {@code false}
     */
    public boolean isIndividualType(DescriptorType type) {
        if (isEmpty() || isMixed()) {
            return false;
        }

        /* Check if the actual type can be considered numerical */
        DescriptorType actual = fType.iterator().next();
        if (type == DescriptorType.NUMERICAL) {
            return actual.isNumerical();
        }

        return fType.contains(type);
    }

}
