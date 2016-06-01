/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNullContents;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.internal.analysis.lami.core.Activator;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.LamiStrings;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.ShellUtils;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiDurationAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiEmptyAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiGenericAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiIRQNameAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiIRQNumberAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiIRQTypeAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiMixedAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiProcessNameAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiProcessPIDAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiProcessTIDAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTableEntryAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTimeRangeBeginAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTimeRangeDurationAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTimeRangeEndAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.aspect.LamiTimestampAspect;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiData.DataType;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiTimeRange;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysis;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Base class for analysis modules that call external scripts implementing the
 * LAMI protocol.
 *
 * @author Alexandre Montplaisir
 */
public class LamiAnalysis implements IOnDemandAnalysis {

    private static final Logger LOGGER = TraceCompassLog.getLogger(LamiAnalysis.class);

    /**
     * Maximum major version of the LAMI protocol we support.
     *
     * Currently only 0.x/unversioned MI, outputted by lttng-analyses 0.4.x
     */
    private static final int MAX_SUPPORTED_MAJOR_VERSION = 0;

    private static final String DOUBLE_QUOTES = "\""; //$NON-NLS-1$

    /* Flags passed to the analysis scripts */
    private static final String METADATA_FLAG = "--metadata"; //$NON-NLS-1$
    private static final String PROGRESS_FLAG = "--output-progress"; //$NON-NLS-1$
    private static final String BEGIN_FLAG = "--begin"; //$NON-NLS-1$
    private static final String END_FLAG = "--end"; //$NON-NLS-1$

    private final List<String> fScriptCommand;

    /**
     * The LAMI analysis is considered initialized after we have read the
     * script's --metadata once. This will assign the fields below.
     */
    private boolean fInitialized = false;

    private boolean fIsAvailable;
    private final String fName;
    private final boolean fIsUserDefined;
    private final Predicate<ITmfTrace> fAppliesTo;

    /* Data defined by the analysis's metadata */
    private @Nullable String fAnalysisTitle;
    private @Nullable Map<String, LamiTableClass> fTableClasses;
    private boolean fUseProgressOutput;

    /**
     * Constructor. To be called by implementing classes.
     *
     * @param name
     *            Name of this analysis
     * @param isUserDefined
     *            {@code true} if this is a user-defined analysis
     * @param appliesTo
     *            Predicate to use to check whether or not this analysis applies
     *            to a given trace
     * @param args
     *            Analysis arguments, including the executable name (first
     *            argument)
     */
    public LamiAnalysis(String name, boolean isUserDefined, Predicate<ITmfTrace> appliesTo,
            List<String> args) {
        fScriptCommand = ImmutableList.copyOf(args);
        fName = name;
        fIsUserDefined = isUserDefined;
        fAppliesTo = appliesTo;
    }

    /**
     * Map of pre-defined charts, for every table class names.
     *
     * If a table class is not in this map then it means that table has no
     * predefined charts.
     *
     * @return The chart models, per table class names
     */
    protected Multimap<String, LamiChartModel> getPredefinedCharts() {
        return ImmutableMultimap.of();
    }

    @Override
    public final boolean appliesTo(ITmfTrace trace) {
        return fAppliesTo.test(trace);
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {
        initialize();
        return fIsAvailable;
    }

    private static boolean executableExists(String name) {
        if (name.contains(File.separator)) {
            // This seems like a path, not just an executable name
            return Files.isExecutable(Paths.get(name));
        }

        // Check if this name is found in the PATH environment variable
        final String pathEnv = System.getenv("PATH"); //$NON-NLS-1$
        final String[] exeDirs = pathEnv.split(checkNotNull(Pattern.quote(File.pathSeparator)));

        return Stream.of(exeDirs)
                .map(Paths::get)
                .anyMatch(path -> Files.isExecutable(path.resolve(name)));
    }

    /**
     * Perform initialization of the LAMI script. This means verifying that it
     * is actually present on disk, and that it returns correct --metadata.
     */
    @VisibleForTesting
    protected synchronized void initialize() {
        if (fInitialized) {
            return;
        }

        /* Do the analysis's initialization */

        /* Check if the script's expected executable is on the PATH */
        final String executable = fScriptCommand.get(0);
        final boolean executableExists = executableExists(executable);

        if (!executableExists) {
            /* Script is not found */
            fIsAvailable = false;
            fInitialized = true;
            return;
        }

        fIsAvailable = checkMetadata();
        fInitialized = true;
    }

    /**
     * Verify that this script returns valid metadata.
     *
     * This will populate all remaining non-final fields of this class.
     *
     * @return If the metadata is valid or not
     */
    @VisibleForTesting
    protected boolean checkMetadata() {
        /*
         * The initialize() phase of the analysis will be used to check the
         * script's metadata. Actual runs of the script will use the execute()
         * method below.
         */
        List<String> command = ImmutableList.<@NonNull String> builder()
                .addAll(fScriptCommand).add(METADATA_FLAG).build();

        LOGGER.info(() -> "[LamiAnalysis:RunningMetadataCommand] command=" + command.toString()); //$NON-NLS-1$

        String output = getOutputFromCommand(command);
        if (output == null || output.isEmpty()) {
            return false;
        }

        /*
         *
         * Metadata should look this this:
         *
         * {
         *   "version": [1, 5, 2, "dev"],
         *   "title": "I/O latency statistics",
         *   "authors": [
         *     "Julien Desfossez",
         *     "Antoine Busque"
         *   ],
         *   "description": "Provides statistics about the latency involved in various I/O operations.",
         *   "url": "https://github.com/lttng/lttng-analyses",
         *   "tags": [
         *     "io",
         *     "stats",
         *     "linux-kernel",
         *     "lttng-analyses"
         *   ],
         *   "table-classes": {
         *     "syscall-latency": {
         *       "title": "System calls latency statistics",
         *       "column-descriptions": [
         *         {"title": "System call", "type": "syscall"},
         *         {"title": "Count", "type": "int", "unit": "operations"},
         *         {"title": "Minimum duration", "type": "duration"},
         *         {"title": "Average duration", "type": "duration"},
         *         {"title": "Maximum duration", "type": "duration"},
         *         {"title": "Standard deviation", "type": "duration"}
         *       ]
         *     },
         *     "disk-latency": {
         *       "title": "Disk latency statistics",
         *       "column-descriptions": [
         *         {"title": "Disk name", "type": "disk"},
         *         {"title": "Count", "type": "int", "unit": "operations"},
         *         {"title": "Minimum duration", "type": "duration"},
         *         {"title": "Average duration", "type": "duration"},
         *         {"title": "Maximum duration", "type": "duration"},
         *         {"title": "Standard deviation", "type": "duration"}
         *       ]
         *     }
         *   }
         * }
         *
         */

        try {
            JSONObject obj = new JSONObject(output);
            fAnalysisTitle = obj.getString(LamiStrings.TITLE);

            /* Very early scripts may not contain the "mi-version" */
            JSONObject miVersion = obj.optJSONObject(LamiStrings.MI_VERSION);
            if (miVersion == null) {
                /* Before version 0.1 */
                fUseProgressOutput = false;
            } else {
                int majorVersion = miVersion.getInt(LamiStrings.MAJOR);
                if (majorVersion <= MAX_SUPPORTED_MAJOR_VERSION) {
                    fUseProgressOutput = true;
                } else {
                    /* Unknown version, we do not support it */
                    return false;
                }
            }

            JSONObject tableClasses = obj.getJSONObject(LamiStrings.TABLE_CLASSES);
            @NonNull String[] tableClassNames = checkNotNullContents(JSONObject.getNames(tableClasses));

            ImmutableMap.Builder<String, LamiTableClass> tablesBuilder = ImmutableMap.builder();
            for (String tableClassName : tableClassNames) {
                JSONObject tableClass = tableClasses.getJSONObject(tableClassName);

                final String tableTitle = checkNotNull(tableClass.getString(LamiStrings.TITLE));
                @NonNull JSONArray columnDescriptions = checkNotNull(tableClass.getJSONArray(LamiStrings.COLUMN_DESCRIPTIONS));

                List<LamiTableEntryAspect> aspects = getAspectsFromColumnDescriptions(columnDescriptions);
                Collection<LamiChartModel> chartModels = getPredefinedCharts().get(tableClassName);

                tablesBuilder.put(tableClassName, new LamiTableClass(tableClassName, tableTitle, aspects, chartModels));
            }

            try {
                fTableClasses = tablesBuilder.build();
            } catch (IllegalArgumentException e) {
                /*
                 * This is thrown if there are duplicate keys in the map
                 * builder.
                 */
                throw new JSONException("Duplicate table class entry in " + fAnalysisTitle); //$NON-NLS-1$
            }

        } catch (JSONException e) {
            /* Error in the parsing of the JSON, script is broken? */
            LOGGER.severe(() -> "[LamiAnalysis:ErrorParsingMetadata] msg=" + e.getMessage()); //$NON-NLS-1$
            Activator.instance().logError(e.getMessage());
            return false;
        }
        return true;
    }

    private static List<LamiTableEntryAspect> getAspectsFromColumnDescriptions(JSONArray columnDescriptions) throws JSONException {
        ImmutableList.Builder<LamiTableEntryAspect> aspectsBuilder = new ImmutableList.Builder<>();
        for (int j = 0; j < columnDescriptions.length(); j++) {
            JSONObject column = columnDescriptions.getJSONObject(j);
            DataType columnDataType;
            String columnClass = column.optString(LamiStrings.CLASS, null);

            if (columnClass == null) {
                columnDataType = DataType.MIXED;
            } else {
                columnDataType = getDataTypeFromString(columnClass);
            }

            String columnTitle = column.optString(LamiStrings.TITLE, null);

            if (columnTitle == null) {
                columnTitle = String.format("%s #%d", columnDataType.getTitle(), j + 1); //$NON-NLS-1$
            }

            final int colIndex = j;
            switch (columnDataType) {
            case TIME_RANGE:
                /*
                 * We will add 3 aspects, to represent the start, end and
                 * duration of this time range.
                 */
                aspectsBuilder.add(new LamiTimeRangeBeginAspect(columnTitle, colIndex));
                aspectsBuilder.add(new LamiTimeRangeEndAspect(columnTitle, colIndex));
                aspectsBuilder.add(new LamiTimeRangeDurationAspect(columnTitle, colIndex));
                break;

            case TIMESTAMP:
                aspectsBuilder.add(new LamiTimestampAspect(columnTitle, colIndex));
                break;

            case PROCESS:
                aspectsBuilder.add(new LamiProcessNameAspect(columnTitle, colIndex));
                aspectsBuilder.add(new LamiProcessPIDAspect(columnTitle, colIndex));
                aspectsBuilder.add(new LamiProcessTIDAspect(columnTitle, colIndex));
                break;

            case IRQ:
                aspectsBuilder.add(new LamiIRQTypeAspect(columnTitle, colIndex));
                aspectsBuilder.add(new LamiIRQNameAspect(columnTitle, colIndex));
                aspectsBuilder.add(new LamiIRQNumberAspect(columnTitle, colIndex));
                break;

            case DURATION:
                aspectsBuilder.add(new LamiDurationAspect(columnTitle, colIndex));
                break;

            case MIXED:
                aspectsBuilder.add(new LamiMixedAspect(columnTitle, colIndex));
                break;

            // $CASES-OMITTED$
            default:
                String units = column.optString(LamiStrings.UNIT, null);

                if (units == null) {
                    units = columnDataType.getUnits();
                }

                /* We will add only one aspect representing the element */
                LamiTableEntryAspect aspect = new LamiGenericAspect(columnTitle,
                        units, colIndex, columnDataType.isContinuous(), false);
                aspectsBuilder.add(aspect);
                break;
            }
        }
        /*
         * SWT quirk : we need an empty column at the end or else the last data
         * column will clamp to the right edge of the view if it is
         * right-aligned.
         */
        aspectsBuilder.add(LamiEmptyAspect.INSTANCE);

        return aspectsBuilder.build();
    }

    private static DataType getDataTypeFromString(String value) throws JSONException {
        try {
            return DataType.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new JSONException("Unrecognized data type: " + value); //$NON-NLS-1$
        }
    }

    /**
     * Get the title of this analysis, as read from the script's metadata.
     *
     * @return The analysis title. Should not be null after the initialization
     *         completed successfully.
     */
    public @Nullable String getAnalysisTitle() {
        return fAnalysisTitle;
    }

    /**
     * Get the result table classes defined by this analysis, as read from the
     * script's metadata.
     *
     * @return The analysis' result table classes. Should not be null after the
     *         execution completed successfully.
     */
    public @Nullable Map<String, LamiTableClass> getTableClasses() {
        return fTableClasses;
    }

    /**
     * Print the full command that will be run when calling {@link #execute},
     * with the exception of the 'extraParams' that will be passed to execute().
     *
     * This can be used to display the command in the UI before it is actually
     * run.
     *
     * @param trace
     *            The trace on which to run the analysis
     * @param range
     *            The time range to specify. Null will not specify a time range,
     *            which means the whole trace will be taken.
     * @return The command as a single, space-separated string
     */
    public String getFullCommandAsString(ITmfTrace trace, @Nullable TmfTimeRange range) {
        String tracePath = checkNotNull(trace.getPath());

        ImmutableList.Builder<String> builder = getBaseCommand(range);
        /*
         * We can add double-quotes around the trace path, which could contain
         * spaces, so that the resulting command can be easily copy-pasted into
         * a shell.
         */
        builder.add(DOUBLE_QUOTES + tracePath + DOUBLE_QUOTES);
        List<String> list = builder.build();
        String ret = list.stream().collect(Collectors.joining(" ")); //$NON-NLS-1$
        return checkNotNull(ret);
    }

    /**
     * Get the base part of the command that will be executed to run this
     * analysis, supplying the given time range. Base part meaning:
     *
     * <pre>
     * [script executable] [statically-defined parameters] [--begin/--end (if applicable)]
     * </pre>
     *
     * Note that it does not include the path to the trace, that is to be added
     * separately.
     *
     * @param range
     *            The time range that will be passed
     * @return The elements of the command
     */
    private ImmutableList.Builder<String> getBaseCommand(@Nullable TmfTimeRange range) {
        long begin = 0;
        long end = 0;
        if (range != null) {
            begin = range.getStartTime().getValue();
            end = range.getEndTime().getValue();
        }

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.addAll(fScriptCommand);

        if (fUseProgressOutput) {
            builder.add(PROGRESS_FLAG);
        }

        if (range != null) {
            builder.add(BEGIN_FLAG).add(String.valueOf(begin));
            builder.add(END_FLAG).add(String.valueOf(end));
        }
        return builder;
    }

    @Override
    public List<LamiResultTable> execute(ITmfTrace trace, @Nullable TmfTimeRange timeRange,
            String extraParamsString, IProgressMonitor monitor) throws CoreException {
        /* Should have been called already, but in case it was not */
        initialize();

        final @NonNull String tracePath = checkNotNull(trace.getPath());
        final @NonNull String trimmedExtraParamsString = checkNotNull(extraParamsString.trim());
        final List<String> extraParams = ShellUtils.commandStringToArgs(trimmedExtraParamsString);

        ImmutableList.Builder<String> builder = getBaseCommand(timeRange);

        builder.addAll(extraParams);
        builder.add(tracePath);
        List<String> command = builder.build();

        LOGGER.info(() -> "[LamiAnalysis:RunningExecuteCommand] command=" + command.toString()); //$NON-NLS-1$
        String output = getResultsFromCommand(command, monitor);

        if (output.isEmpty()) {
            IStatus status = new Status(IStatus.INFO, Activator.instance().getPluginId(), Messages.LamiAnalysis_NoResults);
            throw new CoreException(status);
        }

        /*
         * {
         *   "results": [
         *     {
         *       "time-range": {
         *         "type": "time-range",
         *         "begin": 1444334398154194201,
         *         "end": 1444334425194487548
         *       },
         *       "class": "syscall-latency",
         *       "data": [
         *         [
         *           {"type": "syscall", "name": "open"},
         *           45,
         *           {"type": "duration", "value": 5562},
         *           {"type": "duration", "value": 13835},
         *           {"type": "duration", "value": 77683},
         *           {"type": "duration", "value": 15263}
         *         ],
         *         [
         *           {"type": "syscall", "name": "read"},
         *           109,
         *           {"type": "duration", "value": 316},
         *           {"type": "duration", "value": 5774},
         *           {"type": "duration", "value": 62569},
         *           {"type": "duration", "value": 9277}
         *         ]
         *       ]
         *     },
         *     {
         *       "time-range": {
         *         "type": "time-range",
         *         "begin": 1444334425194487549,
         *         "end": 1444334425254887190
         *       },
         *       "class": "syscall-latency",
         *       "data": [
         *         [
         *           {"type": "syscall", "name": "open"},
         *           45,
         *           {"type": "duration", "value": 1578},
         *           {"type": "duration", "value": 16648},
         *           {"type": "duration", "value": 15444},
         *           {"type": "duration", "value": 68540}
         *         ],
         *         [
         *           {"type": "syscall", "name": "read"},
         *           109,
         *           {"type": "duration", "value": 78},
         *           {"type": "duration", "value": 1948},
         *           {"type": "duration", "value": 11184},
         *           {"type": "duration", "value": 94670}
         *         ]
         *       ]
         *     }
         *   ]
         * }
         *
         */

        ImmutableList.Builder<LamiResultTable> resultsBuilder = new ImmutableList.Builder<>();

        try {
            JSONObject obj = new JSONObject(output);
            JSONArray results = obj.getJSONArray(LamiStrings.RESULTS);

            if (results.length() == 0) {
                /*
                 * No results were reported. This may be normal, but warn the
                 * user why a report won't be created.
                 */
                IStatus status = new Status(IStatus.INFO, Activator.instance().getPluginId(), Messages.LamiAnalysis_NoResults);
                throw new CoreException(status);
            }

            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);

                /* Parse the time-range */
                JSONObject trObject = checkNotNull(result.getJSONObject(LamiStrings.TIME_RANGE));
                LamiData trData = LamiData.createFromObject(trObject);
                if (!(trData instanceof LamiTimeRange)) {
                    throw new JSONException("Time range did not have expected class type."); //$NON-NLS-1$
                }
                LamiTimeRange tr = (LamiTimeRange) trData;

                /* Parse the table's class */
                LamiTableClass tableClass;
                JSONObject tableClassObject = result.optJSONObject(LamiStrings.CLASS);
                if (tableClassObject == null) {
                    /*
                     * "class" is just a standard string, indicating we use a
                     * metadata-defined table class as-is
                     */
                    @NonNull String tableClassName = checkNotNull(result.getString(LamiStrings.CLASS));
                    tableClass = getTableClassFromName(tableClassName);

                    // FIXME Rest will become more generic eventually in the LAMI format.
                } else if (tableClassObject.has(LamiStrings.INHERIT)) {
                    /*
                     * Dynamic title: We reuse an existing table class but
                     * override the title.
                     */
                    String baseTableName = checkNotNull(tableClassObject.getString(LamiStrings.INHERIT));
                    LamiTableClass baseTableClass = getTableClassFromName(baseTableName);
                    String newTitle = checkNotNull(tableClassObject.getString(LamiStrings.TITLE));

                    tableClass = new LamiTableClass(baseTableClass, newTitle);
                } else {
                    /*
                     * Dynamic column descriptions: we implement a new table
                     * class entirely.
                     */
                    String title = checkNotNull(tableClassObject.getString(LamiStrings.TITLE));
                    JSONArray columnDescriptions = checkNotNull(tableClassObject.getJSONArray(LamiStrings.COLUMN_DESCRIPTIONS));
                    List<LamiTableEntryAspect> aspects = getAspectsFromColumnDescriptions(columnDescriptions);

                    tableClass = new LamiTableClass(nullToEmptyString(Messages.LamiAnalysis_DefaultDynamicTableName), title, aspects, Collections.EMPTY_SET);
                }

                /* Parse the "data", which is the array of rows */
                JSONArray data = result.getJSONArray(LamiStrings.DATA);
                ImmutableList.Builder<LamiTableEntry> dataBuilder = new ImmutableList.Builder<>();

                for (int j = 0; j < data.length(); j++) {
                    /* A row is an array of cells */
                    JSONArray row = data.getJSONArray(j);
                    ImmutableList.Builder<LamiData> rowBuilder = ImmutableList.builder();

                    for (int k = 0; k < row.length(); k++) {
                        Object cellObject = checkNotNull(row.get(k));
                        LamiData cellValue = LamiData.createFromObject(cellObject);
                        rowBuilder.add(cellValue);

                    }
                    dataBuilder.add(new LamiTableEntry(rowBuilder.build()));
                }
                resultsBuilder.add(new LamiResultTable(tr, tableClass, dataBuilder.build()));
            }

        } catch (JSONException e) {
            LOGGER.severe(() -> "[LamiAnalysis:ErrorParsingExecutionOutput] msg=" + e.getMessage()); //$NON-NLS-1$
            IStatus status = new Status(IStatus.ERROR, Activator.instance().getPluginId(), e.getMessage(), e);
            throw new CoreException(status);
        }

        return resultsBuilder.build();
    }

    private LamiTableClass getTableClassFromName(String tableClassName) throws JSONException {
        Map<String, LamiTableClass> map = checkNotNull(fTableClasses);
        LamiTableClass tableClass = map.get(tableClassName);
        if (tableClass == null) {
            throw new JSONException("Table class " + tableClassName + //$NON-NLS-1$
                    " was not declared in the metadata"); //$NON-NLS-1$
        }
        return tableClass;
    }

    /**
     * Get the output of an external command, used for getting the metadata.
     * Cannot be cancelled, and will not report errors, simply returns null if
     * the process ended abnormally.
     *
     * @param command
     *            The parameters of the command, passed to
     *            {@link ProcessBuilder}
     * @return The command output as a string
     */
    @VisibleForTesting
    protected @Nullable String getOutputFromCommand(List<String> command) {
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);

            Process p = builder.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));) {
                int ret = p.waitFor();
                String output = br.lines().collect(Collectors.joining());

                return (ret == 0 ? output : null);
            }
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    /**
     * Get the results of invoking the specified command.
     *
     * The result should start with '{"results":...', as specified by the LAMI
     * JSON protocol. The JSON itself may be split over multiple lines.
     *
     * @param command
     *            The command to run (program and its arguments)
     * @param monitor
     *            The progress monitor
     * @return The analysis results
     * @throws CoreException
     *             If the command ended abnormally, and normal results were not
     *             returned
     */
    @VisibleForTesting
    protected String getResultsFromCommand(List<String> command, IProgressMonitor monitor)
            throws CoreException {

        final int scale = 1000;
        double workedSoFar = 0.0;

        ProcessCanceller cancellerRunnable = null;
        Thread cancellerThread = null;

        try {
            monitor.beginTask(Messages.LamiAnalysis_MainTaskName, scale);

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(false);

            Process p = checkNotNull(builder.start());

            cancellerRunnable = new ProcessCanceller(p, monitor);
            cancellerThread = new Thread(cancellerRunnable);
            cancellerThread.start();

            List<String> results = new ArrayList<>();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));) {
                String line = in.readLine();
                while (line != null && !line.matches("\\s*\\{.*")) { //$NON-NLS-1$
                    /*
                     * This is a line indicating progress, it has the form:
                     *
                     * 0.123 3000 of 5000 events processed
                     *
                     * The first part indicates the estimated fraction (out of
                     * 1.0) of work done. The second part is status text.
                     */

                    // Trim the line first to make sure the first character is
                    // significant
                    line = line.trim();

                    // Split at the first space
                    String[] elems = line.split(" ", 2); //$NON-NLS-1$

                    if (elems[0].matches("\\d.*")) { //$NON-NLS-1$
                        // It looks like we have a progress indication
                        try {
                            // Try parsing the number
                            double cumulativeWork = Double.parseDouble(elems[0]) * scale;
                            double workedThisLoop = cumulativeWork - workedSoFar;

                            // We're going backwards? Do not update the
                            // monitor's value
                            if (workedThisLoop > 0) {
                                monitor.internalWorked(workedThisLoop);
                                workedSoFar = cumulativeWork;
                            }

                            // There is a message: update the monitor's task name
                            if (elems.length >= 2) {
                                monitor.setTaskName(elems[1].trim());
                            }
                        } catch (NumberFormatException e) {
                            // Continue reading progress lines anyway
                        }
                    }

                    line = in.readLine();
                }
                while (line != null) {
                    /*
                     * We have seen the first line containing a '{', this is our
                     * JSON output!
                     */
                    results.add(line);
                    line = in.readLine();
                }
            }
            int ret = p.waitFor();

            if (monitor.isCanceled()) {
                /* We were interrupted by the canceller thread. */
                IStatus status = new Status(IStatus.CANCEL, Activator.instance().getPluginId(), null);
                throw new CoreException(status);
            }

            if (ret != 0) {
                /*
                 * Something went wrong running the external script. We will
                 * gather the stderr and report it to the user.
                 */
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                List<String> stdErrOutput = br.lines().collect(Collectors.toList());

                MultiStatus status = new MultiStatus(Activator.instance().getPluginId(),
                        IStatus.ERROR, Messages.LamiAnalysis_ErrorDuringExecution, null);
                for (String str : stdErrOutput) {
                    status.add(new Status(IStatus.ERROR, Activator.instance().getPluginId(), str));
                }
                if (stdErrOutput.isEmpty()) {
                    /*
                     * At least say "no output", so an error message actually
                     * shows up.
                     */
                    status.add(new Status(IStatus.ERROR, Activator.instance().getPluginId(), Messages.LamiAnalysis_ErrorNoOutput));
                }
                throw new CoreException(status);
            }

            /* External script ended successfully, all is fine! */
            String resultsStr = results.stream().collect(Collectors.joining());
            return checkNotNull(resultsStr);

        } catch (IOException | InterruptedException e) {
            IStatus status = new Status(IStatus.ERROR, Activator.instance().getPluginId(), Messages.LamiAnalysis_ExecutionInterrupted, e);
            throw new CoreException(status);

        } finally {
            if (cancellerRunnable != null) {
                cancellerRunnable.setFinished();
            }
            if (cancellerThread != null) {
                try {
                    cancellerThread.join();
                } catch (InterruptedException e) {
                }
            }

            monitor.done();
        }
    }

    private static class ProcessCanceller implements Runnable {

        private final Process fProcess;
        private final IProgressMonitor fMonitor;

        private boolean fIsFinished = false;

        public ProcessCanceller(Process process, IProgressMonitor monitor) {
            fProcess = process;
            fMonitor = monitor;
        }

        public void setFinished() {
            fIsFinished = true;
        }

        @Override
        public void run() {
            try {
                while (!fIsFinished) {
                    Thread.sleep(500);
                    if (fMonitor.isCanceled()) {
                        fProcess.destroy();
                        return;
                    }
                }
            } catch (InterruptedException e) {
            }
        }

    }

    @Override
    public @NonNull String getName() {
        return fName;
    }

    @Override
    public boolean isUserDefined() {
        return fIsUserDefined;
    }

}
