/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.TmfCorePlugin;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

public class SelectTraceTypeContributionItem extends CompoundContributionItem {

    //private static final ImageDescriptor SELECTED_ICON = ImageDescriptor.createFromImage(TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/bullet.gif")); //$NON-NLS-1$
    private static final ImageDescriptor SELECTED_ICON = TmfUiPlugin.getDefault().getImageDescripterFromPath(
            "icons/elcl16/bullet.gif"); //$NON-NLS-1$

    @Override
    protected IContributionItem[] getContributionItems() {
        final String CATEGORY_ELEMENT = "category"; //$NON-NLS-1$
        final String TYPE_ELEMENT = "type"; //$NON-NLS-1$
        final String NAME_ATTRIBUTE = "name"; //$NON-NLS-1$
        final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
        final String BUNDLE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.select_trace_type.bundle"; //$NON-NLS-1$
        final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.select_trace_type.type"; //$NON-NLS-1$
        final String ICON_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.select_trace_type.icon"; //$NON-NLS-1$
        final String SELECT_TRACE_TYPE_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.project.trace.select_trace_type"; //$NON-NLS-1$

        Map<String, String> params;
        LinkedList<IContributionItem> list = new LinkedList<IContributionItem>();

        HashMap<String, MenuManager> categoriesMap = new HashMap<String, MenuManager>();
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
                TmfCorePlugin.TMF_TRACE_TYPE_ID);
        for (IConfigurationElement ce : config) {
            if (ce.getName().equals(CATEGORY_ELEMENT)) {
                MenuManager subMenu = new MenuManager(ce.getAttribute(NAME_ATTRIBUTE));
                categoriesMap.put(ce.getAttribute(ID_ATTRIBUTE), subMenu);
                list.add(subMenu);
            }
        }

        HashSet<String> selectedTraceTypes = new HashSet<String>();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        ISelection selection = page.getSelection();
        if (selection instanceof StructuredSelection) {
            for (Object element : ((StructuredSelection) selection).toList()) {
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement trace = (TmfTraceElement) element;
                    selectedTraceTypes.add(trace.getTraceType());
                }
            }
        }

        for (IConfigurationElement ce : config) {
            if (ce.getName().equals(TYPE_ELEMENT)) {
                String traceBundle = ce.getContributor().getName();
                String traceType = ce.getAttribute(ID_ATTRIBUTE);
                String traceIcon = ce.getAttribute(TmfTraceElement.ICON);
                params = new HashMap<String, String>();
                params.put(BUNDLE_PARAMETER, traceBundle);
                params.put(TYPE_PARAMETER, traceType);
                params.put(ICON_PARAMETER, traceIcon);

                String label = ce.getAttribute(NAME_ATTRIBUTE).replaceAll("&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
                ImageDescriptor icon = null;
                if (selectedTraceTypes.contains(traceType)) {
                    icon = SELECTED_ICON;
                }

                CommandContributionItemParameter param = new CommandContributionItemParameter(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow(), "my.parameterid", // id //$NON-NLS-1$
                        SELECT_TRACE_TYPE_COMMAND_ID, // commandId
                        params, // parameters
                        icon, // icon
                        icon, // disabled icon
                        icon, // hover icon
                        label, // label
                        null, // mnemonic
                        null, // tooltip
                        CommandContributionItem.STYLE_PUSH, // style
                        null, // help context id
                        true // visibleEnable
                );

                MenuManager subMenu = categoriesMap.get(ce.getAttribute(TmfTraceElement.CATEGORY));
                if (subMenu != null) {
                    subMenu.add(new CommandContributionItem(param));
                } else {
                    list.add(new CommandContributionItem(param));
                }
            }
        }

        return list.toArray(new IContributionItem[list.size()]);
    }

}
