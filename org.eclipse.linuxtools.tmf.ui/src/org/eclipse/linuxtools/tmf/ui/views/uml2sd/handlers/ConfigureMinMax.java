/**********************************************************************
 * Copyright (c) 2005, 2006, 2011 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * $Id: ConfigureMinMax.java,v 1.1 2006/08/16 21:15:28 amehregani Exp $
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.handlers;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDView;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.util.MinMaxDialog;
import org.eclipse.ui.IViewPart;

/**
 * @author sveyrier
 * 
 */
public class ConfigureMinMax extends Action {

    protected SDWidget viewer = null;
    protected SDView view = null;

    public ConfigureMinMax(IViewPart _view) {
        super();
        if (_view instanceof SDView) {
            view = (SDView) _view;
        }
    }

    @Override
    public void run() {
        if (view != null && view.getSDWidget() != null) {
            MinMaxDialog minMax = new MinMaxDialog(view.getSite().getShell(), view.getSDWidget());
            minMax.open();
        }
    }
}
