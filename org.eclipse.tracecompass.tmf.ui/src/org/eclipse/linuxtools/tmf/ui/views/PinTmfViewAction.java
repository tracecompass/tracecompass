/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.ITmfImageConstants;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;

/**
 *
 * @version 1.0
 * @author Bernd Hufmann
 * @since 2.0
 */
public class PinTmfViewAction extends Action {
    /**
    * Creates a new <code>PinPropertySheetAction</code>.
    */
   public PinTmfViewAction() {
       super(Messages.TmfView_PinActionNameText, IAction.AS_CHECK_BOX);

       setId("org.eclipse.linuxtools.tmf.ui.views.PinTmfViewAction"); //$NON-NLS-1$
       setToolTipText(Messages.TmfView_PinActionToolTipText);
       setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_PIN_VIEW));
   }
}
