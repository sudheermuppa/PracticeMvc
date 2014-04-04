-- Grant the SIEBEL user access to the BATCH schema objects

BEGIN
	FOR i IN (SELECT object_name FROM user_objects WHERE object_type IN ('TABLE','VIEW'))
	LOOP	
		EXECUTE IMMEDIATE 'GRANT SELECT, INSERT, DELETE, UPDATE ON ' || i.object_name || ' TO &1';
	END LOOP;
	
	FOR i IN (SELECT object_name FROM user_objects WHERE object_type IN ('PROCEDURE','FUNCTION'))
	LOOP	
		EXECUTE IMMEDIATE 'GRANT EXECUTE ON ' || i.object_name || ' TO &1';
	END LOOP;


	FOR i IN (SELECT object_name FROM user_objects WHERE object_type IN ('SEQUENCE'))
	LOOP	
		EXECUTE IMMEDIATE 'GRANT SELECT ON ' || i.object_name || ' TO &1';
	END LOOP;

END;
/
quit
