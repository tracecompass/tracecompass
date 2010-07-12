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

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.linuxtools.tmf.ui.parsers.ParserProviderManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;


public class SelectParserContributionItem extends CompoundContributionItem {

    @Override
    protected IContributionItem[] getContributionItems() {
        Map<String, String> params;
        LinkedList<IContributionItem> list = new LinkedList<IContributionItem>();
        
        ParserProviderManager.getParserMap();
        
        for(Entry<String, Map<String, String>> providerEntry : ParserProviderManager.getParserMap().entrySet()) {
            MenuManager subMenu = new MenuManager(providerEntry.getKey());
            for(Entry<String, String> entry : providerEntry.getValue().entrySet()) {
                params = new HashMap<String, String>();
                params.put("org.eclipse.linuxtools.tmf.ui.commandparameter.project.trace.selectparser.parser", entry.getValue());
    
                CommandContributionItemParameter param = new CommandContributionItemParameter(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), "my.parameterid",
                        "org.eclipse.linuxtools.tmf.ui.command.project.trace.selectparser",
                        params,
                        null, // icon
                        null, // disabled icon
                        null, // hover icon
                        entry.getKey().replaceAll("&", "&&"), // label
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
