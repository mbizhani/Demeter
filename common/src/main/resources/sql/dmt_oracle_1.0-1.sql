----------------
-- ALTER TABLES
----------------

DROP TABLE a_mt_dmt_prvlg_user;
DROP TABLE mt_dmt_prvlg_user;

ALTER TABLE t_dmt_d_page_inst
	MODIFY f_page_info NUMBER(19, 0) NOT NULL;

-----------------------
-- CREATE AUDIT TABLES
-----------------------

CREATE TABLE a_mt_dmt_prvlg_user_deny (
	r_num   NUMBER(10, 0) NOT NULL,
	f_user  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL,
	r_type  NUMBER(3, 0),
	PRIMARY KEY (r_num, f_user, f_prvlg)
);

CREATE TABLE a_mt_dmt_prvlg_user_perm (
	r_num   NUMBER(10, 0) NOT NULL,
	f_user  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL,
	r_type  NUMBER(3, 0),
	PRIMARY KEY (r_num, f_user, f_prvlg)
);

------------------------
-- CREATE MIDDLE TABLES
------------------------

CREATE TABLE mt_dmt_prvlg_user_deny (
	f_user  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_dmt_prvlg_user_perm (
	f_user  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL
);

----------------------------------
-- CREATE REFERENTIAL CONSTRAINTS
----------------------------------

ALTER TABLE a_mt_dmt_prvlg_user_deny
	ADD CONSTRAINT FKku4j3tk30eowve9q7nf406aln
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_dmt_prvlg_user_perm
	ADD CONSTRAINT FK20l0ho8iplbwhaxmgtclo18ck
FOREIGN KEY (r_num)
REFERENCES REVINFO;

-----------------

ALTER TABLE mt_dmt_prvlg_user_deny
	ADD CONSTRAINT prvlgUserDeny2prvlg
FOREIGN KEY (f_prvlg)
REFERENCES t_dmt_privilege;

ALTER TABLE mt_dmt_prvlg_user_deny
	ADD CONSTRAINT prvlgUserDeny2user
FOREIGN KEY (f_user)
REFERENCES t_dmt_user;

ALTER TABLE mt_dmt_prvlg_user_perm
	ADD CONSTRAINT prvlgUserPerm2prvlg
FOREIGN KEY (f_prvlg)
REFERENCES t_dmt_privilege;

ALTER TABLE mt_dmt_prvlg_user_perm
	ADD CONSTRAINT prvlgUserPerm2user
FOREIGN KEY (f_user)
REFERENCES t_dmt_user;