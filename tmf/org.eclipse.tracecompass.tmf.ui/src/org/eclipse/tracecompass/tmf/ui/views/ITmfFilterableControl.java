/**********************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.views;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;

/**
 * An interface that views can implement if they can be filtered using a local
 * filter.
 *
 * By default, if views have the tmf view context:
 * <code>org.eclipse.tracecompass.tmf.ui.view.context</code> and implement this
 * interface, this interface will automatically work with one of the default
 * handlers for the '/' key press.
 *
 * If the view does not have the tmf view context, proper handlers will need to
 * be implemented.
 *
 * @author Geneviève Bastien
 * @since 6.0
 */
public interface ITmfFilterableControl {

    /**
     * Get the action to execute when the users asks for a filter
     *
     * @return The action to execute
     */
    Action getFilterAction();

    /**
     * The filters were updated by the user. This is where the view can act on
     * the filters entered by the user.
     *
     * @param regex
     *            The filter being entered in the textbox
     * @param filterRegexes
     *            The saved filters, after the user has pressed
     *            <code>enter</code> on the filter box.
     */
    void filterUpdated(String regex, @NonNull Set<@NonNull String> filterRegexes);

}
