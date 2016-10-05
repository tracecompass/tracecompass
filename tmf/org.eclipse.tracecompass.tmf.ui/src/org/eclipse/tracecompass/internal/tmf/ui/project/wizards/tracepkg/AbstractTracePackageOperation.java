/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.tracepkg;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.TarFile;

/**
 * An abstract operation containing common code useful for other trace package
 * operations
 *
 * @author Marc-Andre Laperle
 */
public abstract class AbstractTracePackageOperation {
    private IStatus fStatus;
    // Result of this operation, if any
    private TracePackageElement[] fResultElements;

    private final String fFileName;

    /**
     * Constructs a new trace package operation
     *
     * @param fileName
     *            the output file name
     */
    public AbstractTracePackageOperation(String fileName) {
        fFileName = fileName;
    }

    /**
     * Run the operation. The status (result) of the operation can be obtained
     * with {@link #getStatus}
     *
     * @param progressMonitor
     *            the progress monitor to use to display progress and receive
     *            requests for cancellation
     */
    public abstract void run(IProgressMonitor progressMonitor);

    /**
     * Returns the status of the operation (result)
     *
     * @return the status of the operation
     */
    public IStatus getStatus() {
        return fStatus;
    }

    /**
     * Get the resulting elements for this operation, if any
     *
     * @return the resulting elements or null if no result is produced by this
     *         operation
     */
    public TracePackageElement[] getResultElements() {
        return fResultElements;
    }

    /**
     * Set the resulting elements for this operation, if any
     *
     * @param elements
     *            the resulting elements produced by this operation, can be set
     *            to null
     */
    public void setResultElements(TracePackageElement[] elements) {
        fResultElements = elements;
    }

    /**
     * Set the status for this operation
     *
     * @param status
     *            the status
     */
    protected void setStatus(IStatus status) {
        fStatus = status;
    }

    /**
     * Get the file name of the package
     *
     * @return the file name
     */
    protected String getFileName() {
        return fFileName;
    }

    /**
     * Answer a handle to the archive file currently specified as being the
     * source. Return null if this file does not exist or is not of valid
     * format.
     *
     * @return the archive file
     */
    public ArchiveFile getSpecifiedArchiveFile() {
        if (fFileName.length() == 0) {
            return null;
        }

        File file = new File(fFileName);
        if (file.isDirectory()) {
            return null;
        }

        try {
            return new ZipArchiveFile(new ZipFile(file));
        } catch (IOException e) {
            // ignore
        }

        try {
            return new TarArchiveFile(new TarFile(file));
        } catch (IOException e) {
            // ignore
        }

        return null;
    }

    /**
     * Get the number of checked elements in the array and the children
     *
     * @param elements
     *            the elements to check for checked
     * @return the number of checked elements
     */
    protected int getNbCheckedElements(TracePackageElement[] elements) {
        int totalWork = 0;
        for (TracePackageElement tracePackageElement : elements) {
            TracePackageElement[] children = tracePackageElement.getChildren();
            if (children != null && children.length > 0) {
                totalWork += getNbCheckedElements(children);
            } else if (tracePackageElement.isChecked()) {
                ++totalWork;
            }
        }

        return totalWork;
    }

    /**
     * Returns whether or not the Files element is checked under the given trace
     * package element
     *
     * @param tracePackageElement
     *            the trace package element
     * @return whether or not the Files element is checked under the given trace
     *         package element
     */
    public static boolean isFilesChecked(TracePackageElement tracePackageElement) {
        for (TracePackageElement element : tracePackageElement.getChildren()) {
            if (element instanceof TracePackageFilesElement) {
                return element.isChecked();
            }
        }

        return false;
    }

    /**
     * Common interface between ZipArchiveEntry and TarArchiveEntry
     */
    protected interface ArchiveEntry {
        /**
         * The name of the entry
         *
         * @return The name of the entry
         */
        String getName();
    }

    /**
     * Common interface between ZipFile and TarFile
     */
    protected interface ArchiveFile {
        /**
         * Returns an enumeration cataloging the archive.
         *
         * @return enumeration of all files in the archive
         */
        Enumeration<@NonNull ? extends ArchiveEntry> entries();

        /**
         * Close the file input stream.
         *
         * @throws IOException
         */
        void close() throws IOException;

        /**
         * Returns a new InputStream for the given file in the archive.
         *
         * @param entry
         *            the given file
         * @return an input stream for the given file
         * @throws IOException
         */
        InputStream getInputStream(ArchiveEntry entry) throws IOException;
    }

    /**
     * Adapter for TarFile to ArchiveFile
     */
    protected class TarArchiveFile implements ArchiveFile {

        private TarFile fTarFile;

        /**
         * Constructs a TarAchiveFile for a TarFile
         *
         * @param tarFile
         *            the TarFile
         */
        public TarArchiveFile(TarFile tarFile) {
            this.fTarFile = tarFile;
        }

        @Override
        public Enumeration<@NonNull ? extends ArchiveEntry> entries() {
            Vector<@NonNull ArchiveEntry> v = new Vector<>();
            for (Enumeration<?> e = fTarFile.entries(); e.hasMoreElements();) {
                v.add(new TarArchiveEntryAdapter((TarArchiveEntry) e.nextElement()));
            }

            return v.elements();
        }

        @Override
        public void close() throws IOException {
            fTarFile.close();
        }

        @Override
        public InputStream getInputStream(ArchiveEntry entry) throws IOException {
            return fTarFile.getInputStream(((TarArchiveEntryAdapter) entry).getTarEntry());
        }
    }

    /**
     * Adapter for TarArchiveEntry to ArchiveEntry
     */
    protected class TarArchiveEntryAdapter implements ArchiveEntry {
        private TarArchiveEntry fTarEntry;

        /**
         * Constructs a TarArchiveEntry for a TarArchiveEntry
         *
         * @param tarEntry
         *            the TarArchiveEntry
         */
        public TarArchiveEntryAdapter(TarArchiveEntry tarEntry) {
            this.fTarEntry = tarEntry;
        }

        @Override
        public String getName() {
            return fTarEntry.getName();
        }

        /**
         * Get the corresponding TarArchiveEntry
         *
         * @return the corresponding TarArchiveEntry
         */
        public TarArchiveEntry getTarEntry() {
            return fTarEntry;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    /**
     * Adapter for ArchiveEntry to ArchiveEntry
     */
    protected class ZipAchiveEntryAdapter implements ArchiveEntry {

        private ZipArchiveEntry fZipEntry;

        /**
         * Constructs a ZipAchiveEntryAdapter for a ZipArchiveEntry
         *
         * @param zipEntry
         *            the ZipArchiveEntry
         */
        public ZipAchiveEntryAdapter(ZipArchiveEntry zipEntry) {
            this.fZipEntry = zipEntry;
        }

        @Override
        public String getName() {
            return fZipEntry.getName();
        }

        /**
         * Get the corresponding ZipArchiveEntry
         *
         * @return the corresponding ZipArchiveEntry
         */
        public ZipArchiveEntry getZipEntry() {
            return fZipEntry;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    /**
     * Adapter for ZipFile to ArchiveFile
     */
    protected class ZipArchiveFile implements ArchiveFile {

        private ZipFile fZipFile;

        /**
         * Constructs a ZipArchiveFile for a ZipFile
         *
         * @param zipFile
         *            the ZipFile
         */
        public ZipArchiveFile(ZipFile zipFile) {
            this.fZipFile = zipFile;
        }

        @Override
        public Enumeration<@NonNull ? extends ArchiveEntry> entries() {
            Vector<@NonNull ArchiveEntry> v = new Vector<>();
            for (Enumeration<ZipArchiveEntry> e = fZipFile.getEntries(); e.hasMoreElements();) {
                v.add(new ZipAchiveEntryAdapter(e.nextElement()));
            }

            return v.elements();
        }

        @Override
        public void close() throws IOException {
            fZipFile.close();
        }

        @Override
        public InputStream getInputStream(ArchiveEntry entry) throws IOException {
            return fZipFile.getInputStream(((ZipAchiveEntryAdapter) entry).getZipEntry());
        }
    }

}
