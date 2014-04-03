create or replace FUNCTION LM_GEN_UID
	RETURN VARCHAR2
AS
    l_jdatenseq   VARCHAR2 (30) := NULL;
    l_str         VARCHAR2 (30) := NULL;
	BEGIN
	  	SELECT TO_CHAR (SYSDATE, 'DDDHH24MISS') || LM_ROWID_SEQ.NEXTVAL
	      INTO l_jdatenseq
		    FROM DUAL;

        SELECT TO_62BASE(l_jdatenseq) INTO l_str FROM DUAL;

	    RETURN l_str;
    EXCEPTION
    WHEN OTHERS
       THEN RETURN NULL;
END;
