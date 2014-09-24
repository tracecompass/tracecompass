/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Add support for unknown trace type icon
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType.TraceElementType;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.osgi.framework.Bundle;

/**
 * The TMF project label provider for the tree viewer in the project explorer view.
 * <p>
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfNavigatorLabelProvider implements ICommonLabelProvider, IStyledLabelProvider {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final Image fFolderIcon = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    private static final String fTraceIconFile = "icons/elcl16/trace.gif"; //$NON-NLS-1$
    private static final String fExperimentIconFile = "icons/elcl16/experiment.gif"; //$NON-NLS-1$
    private static final String fAnalysisIconFile = "icons/ovr16/experiment_folder_ovr.png"; //$NON-NLS-1$
    private static final String fViewIconFile = "icons/obj16/node_obj.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Image fTraceFolderIcon = fFolderIcon;
    private final Image fExperimentFolderIcon = fFolderIcon;

    private final Image fDefaultTraceIcon;
    private final Image fExperimentIcon;
    private final Image fDefaultAnalysisIcon;
    private final Image fDefaultViewIcon;

    private final WorkbenchLabelProvider fWorkspaceLabelProvider = new WorkbenchLabelProvider();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor.
     *
     * Creates the TMF navigator content provider.
     */
    public TmfNavigatorLabelProvider() {
        Bundle bundle = Activator.getDefault().getBundle();
        fDefaultTraceIcon = loadIcon(bundle, fTraceIconFile);
        fExperimentIcon = loadIcon(bundle, fExperimentIconFile);
        fDefaultAnalysisIcon = loadIcon(bundle, fAnalysisIconFile);
        fDefaultViewIcon = loadIcon(bundle, fViewIconFile);
    }

    private static Image loadIcon(Bundle bundle, String url) {
        Activator plugin = Activator.getDefault();
        String key = bundle.getSymbolicName() + "/" + url; //$NON-NLS-1$
        Image icon = plugin.getImageRegistry().get(key);
        if (icon == null) {
            URL imageURL = bundle.getResource(url);
            ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
            if (descriptor != null) {
                icon = descriptor.createImage();
                plugin.getImageRegistry().put(key, icon);
            }
        }
        return icon;
    }

    // ------------------------------------------------------------------------
    // ICommonLabelProvider
    // ------------------------------------------------------------------------

    @Override
    public Image getImage(Object element) {

        if (element instanceof TmfCommonProjectElement) {
            TmfCommonProjectElement trace = (TmfCommonProjectElement) element;
            String traceType = trace.getTraceType();
            if (traceType == null || TmfTraceType.getTraceType(traceType) == null) {
                // request the label to the Eclipse platform
                return fWorkspaceLabelProvider.getImage(((TmfCommonProjectElement) element).getResource());
            }

            IConfigurationElement traceUIAttributes = TmfTraceTypeUIUtils.getTraceUIAttributes(traceType, (element instanceof TmfTraceElement) ? TraceElementType.TRACE : TraceElementType.EXPERIMENT);
            if (traceUIAttributes != null) {
                String iconAttr = traceUIAttributes.getAttribute(TmfTraceTypeUIUtils.ICON_ATTR);
                if (iconAttr != null) {
                    String name = traceUIAttributes.getContributor().getName();
                    if (name != null) {
                        Bundle bundle = Platform.getBundle(name);
                        if (bundle != null) {
                            Image image = loadIcon(bundle, iconAttr);
                            if (image != null) {
                                return image;
                            }
                        }
                    }
                }

            }
            if (element instanceof TmfTraceElement) {
                return fDefaultTraceIcon;
            }
            return fExperimentIcon;
        }

        if (element instanceof TmfExperimentFolder) {
            return fExperimentFolderIcon;
        }

        if (element instanceof TmfTraceFolder) {
            return fTraceFolderIcon;
        }

        if (element instanceof TmfAnalysisOutputElement) {
            TmfAnalysisOutputElement output = (TmfAnalysisOutputElement) element;
            Image icon = output.getIcon();
            if (icon == null) {
                return fDefaultViewIcon;
            }
            return icon;
        }

        if (element instanceof TmfAnalysisElement) {
            TmfAnalysisElement analysis = (TmfAnalysisElement) element;
            String iconFile = analysis.getIconFile();
            if (iconFile != null) {
                Bundle bundle = analysis.getBundle();
                if (bundle != null) {
                    Image icon = loadIcon(bundle, iconFile);
                    return icon;
                }
            }
            return fDefaultAnalysisIcon;
        }

        return null;
    }

    @Override
    public String getText(Object element) {

        if (element instanceof TmfTracesFolder) {
            TmfTracesFolder folder = (TmfTracesFolder) element;
            return folder.getName() + " [" + folder.getTraces().size() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (element instanceof TmfTraceFolder) {
            TmfTraceFolder folder = (TmfTraceFolder) element;
            int nbTraces = folder.getTraces().size();
            if (nbTraces > 0) {
                return folder.getName() + " [" + folder.getTraces().size() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
            }
            return folder.getName();
        }

        if (element instanceof TmfTraceElement) {
            TmfTraceElement trace = (TmfTraceElement) element;
            if (trace.getParent() instanceof TmfExperimentElement) {
                return trace.getElementPath();
            }
            return trace.getName();
        }

        if (element instanceof TmfExperimentElement) {
            TmfExperimentElement folder = (TmfExperimentElement) element;
            return folder.getName() + " [" + folder.getTraces().size() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (element instanceof TmfExperimentFolder) {
            TmfExperimentFolder folder = (TmfExperimentFolder) element;
            return folder.getName() + " [" + folder.getChildren().size() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Catch all
        if (element instanceof ITmfProjectModelElement) {
            return ((ITmfProjectModelElement) element).getName();
        }

        return null;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public void restoreState(IMemento aMemento) {
    }

    @Override
    public void saveState(IMemento aMemento) {
    }

    @Override
    public String getDescription(Object anElement) {
        return getText(anElement);
    }

    @Override
    public void init(ICommonContentExtensionSite aConfig) {
    }

    /**
     * @since 3.0
     */
    @Override
    public StyledString getStyledText(Object element) {
        String text = getText(element);
        if (text != null) {
            if (element instanceof ITmfStyledProjectModelElement) {
                Styler styler = ((ITmfStyledProjectModelElement) element).getStyler();
                if (styler != null) {
                    return new StyledString(text, styler);
                }
            }
            return new StyledString(text);
        }
        return null;
    }

}
