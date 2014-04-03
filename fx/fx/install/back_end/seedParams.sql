DELETE FROM SIEBEL.CX_FINT_SETUP;
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.siebel.jdbc.user',
				'dsa', 'SIEBEL data source username');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.siebel.jdbc.password',
				'sdad', 'SIEBEL data source password');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.batch.jdbc.user',
				'sa', 'BATCH data source user name');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.batch.jdbc.password',
				'sadasd', 'BATCH data source password');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'misc.timezone',
				'fsd', 'Default timezone for dates - should match whatever the setting is in Siebel otherwise times in the UI will be incosistent.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.jdbc.url',
				'd', '');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'db.jdbc.driver',
				'd', '');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.username',
				'd', 'What to pass under the /u switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.password',
				'fsd', 'What to pass under the /p switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.gateway',
				'df', 'What to pass under the /g switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.enterprise',
				'd', 'What to pass under the /e switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'srvrmgr.param.server',
				'sdf', 'What to pass under the /e switch for srvrmgr');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'mail.host',
				'ds', 'Mail gateway host address');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'mail.port',
				'd', 'Mail gateway TCP/IP port number');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'mail.authenticate',
				'fds', 'Mail gateway authentication required - true/false');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'mail.enableTLS',
				'df', 'Mail gateway usees TLS - true/false');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.incoming.path',
				'ds', 'Where FeedXChange looks for new files to be processed');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.outgoing.path',
				'fd', 'Path to store response files that are outgoing to partners');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.processed.path',
				'fd', 'Path where we move files which are processed. We move them from the batch.incoming.path.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'file.page.size',
				'40', 'How many lines to display at a time when browsing a log or response file.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'job.fileget.retry.sleep',
				'300000', 'How long (ms) to sleep between retry of incoming file retrieval.');
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
				'batch.request.server',
				'sdf', 'Server name where requests for FX will go by default.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.conf.path',
				'C:\eclipse\backendinstall\fx/conf', 'Backend system configuration path for storing log4j.properties and batch.properties');
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
				'C:\eclipse\backendinstall\fx/jobs', 'Backend system where all job definitions are stored.');
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
				'job.error.threshold',
				'0', 'If Error records in a file exceed this threshold we stop the job, otherwise mark Unfixable and respond.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'dll.purge.name',
				'purgefile', 'Executable to run when purging a file.');
		INSERT INTO SIEBEL.CX_FINT_SETUP (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, NAME, VALUE, COMMENTS) 
		VALUES  (
				'1-' || LM_GEN_UID(),
				CURRENT_DATE,
				'0-1',
				CURRENT_DATE,
				'0-1',
				'batch.bin.path',
				'C:\eclipse\backendinstall\fx/bin', 'Backend system binary path where all DLLs will be located');
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
				'mail.message.from',
				'FeedXChange', 'What address to use in the FROM: field of any alerts FeedXChange sends');
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
				'C:\eclipse\backendinstall\fx/logs', 'Backend system log path whwere all log files will go by default.');
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
				'job.fileget.retry.max',
				'3', 'How many times to try to retrieve incoming files before giving up.');
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
				'eim.status.timeout',
				'18000', 'How long (milliseconds) to wait before giving up on EIM execution');
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
				'srvrmgr.executable.path',
				'C:\eclipse\backendinstall\fx/bin/srvrmgr', 'Path to wrapper script that runs srvrmgr for us.');
DELETE FROM SIEBEL.CX_FINT_ERROR
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0003',
		'Field ''%1'' with value of ''%2'' can not be parsed with format ''%3''.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0015',
		'Bad metadata: missing metadata for field ''%1''. You need to specify at least Type.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0014',
		'No default specified for unmapped field ''%1''.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0013',
		'All records failed to process.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0012',
		'Some records failed to process.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0002',
		'Date field ''%1'' with value of ''%2'' can not be parsed with format ''%3'': %4' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0009',
		'One or more records with the same ( %1 ) found.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0010',
		'EIM failed to import record with error ''%1''' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0004',
		'Field ''%1'' exceeds its maximum length of %2.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0017',
		'Attempted to process duplicate file.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0011',
		'Engine failed to process transaction. Engine marked record with status ''%1''.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0008',
		'Field ''%1'' with value ''%2'' does not match lookup ''%3''.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0006',
		'Product ''%1'' does not exist or it is not offered by partner ''%2''.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0007',
		'No active member number found for member number ''%1''' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0001',
		'Field ''%1'' is marked as required but has no value and is not defaulted.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0000',
		'Unknown error: %1' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0005',
		'Field ''%1'' does not match pattern ''%2''.' );
	INSERT INTO SIEBEL.CX_FINT_ERROR (ROW_ID, CREATED, CREATED_BY, LAST_UPD, LAST_UPD_BY, ERROR_CD, ERROR_DESC)
	 VALUES (
	 	'1-' || LM_GEN_UID(),
		CURRENT_DATE,
		'0-1',
		CURRENT_DATE,
		'0-1',
		'SBL-FINT-0016',
		'Bad metadata: type is unknown for field ''%1''. Valid types are Character, Number or Date.' );
COMMIT;
/
QUIT
/
