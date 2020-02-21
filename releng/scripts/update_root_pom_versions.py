###############################################################################
# Copyright (c) 2016 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
###############################################################################

#args: filename oldversion newversion

#Update root pom.xml versions. For example
#   <artifactId>org.eclipse.tracecompass</artifactId>
#-  <version>2.0.0-SNAPSHOT</version>
#+  <version>2.0.1-SNAPSHOT</version>
#
# Also the target plarform:
#
#               <classifier>${target-platform}</classifier>
#-              <version>2.0.0-SNAPSHOT</version>
#+              <version>2.0.1-SNAPSHOT</version>
#             </artifact>

import sys, re
if len(sys.argv) < 4:
    sys.exit('Usage: python update_root_pom_versions.py [file] [old version] [new version]')
fileContent = open(sys.argv[1]).read()
fileContent = re.sub("(<artifactId>org.eclipse.tracecompass.*</artifactId>\n\s+)<version>" + sys.argv[2] + "-SNAPSHOT</version>", "\g<1><version>" + sys.argv[3] + "-SNAPSHOT</version>", fileContent)
# Also the target platform version being used
fileContent = re.sub("<version>" + sys.argv[2] + "-SNAPSHOT</version>(\n\s+</artifact>)", "<version>" + sys.argv[3] + "-SNAPSHOT</version>\g<1>", fileContent)
open(sys.argv[1], "w").write(fileContent)
