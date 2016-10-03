###############################################################################
# Copyright (c) 2016 Ericsson
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
###############################################################################

#args: filename

# Parses the HTML of download stats and converts it to CSV
#
# Example usage:
# 1. Login to eclipse.org with your acount and open this page:
#https://dev.eclipse.org/committers/committertools/stats.php?filename=/tracecompass/releases/%trace-compass%
# 2. Save this page as HTML
# 3. python html_rcp_download_stats_to_csv.py page.html > out.csv

# Example html content
# 			<td><a href="javascript:fnViewDaily('/tracecompass/releases/1.0.0/rcp/trace-compass-1.0.0-20150610-1449-linux.gtk.x86_64.tar.gz',%20'daily');">/tracecompass/releases/1.0.0/rcp/trace-compass-1.0.0-20150610-1449-linux.gtk.x86_64.tar.gz</a></td>
#			<td align="right">3</td>
# ...
#			<td><a href="javascript:fnViewDaily('/tracecompass/releases/1.0.0/rcp/trace-compass-1.0.0-20150610-1449-win32.win32.x86_64.zip',%20'daily');">/tracecompass/releases/1.0.0/rcp/trace-compass-1.0.0-20150610-1449-win32.win32.x86_64.zip</a></td>
#			<td align="right">5</td>


import sys, re
fileContent = open(sys.argv[1]).read()
matchObject = re.finditer(".*releases/(\d+.\d+.\d+)/.*trace-compass-\d+\.\d+\.\d+-\d+-\d+-(.*)\..*\.(.*)\.(tar|zip).*\..*\n.*right\">(\d+).*", fileContent)
for m in matchObject:
	print(m.group(1) + "," + m.group(2) + "," + m.group(3) + "," + m.group(5))

