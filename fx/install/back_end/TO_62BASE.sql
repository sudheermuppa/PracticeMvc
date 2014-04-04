create or replace
FUNCTION TO_62BASE (p_num NUMBER)
	RETURN VARCHAR2
AS
	l_str    VARCHAR2 (30) := NULL;
	l_base   NUMBER := 62;
    l_hex    VARCHAR2 (68)
			 DEFAULT '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';
	l_num    NUMBER := 0;
    l_num1   NUMBER := 0;
BEGIN
	IF (TRUNC (p_num) <> p_num OR p_num < 0)
	THEN
		RETURN NULL;
	END IF;

	l_num := p_num;

	BEGIN
		LOOP
			l_num1 := l_num1 + 1;
			l_str := SUBSTR (l_hex, MOD (l_num, l_base) + 1, 1) || l_str;
			l_num := TRUNC (l_num / l_base);
			EXIT WHEN (l_num = 0 OR l_num1 > 30);
		END LOOP;

		RETURN l_str;
		EXCEPTION
		WHEN OTHERS
		THEN
			RETURN NULL;
	END;
END;
/
quit
/
