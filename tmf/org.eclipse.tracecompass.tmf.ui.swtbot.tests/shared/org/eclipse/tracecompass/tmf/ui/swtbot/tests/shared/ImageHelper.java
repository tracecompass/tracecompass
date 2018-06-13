/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared;

import java.awt.AWTException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitTimeoutException;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * Test helpers, allow looking up the frame buffer and testing what is really
 * displayed
 */
public final class ImageHelper {

    private final int[] fPixels;
    private final Rectangle fBounds;

    /**
     * Constructor
     *
     * @param pixels
     *            the pixel map
     * @param bounds
     *            the bounds
     */
    private ImageHelper(int[] pixels, Rectangle bounds) {
        if (pixels.length != bounds.height * bounds.width) {
            throw new IllegalArgumentException("Incoherent image");
        }
        fPixels = Arrays.copyOf(pixels, pixels.length);
        fBounds = bounds;
    }

    /**
     * Gets a screen grab of the rectangle r; the way to access a given pixel is
     * <code>pixel = rect.width * y + x;</code>
     *
     * @param rect
     *            the area to grab in display relative coordinates (top left is the
     *            origin)
     * @return an ImageHelper, cannot be null
     */
    public static ImageHelper grabImage(final Rectangle rect) {
        return UIThreadRunnable.syncExec(new Result<ImageHelper>() {
            @Override
            public ImageHelper run() {
                try {
                    // note: awt is explicitly called until we can use SWT to
                    // replace it.
                    java.awt.Robot rb = new java.awt.Robot();
                    java.awt.image.BufferedImage bi = rb.createScreenCapture(new java.awt.Rectangle(rect.x, rect.y, rect.width, rect.height));
                    return new ImageHelper(bi.getRGB(0, 0, rect.width, rect.height, null, 0, rect.width), rect);
                } catch (AWTException e) {
                }
                return new ImageHelper(new int[0], new Rectangle(0, 0, 0, 0));
            }
        });
    }

    /**
     * Create an image helper from a file
     *
     * @param file
     *            the file (only tested with PNG)
     * @return an image helper or null if failed
     * @throws IOException
     *             the file is not valid
     */
    public static ImageHelper fromFile(File file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file);
        if (bufferedImage == null) {
            return null;
        }
        Rectangle bounds = new Rectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        int[] rgb = bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), null, 0, bufferedImage.getWidth());
        return new ImageHelper(rgb, bounds);
    }

    /**
     * Wait for the image at the specified bounds to change from the current image.
     * The method returns when the new image is stable.
     *
     * @param bounds
     *            the bounds in display coordinates
     * @param currentImage
     *            the current image, or null
     * @return the new image
     */
    public static ImageHelper waitForNewImage(Rectangle bounds, ImageHelper currentImage) {
        SWTBotUtils.waitUntil(arg -> {
            return !ImageHelper.grabImage(bounds).equals(currentImage);
        }, null, "Image at bounds " + bounds + " did not change");
        AtomicReference<ImageHelper> newImage = new AtomicReference<>();
        AtomicInteger count = new AtomicInteger();
        SWTBotUtils.waitUntil(arg -> {
            ImageHelper image = ImageHelper.grabImage(bounds);
            if (image.equals(newImage.get())) {
                count.incrementAndGet();
            } else {
                count.set(0);
                newImage.set(image);
            }
            return count.get() > 8;
        }, null, "Image at bounds " + bounds + " is not stable");
        if (newImage.get().equals(currentImage)) {
            throw new WaitTimeoutException("Image at bounds " + bounds + " did not change");
        }
        return newImage.get();
    }

    /**
     * Get the bounds
     *
     * @return the bounds
     */
    public Rectangle getBounds() {
        return fBounds;
    }

    /**
     * Get the pixel for a given set of coordinates
     *
     * @param x
     *            x
     * @param y
     *            y
     * @return the RGB, can return an {@link ArrayIndexOutOfBoundsException}
     */
    public RGB getPixel(int x, int y) {
        return getRgbFromRGBPixel(fPixels[x + y * fBounds.width]);
    }

    /**
     * Sample an image at n points
     *
     * @param samplePoints
     *            a list of points to sample at
     * @return a list of RGBs corresponding to the pixel coordinates. Can throw an
     *         {@link IllegalArgumentException} if the point is outside of the image
     *         bounds
     */
    public List<RGB> sample(List<Point> samplePoints) {
        for (Point p : samplePoints) {
            if (!getBounds().contains(p)) {
                throw new IllegalArgumentException("Point outside of the image");
            }

        }
        List<RGB> retVal = new ArrayList<>(samplePoints.size());
        for (Point p : samplePoints) {
            retVal.add(getPixel(p.x, p.y));
        }
        return retVal;
    }

    /**
     * Get the color histogram of the image
     *
     * @return The color density of the image
     */
    public Multiset<RGB> getHistogram() {
        Multiset<RGB> colors = HashMultiset.create();
        for (int pixel : fPixels) {
            RGB pixelColor = getRgbFromRGBPixel(pixel);
            colors.add(pixelColor);
        }
        return colors;
    }

    /**
     * Get the color histogram of the row of the image
     *
     * @param row
     *            the row to lookup
     *
     * @return The x oriented line
     */
    public List<RGB> getPixelRow(int row) {
        List<RGB> retVal = new ArrayList<>();
        for (int x = 0; x < getBounds().width; x++) {
            retVal.add(getPixel(x, row));
        }
        return retVal;
    }

    /**
     * Get the color histogram of a column of the image
     *
     * @param col
     *            the column to lookup
     *
     * @return The y oriented line
     */
    public List<RGB> getPixelColumn(int col) {
        List<RGB> retVal = new ArrayList<>();
        for (int y = 0; y < getBounds().height; y++) {
            retVal.add(getPixel(col, y));
        }
        return retVal;
    }

    /**
     * Difference between two images (this - other)
     *
     * @param other
     *            the other image to compare
     * @return an {@link ImageHelper} that is the per pixel difference between the
     *         two images
     *
     */
    public ImageHelper diff(ImageHelper other) {
        if (other.getBounds().width != fBounds.width && other.getBounds().height != fBounds.height) {
            throw new IllegalArgumentException("Different sized images");
        }
        int[] fBuffer = new int[fPixels.length];
        for (int i = 0; i < fPixels.length; i++) {
            RGB local = getRgbFromRGBPixel(fPixels[i]);
            RGB otherPixel = getRgbFromRGBPixel(other.fPixels[i]);
            byte r = (byte) (local.red - otherPixel.red);
            byte g = (byte) (local.green - otherPixel.green);
            byte b = (byte) (local.blue - otherPixel.blue);
            fBuffer[i] = 0xff << 24 | (r << 16) | (g << 8) | b;
        }
        return new ImageHelper(fBuffer, getBounds());
    }

    /**
     * Write the image to disk in PNG form
     *
     * @param outputFile
     *            the file to write it to
     * @throws IOException
     *             file not found and such
     */
    public void writePng(File outputFile) throws IOException {
        BufferedImage image = new BufferedImage(fBounds.width, fBounds.height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, fBounds.width, fBounds.height, fPixels, 0, fBounds.width);
        ImageIO.write(image, "png", outputFile);
    }

    /**
     * Converter from ARGB to {@link RGB}
     *
     * @param pixel
     *            an ARGB encoded pixel
     * @return the {@link RGB}
     */
    private static RGB getRgbFromRGBPixel(int pixel) {
        return new RGB(((pixel >> 16) & 0xff), ((pixel >> 8) & 0xff), ((pixel) & 0xff));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fBounds == null) ? 0 : fBounds.hashCode());
        result = prime * result + Arrays.hashCode(fPixels);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImageHelper other = (ImageHelper) obj;
        if (fBounds == null) {
            if (other.fBounds != null) {
                return false;
            }
        } else if (!fBounds.equals(other.fBounds)) {
            return false;
        }
        if (!Arrays.equals(fPixels, other.fPixels)) {
            return false;
        }
        return true;
    }

    /**
     * On some platforms, RGB values that are captured with ImageHelper are affected
     * by monitor color profiles. To account for this, we can draw the expected
     * color in a simple shell and use that color as expected value instead.
     *
     * @param original
     *            original color to adjust
     * @return adjusted color
     */
    public static synchronized RGB adjustExpectedColor(RGB original) {
        /* Create shell with desired color as background */
        boolean painted[] = new boolean[1];
        final Shell shell = UIThreadRunnable.syncExec(new Result<Shell>() {
            @Override
            public Shell run() {
                Shell s = new Shell(Display.getDefault(), SWT.ON_TOP);
                s.setSize(16, 16);
                Color color = new Color(Display.getDefault(), original);
                s.setBackground(color);
                s.addPaintListener(new PaintListener() {
                    @Override
                    public void paintControl(PaintEvent e) {
                        e.gc.setBackground(color);
                        e.gc.fillRectangle(0, 0, 16, 16);
                        painted[0] = true;
                    }
                });
                s.open();
                return s;
            }
        });

        /* Make sure the shell has been painted before getting the color */
        SWTBotUtils.waitUntil(arg -> painted[0], null, "Shell was not painted");

        /* Get the color, after waiting for it to stabilize */
        AtomicReference<RGB> adjusted = new AtomicReference<>();
        AtomicInteger count = new AtomicInteger();
        SWTBotUtils.waitUntil(s -> {
            RGB pixel = UIThreadRunnable.syncExec(new Result<RGB>() {
                @Override
                public RGB run() {
                    shell.redraw();
                    shell.update();
                    return ImageHelper.grabImage(shell.getBounds()).getPixel(8, 8);
                }
            });
            if (pixel.equals(adjusted.get())) {
                count.incrementAndGet();
            } else {
                count.set(0);
                adjusted.set(pixel);
            }
            return count.get() > 8;
        }, shell, "Color did not stabilize");

        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                shell.close();
            }
        });

        return adjusted.get();
    }
}
