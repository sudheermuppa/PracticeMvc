---------- STAGE EIM TRANSACTIONS ---------
DECLARE
	IN_FILE_ID VARCHAR2(15) := :IN_FILE_ID;
CURSOR c_Txns
IS
  SELECT *
    FROM SIEBEL._LM_<PrestageTable>
   WHERE FILE_ID = IN_FILE_ID
     AND REC_STATUS IN ('Prestaged', 'Queued');

TYPE T_CX_FINT_TXN IS TABLE OF c_Txns%ROWTYPE
      INDEX BY BINARY_INTEGER;

l_Fint_Txn_Table    T_CX_FINT_TXN;
l_If_Batch_Row      VARCHAR2(50);
l_Num_Rows			NUMBER;
l_Min_Batch_Size	NUMBER := 5000;

BEGIN
   SELECT LM_EIM_SEQ.NEXTVAL-99 INTO l_If_Batch_Row FROM DUAL; -- assumed sequence increment is 100
   
   SELECT COUNT(*) INTO l_Num_Rows 
     FROM SIEBEL._LM_<PrestageTable> 
    WHERE REC_STATUS IN ('Prestaged', 'Queued') AND FILE_ID = IN_FILE_ID;
   
   IF (l_Num_Rows+100) / 100 > l_Min_Batch_Size THEN
   	 l_Min_Batch_Size := (l_Num_Rows + 100) / 100;
   END IF;
   
   IF l_Num_Rows = 0 THEN
   	 RETURN;
   END IF;
   	
  -- if there was anything there before, clear it before re-staging
   DELETE
    FROM SIEBEL._LM_<EIMTable>
   WHERE FILE_ID = IN_FILE_ID;

  COMMIT;

  OPEN c_Txns;

  LOOP
    FETCH c_Txns BULK COLLECT INTO l_Fint_Txn_Table  LIMIT l_Min_Batch_Size;
    EXIT WHEN l_Fint_Txn_Table.COUNT = 0;
    
    FOR i IN 1 .. l_Fint_Txn_Table.COUNT
    LOOP
      INSERT INTO SIEBEL._LM_<EIMTable>( ROW_ID,
      							FILE_ID,
                                IF_ROW_STAT,
                                IF_ROW_BATCH_NUM,
                                _LM_<InsertColumns>)
            VALUES (   l_Fint_Txn_Table (i).ROW_ID,
            		   IN_FILE_ID,
                       'FOR_IMPORT',
                       l_If_Batch_Row,
                       _LM_<InsertValues>);
      END LOOP;
      COMMIT;
      l_if_batch_row := l_if_batch_row + 1;
    END LOOP;

    IF c_Txns%ISOPEN THEN
      CLOSE c_Txns;
    END IF;
END;