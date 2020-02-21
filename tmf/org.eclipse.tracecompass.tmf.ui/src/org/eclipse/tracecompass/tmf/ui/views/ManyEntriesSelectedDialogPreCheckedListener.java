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

package org.eclipse.tracecompass.tmf.ui.views;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfUIPreferences;
import org.eclipse.tracecompass.internal.tmf.ui.views.Messages;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.FilteredCheckboxTree;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.dialogs.IPreCheckStateListener;

/**
 * {@link IPreCheckStateListener} to warn the user of potential slowness before
 * checking a sub-tree
 *
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class ManyEntriesSelectedDialogPreCheckedListener implements IPreCheckStateListener {

    private final FilteredCheckboxTree fFilteredCheckboxTree;

    /**
     * Constructor
     *
     * @param filteredCheckboxTree
     *            the {@link FilteredCheckboxTree} that this listener will be
     *            applied to.
     */
    public ManyEntriesSelectedDialogPreCheckedListener(FilteredCheckboxTree filteredCheckboxTree) {
        fFilteredCheckboxTree = filteredCheckboxTree;
    }

    @Override
    public boolean setSubtreeChecked(Object element, boolean state) {
        if (state) {
            ITreeContentProvider contentProvider = (ITreeContentProvider) fFilteredCheckboxTree.getCheckboxTreeViewer().getContentProvider();
            int nb = fFilteredCheckboxTree.getCheckedElements().length + getSubTreeSize(contentProvider, element);
            return showWarning(nb);
        }
        return false;
    }

    private int getSubTreeSize(ITreeContentProvider contentProvider, Object element) {
        int size = 1;
        for (Object o : contentProvider.getChildren(element)) {
            size += getSubTreeSize(contentProvider, o);
        }
        return size;
    }

    /**
     *
     * @return if the checking should be cancelled
     */
    private boolean showWarning(int nb) {
        /*
         * Show a dialog warning users that selecting many entries will be slow.
         */
        IPreferenceStore corePreferenceStore = Activator.getDefault().getPreferenceStore();
        boolean hide = corePreferenceStore.getBoolean(ITmfUIPreferences.HIDE_MANY_ENTRIES_SELECTED_TOGGLE);
        if (nb > 20 && !hide) {
            MessageDialogWithToggle openOkCancelConfirm = MessageDialogWithToggle.openOkCancelConfirm(
                    fFilteredCheckboxTree.getShell(),
                    Messages.ManyEntriesSelectedDialogPreCheckedListener_ManyEntriesSelectedTitle,
                    NLS.bind(Messages.ManyEntriesSelectedDialogPreCheckedListener_ManyEntriesSelectedMessage, nb),
                    Messages.ManyEntriesSelectedDialogPreCheckedListener_ManyEntriesSelectedDontShowAgain, false,
                    null, null);
            corePreferenceStore.setValue(ITmfUIPreferences.HIDE_MANY_ENTRIES_SELECTED_TOGGLE, openOkCancelConfirm.getToggleState());
            int retCode = openOkCancelConfirm.getReturnCode();
            return retCode == Window.CANCEL;
        }
        return false;
    }

}
