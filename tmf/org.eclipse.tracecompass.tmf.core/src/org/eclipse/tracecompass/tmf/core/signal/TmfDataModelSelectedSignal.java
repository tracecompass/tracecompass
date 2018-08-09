package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.Multimap;

/**
 * Signal to use when an object that has metadata has been selected. This
 * metadata can be used by other application component to act on that selection
 * when they themselves involve objects with metadata.
 *
 * @author Genevieve Bastien
 * @since 4.2
 */
public class TmfDataModelSelectedSignal extends TmfSignal {

    private final @NonNull Multimap<@NonNull String, @NonNull String> fMetadata;

    /**
     * Constructor
     *
     * @param source
     *            The source of the signal
     * @param metadata
     *            The metadata corresponding to the model object being selected
     */
    public TmfDataModelSelectedSignal(Object source, @NonNull Multimap<@NonNull String, @NonNull String> metadata) {
        super(source);
        fMetadata = metadata;
    }

    /**
     * Get the metadata of the selected object
     *
     * @return The metadata of the selected object
     */
    public @NonNull Multimap<@NonNull String, @NonNull String> getMetadata() {
        return fMetadata;
    }

}
