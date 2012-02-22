/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;

public class TmfEditorInput implements IEditorInput {

    private IResource fResource;
    private ITmfTrace<?> fTrace;

    public TmfEditorInput(IResource resource, ITmfTrace<?> trace) {
        fResource = resource;
        fTrace = trace;
    }
    
    @Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        return null;
    }

    @Override
	public boolean exists() {
        return fResource.exists();
    }

    @Override
	public ImageDescriptor getImageDescriptor() {
        if (fResource instanceof IFile) {
            IFile file = (IFile) fResource;
            IContentType contentType = IDE.getContentType(file);
            return PlatformUI.getWorkbench().getEditorRegistry()
                    .getImageDescriptor(file.getName(), contentType);
        }
        return null;
    }

    @Override
	public String getName() {
        return fTrace.getName();
    }

    @Override
	public IPersistableElement getPersistable() {
        return null;
    }

    @Override
	public String getToolTipText() {
        return fResource.getFullPath().makeRelative().toString();
    }

    public IResource getResource() {
        return fResource;
    }
    
    public ITmfTrace<?> getTrace() {
        return fTrace;
    }

    @Override
	public boolean equals(Object obj) {
        if (obj instanceof TmfEditorInput) {
            return fResource.equals(((TmfEditorInput) obj).fResource);
        } else if (obj instanceof IFileEditorInput) {
            return ((IFileEditorInput) obj).getFile().equals(fResource);
        } else if (obj instanceof FileStoreEditorInput) {
            return ((FileStoreEditorInput) obj).getURI().equals(fResource.getRawLocationURI());
        }
        return false;
    }
}
