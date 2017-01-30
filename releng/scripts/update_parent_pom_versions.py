###############################################################################
# Copyright (c) 2016 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################

#args: filename oldversion newversion

#Update pom.xml version. For example
#   <parent>
#     <artifactId>org.eclipse.tracecompass</artifactId>
#     <groupId>org.eclipse.tracecompass</groupId>
#-    <version>2.0.0-SNAPSHOT</version>
#+    <version>2.0.1-SNAPSHOT</version>
#   </parent>

import sys, re
if len(sys.argv) < 4:
    sys.exit('Usage: python update_parent_pom_versions.py [file] [old version] [new version]')
fileContent = open(sys.argv[1]).read()
fileContent = re.sub("<version>" + sys.argv[2] + "-SNAPSHOT</version>(\n\s+</parent>)", "<version>" + sys.argv[3] + "-SNAPSHOT</version>\g<1>", fileContent)
open(sys.argv[1], "w").write(fileContent)
