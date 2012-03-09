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
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class TmfEditorInput implements IEditorInput {

    private IFile fFile;
    private ITmfTrace<?> fTrace;

    public TmfEditorInput(IFile file, ITmfTrace<?> trace) {
        fFile = file;
        fTrace = trace;
    }
    
    @Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        return null;
    }

    @Override
	public boolean exists() {
        return fFile.exists();
    }

    @Override
	public ImageDescriptor getImageDescriptor() {
        IContentType contentType = IDE.getContentType(fFile);
        return PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(fFile.getName(), contentType);
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
        return fFile.getFullPath().makeRelative().toString();
    }

    public IFile getFile() {
        return fFile;
    }

    public ITmfTrace<?> getTrace() {
        return fTrace;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fFile == null) ? 0 : fFile.getLocation().hashCode());
        result = prime * result + ((fTrace == null) ? 0 : fTrace.getName().hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TmfEditorInput other = (TmfEditorInput) obj;
        if (fFile == null) {
            if (other.fFile != null)
                return false;
        } else if (!fFile.getLocation().equals(other.fFile.getLocation()))
            return false;
        if (fTrace == null) {
            if (other.fTrace != null)
                return false;
        } else if (!fTrace.getName().equals(other.fTrace.getName()))
            return false;
        return true;
    }

}
