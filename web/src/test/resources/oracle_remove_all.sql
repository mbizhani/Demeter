BEGIN
	--Bye Sequences!
	FOR i IN (SELECT us.sequence_name
						FROM USER_SEQUENCES us) LOOP
		EXECUTE IMMEDIATE 'drop sequence ' || i.sequence_name;
	END LOOP;

	--Bye Tables!
	FOR i IN (SELECT ut.table_name
						FROM USER_TABLES UT) LOOP
		EXECUTE IMMEDIATE 'drop table ' || i.table_name || ' cascade constraints purge';
	END LOOP;

END;