/*******************************************************************************
 * Copyright (c) 2011, 2017 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Moved SelectTraceTypeContributionItem to this class
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypeHelper;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * ContributionItem for the element type selection.
 *
 * @author Patrick Tassé
 */
public class SelectElementTypeContributionItem extends CompoundContributionItem {

    private final static class ItemComparator implements Comparator<IContributionItem> {
        @Override
        public int compare(IContributionItem o1, IContributionItem o2) {
            if (o1 instanceof MenuManager) {
                if (o2 instanceof MenuManager) {
                    MenuManager m1 = (MenuManager) o1;
                    MenuManager m2 = (MenuManager) o2;
                    return m1.getMenuText().compareTo(m2.getMenuText());
                }
                return -1;
            }
            if (o2 instanceof MenuManager) {
                return 1;
            }
            CommandContributionItem c1 = (CommandContributionItem) o1;
            CommandContributionItem c2 = (CommandContributionItem) o2;
            return c1.getData().label.compareTo(c2.getData().label);
        }
    }

    private static final Comparator<IContributionItem> ITEM_COMPARATOR = new ItemComparator();
    private static final ImageDescriptor SELECTED_ICON = Activator.getDefault().getImageDescripterFromPath("icons/elcl16/bullet.gif"); //$NON-NLS-1$
    private static final String TYPE_PARAMETER = "org.eclipse.linuxtools.tmf.ui.commandparameter.select_trace_type.type"; //$NON-NLS-1$
    private static final String SELECT_TRACE_TYPE_COMMAND_ID = "org.eclipse.linuxtools.tmf.ui.command.select_trace_type"; //$NON-NLS-1$

    @Override
    protected IContributionItem[] getContributionItems() {

        /*
         * Fill the selected trace types and verify if selection applies only to
         * either traces or experiments
         */
        Set<String> selectedTraceTypes = new HashSet<>();
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        ISelection selection = page.getSelection();
        boolean forTraces = false, forExperiments = false;
        if (selection instanceof StructuredSelection) {
            for (Object element : ((StructuredSelection) selection).toList()) {
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement trace = (TmfTraceElement) element;
                    selectedTraceTypes.add(trace.getTraceType());
                    forTraces = true;
                } else if (element instanceof TmfExperimentElement) {
                    TmfExperimentElement exp = (TmfExperimentElement) element;
                    selectedTraceTypes.add(exp.getTraceType());
                    forExperiments = true;
                }
            }
        }

        if (forTraces && forExperiments) {
            /* This should never happen anyways */
            throw new RuntimeException("You must select only experiments or only traces to set the element type"); //$NON-NLS-1$
        }

        return getContributionItems(selectedTraceTypes, forExperiments);
    }

    /**
     * Get the contribution items for traces
     *
     * @param selectedTraceTypes
     *            The set of selected trace types
     * @param forExperiments
     *            <code>true</code> if the contribution items are requested for
     *            experiments, <code>false</code> for traces
     *
     * @return The list of contribution items
     */
    protected IContributionItem[] getContributionItems(Set<String> selectedTraceTypes, boolean forExperiments) {

        List<IContributionItem> list = new LinkedList<>();

        Map<String, MenuManager> categoriesMap = new HashMap<>();
        for (TraceTypeHelper traceTypeHelper : TmfTraceType.getTraceTypeHelpers()) {
            if (forExperiments != traceTypeHelper.isExperimentType()) {
                continue;
            }

            String categoryName = traceTypeHelper.getCategoryName().replaceAll("&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
            MenuManager subMenu = null;
            if (!categoryName.isEmpty()) {
                subMenu = categoriesMap.get(categoryName);
                if (subMenu == null) {
                    subMenu = new MenuManager(categoryName);
                    categoriesMap.put(categoryName, subMenu);
                    list.add(subMenu);
                }
            }

            String traceTypeId = traceTypeHelper.getTraceTypeId();
            String label = traceTypeHelper.getName().replaceAll("&", "&&"); //$NON-NLS-1$ //$NON-NLS-2$
            boolean selected = selectedTraceTypes.contains(traceTypeId);
            if (selected && subMenu != null) {
                subMenu.setImageDescriptor(SELECTED_ICON);
            }

            addContributionItem(list, traceTypeId, label, selected, subMenu);
        }

        Collections.sort(list, ITEM_COMPARATOR);
        return list.toArray(new IContributionItem[list.size()]);
    }

    private static void addContributionItem(List<IContributionItem> list,
            String traceTypeId, String label, boolean selected, MenuManager subMenu) {
        Map<String, String> params = new HashMap<>();
        params.put(TYPE_PARAMETER, traceTypeId);

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
            CommandContributionItem item = new CommandContributionItem(param);
            int i = Collections.binarySearch(Arrays.asList(subMenu.getItems()), item, ITEM_COMPARATOR);
            i = (i >= 0) ? i : -i - 1;
            subMenu.insert(i, item);
        } else {
            list.add(new CommandContributionItem(param));
        }
    }
}
