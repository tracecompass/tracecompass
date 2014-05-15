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

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace;

import java.io.File;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * Trace label provider for the candidate tree
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
class ImportTraceLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
        if (element instanceof String) {
            return (String) element;
        }
        if (element instanceof FileAndName) {
            final File file = ((FileAndName) element).getFile();
            if (file != null) { // should never not happen since file is final
                                // and always set automatically
                return file.getName();
            }
        }
        return null;
    }
}
