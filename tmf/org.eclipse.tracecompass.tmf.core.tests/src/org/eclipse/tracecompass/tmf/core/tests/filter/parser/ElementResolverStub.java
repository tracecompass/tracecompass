package org.eclipse.tracecompass.tmf.core.tests.filter.parser;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IElementResolver;

/**
 * Implements an {@link IElementResolver} for test
 *
 * @author Jean-Christian
 *
 */
public class ElementResolverStub implements IElementResolver {
    private @NonNull Map<@NonNull String, @NonNull String> fData = new HashMap<>();

    /**
     * Constructor
     *
     * @param data
     *            The data to filter on
     */
    public ElementResolverStub(@NonNull Map<@NonNull String, @NonNull String> data) {
        fData.putAll(data);
    }

    @Override
    public @NonNull Map<@NonNull String, @NonNull String> computeData() {
        return fData;
    }

}
