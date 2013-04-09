/*******************************************************************************
* Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Base action provider.
 *
 * @author Patrick Tassé
 */
public class TmfActionProvider extends CommonActionProvider {

    private OpenAction openAction;
    private DeleteAction deleteAction;
    private RefreshAction refreshAction;

    /**
     * Default constructor
     */
    public TmfActionProvider() {
    }

    @Override
    public void init(ICommonActionExtensionSite aSite) {
        ICommonViewerSite viewSite = aSite.getViewSite();
        if (viewSite instanceof ICommonViewerWorkbenchSite) {
            ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
            openAction = new OpenAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
            deleteAction = new DeleteAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
            refreshAction = new RefreshAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
        }
    }

    @Override
    public void fillActionBars(IActionBars actionBars) {
        if (openAction.isEnabled()) {
            actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
        }
        if (deleteAction.isEnabled()) {
            actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
        }
        if (refreshAction.isEnabled()) {
            actionBars.setGlobalActionHandler(ActionFactory.REFRESH.getId(), refreshAction);
        }
    }

}
