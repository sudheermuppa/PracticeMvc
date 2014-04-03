delete from cx_fint_file where created > '10-Jun-2012';
delete from cx_fint_txn where created > '10-Jun-2012';
delete from eim_loy_txn;
delete from s_loy_txn where created > '10-Jun-2012';
commit;
quit
/
