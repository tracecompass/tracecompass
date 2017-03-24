#!/usr/bin/perl -w
################################################################################
# Copyright (C) 2017, Ericsson.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors
#   Bernd Hufmann - Initial version
################################################################################
use strict;

use File::Basename;
$::me = basename($0, "");

use Getopt::Std;            # standard Perl module
getopts("vh");
$::gVerbose = $::opt_v if ($::opt_v);
$::gHelp = $::opt_h if ($::opt_h);

# Function prototypes to keep perl -w quiet:
sub debug($);

##------------------------------------------------------------------------
# Debug output.  Use instead of "print()"
sub debug($)
{
    my $what = shift;
    print STDERR "$what\n"  if $::gVerbose;
}

sub usage()
{
    print <<USAGE;
    $::me [-v] [-h] <simRelName> <updateRelease> <milestone> <releaseNumber>

    Creating milestone builds and storing the relevant artifacts and doc
    in the corresponding directory.

    Note: to make a release, at least rc1 and rc4 have to be created.

    where:
        -v:             verbose mode
        -h:             print usage
        simRelName:     name of simultaneous release (e.g. neon, oxygen)
        updateRelease:  number of update release 0-3
        milestone:      m1, m2 .. m7, rc1 .. rc4 or release (at release time)
        release nubmer: the actual release number (e.g. 2.3.0)

    Examples:
        ./create-release.pl -v neon 0 m1 2.0.0
        ./create-release.pl -v neon 0 rc1 2.0.0
        ./create-release.pl -v neon 3 release 2.3.0
USAGE
exit;
}

usage() if ($::gHelp);
usage() if ($#ARGV < 3);

################################################################################
# Validate command-line parameters
################################################################################
$::gSimReleaseName = shift;
$::gUpdateRelease = shift;
$::gMilestone = shift;
$::gTraceCompassRelease = shift;

debug ("Sim Release Name:\t$::gSimReleaseName");
debug ("Update Release:\t\t$::gUpdateRelease");
debug ("Milestone:\t\t$::gMilestone");
debug ("Version:\t\t$::gTraceCompassRelease\n");

################################################################################
# Basic constants
################################################################################

$::gBaseDir = "/home/data/httpd/";
$::gTraceCompassDir = $::gBaseDir."download.eclipse.org/tracecompass/";
$::gReleaseBaseDir = $::gTraceCompassDir.$::gSimReleaseName."/";
$::gMilestoneBaseDir = $::gReleaseBaseDir."milestones/";
$::gSimRelStableDir = $::gReleaseBaseDir."stable/";
$::gSimRelStableRepositoryDir = $::gSimRelStableDir."repository/";
$::gSimRelStableRcpDir = $::gSimRelStableDir."rcp/";

$::gStableDir = $::gTraceCompassDir."stable/";
$::gStableRcpDir = $::gStableDir."rcp/";
$::gFinalReleaseDir = $::gTraceCompassDir."releases/".$::gTraceCompassRelease."/";
$::gFinalReleaseRcpDir = $::gFinalReleaseDir."rcp/";

$::gDocBaseDir = $::gBaseDir."archive.eclipse.org/tracecompass/doc/";
$::gDocStableDir = $::gDocBaseDir."stable/";
$::gDocReleaseStableDir = "$::gDocBaseDir".$::gSimReleaseName."/stable/";

$::gBackupArtifactDir = "$::gTraceCompassDir"."tmp/backup/artifacts/";
$::gBackupDocDir = "$::gTraceCompassDir"."tmp/backup/doc/";

$::gMasterArtifactsDir = $::gTraceCompassDir."master/";
$::gMasterRepositoryDir = $::gMasterArtifactsDir."repository/";

$::gStableBranchName = "stable_$::gTraceCompassRelease";
$::gStableBranchName =~ s/(\d+\.\d+)\.\d+/$1/;

$::gNewNoteworthy = "NewIn$::gTraceCompassRelease";
$::gNewNoteworthy =~ s/(\d+)\.(\d+)\.\d+/$1$2/;

$::gMilestoneUpperCase = $::gMilestone;
$::gMilestoneUpperCase =~ s/(\S+)/uc($1)/ge;

$::gSimReleaseNameUpperCase = $::gSimReleaseName;
$::gSimReleaseNameUpperCase =~ s/\b(\w)/\U$1/g;


################################################################################
# Create update release hash table
################################################################################
%::G_UPDATE_RELEASE_HASH = (
    "0" => "",
    "1" => "ur1_",
    "2" => "ur2_",
    "3" => "ur3_",
);

################################################################################
# Validate update release parameter
################################################################################
if (!defined $::G_UPDATE_RELEASE_HASH{$::gUpdateRelease})
{
    die "$::me-error: Update release $::gUpdateRelease not supported "
}

################################################################################
# Create hash table with milestone/release content
################################################################################
$::gMilestoneDir = $::gMilestoneBaseDir.$::G_UPDATE_RELEASE_HASH{$::gUpdateRelease}.$::gMilestone."/";

$::gReminders = "";
my $http = '"http:\/\/"';
if ($::gMilestone ne "release")
{
$::gReminders = "\
- Update tracecompass.aggrcon of repo org.eclipse.simrel.build with:\
$::gMilestoneDir\n";
} 
else 
{
    $::gReminders = "\
- Update tracecompass.aggrcon of repo org.eclipse.simrel.build with:\
http://download.eclipse.org/tracecompass/releases/$::gTraceCompassRelease/repository/\n";
}
$::gReminders =~ s/$::gBaseDir/$http/eeg;

$::gMileStoneEmailBase = "\
Title: Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease $::gMilestoneUpperCase posted\
\
Hi,\
\
I posted the Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease $::gMilestoneUpperCase build:\
$::gMilestoneDir\
\
It is based on commit: <TODO>\
\n";

$::gReleaseEmail = "\
Hi,\
\
The Trace Compass team is pleased to announce that the release $::gTraceCompassRelease for $::gSimReleaseNameUpperCase.$::gUpdateRelease is now available\
from the project's download page [1]. Details are available on the New & Noteworthy page [2].\
\
The Trace Compass p2 repository is up at http://download.eclipse.org/tracecompass/releases/$::gTraceCompassRelease/repository/\
\
The Trace Compass standalone RCP is available from: http://download.eclipse.org/tracecompass/releases/$::gTraceCompassRelease/rcp/\
\
The tag v$::gTraceCompassRelease maps to the commit <TODO>\
\
Congratulations and thanks to everyone!\
\
Regards\
Bernd\
\
[1] https://projects.eclipse.org/projects/tools.tracecompass/downloads\
[2] https://wiki.eclipse.org/Trace_Compass/News/$::gNewNoteworthy\
\n";

$::gGreeting = "\
Regards\
Bernd\
\n";

%::G_MILESTONES_HASH = (
    "m1" => {
        from   => $::gMasterRepositoryDir,
        to     => $::gMilestoneDir,
        email  => $::gMileStoneEmailBase,
        addon  => "Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease M2 will be <TODO>\n".$::gGreeting,
        reminder => $::gReminders
    },
    "m2" => {
        from   => $::gMasterRepositoryDir,
        to     => $::gMilestoneDir,
        email  =>   $::gMileStoneEmailBase,
        addon  => "Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease M3 will be <TODO>\n".$::gGreeting,
        reminder => $::gReminders
    },
    "m3" => {
        from   => $::gMasterRepositoryDir,
        to     => $::gMilestoneDir,
        email  => $::gMileStoneEmailBase,
        addon  => "Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease M4 will be <TODO>\n".$::gGreeting,
        reminder => $::gReminders
    },
    "m4" => {
        from    => $::gMasterRepositoryDir,
        to      => $::gMilestoneDir,
        email   => $::gMileStoneEmailBase,
        addon   => "Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease M5 will be <TODO>\n".$::gGreeting,
        reminder => $::gReminders
    },
    "m5" => {
        from    => $::gMasterRepositoryDir,
        to      => $::gMilestoneDir,
        email   => $::gMileStoneEmailBase,
        addon   => "Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease M6 will be <TODO>\n".$::gGreeting,
        reminder => $::gReminders
    },
    "m6" => {
        from   => $::gMasterRepositoryDir,
        to     => $::gMilestoneDir,
        email  => $::gMileStoneEmailBase,
        addon  => "Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease M7 will be <TODO>\n".$::gGreeting,
        reminder => $::gReminders
    },
    "m7" => {
        from   => $::gMasterRepositoryDir,
        to     => $::gMilestoneDir,
        email  => $::gMileStoneEmailBase,
        addon  => "Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease RC1 will be <TODO>\
\
Reminder: RC1 will be API and feature freeze for this release.\n".$::gGreeting,
        reminder => $::gReminders,
    },
    "rc1" => {
        from          => $::gMasterRepositoryDir,
        to            => $::gMilestoneDir,
        fromArtifacts => $::gMasterArtifactsDir,
        toArtifacts   => $::gSimRelStableDir,
        fromDoc       => $::gDocBaseDir,
        toDoc         => $::gDocReleaseStableDir,
        rcpDir        => $::gSimRelStableRcpDir,
        title         => "Trace Compass Release Candidate",
        email         => $::gMileStoneEmailBase,
        addon         => "The corresponding RCP can be downloaded from:
$::gSimRelStableRcpDir\
\
I've also created the release branch $::gStableBranchName.\
\
Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease RC2 will be <TODO>\
\
Reminders:
  - RC1 is API and feature freeze for this release.
  - For any fixes that need to go into the $::gSimReleaseNameUpperCase release, please provide a
    bug report, submit a patch on master first and then cherry-pick it
    on $::gStableBranchName branch after it's merged to master.\
$::gGreeting",
        reminder => $::gReminders."\
- Create $::gStableBranchName\
- Update and enable tracecompass-stable-release-nightly-new job on Hudson to build $::gStableBranchName branch\n",
    },
    "rc2" => {
        from   => $::gSimRelStableRepositoryDir,
        to     => $::gMilestoneDir,
        email  => $::gMileStoneEmailBase,
        addon  => "The corresponding RCP can be downloaded from:
$::gSimRelStableRcpDir\
$::gGreeting",
        reminder => $::gReminders,
    },
    "rc3" => {
        from   => $::gSimRelStableRepositoryDir,
        to     => $::gMilestoneDir,
        email  =>   $::gMileStoneEmailBase,
        addon  => "The corresponding RCP can be downloaded from:
$::gSimRelStableRcpDir\
\
RC4 will be the final build for this release.\
$::gGreeting",
        reminder => $::gReminders,
    },
    "rc4" => {
        from          => $::gSimRelStableRepositoryDir,
        to            => $::gMilestoneDir,
        fromArtifacts => $::gSimRelStableDir,
        toArtifacts   => $::gBackupArtifactDir,
        fromDoc       => $::gDocReleaseStableDir,
        toDoc         => $::gBackupDocDir,
        email         => $::gMileStoneEmailBase,
        addon         => "The corresponding RCP can be downloaded from:
$::gSimRelStableRcpDir\
\
This is the final build for Trace Compass $::gTraceCompassRelease/$::gSimReleaseNameUpperCase.$::gUpdateRelease.\
The release will take place on <TODO>.\
$::gGreeting",
        reminder => $::gReminders."\
- Disable tracecompass-stable-release-nightly-new job on Hudson\n",
    },
    "release" => {
        from          => $::gBackupArtifactDir,
        to            => $::gFinalReleaseDir,
        fromArtifacts => $::gBackupArtifactDir,
        toArtifacts   => $::gStableDir,
        fromDoc       => $::gBackupDocDir,
        toDoc         => $::gDocStableDir,
        rcpDir        => $::gFinalReleaseRcpDir,
        stableRcpDir  => $::gStableRcpDir,
        title         => "Trace Compass Release $::gTraceCompassRelease",
        stableTitle   => "Trace Compass Latest Stable Version",
        email         => $::gReleaseEmail,
        addon         => "",
        reminder => $::gReminders."\
- Update the Trace Compass download page at https://projects.eclipse.org/projects/tools.tracecompass/downloads\
- Update tracecompass-stable-nightly job on Hudson to build $::gStableBranchName branch\
- In Eclipse, tag commit with release tag v$::gTraceCompassRelease push to the main repository\
- Upload test report to the Eclipse wiki and update Test Reports wiki page\
- Create target baseline\n",
    }
);

################################################################################
# Validate milestone paramter and if relevant directories exists
################################################################################
if (!defined $::G_MILESTONES_HASH{$::gMilestone})
{
    die "$::me-error: Milestone $::gMilestone not supported"
}

if (!-d $::G_MILESTONES_HASH{$::gMilestone}{from})
{
    die "$::me-error: Milestone directory $::G_MILESTONES_HASH{$::gMilestone}{from} doesn't exists"
}

if (-d $::G_MILESTONES_HASH{$::gMilestone}{to})
{
    die "$::me-error: Milestone directory $::G_MILESTONES_HASH{$::gMilestone}{to} already exists"
}

if (defined($::G_MILESTONES_HASH{$::gMilestone}{fromArtifacts}))
{
    if (! -d $::G_MILESTONES_HASH{$::gMilestone}{fromArtifacts})
    {
       die "$::me-error: Artifacts directory $::G_MILESTONES_HASH{$::gMilestone}{fromArtifacts} doesn't exists"
    }
}

if (defined($::G_MILESTONES_HASH{$::gMilestone}{fromDoc}))
{
    if (! -d $::G_MILESTONES_HASH{$::gMilestone}{fromDoc})
    {
        die "$::me-error: Doc directory $::G_MILESTONES_HASH{$::gMilestone}{fromDoc} doesn't exists"
    }
}

################################################################################
# Create milestones and stable base directories
################################################################################
if (! -d $::gMilestoneBaseDir)
{
    System("mkdir -p $::gMilestoneBaseDir");
    System("mkdir -p $::gSimRelStableDir");
}

################################################################################
# Create simRel milestone or release directory
################################################################################
System("mkdir -p $::G_MILESTONES_HASH{$::gMilestone}{to}");

################################################################################
# After each milestone build (not release)
# - Update files composite update site
################################################################################
chdir($::G_MILESTONES_HASH{$::gMilestone}{to});
if ($::gMilestone ne "release")
{
    createCompositeFiles ($::gMilestoneBaseDir, $::gSimReleaseName, "$::G_UPDATE_RELEASE_HASH{$::gUpdateRelease}$::gMilestone");
}

################################################################################
# Copy files and directories
################################################################################
System("cp -r $::G_MILESTONES_HASH{$::gMilestone}{from}* $::G_MILESTONES_HASH{$::gMilestone}{to}");

################################################################################
# - Create artifacts directory, if needed
# - Delete old artifacts directory content, if needed.
# - Save rcp, repository and rcp-repository in artifacts directory.
################################################################################
if (defined($::G_MILESTONES_HASH{$::gMilestone}{toArtifacts}))
{
    if (-d $::G_MILESTONES_HASH{$::gMilestone}{toArtifacts})
    {
       System("\\rm -rf $::G_MILESTONES_HASH{$::gMilestone}{toArtifacts}*");
    }
    else
    {
        System("mkdir -p $::G_MILESTONES_HASH{$::gMilestone}{toArtifacts}");
    }

    System("cp -r $::G_MILESTONES_HASH{$::gMilestone}{fromArtifacts}r* $::G_MILESTONES_HASH{$::gMilestone}{toArtifacts}");
}

################################################################################
# - Create doc directory, if needed
# - Delete old doc directory content, if needed.
# - Save docs in doc directory
################################################################################
if (defined($::G_MILESTONES_HASH{$::gMilestone}{fromDoc}))
{
    if (-d $::G_MILESTONES_HASH{$::gMilestone}{toDoc})
    {
        System("\\rm -rf $::G_MILESTONES_HASH{$::gMilestone}{toDoc}*");
    }
    else
    {
        System("mkdir -p $::G_MILESTONES_HASH{$::gMilestone}{toDoc}");
    }

    System("cp -r $::G_MILESTONES_HASH{$::gMilestone}{fromDoc}org* $::G_MILESTONES_HASH{$::gMilestone}{toDoc}");
}

################################################################################
# Update index.php in RCP directory
################################################################################
if (defined($::G_MILESTONES_HASH{$::gMilestone}{rcpDir}) )
{
    debug("Update index.php");
    updateIndexPhp($::G_MILESTONES_HASH{$::gMilestone}{rcpDir}, $::G_MILESTONES_HASH{$::gMilestone}{title});
}

if (defined($::G_MILESTONES_HASH{$::gMilestone}{stableRcpDir}) )
{
    debug("Update index.php in stable");
    updateIndexPhp($::G_MILESTONES_HASH{$::gMilestone}{stableRcpDir}, $::G_MILESTONES_HASH{$::gMilestone}{stableTitle});
}

my $email = $::G_MILESTONES_HASH{$::gMilestone}{email}.$::G_MILESTONES_HASH{$::gMilestone}{addon};
$email =~ s/$::gBaseDir/$http/eeg;

print ("\n################################################################################\n");
print ($::G_MILESTONES_HASH{$::gMilestone}{reminder});
print ("- Send email to mailing list:\n$email");
print ("\n################################################################################\n");

################################################################################
# subroutines
################################################################################
sub System
{
  debug("@_");
  if (system(@_) != 0)
  {
    print "\n";
    exit -1;
  }
}

sub createCompositeFiles
{
    debug("Create CompositeArtifact.xml");
    createCompositeFile(@_, "artifact");
    debug("Create CompositeContent.xml");
    createCompositeFile(@_, "content");
}

sub createCompositeFile
{
    my $location = shift;
    my $release = shift;
    my $milestone = shift;
    my $what = shift;
    my $compositeFile = "$location/compositeArtifacts.xml";
    my $tag = "compositeArtifactRepository";
    my $type = "org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository";

    if ($what eq "content")
    {
        $compositeFile = "$location/compositeContent.xml";
        $tag = "compositeMetadataRepository";
        $type = "org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository";
    }

    my $count = 1;
    my @children;
    if (-e $compositeFile)
    {
        open FH_IN, "$compositeFile"
            or die "$::me-error: could not open $compositeFile\n";

        while (<FH_IN>)
        {
            if (/<child location="\S+"\/>/)
            {
                $_ =~ s/^\s*//;     # removing leading whitespaces
                $_ =~ s/\s*$//;     # removing ending whitespaces
                $_ =~ s/<child location="(\S+)"\/>/$1/;
                $children[$count] = $_;
                $count++;
            }
        }
        close FH_IN;
    }

    open FH_OUT, ">$compositeFile" or die "$::me-error: Could not open $compositeFile";
    print FH_OUT "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    print FH_OUT "<?$tag version='1.0.0'?>\n";
    print FH_OUT "<repository name=\"&quot;Trace Compass $release Milestone Site&quot;\" type=\"$type\" version=\"1.0.0\">\n";
    print FH_OUT "  <properties size=\"1\">";
    print FH_OUT "    <property name=\"p2.timestamp\" value=\"1380637685016\"/>\n";
    print FH_OUT "  </properties>\n";
    print FH_OUT "  <children size=\"$count\">\n";
    for (my $i = 1 ; $i <= $#children ; $i++)
    {
        print FH_OUT "    <child location=\"$children[$i]\"/>\n";
    }
    print FH_OUT "    <child location=\"$milestone\"/>\n";
    print FH_OUT "  </children>\n";
    print FH_OUT "</repository>\n";
    close FH_OUT;
}

sub updateIndexPhp
{
    my $location=shift;
    my $title=shift;
    my $indexFile = "$location/index.php";
    my $tmpFile = "$indexFile.tmp";

    if (! -e $indexFile)
    {
        print STDERR "$location/index.php doesn't exist";
        return;
    }

    open FH_IN, "$indexFile"
            or die "$::me-error: could not open $indexFile\n";

    open FH_OUT, ">$tmpFile"
            or die "$::me-error: Could not open $tmpFile";

    while (<FH_IN>)
    {
        if (/<title>Trace Compass/)
        {
            print FH_OUT "<title>$title</title>\n";
        }
        elsif (/.*echo.*Trace Compass.*/)
        {
            print FH_OUT "echo \"<tr class=\\\"h\\\"><td colspan=\\\"3\\\"><h1 class=\\\"p\\\">$title</h1></td></tr>\";\n";
        }
        else
        {
            print FH_OUT $_;
        }
    }
    System("\\rm -f $indexFile");
    System("mv -f $tmpFile $indexFile");
    close FH_IN;
    close FH_OUT;
}
