/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.callstack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Class containing the different methods to import an address->name mapping.
 *
 * @author Alexandre Montplaisir
 */
class FunctionNameMapper {

    public static @Nullable Map<String, String> mapFromNmTextFile(File mappingFile) {
        Map<String, String> map = new HashMap<String, String>();

        FileReader fr;
        try {
            fr = new FileReader(mappingFile);
        } catch (FileNotFoundException e) {
            return null;
        }
        BufferedReader reader = new BufferedReader(fr);

        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] elems = line.split(" "); //$NON-NLS-1$
                /* Only lines with 3 elements contain addresses */
                if (elems.length == 3) {
                    /* Strip the leading zeroes from the address */
                    String address = elems[0].replaceFirst("^0+(?!$)", ""); //$NON-NLS-1$ //$NON-NLS-2$;
                    String name = elems[elems.length - 1];
                    map.put(address, name);
                }
            }

        } catch (IOException e) {
            /* Stop reading the file at this point */
        } finally {
            try {
                reader.close();
            } catch (IOException e) {}
        }

        if (map.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableMap(map);
    }

}
