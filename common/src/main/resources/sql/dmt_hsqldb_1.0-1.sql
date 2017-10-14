------------------
-- ALTER PREVIOUS
------------------
DROP TABLE a_mt_dmt_prvlg_user;
DROP TABLE mt_dmt_prvlg_user;

ALTER TABLE t_dmt_d_page_inst ALTER COLUMN f_page_info SET NOT NULL;

-----------------------
-- CREATE AUDIT TABLES
-----------------------

CREATE TABLE a_mt_dmt_prvlg_user_deny (
	r_num   INTEGER NOT NULL,
	f_user  BIGINT  NOT NULL,
	f_prvlg BIGINT  NOT NULL,
	r_type  TINYINT,
	PRIMARY KEY (r_num, f_user, f_prvlg)
);

CREATE TABLE a_mt_dmt_prvlg_user_perm (
	r_num   INTEGER NOT NULL,
	f_user  BIGINT  NOT NULL,
	f_prvlg BIGINT  NOT NULL,
	r_type  TINYINT,
	PRIMARY KEY (r_num, f_user, f_prvlg)
);

------------------------
-- CREATE MIDDLE TABLES
------------------------

CREATE TABLE mt_dmt_prvlg_user_deny (
	f_user  BIGINT NOT NULL,
	f_prvlg BIGINT NOT NULL
);

CREATE TABLE mt_dmt_prvlg_user_perm (
	f_user  BIGINT NOT NULL,
	f_prvlg BIGINT NOT NULL
);

----------------------------------
-- CREATE REFERENTIAL CONSTRAINTS
----------------------------------

ALTER TABLE a_mt_dmt_prvlg_user_deny
ADD CONSTRAINT FK_3knmjfe81yklpk0k7i6vw29j7
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_dmt_prvlg_user_perm
ADD CONSTRAINT FK_kyqy7cjc782ctdko74hshhexc
FOREIGN KEY (r_num)
REFERENCES REVINFO;

-----------------------------
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