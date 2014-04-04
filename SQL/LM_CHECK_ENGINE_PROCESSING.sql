
------ CHECK ENGINE PROCESSING RESULTS --------
DECLARE
  IN_FILE_ID VARCHAR2(15) := :IN_FILE_ID;
  CURSOR C_Txn IS
    SELECT E.ROW_ID, S.STATUS_CD
      FROM SIEBEL.EIM_LOY_TXN E, SIEBEL.S_LOY_TXN S
     WHERE FILE_ID = IN_FILE_ID
       AND E.IF_ROW_STAT = 'IMPORTED'
       AND E.T_LOY_TXN__RID = S.ROW_ID;

  TYPE T_Txn IS TABLE OF C_Txn%ROWTYPE;
  
  L_Rec T_Txn;
  L_Rec_Count NUMBER := 0;
BEGIN
  OPEN C_Txn;
  LOOP
    FETCH C_Txn BULK COLLECT INTO L_Rec LIMIT 25000;
    EXIT WHEN L_Rec.COUNT = 0;
    
    FOR i IN 1..L_Rec.COUNT
    LOOP
		-- SBL-FINT-0015: Engine failed to process transaction. Engine marked record with status '%1'
    UPDATE SIEBEL.CX_FINT_TXN
         SET ERROR_CODE = DECODE(L_Rec(i).STATUS_CD,'Processed', NULL,'SBL-FINT-0011'),             
             ERROR_DESC = DECODE(L_Rec(i).STATUS_CD,'Processed', NULL,LM_ERROR('SBL-FINT-0011',L_Rec(i).STATUS_CD)),		
             REC_STATUS = DECODE(L_Rec(i).STATUS_CD,'Processed','Processed', 'Unfixable')
       WHERE ROW_ID = L_Rec(i).ROW_ID;
    END LOOP;  
    COMMIT;
  END LOOP;
  COMMIT;
  IF C_Txn%ISOPEN THEN
    CLOSE C_Txn;
  END IF;
END;