
------ CONNECT TRANSACTION IDS --------
DECLARE
  IN_FILE_ID VARCHAR2(15) := :IN_FILE_ID;
  CURSOR C_Txn IS
    SELECT _LM_<EIMRowIdColumn>, _LM_<EIMConnectingColumn>, IF_ROW_STAT
      FROM SIEBEL._LM_<EIMTable>
     WHERE FILE_ID = IN_FILE_ID;

  TYPE T_Txn IS TABLE OF C_Txn%ROWTYPE;
  
  L_Rec T_Txn;
BEGIN
  OPEN C_Txn;
  LOOP
    FETCH C_Txn BULK COLLECT INTO L_Rec LIMIT 500;
    EXIT WHEN L_Rec.COUNT = 0;
    
    FOR i IN 1..L_Rec.COUNT
    LOOP
      UPDATE SIEBEL._LM_<PrestageTable>
         SET SBL_ROW_ID = L_Rec(i)._LM_<EIMRowIdColumn>,
             ERROR_CODE = DECODE(L_Rec(i).IF_ROW_STAT,'IMPORTED', NULL,'ROW_REJECTED',NULL, 'SBL-FINT-0010'),             
             ERROR_DESC = DECODE(L_Rec(i).IF_ROW_STAT,'IMPORTED', NULL,'ROW_REJECTED',NULL, LM_ERROR('SBL-FINT-0010', L_Rec(i).IF_ROW_STAT)),
             REC_STATUS = DECODE(L_Rec(i).IF_ROW_STAT,'IMPORTED', 'Loaded', 'ROW_REJECTED', REC_STATUS, 'Error')
       WHERE _LM_<EIMConnectingColumn> = L_Rec(i)._LM_<EIMConnectingColumn>;
    END LOOP;  
    COMMIT;
  END LOOP;
  COMMIT;

  -- Hide the IMPORTED ones in case we have ROW_REJECTED and need to go through this recordset again.
  UPDATE SIEBEL._LM_<EIMTable> SET IF_ROW_BATCH_NUM = 0 WHERE IF_ROW_STAT = 'IMPORTED' AND FILE_ID = IN_FILE_ID;
  COMMIT; 

  -- in case we got filtered out, prepare for the next step.
  UPDATE SIEBEL._LM_<EIMTable> SET IF_ROW_STAT = 'FOR_IMPORT' WHERE IF_ROW_STAT = 'ROW_REJECTED' AND FILE_ID = IN_FILE_ID;
  COMMIT; 
  
  IF C_Txn%ISOPEN THEN
    CLOSE C_Txn;
  END IF;
END;