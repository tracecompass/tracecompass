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
 *   Mikael Ferland - Improve validation for function name mapping from a text file
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;

import com.google.common.collect.ImmutableMap;

/**
 * Class containing the different methods to import an address->name mapping.
 *
 * @author Alexandre Montplaisir
 * @deprecated Use the class with same name in the
 *             org.eclipse.tracecompass.analysis.profiling.core plugin
 */
@Deprecated
public final class FunctionNameMapper {

    /**
     * Arbitrary value used to guess the mapping type, if one pattern has this more
     * hits than others, stop
     */
    private static final int DIFF_LIMIT = 10;
    private static final Pattern REMOVE_ZEROS_PATTERN = Pattern.compile("^0+(?!$)"); //$NON-NLS-1$
    private static final Pattern NM_PATTERN = Pattern.compile("([0-9a-f]+)([\\s][a-zA-Z][\\s])(.+)"); //$NON-NLS-1$
    private static final Pattern MAP_WITH_SIZE_PATTERN = Pattern.compile("([0-9a-f]+)[\\s]([a-f0-9]+)[\\s](.+)"); //$NON-NLS-1$

    /**
     * The type of mapping used in a file. Each type of mapping has its pattern and
     * they may overlap
     *
     * @author Geneviève Bastien
     */
    public enum MappingType {
        /**
         * The format of the mapping is the same as the one generated with the nm
         * command
         */
        NM,
        /**
         * The format of the mapping is address size symbol_text
         */
        MAP_WITH_SIZE,
        /**
         * The format is unknown
         */
        UNKNOWN
    }

    private FunctionNameMapper() {
        // No to be instantiated
    }

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
    public static @Nullable Map<@NonNull Long, @NonNull TmfResolvedSymbol> mapFromNmTextFile(File mappingFile) {
        Map<@NonNull Long, @NonNull TmfResolvedSymbol> map = new TreeMap<>();

        try (FileReader fr = new FileReader(mappingFile);
                BufferedReader reader = new BufferedReader(fr);) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Matcher matcher = NM_PATTERN.matcher(line);
                if (matcher.find()) {
                    long address = Long.parseUnsignedLong(stripLeadingZeros(matcher.group(1)), 16);
                    String name = Objects.requireNonNull(matcher.group(3));
                    map.put(address, new TmfResolvedSymbol(address, name));
                }
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            /* Stop reading the file at this point */
        }

        return map.isEmpty() ? null : ImmutableMap.copyOf(map);
    }

    /**
     * Get the function name mapping from a text file formatted as address size
     * name, for example, files obtained using the perf-map-agent for java
     *
     * @param mappingFile
     *            The file to import
     * @return A map&lt;address, function name&gt; of the results
     */
    public static @Nullable Map<@NonNull Long, @NonNull TmfResolvedSymbol> mapFromSizedTextFile(File mappingFile) {
        Map<@NonNull Long, @NonNull TmfResolvedSymbol> map = new TreeMap<>();

        try (FileReader fr = new FileReader(mappingFile);
                BufferedReader reader = new BufferedReader(fr);) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Matcher matcher = MAP_WITH_SIZE_PATTERN.matcher(line);
                if (matcher.find()) {
                    long address = Long.parseUnsignedLong(stripLeadingZeros(matcher.group(1)), 16);
                    long size = Long.parseUnsignedLong(stripLeadingZeros(matcher.group(2)), 16);
                    String name = Objects.requireNonNull(matcher.group(3));
                    map.put(address, new TmfResolvedSizedSymbol(address, name, size));
                }
            }
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            /* Stop reading the file at this point */
        }

        return map.isEmpty() ? null : ImmutableMap.copyOf(map);
    }

    /**
     * Guesses the type of mapping in this file by parsing its line and finding the
     * one with the most hits
     *
     * @param mappingFile
     *            The file containing the symbol mapping
     * @return The most likely mapping type or {@link MappingType#UNKNOWN} if the
     *         file corresponds to no known mapping
     */
    public static MappingType guessMappingType(File mappingFile) {
        int nmHits = 0;
        int sizeHits = 0;
        try (FileReader fr = new FileReader(mappingFile);
                BufferedReader reader = new BufferedReader(fr);) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                Matcher matcher = NM_PATTERN.matcher(line);
                if (matcher.find()) {
                    nmHits++;
                }
                matcher = MAP_WITH_SIZE_PATTERN.matcher(line);
                if (matcher.find()) {
                    sizeHits++;
                }
                if (Math.abs(sizeHits - nmHits) > DIFF_LIMIT) {
                    break;
                }
            }
            // No hits either way, return null
            if (!(nmHits > 0 || sizeHits > 0)) {
                return MappingType.UNKNOWN;
            }
            return nmHits > sizeHits ? MappingType.NM : MappingType.MAP_WITH_SIZE;
        } catch (IOException e) {
            // Simply return unknown type
        }
        return MappingType.UNKNOWN;
    }

    /**
     * Get the function name mapping from an executable binary.
     *
     * @param file
     *            The file to import
     * @return A map&lt;address, function name&gt; of the results
     */
    public static Map<@NonNull Long, @NonNull TmfResolvedSymbol> mapFromBinaryFile(File file) {
        Map<@NonNull Long, @NonNull TmfResolvedSymbol> map = new TreeMap<>();
        IBinaryParser.IBinaryObject binaryObject = getBinaryObject(file);
        if (binaryObject != null) {
            ISymbol[] symbols = binaryObject.getSymbols();
            for (ISymbol symbol : symbols) {
                String address = symbol.getAddress().toHexAddressString();
                Long decodedAddr = Long.decode(address);
                map.put(decodedAddr, new TmfResolvedSymbol(decodedAddr, Objects.requireNonNull(symbol.getName())));
            }
        }

        return ImmutableMap.copyOf(map);
    }

    /**
     * Strip the leading zeroes from the address
     */
    private static String stripLeadingZeros(String address) {
        return REMOVE_ZEROS_PATTERN.matcher(address).replaceFirst(""); //$NON-NLS-1$
    }

    private static IBinaryParser.@Nullable IBinaryObject getBinaryObject(File file) {
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
                    public void handleException(@Nullable Throwable exception) {
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
            try (InputStream is = new FileInputStream(file)) {

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
                    if (binFile instanceof IBinaryParser.IBinaryObject) {
                        return (IBinaryParser.IBinaryObject) binFile;
                    }
                } catch (IOException e) {
                    Activator.logError("Error parsing binary file", e); //$NON-NLS-1$
                }
            }
        }

        return null;
    }

}
