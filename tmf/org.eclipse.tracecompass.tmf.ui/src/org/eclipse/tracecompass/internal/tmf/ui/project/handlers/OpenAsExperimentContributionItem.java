/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
        return getContributionItems(Collections.EMPTY_SET, true);
    }

    @Override
    protected String getContributionItemCommandId() {
        return OPEN_AS_EXPERIMENT_COMMAND_ID;
    }
}
