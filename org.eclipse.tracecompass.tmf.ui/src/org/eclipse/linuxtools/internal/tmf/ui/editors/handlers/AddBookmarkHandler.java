/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.editors.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Handler to add bookmarks.
 *
 * @author Patrick Tasse
 */
public class AddBookmarkHandler extends AbstractHandler {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbench wb = PlatformUI.getWorkbench();
        IWorkbenchPage activePage = wb.getActiveWorkbenchWindow().getActivePage();
        IEditorPart activeEditor = activePage.getActiveEditor();
        if (activeEditor instanceof TmfEventsEditor) {
            TmfEventsEditor editor = (TmfEventsEditor) activeEditor;
            editor.addBookmark();
        }
        return null;
    }

}
