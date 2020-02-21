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
 *   Marc-Andre Laperle - Initial API and implementation.
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

/**
 * Wrapper for a Gzipped file
 */
public class GzipFile implements AutoCloseable {

    private static final String GZIP_EXTENSION = ".gz"; //$NON-NLS-1$

    private final File fFile;
    private final GzipEntry fEntry;
    private GzipEntry fCurEntry;
    private boolean fIsClosed = false;

    private final InputStream fInternalEntryStream;

    /**
     * Create a new GzipFile for the given file.
     *
     * @param source the source file
     * @throws IOException
     *             File not found and such
     */
    public GzipFile(File source) throws IOException {
        fFile = source;

        InputStream in = new FileInputStream(source);
        try {
            // Check if it's a GZIPInputStream.
            fInternalEntryStream = new GZIPInputStream(in);
        } catch (IOException e) {
            in.close();
            throw e;
        }
        String name = source.getName();
        fEntry = new GzipEntry(name.substring(0, name.lastIndexOf(GZIP_EXTENSION)));
        fCurEntry = fEntry;
    }

    /**
     * Close the tar file input stream.
     *
     * @throws IOException if the file cannot be successfully closed
     */
    @Override
    public void close() throws IOException {
        if (fInternalEntryStream != null && !fIsClosed) {
            fInternalEntryStream.close();
            fIsClosed = true;

        }
    }

    /**
     * Create a new GzipFile for the given path name.
     *
     * @param filename
     *            the filename of the gzip file
     * @throws IOException
     *             if the file cannot be opened
     */
    public GzipFile(String filename) throws IOException {
        this(new File(filename));
    }

    /**
     * Returns an enumeration cataloguing the tar archive.
     *
     * @return enumeration of all files in the archive
     */
    public Enumeration<GzipEntry> entries() {
        return new Enumeration<GzipEntry>() {
            @Override
            public boolean hasMoreElements() {
                return (fCurEntry != null);
            }

            @Override
            public GzipEntry nextElement() {
                GzipEntry oldEntry = fCurEntry;
                fCurEntry = null;
                return oldEntry;
            }
        };
    }

    /**
     * Returns a new InputStream for the given file in the tar archive.
     *
     * @param entry
     *            the GzipEntry
     * @return an input stream for the given file
     */
    public InputStream getInputStream(GzipEntry entry) {
        if (entry != fEntry) {
            throw new IllegalArgumentException();
        }
        return fInternalEntryStream;
    }

    /**
     * Returns the path name of the file this archive represents.
     *
     * @return path
     */
    public String getName() {
        return fFile.getPath();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

}
