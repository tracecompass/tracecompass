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

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
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
    private static final String BUNDLE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.select_trace_type.bundle"; //$NON-NLS-1$
    private static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.select_trace_type.type"; //$NON-NLS-1$
    private static final String ICON_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.select_trace_type.icon"; //$NON-NLS-1$
    private static final String SELECT_TRACE_TYPE_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.project.trace.select_trace_type"; //$NON-NLS-1$
    private static final String CUSTOM_TXT_CATEGORY = "Custom Text"; //$NON-NLS-1$
    private static final String CUSTOM_XML_CATEGORY = "Custom XML"; //$NON-NLS-1$
    private static final String DEFAULT_TRACE_ICON_PATH = "icons/elcl16/trace.gif"; //$NON-NLS-1$

    @Override
    protected IContributionItem[] getContributionItems() {
        List<IContributionItem> list = new LinkedList<IContributionItem>();

        Map<String, MenuManager> categoriesMap = new HashMap<String, MenuManager>();
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
                TmfTraceType.TMF_TRACE_TYPE_ID);
        for (IConfigurationElement ce : config) {
            if (ce.getName().equals(TmfTraceType.CATEGORY_ELEM)) {
                MenuManager subMenu = new MenuManager(ce.getAttribute(TmfTraceType.NAME_ATTR));
                categoriesMap.put(ce.getAttribute(TmfTraceType.ID_ATTR), subMenu);
                list.add(subMenu);
            }
        }
        if (CustomTxtTraceDefinition.loadAll().length > 0) {
            MenuManager subMenu = new MenuManager(CUSTOM_TXT_CATEGORY);
            categoriesMap.put(CUSTOM_TXT_CATEGORY, subMenu);
            list.add(subMenu);
        }
        if (CustomXmlTraceDefinition.loadAll().length > 0) {
            MenuManager subMenu = new MenuManager(CUSTOM_XML_CATEGORY);
            categoriesMap.put(CUSTOM_XML_CATEGORY, subMenu);
            list.add(subMenu);
        }

        Set<String> selectedTraceTypes = new HashSet<String>();
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
            if (ce.getName().equals(TmfTraceType.TYPE_ELEM)) {
                String traceBundle = ce.getContributor().getName();
                String traceTypeId = ce.getAttribute(TmfTraceType.ID_ATTR);
                String traceIcon = ce.getAttribute(TmfTraceType.ICON_ATTR);
                String label = ce.getAttribute(TmfTraceType.NAME_ATTR).replaceAll("&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
                boolean selected =  selectedTraceTypes.contains(traceTypeId);
                MenuManager subMenu = categoriesMap.get(ce.getAttribute(TmfTraceType.CATEGORY_ATTR));

                addContributionItem(list, traceBundle, traceTypeId, traceIcon, label, selected, subMenu);
            }
        }

        // add the custom trace types
        for (CustomTxtTraceDefinition def : CustomTxtTraceDefinition.loadAll()) {
            String traceBundle = TmfUiPlugin.getDefault().getBundle().getSymbolicName();
            String traceTypeId = CustomTxtTrace.class.getCanonicalName() + ":" + def.definitionName; //$NON-NLS-1$
            String traceIcon = DEFAULT_TRACE_ICON_PATH;
            String label = def.definitionName;
            boolean selected = selectedTraceTypes.contains(traceTypeId);
            MenuManager subMenu = categoriesMap.get(CUSTOM_TXT_CATEGORY);

            addContributionItem(list, traceBundle, traceTypeId, traceIcon, label, selected, subMenu);
        }
        for (CustomXmlTraceDefinition def : CustomXmlTraceDefinition.loadAll()) {
            String traceBundle = TmfUiPlugin.getDefault().getBundle().getSymbolicName();
            String traceTypeId = CustomXmlTrace.class.getCanonicalName() + ":" + def.definitionName; //$NON-NLS-1$
            String traceIcon = DEFAULT_TRACE_ICON_PATH;
            String label = def.definitionName;
            boolean selected = selectedTraceTypes.contains(traceTypeId);
            MenuManager subMenu = categoriesMap.get(CUSTOM_XML_CATEGORY);

            addContributionItem(list, traceBundle, traceTypeId, traceIcon, label, selected, subMenu);
        }

        return list.toArray(new IContributionItem[list.size()]);
    }

    private void addContributionItem(List<IContributionItem> list,
            String traceBundle, String traceTypeId, String traceIcon, String label, boolean selected,
            MenuManager subMenu) {
        Map<String, String> params;
        
        params = new HashMap<String, String>();
        params.put(BUNDLE_PARAMETER, traceBundle);
        params.put(TYPE_PARAMETER, traceTypeId);
        params.put(ICON_PARAMETER, traceIcon);

        ImageDescriptor icon = null;
        if (selected) {
            icon = SELECTED_ICON;
        }

        CommandContributionItemParameter param = new CommandContributionItemParameter(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow(), // serviceLocator
                "my.parameterid", // id //$NON-NLS-1$
                SELECT_TRACE_TYPE_COMMAND_ID, // commandId
                CommandContributionItem.STYLE_PUSH // style
        );
        param.parameters = params;
        param.icon = icon;
        param.disabledIcon = icon;
        param.hoverIcon = icon;
        param.label = label;
        param.visibleEnabled = true;

        if (subMenu != null) {
            subMenu.add(new CommandContributionItem(param));
        } else {
            list.add(new CommandContributionItem(param));
        }
    }
}
