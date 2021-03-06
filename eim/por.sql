INSERT INTO SIEBEL.S_LOY_TXN (ROW_ID ,
CREATED            ,
CREATED_BY         ,
LAST_UPD           ,
LAST_UPD_BY        ,
MODIFICATION_NUM   ,
CONFLICT_ID        ,
BID_FLG            ,
BU_ID              ,
EXTERNAL_FLG       ,
OVR_DUP_CHECK_FLG  ,
OVR_PRI_FLAG       ,
QUAL_FLG           ,
STATUS_CD          ,
SUB_TYPE_CD        ,
TXN_NUM            ,
TYPE_CD            ,
UNACC_MINOR_FLG    )  
(SELECT ROW_ID,
CREATED,
CREATED_BY,
LAST_UPD,
LAST_UPD_BY,
MODIFICATION_NUM,
CONFLICT_ID,
'Y',
'1-1',
'Y',
'N',
'N',
'N',
'Queued',
'Crazy',
LM_ROWID_SEQ.NEXTVAL,
'Product',
'N' FROM SIEBEL.CX_FINT_TXN WHERE FILE_ID=(SELECT ROW_ID FROM SIEBEL.CX_FINT_FILE WHERE LAST_UPD = (SELECT MAX(LAST_UPD) FROM SIEBEL.CX_FINT_FILE)));

