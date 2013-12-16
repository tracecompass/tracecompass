/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Implement getParent()
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

/**
 * The TMF project content provider for the tree viewer in the project explorer view.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfNavigatorContentProvider implements IPipelinedTreeContentProvider {

    // ------------------------------------------------------------------------
    // ICommonContentProvider
    // ------------------------------------------------------------------------

    @Override
    public Object[] getElements(Object inputElement) {
        return null;
    }

    @Override
    public Object getParent(Object element) {
        if (element instanceof IProject) {
            IProject project = (IProject) element;
            return project.getParent();
        }

        if (element instanceof TmfTraceFolder) {
            TmfTraceFolder folder = (TmfTraceFolder) element;
            // Return the corresponding IProject as parent because from CNF point of view the IProject is the parent.
            // The IProject is needed e.g. for link with Editor to work correctly.
            return folder.getParent().getResource();
        }

        if (element instanceof TmfTraceElement) {
            TmfTraceElement traceElement = (TmfTraceElement) element;
            return traceElement.getParent();
        }

        if (element instanceof TmfExperimentFolder) {
            TmfExperimentFolder folder = (TmfExperimentFolder) element;
            // Return the corresponding IProject as parent because from CNF point of view the IProject is the parent.
            // The IProject is needed e.g. for link with Editor to work correctly.
            return folder.getParent().getResource();
        }

        if (element instanceof TmfExperimentElement) {
            TmfExperimentElement expElement = (TmfExperimentElement) element;
            return expElement.getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        if (element instanceof IProject) {
            IProject project = (IProject) element;
            return project.isAccessible();
        }
        if (element instanceof TmfProjectModelElement) {
            TmfProjectModelElement modelElement = (TmfProjectModelElement) element;
            return modelElement.hasChildren();
        }
        return false;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void restoreState(IMemento aMemento) {
    }

    @Override
    public void saveState(IMemento aMemento) {
    }

    @Override
    public void init(ICommonContentExtensionSite aConfig) {
    }

    // ------------------------------------------------------------------------
    // ICommonContentProvider - getChildren()
    // ------------------------------------------------------------------------

    @Override
    public synchronized Object[] getChildren(Object parentElement) {

        // Tracing project level
        if (parentElement instanceof IProject) {
            TmfProjectElement element = TmfProjectRegistry.getProject((IProject) parentElement, true);
            return element.getChildren().toArray();
        }

        // Other project model elements
        if (parentElement instanceof ITmfProjectModelElement) {
            return ((ITmfProjectModelElement) parentElement).getChildren().toArray();
        }

        return new Object[0];
    }

    // ------------------------------------------------------------------------
    // IPipelinedTreeContentProvider
    // ------------------------------------------------------------------------

    @Override
    public void getPipelinedChildren(Object parent, Set currentChildren) {
        customizeTmfElements(getChildren(parent), currentChildren);
    }

    @Override
    public void getPipelinedElements(Object input, Set currentElements) {
        customizeTmfElements(getElements(input), currentElements);
    }

    /**
     * Add/replace the ITmfProjectElement to the list of children
     *
     * @param elements
     *            the list returned by getChildren()
     * @param children
     *            the current children
     */
    private static void customizeTmfElements(Object[] elements,
            Set<Object> children) {
        if (elements != null && children != null) {
            for (Object element : elements) {
                if (element instanceof ITmfProjectModelElement) {
                    ITmfProjectModelElement tmfElement = (ITmfProjectModelElement) element;
                    IResource resource = tmfElement.getResource();
                    if (resource != null) {
                        children.remove(resource);
                    }
                    children.add(element);
                }
                else if (element != null) {
                    children.add(element);
                }
            }
        }
    }

    @Override
    public Object getPipelinedParent(Object anObject, Object aSuggestedParent) {
        return aSuggestedParent;
    }

    @Override
    public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification) {
        return anAddModification;
    }

    @Override
    public PipelinedShapeModification interceptRemove(PipelinedShapeModification aRemoveModification) {
        return null;
    }

    @Override
    public boolean interceptRefresh(PipelinedViewerUpdate aRefreshSynchronization) {
        return false;
    }

    @Override
    public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization) {
        return false;
    }
}
