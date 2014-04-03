ALTER TABLE :sbluser.EIM_LOY_TXN ADD ( FILE_ID VARCHAR2(15) );
ALTER TABLE :sbluser.EIM_LOY_MEMBER ADD ( FILE_ID VARCHAR2(15) );

CREATE INDEX :sbluser.FX_EIM_LOY_TXN_F1 ON :sbluser.EIM_LOY_TXN (FILE_ID);

CREATE INDEX :sbluser.FX_EIM_LOY_MEMBER_F1 ON :sbluser.EIM_LOY_MEMBER ( FILE_ID);