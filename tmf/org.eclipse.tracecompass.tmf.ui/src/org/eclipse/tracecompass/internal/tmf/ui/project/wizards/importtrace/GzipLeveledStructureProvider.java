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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;

/**
 * Leveled Structure provider for Gzip file
 */
@SuppressWarnings("restriction")
public class GzipLeveledStructureProvider implements ILeveledImportStructureProvider {

    private final GzipFile fFile;
    private final GzipEntry root = new GzipEntry();
    private final GzipEntry fEntry;

    /**
     * Creates a <code>GzipFileStructureProvider</code>, which will operate on
     * the passed Gzip file.
     *
     * @param sourceFile
     *            the source GzipFile
     */
    public GzipLeveledStructureProvider(GzipFile sourceFile) {
        super();

        fFile = sourceFile;
        fEntry = sourceFile.entries().nextElement();
    }

    @Override
    public List getChildren(Object element) {
        ArrayList<Object> children = new ArrayList<>();
        if (element == root) {
            children.add(fEntry);
        }
        return children;
    }

    @Override
    public InputStream getContents(Object element) {
        return fFile.getInputStream((GzipEntry) element);
    }

    @Override
    public String getFullPath(Object element) {
        String name = ((GzipEntry) element).getName();
        return ArchiveUtil.toValidNamesPath(name).toOSString();
    }

    @Override
    public String getLabel(Object element) {
        if (element != root && element != fEntry) {
            throw new IllegalArgumentException();
        }
        String name = ((GzipEntry) element).getName();
        if (element.equals(root)) {
            return name;
        }

        return ArchiveUtil.toValidNamesPath(name).lastSegment();
    }

    /**
     * Returns the entry that this importer uses as the root sentinel.
     *
     * @return GzipEntry entry
     */
    @Override
    public GzipEntry getRoot() {
        return root;
    }

    @Override
    public boolean closeArchive() {
        try {
            fFile.close();
        } catch (IOException e) {
            Activator.getDefault().logError(DataTransferMessages.ZipImport_couldNotClose
                    + fFile.getName(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean isFolder(Object element) {
        return ((GzipEntry) element).getFileType() == GzipEntry.DIRECTORY;
    }

    @Override
    public void setStrip(int level) {
        // Do nothing
    }

    @Override
    public int getStrip() {
        return 0;
    }
}
