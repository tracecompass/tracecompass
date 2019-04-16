package org.eclipse.tracecompass.tmf.core.tests.filter.parser;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IElementResolver;

import com.google.common.collect.Multimap;

/**
 * Implements an {@link IElementResolver} for test
 *
 * @author Jean-Christian
 *
 */
public class ElementResolverStub implements IElementResolver {
    private final @NonNull Multimap<@NonNull String, @NonNull String> fData;

    /**
     * Constructor
     *
     * @param data
     *            The data to filter on
     */
    public ElementResolverStub(@NonNull Multimap<@NonNull String, @NonNull String> data) {
        fData = data;
    }

    @Override
    public @NonNull Multimap<@NonNull String, @NonNull String> getMetadata() {
        return fData;
    }

    @Deprecated
    @Override
    public @NonNull Map<@NonNull String, @NonNull String> computeData() {
        return Collections.emptyMap();
    }

}
