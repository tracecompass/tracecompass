/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.tracing.rcp.ui;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.internal.tracing.rcp.ui.messages.Messages;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.splash.BasicSplashHandler;

/**
 * Custom splash handler
 *
 * @author Bernd Hufmann
 */
public class SplashHandler extends BasicSplashHandler {

    private static final Point VERSION_LOCATION = new Point(10, 280);
    private static final Rectangle PROCESS_BAR_RECTANGLE = new Rectangle(10, 300, 480, 15);
    private static final RGB FOREGROUND_COLOR = new RGB(255, 255, 255);

    @Override
    public void init(Shell splash) {
        super.init(splash);

        String progressString = null;

        // Try to get the progress bar and message updater.
        IProduct product = Platform.getProduct();
        if (product != null) {
            progressString = product.getProperty(IProductConstants.STARTUP_PROGRESS_RECT);
        }
        Rectangle progressRect = StringConverter.asRectangle(progressString, PROCESS_BAR_RECTANGLE);
        setProgressRect(progressRect);

        // Set font color.
        setForeground(FOREGROUND_COLOR);

        // Set the software version.
        getContent().addPaintListener(e -> {
            e.gc.setForeground(getForeground());
            e.gc.drawText(
                    NLS.bind(Messages.SplahScreen_VersionString,
                            TracingRcpPlugin.getDefault().getBundle().getVersion().toString()),
                    VERSION_LOCATION.x, VERSION_LOCATION.y, true);
        });
    }
}
