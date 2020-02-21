/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Remy Chi Jian Suen <remy.suen@gmail.com> - Bug 243347 TarFile should not throw NPE in finalize()
 * Marc-Andre Laperle <marc-andre.laperle@ericsson.com> - Bug 463633
 * Marc-Andre Laperle <marc-andre.laperle@ericsson.com> - Copied to Trace Compass to work around bug 501379
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;


/**
 * Reads a .tar or .tar.gz archive file, providing an index enumeration
 * and allows for accessing an InputStream for arbitrary files in the
 * archive.
 */
public class TarFile {
    private File file;
    private TarArchiveInputStream entryEnumerationStream;
    private TarArchiveEntry curEntry;
    private TarArchiveInputStream entryStream;

    private InputStream internalEntryStream;
    // This field is just to prevent try with resources error and keep the code
    // similar to the original
    private InputStream fInputStream;

    /**
     * Create a new TarFile for the given file.
     *
     * @param file the file
     * @throws IOException on i/o error (bad format, etc)
     */
    public TarFile(File file) throws IOException {
        this.file = file;

        fInputStream = new FileInputStream(file);
        // First, check if it's a GZIPInputStream.
        try {
            fInputStream = new GzipCompressorInputStream(fInputStream);
        } catch (IOException e) {
            //If it is not compressed we close
            //the old one and recreate
            fInputStream.close();
            fInputStream = new FileInputStream(file);
        }
        entryEnumerationStream = new TarArchiveInputStream(fInputStream);
        try {
            curEntry = (TarArchiveEntry) entryEnumerationStream.getNextEntry();
            if (curEntry == null || !curEntry.isCheckSumOK()) {
                throw new IOException("Error detected parsing initial entry header"); //$NON-NLS-1$
            }
        } catch (IOException e) {
            fInputStream.close();
            throw e;
        }
    }

    /**
     * Close the tar file input stream.
     *
     * @throws IOException if the file cannot be successfully closed
     */
    public void close() throws IOException {
        if (entryEnumerationStream != null) {
            entryEnumerationStream.close();
        }
        if (internalEntryStream != null) {
            internalEntryStream.close();
        }
    }

    /**
     * Create a new TarFile for the given path name.
     *
     * @param filename the file name to create the TarFile from
     * @throws IOException on i/o error (bad format, etc)
     */
    public TarFile(String filename) throws IOException {
        this(new File(filename));
    }

    /**
     * Returns an enumeration cataloguing the tar archive.
     *
     * @return enumeration of all files in the archive
     */
    public Enumeration<TarArchiveEntry> entries() {
        return new Enumeration<TarArchiveEntry>() {
            @Override
            public boolean hasMoreElements() {
                return (curEntry != null);
            }

            @Override
            public TarArchiveEntry nextElement() {
                TarArchiveEntry oldEntry = curEntry;
                try {
                    curEntry = (TarArchiveEntry) entryEnumerationStream.getNextEntry();
                } catch(IOException e) {
                    curEntry = null;
                }
                return oldEntry;
            }
        };
    }

    /**
     * Returns a new InputStream for the given file in the tar archive.
     *
     * @param entry the entry to get the InputStream from
     * @return an input stream for the given file
     * @throws IOException on i/o error (bad format, etc)
     */
    public InputStream getInputStream(TarArchiveEntry entry) throws IOException {
        if(entryStream == null || !skipToEntry(entryStream, entry)) {
            if (internalEntryStream != null) {
                internalEntryStream.close();
            }
            internalEntryStream = new FileInputStream(file);
            // First, check if it's a GzipCompressorInputStream.
            try {
                internalEntryStream = new GzipCompressorInputStream(internalEntryStream);
            } catch(IOException e) {
                //If it is not compressed we close
                //the old one and recreate
                internalEntryStream.close();
                internalEntryStream = new FileInputStream(file);
            }
            entryStream = new TarArchiveInputStream(internalEntryStream) {
                @Override
                public void close() {
                    // Ignore close() since we want to reuse the stream.
                }
            };
            skipToEntry(entryStream, entry);
        }
        return entryStream;
    }

    private static boolean skipToEntry(TarArchiveInputStream entryStream, TarArchiveEntry entry) throws IOException {
        TarArchiveEntry e = entryStream.getNextTarEntry();
        while (e != null) {
            if (e.equals(entry)) {
                return true;
            }

            e = entryStream.getNextTarEntry();
        }

        return false;
    }

    /**
     * Returns the path name of the file this archive represents.
     *
     * @return path
     */
    public String getName() {
        return file.getPath();
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }
}
