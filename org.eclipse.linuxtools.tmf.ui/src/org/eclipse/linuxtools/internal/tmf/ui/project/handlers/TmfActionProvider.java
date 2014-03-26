/*******************************************************************************
* Copyright (c) 2012, 2014 Ericsson
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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
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

    private IWorkbenchPage page;

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
            page = workbenchSite.getPage();
            openAction = new OpenAction(page, workbenchSite.getSelectionProvider());
            deleteAction = new DeleteAction(page, workbenchSite.getSelectionProvider());
            refreshAction = new RefreshAction(page, workbenchSite.getSelectionProvider());
        }
    }

    @Override
    public void fillContextMenu(IMenuManager menu) {
        ISelection selection = getContext().getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            if (structuredSelection.size() == 1 && structuredSelection.getFirstElement() instanceof TmfTraceElement) {
                TmfTraceElement traceElement = (TmfTraceElement) structuredSelection.getFirstElement();
                if (traceElement.getResource() instanceof IFile) {
                    MenuManager openWithMenu = new MenuManager(Messages.TmfActionProvider_OpenWith);
                    openWithMenu.add(new OpenWithMenu(page, traceElement.getResource()));
                    menu.insertAfter(ICommonMenuConstants.GROUP_OPEN_WITH, openWithMenu);
                }
            }
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
