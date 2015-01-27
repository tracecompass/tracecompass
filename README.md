Eclipse Trace Compass
=====================

This source tree contains the source code for the Trace Compass plugins for
Eclipse.

The plugins are categorized as follows:

    org.eclipse.tracecompass.analysis.*    | Generic extensions to the base framework
    org.eclipse.tracecompass.btf.*         | Best Trace Format (BTF) integration
    org.eclipse.tracecompass.ctf.*         | Common Trace Format (CTF) reader library
    org.eclipse.tracecompass.gdbtrace.*    | Support for reading and viewing GDB traces
    org.eclipse.tracecompass.lttng2.*      | LTTng 2.x integration
    org.eclipse.tracecompass.pcap.*        | libpcap integration
    org.eclipse.tracecompass.rcp.*         | Code specific to the RCP version
    org.eclipse.tracecompass.statesystem.* | State System library
    org.eclipse.tracecompass.tmf.*         | Core framework

See the `plugins.svg` file for a diagram showing the dependencies between the
different plugins.


Setting up the development environment
--------------------------------------

To set up the environment to build Trace Compass from within Eclipse, see this
wiki page:
<http://wiki.eclipse.org/Trace_Compass/Development_Environment_Setup>


Compiling manually
------------------

To build the plugins manually using Maven, simply run the following command from
the top-level directory:

    mvn clean install

The default command will compile and run the unit tests. Running the tests can
take some time, to skip them you can append `-Dmaven.test.skip=true` to the
`mvn` command:

    mvn clean install -Dmaven.test.skip=true


The RCP is not built by default, to build it you need to add `-Pbuild-rcp` to
the `mvn` command:

    mvn clean install -Pbuild-rcp -Dmaven.test.skip=true

This will build the RCP for all supported architectures. The resulting archives
will be placed in `org.eclipse.tracecompass.rcp.product/target/products`.


To build a local p2 update site:

    mvn clean install -Pdeploy-update-site "-DsiteDestination=/path/to/destination"

where `/path/to/destination` is the **absolute** path to destination directory
on your disk.

