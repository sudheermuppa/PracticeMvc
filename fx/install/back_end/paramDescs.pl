# --------------------------------------- Desciprtions of Properties -----------------------

%descs = (
"dll.purge.name" => "Executable to run when purging a file.",
"job.fileget.retry.sleep" => "How long (ms) to sleep between retry of incoming file retrieval.",
"job.fileget.retry.max" => "How many times to try to retrieve incoming files before giving up.",
"job.error.threshold" => "If Error records in a file exceed this threshold we stop the job, otherwise mark Unfixable and respond.",
"batch.request.server" => "Server name where requests for FX will go by default.",
"status.record.processed" => "What text to place in REC_STATUS when the record is successfully processed.",
"status.record.error" => "What text to place in REC_STATUS when the record is in error.",
"batch.meta.path" => "Backend system where all job definitions are stored.",
"batch.bin.path" => "Backend system binary path where all DLLs will be located",
"response.suffix" => "Default suffix to append to files for response",
"loyengine.inactivity.timeout" => "How long (milliseconds) to wait before assuming that the Loyalty Engine is down. (i.e. # Queued hasn''t changed for the duration of this time)",
"loyengine.inactivity.interval" => "How often (milliseconds) to check the change in # of Queued records for an integration.",
"batch.log.path" => "Backend system log path whwere all log files will go by default.",
"revalidate.commit.interval" => "Commit interval when reading and writing records from the DB during a revalidation.",
"validate.commit.interval" => "Commit interval when reading from a file and writing to the DB during intial validation.",
"db.batch.jdbc.driver" => "BATCH data source JDBC driver",
"db.batch.jdbc.url" => "BATCH data source URL",
"db.batch.jdbc.user" => "BATCH data source user name",
"db.batch.jdbc.password" => "BATCH data source password",
"db.siebel.jdbc.driver" => "SIEBEL data source JDBC driver",
"db.siebel.jdbc.url" => "SIEBEL data source URL",
"db.siebel.jdbc.user" => "SIEBEL data source username",
"db.siebel.jdbc.password" => "SIEBEL data source password",
"srvrmgr.executable.path" => "Path to wrapper script that runs srvrmgr for us.",
"srvrmgr.param.username" => "What to pass under the /u switch for srvrmgr",
"srvrmgr.param.password" => "What to pass under the /p switch for srvrmgr",
"srvrmgr.param.gateway" => "What to pass under the /g switch for srvrmgr",
"srvrmgr.param.server" => "What to pass under the /e switch for srvrmgr",
"srvrmgr.param.enterprise" => "What to pass under the /e switch for srvrmgr",
"eim.status.interval" => "How often (milliseconds) to check on EIM task status",
"eim.status.timeout" => "How long (milliseconds) to wait before giving up on EIM execution",
"demo.sleep" => "How long (milliseconds) to sleep between steps - useful if we are trying to slow down the process for a demo",
"mail.host" => "Mail gateway host address",
"mail.port" => "Mail gateway TCP/IP port number",
"mail.auth.username" => "Mail gateway authentication username",
"mail.auth.password" => "Mail gateway authentication password",
"mail.authenticate" => "Mail gateway authentication required - true/false",
"mail.enableTLS" => "Mail gateway usees TLS - true/false",
"mail.message.from" => "What address to use in the FROM: field of any alerts FeedXChange sends",
"batch.incoming.path" => "Where FeedXChange looks for new files to be processed",
"batch.processed.path" => "Path where we move files which are processed. We move them from the batch.incoming.path.",
"batch.outgoing.path" => "Path to store response files that are outgoing to partners",
"batch.conf.path" => "Backend system configuration path for storing log4j.properties and batch.properties",
"misc.timezone" => "Default timezone for dates - should match whatever the setting is in Siebel otherwise times in the UI will be incosistent.",
"dll.meta.name" => "Generates metadata for a given integration.",
"dll.run.name" => "Used to run an integration.",
"dll.rerun.name" => "Used to re-run a file which failed during an integration run.",
"dll.param.name" => "Generates batch.properties file from these paramters.", 
"file.page.size" => "How many lines to display at a time when browsing a log or response file.");
