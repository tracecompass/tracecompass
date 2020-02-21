/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.commands;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.google.common.base.Joiner;

/**
 * Utility class to export data to TSV file
 */
public class ExportToTsvUtils {

    private ExportToTsvUtils() {
        // Private constructor
    }

    /**
     * Export content of a tree to TSV file
     * @param tree
     *              the tree to export
     * @param stream
     *              the output stream
     */
    public static void exportTreeToTsv(@Nullable Tree tree, @Nullable OutputStream stream) {
        if (tree == null || stream == null) {
            return;
        }
        try (PrintWriter pw = new PrintWriter(stream)) {
            int size = tree.getItemCount();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < tree.getColumnCount(); i++) {
                String valueOf = String.valueOf(tree.getColumn(i).getText());
                if (valueOf.isEmpty() && i == tree.getColumnCount() - 1) {
                    // Linux "feature", an invisible column is added at the end
                    // with gtk2
                    break;
                }
                columns.add(valueOf);
            }
            String join = Joiner.on('\t').skipNulls().join(columns);
            pw.println(join);
            for (int i = 0; i < size; i++) {
                TreeItem item = tree.getItem(i);
                printItem(pw, columns, item);
            }
        }
    }

    private static void printItem(PrintWriter pw, List<String> columns, @Nullable TreeItem item) {
        if (item == null) {
            return;
        }
        List<String> data = new ArrayList<>();
        for (int col = 0; col < columns.size(); col++) {
            data.add(String.valueOf(item.getText(col)));
        }
        pw.println(Joiner.on('\t').join(data));
        for (TreeItem child : item.getItems()) {
            printItem(pw, columns, child);
        }
    }
    /**
     * Export content of a table to TSV file
     * @param table
     *              the table to export
     * @param stream
     *              the output stream
     */
    public static void exportTableToTsv(Table table, @Nullable OutputStream stream) {
        if (table == null || stream == null) {
            return;
        }
        try (PrintWriter pw = new PrintWriter(stream)) {
            int size = table.getItemCount();
            List<String> columns = new ArrayList<>();
            for (int i = 0; i < table.getColumnCount(); i++) {
                TableColumn column = table.getColumn(i);
                if (column == null) {
                    return;
                }
                String columnName = String.valueOf(column.getText());
                if (columnName.isEmpty() && i == table.getColumnCount() - 1) {
                    // Linux GTK2 undocumented feature
                    break;
                }
                columns.add(columnName);
            }
            pw.println(Joiner.on('\t').join(columns));
            for (int i = 0; i < size; i++) {
                TableItem item = table.getItem(i);
                if (item == null) {
                    continue;
                }
                List<String> data = new ArrayList<>();
                for (int col = 0; col < columns.size(); col++) {
                    data.add(String.valueOf(item.getText(col)));
                }
                pw.println(Joiner.on('\t').join(data));
            }
        }
    }

}
