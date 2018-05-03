/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Manager for XML analysis files
 *
 * @author Jean-Christian Kouame
 */
public class ManageXMLAnalysisCommandHandler extends AbstractHandler {

    private static final String PAGE_ID = "org.eclipse.tracecompass.tmf.analysis.xml.ui.manager"; //$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        PreferencesUtil.createPreferenceDialogOn(Display.getDefault().getActiveShell(), PAGE_ID, new String[] {PAGE_ID}, null).open();
        return null;
    }

}
