/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * Is a tree node available?
 *
 * @author Matthew Khouzam
 */
class TreeNodeAvailable implements ICondition {

    private final SWTBotTree fTree;
    private final String fName;

    /**
     * Is a tree node available
     *
     * @param name
     *            The name of the node
     * @param tree
     *            the swtbotTree
     */
    public TreeNodeAvailable(String name, SWTBotTree tree) {
        fTree = tree;
        fName = name;
    }

    @Override
    public boolean test() throws Exception {
        try {
            final SWTBotTreeItem[] treeItems = fTree.getAllItems();
            for( SWTBotTreeItem ti : treeItems){
                final String text = ti.getText();
                if( text.equals(fName)) {
                    return true;
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public void init(SWTBot bot) {
    }

    @Override
    public String getFailureMessage() {
        return null;
    }

}
