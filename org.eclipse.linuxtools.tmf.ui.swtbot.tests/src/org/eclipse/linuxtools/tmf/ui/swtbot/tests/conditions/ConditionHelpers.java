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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * Is a tree node available
 *
 * @author Matthew Khouzam
 */
public abstract class ConditionHelpers {

    /**
     * Is a tree node available
     *
     * @param name
     *            the name of the node
     * @param tree
     *            the parent tree
     * @return true or false, it should swallow all exceptions
     */
    public static ICondition IsTreeNodeAvailable(String name, SWTBotTree tree) {
        return new TreeNodeAvailable(name, tree);
    }

    /**
     * Is the treeItem's node available
     *
     * @param name
     *            the name of the node
     * @param treeItem
     *            the treeItem
     * @return true or false, it should swallow all exceptions
     */
    public static ICondition IsTreeChildNodeAvailable(String name, SWTBotTreeItem treeItem) {
        return new TreeItemNodeAvailable(name, treeItem);
    }

    /**
     * Checks if the wizard's shell is null
     *
     * @param wizard
     *            the null
     * @return false if either are null
     */
    public static ICondition isWizardReady(Wizard wizard) {
        return new WizardReady(wizard);
    }

    /**
     * Is the wizard on the page you want?
     *
     * @param wizard
     *            wizard
     * @param desiredPage
     *            the desired page
     * @return true or false
     */
    public static ICondition isWizardOnPage(Wizard wizard, IWizardPage desiredPage) {
        return new WizardOnPage(wizard, desiredPage);
    }

    /**
     * Wait for a view to close
     *
     * @param view
     *            bot view for the view
     * @return true if the view is closed, false if it's active.
     */
    public static ICondition ViewIsClosed(SWTBotView view) {
        return new ViewClosed(view);
    }

    /**
     * Wait till table cell has a given content.
     *
     * @param table
     *            the table bot reference
     * @param content
     *            the content to check
     * @param row
     *            the row of the cell
     * @param column
     *            the column of the cell
     * @return ICondition for verification
     */
    public static ICondition isTableCellFilled(SWTBotTable table, String content, int row, int column) {
        return new TableCellFilled(table, content, row, column);
    }
}
