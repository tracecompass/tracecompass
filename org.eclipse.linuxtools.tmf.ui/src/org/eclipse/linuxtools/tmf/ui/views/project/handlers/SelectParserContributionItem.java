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

package org.eclipse.linuxtools.tmf.ui.views.project.handlers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.tmf.ui.TmfUiPlugin;
import org.eclipse.linuxtools.tmf.ui.parsers.ParserProviderManager;
import org.eclipse.linuxtools.tmf.ui.views.project.ProjectView;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfExperimentNode;
import org.eclipse.linuxtools.tmf.ui.views.project.model.TmfTraceNode;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;


public class SelectParserContributionItem extends CompoundContributionItem {

	private static final ImageDescriptor SELECTED_ICON = ImageDescriptor.createFromImage(TmfUiPlugin.getDefault().getImageFromPath("icons/elcl16/bullet.gif")); //$NON-NLS-1$
	
    @Override
    protected IContributionItem[] getContributionItems() {
        Map<String, String> params;
        LinkedList<IContributionItem> list = new LinkedList<IContributionItem>();
        
        ParserProviderManager.getParserMap();

        String parser = null;
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();
        ISelection selection = page.getSelection(ProjectView.ID);
        if (selection instanceof StructuredSelection) {
            Object element = ((StructuredSelection) selection).getFirstElement();
            if (! (element instanceof TmfTraceNode)) {
            	return new IContributionItem[0];
            }
            TmfTraceNode trace = (TmfTraceNode) element;
            IResource resource = trace.getResource();
            if (trace.getParent() instanceof TmfExperimentNode) {
                resource = trace.getProject().getTracesFolder().getTraceForLocation(resource.getLocation()).getResource();
            }
            try {
				parser = resource.getPersistentProperty(ParserProviderManager.PARSER_PROPERTY);
			} catch (CoreException e) {
				e.printStackTrace();
			}
        }

        for(Entry<String, Map<String, String>> providerEntry : ParserProviderManager.getParserMap().entrySet()) {
            MenuManager subMenu = new MenuManager(providerEntry.getKey());
            for(Entry<String, String> entry : providerEntry.getValue().entrySet()) {
                params = new HashMap<String, String>();
                params.put("org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.selectparser.parser", entry.getValue()); //$NON-NLS-1$
    
                ImageDescriptor icon = null;
                if (entry.getValue().equals(parser)) {
                	icon = SELECTED_ICON;
                }
                
                CommandContributionItemParameter param = new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                		"my.parameterid", //$NON-NLS-1$
                        "org.eclipse.linuxtools.tmf.ui.command.project.trace.selectparser", //$NON-NLS-1$
                        params,
                        icon, // icon
                        null, // disabled icon
                        null, // hover icon
                        entry.getKey().replaceAll("&", "&&"), // label  //$NON-NLS-1$//$NON-NLS-2$
                        null, // mnemonic
                        null, // tooltip
                        CommandContributionItem.STYLE_PUSH,
                        null, // help context id
                        true // visibleEnable
                );
                
                subMenu.add(new CommandContributionItem(param));
            }
            list.add(subMenu);
        }

        return (IContributionItem[]) list.toArray(new IContributionItem[list.size()]);
    }

}
