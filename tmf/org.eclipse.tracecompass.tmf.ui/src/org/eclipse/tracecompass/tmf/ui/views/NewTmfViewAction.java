/**********************************************************************
 * Copyright (c) 2016, 2017 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.views;

import java.text.MessageFormat;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;

/**
 * Action to instantiate a new instance of views that support it.
 * @author Jonathan Rajotte Julien
 * @since 3.2
 */
public class NewTmfViewAction extends Action {

    private final @NonNull String fViewId;

    /**
     * Creates a new <code>NewTmfViewAction</code>.
     *
     * @param view
     *            The view for which the action is created
     */
    public NewTmfViewAction(TmfView view) {
        super(MessageFormat.format(Messages.TmfView_NewTmfViewNameText, view.getTitle()));
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NEW_VIEW));
        fViewId = view.getViewId();
    }

    @Override
    public void run() {
        TmfViewFactory.newView(fViewId, true);
    }
}
