/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Marc-Andre Laperle - Map from binary file
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.callstack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;

/**
 * Class containing the different methods to import an address->name mapping.
 *
 * @author Alexandre Montplaisir
 */
public class FunctionNameMapper {

    /**
     * Get the function name mapping from a text file obtained by doing
     *
     * <pre>
     * nm[--demangle][binary] &gt; file.txt
     * </pre>
     *
     * @param mappingFile
     *            The file to import
     * @return A map&lt;address, function name&gt; of the results
     */
    public static @Nullable Map<String, String> mapFromNmTextFile(File mappingFile) {
        Map<String, String> map = new HashMap<>();

        try (FileReader fr = new FileReader(mappingFile);
                BufferedReader reader = new BufferedReader(fr);) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                /* Only lines with 3 elements contain addresses */
                String[] elems = line.split(" ", 3); //$NON-NLS-1$
                if (elems.length == 3) {
                    String address = stripLeadingZeros(elems[0]);
                    String name = elems[elems.length - 1];
                    map.put(address, name);
                }
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            /* Stop reading the file at this point */
        }

        if (map.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Strip the leading zeroes from the address
     * */
    private static String stripLeadingZeros(String address) {
        return address.replaceFirst("^0+(?!$)", "");  //$NON-NLS-1$ //$NON-NLS-2$;
    }

    /**
     * Get the function name mapping from an executable binary.
     *
     * @param file
     *            The file to import
     * @return A map&lt;address, function name&gt; of the results
     */
    public static @Nullable Map<String, String> mapFromBinaryFile(File file) {
        Map<String, String> map = new HashMap<>();
        IBinaryParser.IBinaryObject binaryObject = getBinaryObject(file);
        if (binaryObject != null) {
            ISymbol[] symbols = binaryObject.getSymbols();
            for (ISymbol symbol : symbols) {
                String address = symbol.getAddress().toHexAddressString();
                /* Remove "0x" */
                address = address.substring(2);
                /* Strip the leading zeroes from the address */
                address = stripLeadingZeros(address);
                map.put(address, symbol.getName());
            }
        }

        return map;
    }

    private static @Nullable IBinaryParser.IBinaryObject getBinaryObject(File file) {
        IPath filePath = new Path(file.toString());

        /* Get all the available binary parsers */
        final List<IBinaryParser> binaryParsers = new ArrayList<>();
        IConfigurationElement[] elements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(CCorePlugin.BINARY_PARSER_UNIQ_ID);
        for (IConfigurationElement element : elements) {
            IConfigurationElement[] children = element.getChildren("run"); //$NON-NLS-1$
            for (final IConfigurationElement run : children) {
                SafeRunner.run(new ISafeRunnable() {
                    @Override
                    public void run() throws Exception {
                        IBinaryParser binaryParser = (IBinaryParser) run.createExecutableExtension("class"); //$NON-NLS-1$
                        binaryParsers.add(binaryParser);
                    }

                    @Override
                    public void handleException(Throwable exception) {
                        Activator.logError("Error creating binary parser", exception); //$NON-NLS-1$
                    }
                });
            }
        }

        /* Find the maximum "hint" buffer size we'll need from all the parsers */
        int hintBufferSize = 0;
        for (IBinaryParser parser : binaryParsers) {
            if (parser.getHintBufferSize() > hintBufferSize) {
                hintBufferSize = Math.max(hintBufferSize, parser.getHintBufferSize());
            }
        }

        /* Read the initial "hint" bytes */
        byte[] hintBuffer = new byte[hintBufferSize];
        if (hintBufferSize > 0) {
            try (InputStream is = new FileInputStream(file) ){

                int count = 0;
                // Make sure we read up to 'hints' bytes if we possibly can
                while (count < hintBufferSize) {
                    int bytesRead = is.read(hintBuffer, count, hintBufferSize - count);
                    if (bytesRead < 0) {
                        break;
                    }
                    count += bytesRead;
                }
                if (count > 0 && count < hintBuffer.length) {
                    byte[] array = new byte[count];
                    System.arraycopy(hintBuffer, 0, array, 0, count);
                    hintBuffer = array;
                }
            } catch (IOException e) {
                Activator.logError("Error reading initial bytes of binary file", e); //$NON-NLS-1$
                return null;
            }
        }

        /* For all binary parsers, try to get a binary object */
        for (IBinaryParser parser : binaryParsers) {
            if (parser.isBinary(hintBuffer, filePath)) {
                IBinaryFile binFile;
                try {
                    binFile = parser.getBinary(hintBuffer, filePath);
                    if (binFile != null && binFile instanceof IBinaryParser.IBinaryObject) {
                        return (IBinaryParser.IBinaryObject)binFile;
                    }
                } catch (IOException e) {
                    Activator.logError("Error parsing binary file", e); //$NON-NLS-1$
                }
            }
        }

        return null;
    }

}
