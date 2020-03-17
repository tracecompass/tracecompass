package org.eclipse.tracecompass.tmf.ui.viewers.tree;

import java.util.Collection;

/**
 * Listens to changes in the accompanying tree viewer.
 *
 * This interface can be used by any chart which needs to modify its displayed
 * data depending on the selected entries in the tree.
 *
 * @author Mikael Ferland
 * @since 3.2
 */
public interface ICheckboxTreeViewerListener {

    /**
     * Handler for events where the tree viewer's selection has changed.
     *
     * @param entries
     *            Entries which have been checked in the tree
     */
    void handleCheckStateChangedEvent(Collection<ITmfTreeViewerEntry> entries);
}
