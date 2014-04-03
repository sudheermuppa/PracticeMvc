Drop user :batch cascade;
create user :batch identified by :batch;
grant create session to :batch;
grant unlimited tablespace to :batch;
GRANT SSE_ROLE TO :batch;
GRANT CREATE TABLE TO :batch;
GRANT CREATE PROCEDURE TO :batch;
GRANT CREATE SEQUENCE TO :batch; 
