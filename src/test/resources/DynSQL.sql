DECLARE
  IN_FILE_ID VARCHAR(30) := :IN_FILE_ID;
 
  CURSOR C_MEMBERS
  IS
    SELECT  SBL.ROW_ID ROW_ID, SBL.MEM_NUM MEM_NUM, SBL.NAME PROG_NAME, STG.MEMBER_MEM_NUM MEMBER_MEM_NUM     --+ parallel(STG,12)
      FROM ( SELECT DISTINCT MEMBER_MEM_NUM, PROG_NAME
               FROM SIEBEL.CX_FINT_TXN
              WHERE REC_STATUS IN ('Prestaged', 'Queued')
                AND FILE_ID = IN_FILE_ID ) STG,
           ( SELECT SIEBEL.S_LOY_MEMBER.ROW_ID, SIEBEL.S_LOY_MEMBER.MEM_NUM, SIEBEL.S_LOY_PROGRAM.NAME
               FROM SIEBEL.S_LOY_MEMBER, SIEBEL.S_LOY_PROGRAM
              WHERE SIEBEL.S_LOY_MEMBER.PROGRAM_ID = SIEBEL.S_LOY_PROGRAM.ROW_ID
                AND SIEBEL.S_LOY_MEMBER.STATUS_CD = 'Active' ) SBL
     WHERE STG.MEMBER_MEM_NUM = SBL.MEM_NUM (+)
       AND STG.PROG_NAME = SBL.NAME (+);

  TYPE L_MEM_NUM_TAB IS TABLE OF C_MEMBERS%ROWTYPE;
  L_MEMNUM  L_MEM_NUM_TAB;

BEGIN
  OPEN C_MEMBERS;

  LOOP
    FETCH C_MEMBERS BULK COLLECT INTO L_MEMNUM LIMIT 25000;
    EXIT WHEN L_MEMNUM.COUNT = 0;

    FOR I IN 1..L_MEMNUM.COUNT
    LOOP
      IF L_MEMNUM(i).MEM_NUM IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('NULL');
      END IF;

      UPDATE SIEBEL.CX_FINT_TXN
         SET MEM_ID = L_MEMNUM(i).ROW_ID,
         ERROR_CODE =
                DECODE(L_MEMNUM(I).MEM_NUM, NULL, 'SBL-FINT-007', ERROR_CODE),
         ERROR_DESC =
                DECODE(L_MEMNUM(I).MEM_NUM, NULL, 'No active member number found for member number ''' || L_MEMNUM(I).MEMBER_MEM_NUM || '''', ERROR_DESC),
        REC_STATUS =
                DECODE(L_MEMNUM(I).MEM_NUM, NULL, 'Error', REC_STATUS)
       WHERE MEMBER_MEM_NUM = L_MEMNUM(I).MEMBER_MEM_NUM AND FILE_ID = IN_FILE_ID;
    END LOOP;

    COMMIT;
  END LOOP;

  IF C_MEMBERS%ISOPEN THEN
    CLOSE C_MEMBERS;
  END IF;

END LM_CHECK_TXN_MEMBERS;