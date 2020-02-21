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

package org.eclipse.tracecompass.internal.tmf.chart.ui.data;

import java.util.Collection;

import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DescriptorTypeVisitor;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.DescriptorTypeVisitor.DescriptorType;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.descriptor.IDataChartDescriptor;

/**
 * This class keeps informations about a group of descriptors.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class DescriptorsInformation {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    private final boolean fAreDescriptorsNumerical;
    private final boolean fAreDescriptorsDuration;
    private final boolean fAreDescriptorsTimestamp;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Factory to create the descriptors information
     *
     * @param descriptors
     *            A collection of descriptors to check
     * @return The descriptors information
     */
    public static DescriptorsInformation create(Collection<IDataChartDescriptor<?, ?>> descriptors) {
        /* Visit each descriptor for checking if they share the same type */
        DescriptorTypeVisitor visitor = new DescriptorTypeVisitor();
        descriptors.forEach(desc -> desc.accept(visitor));

        /* Make sure there was at least one descriptor */
        if (visitor.isEmpty()) {
            throw new IllegalArgumentException("No descriptor were given."); //$NON-NLS-1$
        }

        /* Make sure each descriptor have the same type */
        if (visitor.isMixed()) {
            throw new IllegalArgumentException("Each descriptor must be the same type."); //$NON-NLS-1$
        }

        /* Check what are the type of the descriptors */
        if (visitor.isIndividualType(DescriptorType.NUMERICAL)) {

            if (visitor.isIndividualType(DescriptorType.DURATION)) {
                return new DescriptorsInformation(true, true, false);
            } else if (visitor.isIndividualType(DescriptorType.TIMESTAMP)) {
                return new DescriptorsInformation(true, false, true);
            } else {
                return new DescriptorsInformation(true, false, false);
            }
        }
        return new DescriptorsInformation(false, false, false);
    }

    /**
     * Constructor.
     *
     * @param descriptors
     *            A collection of descriptors to check
     */
    private DescriptorsInformation(boolean numerical, boolean durations, boolean timestamps) {
            fAreDescriptorsNumerical = numerical;
            fAreDescriptorsDuration = durations;
            fAreDescriptorsTimestamp = timestamps;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Accessor that returns whether all descriptors are numericals or not.
     *
     * @return A boolean
     */
    public boolean areNumerical() {
        return fAreDescriptorsNumerical;
    }

    /**
     * Accessor that returns whether all descriptors are time durations or not.
     *
     * @return A boolean
     */
    public boolean areDuration() {
        return fAreDescriptorsDuration;
    }

    /**
     * Accessor that returns whether all descriptors are timestamps or not.
     *
     * @return A boolean
     */
    public boolean areTimestamp() {
        return fAreDescriptorsTimestamp;
    }

}
