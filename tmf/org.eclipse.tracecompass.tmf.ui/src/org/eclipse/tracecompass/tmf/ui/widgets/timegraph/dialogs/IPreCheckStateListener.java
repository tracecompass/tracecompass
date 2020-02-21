/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs;

/**
 * Interface for actions to execute before checking a sub tree in a
 * {@link TriStateFilteredCheckboxTree}.
 *
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public interface IPreCheckStateListener {

    /**
     * Action to execute before selecting the tree
     *
     * @param element
     *            root element of the sub tree to select
     * @param state
     *            selected state
     * @return if we cancel the selection
     */
    boolean setSubtreeChecked(Object element, boolean state);
}
