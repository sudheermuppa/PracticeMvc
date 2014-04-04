
--------- CHECK TRANSACTION DUPLICATES ----------
DECLARE
  
  IN_FILE_ID VARCHAR2(15) := :IN_FILE_ID;
  IN_SQL_PART VARCHAR2(250) := :USER_KEY;
  IN_PARTNER_NAME VARCHAR(50) := :PARTNER_NAME;

  BEGIN
	
  -- If no user key is defined don't do any checking
  
  IF IN_SQL_PART IS NULL OR IN_SQL_PART = '' THEN
  	RETURN;
  END IF;
	
  -- Generate the key for any records that might need refresh.
  
  EXECUTE IMMEDIATE
   'UPDATE SIEBEL.CX_FINT_TXN ' ||
   '   SET PRTNR_SYS_KEY=''' || IN_PARTNER_NAME || ''' || ' || REPLACE(IN_SQL_PART,',','||') ||
   ' WHERE FILE_ID = ''' || IN_FILE_ID || ''' AND REC_STATUS IN (''Prestaged'') ';
   
   COMMIT;
   
   -- Now check the key and flag the records.
   
   UPDATE SIEBEL.CX_FINT_TXN
      SET REC_STATUS = 'Error', 
          ERROR_CODE = 'SBL-FINT-0009',
          ERROR_DESC = LM_ERROR('SBL-FINT-0009', IN_SQL_PART)
    WHERE PRTNR_SYS_KEY IN (
    				  SELECT PRTNR_SYS_KEY
                        FROM SIEBEL.CX_FINT_TXN
                       WHERE FILE_ID IN (SELECT FILE_ID 
                                           FROM SIEBEL.CX_FINT_FILE F, SIEBEL.CX_FINT_INTG I, SIEBEL.S_ORG_EXT P
                                          WHERE F.INTEGRATION_ID=I.ROW_ID 
                                            AND I.PARTNER_ID = P.ROW_ID
                                            AND P.NAME = IN_PARTNER_NAME)
                         AND REC_STATUS IN ('Processed', 'Prestaged')
                         AND PRTNR_SYS_KEY IS NOT NULL
                    GROUP BY PRTNR_SYS_KEY
                      HAVING COUNT(*) > 1 )
      AND FILE_ID = IN_FILE_ID;
    COMMIT;
END;