-- Autogenerated: do not edit this file

DROP TABLE BATCH_JOB_EXECUTION_CONTEXT  ;
DROP TABLE BATCH_STEP_EXECUTION_CONTEXT  ;
DROP TABLE BATCH_STEP_EXECUTION  ;
DROP TABLE BATCH_JOB_EXECUTION  ;
DROP TABLE BATCH_JOB_PARAMS  ;
DROP TABLE BATCH_JOB_INSTANCE ; 

CREATE TABLE BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID NUMBER(19,0)  NOT NULL PRIMARY KEY ,  
	VERSION NUMBER(19,0) ,  
	JOB_NAME VARCHAR2(100) NOT NULL, 
	JOB_KEY VARCHAR2(32) NOT NULL,
        FILE_ID VARCHAR(15),
	constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;

CREATE TABLE BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID NUMBER(19,0)  NOT NULL PRIMARY KEY ,
	VERSION NUMBER(19,0)  ,  
	JOB_INSTANCE_ID NUMBER(19,0) NOT NULL,
	CREATE_TIME TIMESTAMP NOT NULL,
	START_TIME TIMESTAMP DEFAULT NULL , 
	END_TIME TIMESTAMP DEFAULT NULL ,
	STATUS VARCHAR2(10) ,
	EXIT_CODE VARCHAR2(100) ,
	EXIT_MESSAGE VARCHAR2(2500) ,
	LAST_UPDATED TIMESTAMP,
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;
	
CREATE TABLE BATCH_JOB_PARAMS  (
	JOB_INSTANCE_ID NUMBER(19,0) NOT NULL ,
	TYPE_CD VARCHAR2(6) NOT NULL ,
	KEY_NAME VARCHAR2(100) NOT NULL , 
	STRING_VAL VARCHAR2(250) , 
	DATE_VAL TIMESTAMP DEFAULT NULL ,
	LONG_VAL NUMBER(19,0) ,
	DOUBLE_VAL NUMBER ,
	constraint JOB_INST_PARAMS_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;
	
CREATE TABLE BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID NUMBER(19,0)  NOT NULL PRIMARY KEY ,
	VERSION NUMBER(19,0) NOT NULL,  
	STEP_NAME VARCHAR2(100) NOT NULL,
	JOB_EXECUTION_ID NUMBER(19,0) NOT NULL,
	START_TIME TIMESTAMP NOT NULL , 
	END_TIME TIMESTAMP DEFAULT NULL ,  
	STATUS VARCHAR2(10) ,
	COMMIT_COUNT NUMBER(19,0) , 
	READ_COUNT NUMBER(19,0) ,
	FILTER_COUNT NUMBER(19,0) ,
	WRITE_COUNT NUMBER(19,0) ,
	READ_SKIP_COUNT NUMBER(19,0) ,
	WRITE_SKIP_COUNT NUMBER(19,0) ,
	PROCESS_SKIP_COUNT NUMBER(19,0) ,
	ROLLBACK_COUNT NUMBER(19,0) , 
	EXIT_CODE VARCHAR2(100) ,
	EXIT_MESSAGE VARCHAR2(2500) ,
	LAST_UPDATED TIMESTAMP,
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID NUMBER(19,0) NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR2(2500) NOT NULL,
	SERIALIZED_CONTEXT CLOB , 
	constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
	references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
	JOB_EXECUTION_ID NUMBER(19,0) NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR2(2500) NOT NULL,
	SERIALIZED_CONTEXT CLOB , 
	constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

DROP SEQUENCE BATCH_STEP_EXECUTION_SEQ; 
DROP SEQUENCE BATCH_JOB_EXECUTION_SEQ; 
DROP SEQUENCE BATCH_JOB_SEQ; 
CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ START WITH 0 MINVALUE 0 MAXVALUE 9223372036854775807 NOCYCLE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ START WITH 0 MINVALUE 0 MAXVALUE 9223372036854775807 NOCYCLE;
CREATE SEQUENCE BATCH_JOB_SEQ START WITH 0 MINVALUE 0 MAXVALUE 9223372036854775807 NOCYCLE;
quit
