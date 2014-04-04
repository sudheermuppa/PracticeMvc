---------- Mark Records as Processed as Needed ---------
DECLARE
CURSOR c_Txns
IS
  SELECT ROW_ID
    FROM SIEBEL._LM_<PrestageTable>
   WHERE FILE_ID = '_LM_<FileId>'
     AND REC_STATUS IN (_LM_<FromStatus>);

TYPE T_CX_FINT_TXN IS TABLE OF c_Txns%ROWTYPE
      INDEX BY BINARY_INTEGER;

l_Fint_Txn_Table    T_CX_FINT_TXN;
BEGIN
  
  OPEN c_Txns;

  LOOP
    FETCH c_Txns BULK COLLECT INTO l_Fint_Txn_Table  LIMIT 10000;
    EXIT WHEN l_Fint_Txn_Table.COUNT = 0;
    
    FOR i IN 1..l_Fint_Txn_Table.COUNT
    LOOP
	    UPDATE _LM_<PrestageTable>
       	SET REC_STATUS = _LM_<ToStatus>
     	WHERE l_Fint_Txn_Table(i).ROW_ID = ROW_ID;
    END LOOP;
	COMMIT;
  END LOOP;
  
  IF c_Txns%ISOPEN THEN
  	CLOSE c_Txns;
  END IF;
END; 
