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

import org.eclipse.ui.internal.wizards.datatransfer.ILeveledImportStructureProvider;

/**
 * An import provider that both supports using IFileSystemObject and adds
 * "archive functionality" by delegating to a leveled import provider
 * (TarLeveledStructureProvider, ZipLeveledStructureProvider)
 */
@SuppressWarnings("restriction")
class FileSystemObjectLeveledImportStructureProvider extends FileSystemObjectImportStructureProvider implements ILeveledImportStructureProvider {

    private ILeveledImportStructureProvider fLeveledImportProvider;

    FileSystemObjectLeveledImportStructureProvider(ILeveledImportStructureProvider importStructureProvider, String archivePath) {
        super(importStructureProvider, archivePath);
        fLeveledImportProvider = importStructureProvider;
    }

    @Override
    public IFileSystemObject getRoot() {
        return getIFileSystemObject(fLeveledImportProvider.getRoot());
    }

    @Override
    public void setStrip(int level) {
        fLeveledImportProvider.setStrip(level);
    }

    @Override
    public int getStrip() {
        return fLeveledImportProvider.getStrip();
    }

    @Override
    public boolean closeArchive() {
        return fLeveledImportProvider.closeArchive();
    }

    @Override
    public void dispose() {
        super.dispose();
        closeArchive();
    }
}