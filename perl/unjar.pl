#/* ******************************************************** 
#** Copyright (c) 2000-2001, Thomas Maxwell White, all rights reserved. 
#** $Header$
#******************************************************** */ 
# Perl script to automate the following:
#	(1) unjar all files in a directory
#	(2) update any old datafiles
#	(3) run dat2sas on the resulting .dat files
#	(4) optionally remove the .dat files
#
# TODO
#  (1) better documentation
#  (2) consider passing $Prefs to all associated files, rather than passing command line args

use strict;

use Dialogix::Utils;

if ($#ARGV != 0) {
	print "Usage:\nperl unjar.pl <config-file>\n";
	print "see sample.conf for an example of the config parameters\n";
	exit(0);
}

my ($Prefs,$conf_file);
    
&main(@ARGV);

sub main {
	$conf_file = shift;
	$Prefs = &Dialogix::Utils::readDialogixPrefs($conf_file);
	return 0 unless &prepare;
	&doall;
}

sub doall {
	&removeOldAnalysisFiles if ($Prefs->{removeOldAnalysisFiles}==1);	# removes any lingering .log files (since dat2sas appends to them)
	&sched2sas if ($Prefs->{sched2sas}==1); # post-process instrument file
	&unjarall if ($Prefs->{unjarall}==1);	# uncompresses all of the .jar files in the directory
	&moveWorkingFiles if ($Prefs->{moveWorkingFiles}==1);	# move incomplete (unjarred) data files to appropriate sub directories
	&update_dat if ($Prefs->{update_dat}==1);	# converts old Triceps data format (from before version 2.5) to the new format
	&dat2sas if ($Prefs->{dat2sas}==1);		# runs dat2sas on the .dat files in the directory
	&evt2sas if ($Prefs->{evt2sas}==1);		# runs evt2sas.pl
# cleanup
	&removeEvtFiles if ($Prefs->{removeEvtFiles}==1);	# remove .evt files
	&removeErrFiles if ($Prefs->{removeErrFiles}==1);	# remove .err files
	&removeDatFiles if ($Prefs->{removeDatFiles}==1);	# removes the temporary files (.dat and .dat.evt);
}

sub prepare {
	return 0 unless checkDir($Prefs->{PERL_SCRIPTS_PATH},1);
	return 0 unless checkDir($Prefs->{INSTRUMENT_DIR},1);
	checkDir($Prefs->{RESULTS_DIR},0);
	checkDir($Prefs->{UNJAR_DIR},0);
	checkDir($Prefs->{UNFINISHED_DIR},-1);
	return 1;
}

sub checkDir {
	my $dir = shift;
	my $required = shift;
	
	unless (-d $dir) {
		if ($required > 0) {
			print "ERROR -- dir '$dir' must exist\n";
			return 0;
		}
		if ($required != -1) {
			print "creating dir '$dir'\n";
			mkdir($dir, 0777);
		}
	}
}

sub unjarall {
	my @files = glob($Prefs->{JAR_FILES});
	
	&Dialogix::Utils::mychdir($Prefs->{UNJAR_DIR});
	
	my $tmpdir = "$Prefs->{UNJAR_DIR}/../_temp_";
	checkDir($tmpdir,0);
	&Dialogix::Utils::mychdir($tmpdir);
	
	foreach (@files) {
		# unjar to temp directory; then determine which instrument belong to; then move to that new directory
		next if (-d $_);
		my $srcname = $_;
		&doit("$Prefs->{JAR}  xvf \"$_\"");
		foreach (glob("*.err")) {
			unlink $_ unless (-d $_);
		}
		
		my @files = glob("*");
		my $instrument_name = &moveDataFiles($srcname,@files);
		foreach (@files) {
			unlink $_ unless (-d $_);
		}
		
		# move jar files to the appropriate directory for easier maintenance
		my $dstdir = "$Prefs->{JAR_FILES}/$instrument_name";
		$dstdir =~ s/\*\.jar//g;	# remove "*.jar" from path 
		unless (-d $dstdir) {
			mkdir($dstdir, 0777);
		}

		my $msg = "copy \"$srcname\" \"$dstdir\"";
		# convert to dos format
		$msg =~ s/\/+/\\/g;
		&doit($msg);
		unlink $srcname;
	}
	&Dialogix::Utils::mychdir($Prefs->{PERL_SCRIPTS_PATH});
}

sub moveWorkingFiles {
	&Dialogix::Utils::mychdir($Prefs->{UNFINISHED_DIR});
	my @files = glob("*.dat *.");
	
	foreach (@files) {
		next if (-d $_);
		my $srcname = $_;
		my $instrument_name = &moveDataFiles($_,glob("$srcname*"));
		
		# move src files to the appropriate directory for easier maintenance
		my $dstdir = "$Prefs->{UNFINISHED_DIR}/$instrument_name";
		$dstdir =~ s/\/+/\//g;	# remove duplicate '/' from path
		unless (-d $dstdir) {
			mkdir($dstdir, 0777);
		}
		
		foreach my $file (glob("$srcname*")) {
			next if (-d $_);
			my $msg = "copy \"$file\" \"$dstdir\"";
			# convert to dos format
			$msg =~ s/\/+/\\/g;
			&doit($msg);
			unlink $file;		
		}
	}
	&Dialogix::Utils::mychdir($Prefs->{PERL_SCRIPTS_PATH});
}

sub moveDataFiles {
	my $srcname = shift;
	my @files = @_;
	
	if ($srcname =~ /^.+[\\\/](.+)(\.(jar|txt|dat|dat\.evt))$/) {
		$srcname = $1;
	}
	else {
		$srcname = $_;
	}
	
	my ($filename,$inst,$timestamp,$when) = &Dialogix::Utils::whichInstrument($files[0]);	# assume that they are all from the same same data prefix
	print "$files[0] => $inst\n";
	my $locdir = $inst;
	my $dstdir = "$Prefs->{UNJAR_DIR}/$locdir";
	unless (-d $dstdir) {
		mkdir($dstdir, 0777);
	}
	# convert to dos format
	$dstdir =~ s/\//\\/g;
	
	foreach (@files) {
		&doit("copy \"$_\" \"$dstdir\\$when-($srcname)-$_\"");
	}
	return $inst;
}

sub doit {
	my $cmd = shift;
	print "$cmd\n";
	(system($cmd) == 0)	or die "ERROR";
}

sub removeOldAnalysisFiles {
	my @files = glob("$Prefs->{RESULTS_DIR}/*.log");
	push @files, glob("$Prefs->{RESULTS_DIR}/*.s*");
	push @files, glob("$Prefs->{RESULTS_DIR}/*.tsv");

	foreach (@files) {
		unlink $_ unless (-d $_);
	}
}

sub update_dat {
	my $command = "perl $Prefs->{UPDATE_DAT} $conf_file $Prefs->{DAT_FILES}";
	&doit($command);
}

sub dat2sas {
	my $command = "perl $Prefs->{DAT2SAS} $conf_file";
	&doit($command);
}

sub evt2sas {
	my $command = "perl $Prefs->{EVT2SAS} $conf_file";
	&doit($command);
}

sub removeDatFiles {
	my @files = glob("$Prefs->{RESULTS_DIR}/*.dat");
	foreach (@files) {
		unlink $_ unless (-d $_);
	}
}

sub removeEvtFiles {
	my @files = glob("$Prefs->{RESULTS_DIR}/*.evt");
	foreach (@files) {
		unlink $_ unless (-d $_);
	}
}

sub removeErrFiles {
	my @files = glob("$Prefs->{UNJAR_DIR}/*.err");
	foreach (@files) {
		unlink $_ unless (-d $_);
	}
}

sub sched2sas {
	my $command = "perl $Prefs->{SCHED2SAS} $conf_file $Prefs->{INSTRUMENT_FILE}";
	&doit($command);
}