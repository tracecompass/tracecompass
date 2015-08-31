Eclipse Trace Compass
=====================

This source tree contains the source code for the Trace Compass plugins for
Eclipse.

The plugins are categorized as follows:

    analysis/    | Generic extensions to the base framework
    btf/         | Best Trace Format (BTF) integration
    common/      | Generic utilities that can be used by other plugins
    ctf/         | Common Trace Format (CTF) reader library
    doc/         | Documentation and code examples
    gdbtrace/    | Support for reading and viewing GDB traces
    lttng/       | LTTng integration
    pcap/        | libpcap integration
    rcp/         | Code specific to the RCP version
    releng/      | Releng-related plugins
    statesystem/ | State System library
    tmf/         | Core framework

See the `components.svg` file for a diagram showing the dependencies between the
different components.


Setting up the development environment
--------------------------------------

To set up the environment to build Trace Compass from within Eclipse, see this
wiki page:
<http://wiki.eclipse.org/Trace_Compass/Development_Environment_Setup>


Compiling manually
------------------

To build the project manually using Maven, simply run the following command from
the top-level directory:

    mvn clean install

The default command will compile and run the unit tests. Running the tests can
take some time, to skip them you can append `-Dmaven.test.skip=true` to the
`mvn` command:

    mvn clean install -Dmaven.test.skip=true

Stand-alone application (RCP) packages will be placed in
`rcp/org.eclipse.tracecompass.rcp.product/target/products`.

The p2 update site, used for installation as plugins inside Eclipse, will be
placed in `releng/org.eclipse.tracecompass.releng-site/target/repository`.


Maven profiles and properties
-----------------------------

The following Maven profiles, and their corresponding properties, are defined in
the build system. You can set them by using `-P[profile name]` and
`-D[property name]=[value]` in `mvn` commands.

* `-Pctf-grammar`

  Re-compiles the CTF grammar files. This should be enabled if you modify the
  `.g` files in the `ctf.parser` plugin.

* `-P -build-rcp`

  Disables the "build-rcp" profile to skip building the RCP archives.

* `-Pdeploy-rcp`

  Mainly for use on build servers. Copies the generated RCP archives, as well as
  the RCP-specific update site, to the paths specified by
  `-DrcpDestination=/absolute/path/to/destination` and
  `-DrcpSiteDestination=/absolute/path/to/destination`, respectively.
  Must be used with `-Pbuild-rcp`!

* `-Pdeploy-update-site`

  Mainly for use on build servers. Copies the standard update site (for the
  Eclipse plugin installation) to the destination specified by
  `-DsiteDestination=/absolute/path/to/destination`.

* `-Psign-update-site`

  Mainly for use on build servers. Signs all the generated update sites using
  the Eclipse signing server.

* `-Pdeploy-doc`

  Mainly for use on build servers. Copies the generated HTML documentation to
  the destination specified by `-DdocDestination=/absolute/path/to/destination`.
  Some directories may need to already exist at the destination (or Maven will
  throw related errors).

