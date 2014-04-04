DELETE FROM SIEBEL.CX_FINT_SETUP;
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.siebel.jdbc.user',
				'SIEBEL', 'SIEBEL data source username');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.siebel.jdbc.password',
				't4bleown3r', 'SIEBEL data source password');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.batch.jdbc.user',
				'BATCH', 'BATCH data source user name');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.batch.jdbc.password',
				'batch', 'BATCH data source password');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'misc.timezone',
				'America/Los_Angeles', 'Default timezone for dates - should match whatever the setting is in Siebel otherwise times in the UI will be incosistent.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.jdbc.url',
				'jdbc:oracle:thin:@//localhost:1521/XE', '');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.jdbc.driver',
				'oracle.jdbc.driver.OracleDriver', '');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.username',
				'SADMIN', 'What to pass under the /u switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.password',
				'asd', 'What to pass under the /p switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.gateway',
				'localhost', 'What to pass under the /g switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.enterprise',
				'asd', 'What to pass under the /e switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.server',
				'asd', 'What to pass under the /e switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'mail.host',
				'localhost', 'Mail gateway host address');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'mail.port',
				'25', 'Mail gateway TCP/IP port number');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'mail.authenticate',
				'false', 'Mail gateway authentication required - true/false');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'mail.enableTLS',
				'false', 'Mail gateway usees TLS - true/false');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.incoming.path',
				'/c:/Users/Emil/Documents/Dropbox/dev/FeedXChange/fx/data/incoming', 'Where FeedXChange looks for new files to be processed');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.outgoing.path',
				'/c:/Users/Emil/Documents/Dropbox/dev/FeedXChange/fx/data/outgoing', 'Path to store response files that are outgoing to partners');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.processed.path',
				'/c:/Users/Emil/Documents/Dropbox/dev/FeedXChange/fx/data/processed', 'Path where we move files which are processed. We move them from the batch.incoming.path.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'eim.status.interval',
				'1000', 'How often (milliseconds) to check on EIM task status');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.conf.path',
				'/c:/Users/Emil/Documents/Dropbox/dev/FeedXChange/fx/conf', 'Backend system configuration path for storing log4j.properties and batch.properties');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'loyengine.inactivity.interval',
				'5000', 'How often (milliseconds) to check the change in # of Queued records for an integration.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.meta.path',
				'/c:/Users/Emil/Documents/Dropbox/dev/FeedXChange/fx/jobs', 'Backend system where all job definitions are stored.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'loyengine.inactivity.timeout',
				'30000', 'How long (milliseconds) to wait before assuming that the Loyalty Engine is down. (i.e. # Queued hasn''t changed for the duration of this time)');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'dll.meta.name',
				'metagen', 'Generates metadata for a given integration.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.bin.path',
				'/c:/Users/Emil/Documents/Dropbox/dev/FeedXChange/fx/bin', 'Backend system binary path where all DLLs will be located');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'dll.rerun.name',
				'rerunbatch', 'Used to re-run a file which failed during an integration run.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'demo.sleep',
				'1', 'How long (milliseconds) to sleep between steps - useful if we are trying to slow down the process for a demo');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'dll.param.name',
				'paramgen', 'Generates batch.properties file from these paramters.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'dll.run.name',
				'runbatch', 'Used to run an integration.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'response.suffix',
				'ack', 'Default suffix to append to files for response');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'validate.commit.interval',
				'1', 'Commit interval when reading from a file and writing to the DB during intial validation.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.log.path',
				'/c:/Users/Emil/Documents/Dropbox/dev/FeedXChange/fx/logs', 'Backend system log path whwere all log files will go by default.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'status.record.processed',
				'Processed', 'What text to place in REC_STATUS when the record is successfully processed.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'revalidate.commit.interval',
				'1', 'Commit interval when reading and writing records from the DB during a revalidation.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'status.record.error',
				'Error', 'What text to place in REC_STATUS when the record is in error.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'eim.status.timeout',
				'18000', 'How long (milliseconds) to wait before giving up on EIM execution');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.executable.path',
				'/c:/Users/Emil/Documents/Dropbox/dev/FeedXChange/fx/bin/srvrmgr', 'Path to wrapper script that runs srvrmgr for us.');
COMMIT;
/
QUIT
/
