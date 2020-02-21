/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.util.Collections;

import org.eclipse.jface.action.IContributionItem;

/**
 * Contribution item for the Open As Experiment... sub-menu.
 */
public class OpenAsExperimentContributionItem extends SelectElementTypeContributionItem {

    private static final String OPEN_AS_EXPERIMENT_COMMAND_ID = "org.eclipse.tracecompass.tmf.ui.command.open_as_experiment"; //$NON-NLS-1$

    @Override
    protected IContributionItem[] getContributionItems() {
        return getContributionItems(Collections.emptySet(), true);
    }

    @Override
    protected String getContributionItemCommandId() {
        return OPEN_AS_EXPERIMENT_COMMAND_ID;
    }
}
