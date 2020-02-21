/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

/**
 * This interface abstracts the differences between different kinds of
 * FileSystemObjects such as File, TarEntry, ZipEntry, etc. This allows clients
 * (TraceFileSystemElement, TraceValidateAndImportOperation) to handle all the
 * types transparently.
 */
public interface IFileSystemObject {

    /**
     * Get the name of the file system object (last segment).
     *
     * @return the name of the file system object.
     */
    String getName();

    /**
     * Get the absolute path of the file system object.
     *
     * @return the absolute path of the file system object
     */
    String getAbsolutePath();

    /**
     * Get the source location for this file system object.
     *
     * @return the source location
     */
    String getSourceLocation();

    /**
     * Returns the raw object wrapped by this IFileSystemObject (File, TarEntry, etc).
     *
     * @return the raw object wrapped by this IFileSystemObject
     */
    Object getRawFileSystemObject();

    /**
     * Returns whether or not the file system object exists.
     *
     * @return whether or not the file system object exists
     */
    boolean exists();

    /**
     * Returns whether or not this object represents a directory.
     *
     * @return whether or not this object represents a directory.
     */
    boolean isDirectory();
}