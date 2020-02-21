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

package org.eclipse.tracecompass.analysis.os.linux.core.inputoutput;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;

/**
 * Utility methods to return data from a {@link InputOutputAnalysisModule}
 * analysis.
 *
 * @author Geneviève Bastien
 * @author Houssem Daoud
 */
public final class InputOutputInformationProvider {

    private InputOutputInformationProvider() {

    }

    /**
     * Get the disks for an input/output analysis module
     *
     * @param module
     *            The analysis module
     * @return A collection of disks from this analysis
     */
    public static Collection<Disk> getDisks(InputOutputAnalysisModule module) {
        module.schedule();
        if (!module.waitForInitialization()) {
            return Collections.emptySet();
        }
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            throw new IllegalStateException("The state system should not be null at this point"); //$NON-NLS-1$
        }

        Set<Disk> disks = new HashSet<>();
        for (Integer diskQuark : ss.getQuarks(Attributes.DISKS, "*")) { //$NON-NLS-1$
            String devName = ss.getAttributeName(diskQuark);
            disks.add(new Disk(Integer.parseInt(devName), ss, diskQuark));
        }
        return disks;
    }
}
