#!/usr/bin/perl

# simulator for server manager to run eim

while( $t = shift @ARGV) {
	#print $t . "\n";
	if( $t =~ /\/c/ ) {
		$t = shift @ARGV;
		#print $t . "\n";
		if( $t =~ /start task/ ) {
			$file = "start_task";
		}
		else {
			$file = "list_task";
		}
	}
}

system("c:/oraclexe/app/oracle/product/11.2.0/server/bin/sqlplus -S siebel/siebel\@xe \@c:/users/emil/documents/dropbox/dev/FeedXChange/eim/update.sql > NUL ");
system("c:/oraclexe/app/oracle/product/11.2.0/server/bin/sqlplus -S siebel/siebel\@xe \@c:/users/emil/documents/dropbox/dev/FeedXChange/eim/eim.sql > NUL ");


open(F, "c:/users/emil/documents/dropbox/dev/FeedXChange/eim/$file");
while(<F>) {
	print;
};
close F;


