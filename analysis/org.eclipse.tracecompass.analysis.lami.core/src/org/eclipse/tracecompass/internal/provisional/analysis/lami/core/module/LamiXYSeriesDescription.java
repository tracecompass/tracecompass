/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Jonathan Rajotte-Julien
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;

/**
 * Description of a series to use in LAMI charts.
 *
 * Contains the aspects to use on the X and Y axes (one per axis).
 *
 * @author Jonathan Rajotte-Julien
 */
public class LamiXYSeriesDescription {

    private final LamiTableEntryAspect fXAspect;
    private final LamiTableEntryAspect fYAspect;

    /**
     * Constructor
     *
     * @param xAspect
     *            The aspect to use on the X axis
     * @param yAspect
     *            The aspect to use on the Y axis
     */
    public LamiXYSeriesDescription(LamiTableEntryAspect xAspect, LamiTableEntryAspect yAspect) {
        fXAspect = xAspect;
        fYAspect = yAspect;
    }

    /**
     * Get the aspect corresponding to the X axis.
     *
     * @return The X-axis aspect
     */
    public LamiTableEntryAspect getXAspect() {
        return fXAspect;
    }

    /**
     * Get the aspect corresponding to the Y axis.
     *
     * @return The Y-axis aspect
     */
    public LamiTableEntryAspect getYAspect() {
        return fYAspect;
    }


    @Override
    public String toString() {
        return "x:" + fXAspect.getLabel() + " y:" + fYAspect.getLabel(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public int hashCode() {
        return Objects.hash(fXAspect, fYAspect);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LamiXYSeriesDescription other = (LamiXYSeriesDescription) obj;
        if (!fXAspect.equals(other.fXAspect)) {
            return false;
        }
        return (fYAspect.equals(other.fYAspect));
    }

}
