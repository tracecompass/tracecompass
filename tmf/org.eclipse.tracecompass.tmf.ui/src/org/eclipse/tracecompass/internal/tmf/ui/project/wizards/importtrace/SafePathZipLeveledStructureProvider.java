/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.ui.internal.wizards.datatransfer.ZipLeveledStructureProvider;

/**
 * A Zip structure provider that makes sure to return safe paths. For example,
 * if a Zip entry contains a ':' and is extracted on Windows, it will be changed
 * to a '_'
 */
@SuppressWarnings("restriction")
public class SafePathZipLeveledStructureProvider extends ZipLeveledStructureProvider {

    /**
     * Creates a provider which will operate on the passed Zip file.
     *
     * @param sourceFile
     *            The source file to create the provider around
     */
    public SafePathZipLeveledStructureProvider(ZipFile sourceFile) {
        super(sourceFile);
    }

    @Override
    public String getFullPath(Object element) {
        String name = ((ZipEntry) element).getName();
        return ArchiveUtil.toValidNamesPath(name).toOSString();
    }

    @Override
    public String getLabel(Object element) {
        if (element.equals(getRoot())) {
            return ((ZipEntry) element).getName();
        }
        String name = ((ZipEntry) element).getName();
        return stripPath(ArchiveUtil.toValidNamesPath(name).lastSegment());
    }

    /**
     * Strip the leading directories from the path. Copied from
     * {@link ZipLeveledStructureProvider}
     */
    private String stripPath(String path) {
        String strippedPath = path;
        String pathOrig = new String(strippedPath);
        for (int i = 0; i < getStrip(); i++) {
            int firstSep = strippedPath.indexOf('/');
            // If the first character was a separator we must strip to the next
            // separator as well
            if (firstSep == 0) {
                strippedPath = strippedPath.substring(1);
                firstSep = strippedPath.indexOf('/');
            }
            // No separator was present so we're in a higher directory right
            // now
            if (firstSep == -1) {
                return pathOrig;
            }
            strippedPath = strippedPath.substring(firstSep);
        }
        return strippedPath;
    }
}
