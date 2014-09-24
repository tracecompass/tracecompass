/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

/**
 * Is a table cell filled? tests if the table cell has a given content.
 *
 * @author Bernd Hufmann
 */
class TableCellFilled implements ICondition {

    private final SWTBotTable fTable;
    private final String fContent;
    private final int fRow;
    private final int fColumn;

    public TableCellFilled(SWTBotTable table, String content, int row, int column) {
        fTable = table;
        fContent = content;
        fRow = row;
        fColumn = column;
    }

    @Override
    public boolean test() throws Exception {
        try {
            return fContent.equals(fTable.cell(fRow, fColumn));
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
