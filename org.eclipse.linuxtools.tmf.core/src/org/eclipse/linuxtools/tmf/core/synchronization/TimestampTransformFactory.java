/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation and API
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.synchronization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfConstantTransform;
import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfTimestampTransform;
import org.eclipse.linuxtools.internal.tmf.core.synchronization.TmfTimestampTransformLinear;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * A factory to generate timestamp tranforms
 *
 * @author Matthew Khouzam
 * @since 3.1
 */
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
        return new TmfTimestampTransformLinear(factor, offset.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue());
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
        return new TmfTimestampTransformLinear(factor, offset);
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
        return new TmfTimestampTransformLinear(factor, offset);
    }

    /**
     * Returns the file resource used to store synchronization formula. The file
     * may not exist.
     *
     * @param resource
     *            the trace resource
     * @return the synchronization file
     */
    private static File getSyncFormulaFile(IResource resource) {
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
     * @since 3.2
     */
    public static ITmfTimestampTransform getTimestampTransform(IResource resource) {
        File syncFile = getSyncFormulaFile(resource);
        if (syncFile != null && syncFile.exists()) {
            /* Read the serialized object from file */
            try (FileInputStream fis = new FileInputStream(syncFile);
                    ObjectInputStream ois = new ObjectInputStream(fis);) {
                return (ITmfTimestampTransform) ois.readObject();
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
     * @since 3.2
     */
    public static void setTimestampTransform(IResource resource, ITmfTimestampTransform tt) {
        /* Save the timestamp transform to a file */
        File syncFile = getSyncFormulaFile(resource);
        if (syncFile != null) {
            if (syncFile.exists()) {
                syncFile.delete();
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
