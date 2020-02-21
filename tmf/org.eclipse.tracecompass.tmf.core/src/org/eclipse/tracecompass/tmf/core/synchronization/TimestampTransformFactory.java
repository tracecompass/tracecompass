/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation and API
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.core.synchronization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.synchronization.TmfConstantTransform;
import org.eclipse.tracecompass.internal.tmf.core.synchronization.TmfTimestampTransform;
import org.eclipse.tracecompass.internal.tmf.core.synchronization.TmfTimestampTransformLinearFast;
import org.eclipse.tracecompass.tmf.core.TmfCommonConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * A factory to generate timestamp tranforms
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public final class TimestampTransformFactory {

    private static final String SYNCHRONIZATION_FORMULA_FILE = "sync_formula"; //$NON-NLS-1$

    private TimestampTransformFactory() {
    }

    /**
     * Creates the identity timestamp transform
     *
     * @return The identity timestamp transform
     */
    public static ITmfTimestampTransform getDefaultTransform() {
        return TmfTimestampTransform.IDENTITY;
    }

    /**
     * Create an offsetted transform
     *
     * @param offset
     *            the offset in long format, nanosecond scale
     * @return the offsetted transform
     */
    public static ITmfTimestampTransform createWithOffset(long offset) {
        if (offset == 0) {
            return TmfTimestampTransform.IDENTITY;
        }
        return new TmfConstantTransform(offset);
    }

    /**
     * Create an offsetted transform
     *
     * @param offset
     *            the offset in a timestamp with scale
     * @return the offsetted transform
     */
    public static ITmfTimestampTransform createWithOffset(ITmfTimestamp offset) {
        if (offset.getValue() == 0) {
            return TmfTimestampTransform.IDENTITY;
        }
        return new TmfConstantTransform(offset);
    }

    /**
     * Create a timestamp transform corresponding to a linear equation, with
     * slope and offset. The expected timestamp transform is such that f(t) =
     * m*x + b, where m is the slope and b the offset.
     *
     * @param factor
     *            the slope
     * @param offset
     *            the offset
     * @return the transform
     */
    public static ITmfTimestampTransform createLinear(double factor, ITmfTimestamp offset) {
        if (factor == 1.0) {
            return createWithOffset(offset);
        }
        return new TmfTimestampTransformLinearFast(factor, offset.toNanos());
    }

    /**
     * Create a timestamp transform corresponding to a linear equation, with
     * slope and offset. The expected timestamp transform is such that f(t) =
     * m*x + b, where m is the slope and b the offset.
     *
     * @param factor
     *            the slope
     * @param offset
     *            the offset in nanoseconds
     * @return the transform
     */
    public static ITmfTimestampTransform createLinear(double factor, long offset) {
        if (factor == 1.0) {
            return createWithOffset(offset);
        }
        return new TmfTimestampTransformLinearFast(factor, offset);
    }

    /**
     * Create a timestamp transform corresponding to a linear equation, with
     * slope and offset expressed in BigDecimal. The expected timestamp
     * transform is such that f(t) = m*x + b, where m is the slope and b the
     * offset.
     *
     * @param factor
     *            the slope
     * @param offset
     *            the offset in nanoseconds
     * @return the transform
     */
    public static ITmfTimestampTransform createLinear(BigDecimal factor, BigDecimal offset) {
        if (factor.equals(BigDecimal.ONE)) {
            return createWithOffset(offset.longValueExact());
        }
        return new TmfTimestampTransformLinearFast(factor, offset);
    }

    /**
     * Returns the file resource used to store synchronization formula. The file
     * may not exist.
     *
     * @param resource
     *            the trace resource
     * @return the synchronization file
     */
    private static @Nullable File getSyncFormulaFile(@Nullable IResource resource) {
        if (resource == null) {
            return null;
        }
        try {
            String supplDirectory = resource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);
            return new File(supplDirectory + File.separator + SYNCHRONIZATION_FORMULA_FILE);
        } catch (CoreException e) {
            /* Ignored */
        }
        return null;
    }

    /**
     * Returns the timestamp transform for a trace resource
     *
     * @param resource
     *            the trace resource
     * @return the timestamp transform
     */
    public static ITmfTimestampTransform getTimestampTransform(@Nullable IResource resource) {
        File syncFile = getSyncFormulaFile(resource);
        if (syncFile != null && syncFile.exists()) {
            /* Read the serialized object from file */
            try (FileInputStream fis = new FileInputStream(syncFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);) {
                return NonNullUtils.checkNotNull((ITmfTimestampTransform) ois.readObject());
            } catch (ClassNotFoundException | IOException e) {
            }
        }
        return TimestampTransformFactory.getDefaultTransform();
    }

    /**
     * Sets the trace resource's timestamp transform
     *
     * @param resource
     *            the trace resource
     * @param tt
     *            The timestamp transform for all timestamps of this trace, or
     *            null to clear it
     */
    public static void setTimestampTransform(@Nullable IResource resource, @Nullable ITmfTimestampTransform tt) {
        /* Save the timestamp transform to a file */
        File syncFile = getSyncFormulaFile(resource);
        if (syncFile != null) {
            if (syncFile.exists() && !syncFile.delete()) {
                Activator.logError("Error erasing syncfile " + syncFile); //$NON-NLS-1$
            }
            if (tt == null) {
                return;
            }
            /* Write the serialized object to file */
            try (FileOutputStream fos = new FileOutputStream(syncFile, false);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);) {
                oos.writeObject(tt);
            } catch (IOException e1) {
                Activator.logError("Error writing timestamp transform for trace", e1); //$NON-NLS-1$
            }
        }
    }

}
