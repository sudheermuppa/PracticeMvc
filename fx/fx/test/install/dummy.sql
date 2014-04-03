drop user por cascade;
drop user bpor cascade;
create user por identified by por;
create user bpor identified by bpor;
grant dba to por, bpor;
connect por/por
@dummy_EIM_LOY_TXN
@dummy_EIM_LOY_MEMBER
@dummy_CX_FINT_ERROR
quit
/
