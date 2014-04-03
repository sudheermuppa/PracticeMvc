-- Grant the BATCH user access to the Siebel CX_FINT* schema objects
BEGIN
	FOR i IN (SELECT object_name FROM user_objects WHERE object_type IN ('TABLE','VIEW') AND object_name LIKE 'CX_FINT%') 
	LOOP	
		EXECUTE IMMEDIATE 'GRANT SELECT, INSERT, DELETE, UPDATE ON ' || i.object_name || ' TO :batch';
	END LOOP;
END;

