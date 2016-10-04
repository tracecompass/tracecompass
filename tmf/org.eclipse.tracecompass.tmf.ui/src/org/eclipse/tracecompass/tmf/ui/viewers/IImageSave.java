/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Image saving interface. It is to be implemented by view classes that want to
 * allow their content to be exported as an image. The default implementation
 * exports the canvas as a PNG image.
 *
 * @since 3.3
 * @author Matthew Khouzam
 */
public interface IImageSave {

    /**
     * Save the image, This default implementation save the canvas as a PNG image.
     *
     * @param filename
     *            the file to write to
     * @param format
     *            the image format, specified in {@link SWT} constants
     *            ({@link SWT#IMAGE_PNG}, {@link SWT#IMAGE_GIF},
     *            {@link SWT#IMAGE_JPEG} or {@link SWT#IMAGE_BMP}).
     */
    default void saveImage(String filename, int format) {
        Control control = getControl();
        Image image = new Image(Display.getDefault(), control.getBounds());
        GC gc = new GC(image);
        control.print(gc);
        gc.dispose();
        ImageData data = image.getImageData();
        ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] { data };
        loader.save(filename, format);
        image.dispose();
    }

    /**
     * Returns the primary control associated with this view.
     *
     * @return the SWT control which displays this view's content
     */
    Control getControl();

}
