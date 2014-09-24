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

package org.eclipse.linuxtools.internal.tmf.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.linuxtools.internal.tmf.ui.dialogs.ManageCustomParsersDialog;
import org.eclipse.swt.widgets.Display;

/**
 * Command handler for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class ManageCustomParsersCommandHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ManageCustomParsersDialog dialog = new ManageCustomParsersDialog(Display.getDefault().getActiveShell());
        dialog.open();
        return null;
    }

}
