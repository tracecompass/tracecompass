/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
