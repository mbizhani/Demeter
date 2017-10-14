-----------------------
-- CREATE AUDIT TABLES
-----------------------

CREATE TABLE REVINFO (
	REV      NUMBER(10, 0) NOT NULL,
	REVTSTMP NUMBER(19, 0),
	PRIMARY KEY (REV)
);

CREATE TABLE a_mt_dmt_pageinst_role (
	r_num       NUMBER(10, 0) NOT NULL,
	f_page_inst NUMBER(19, 0) NOT NULL,
	f_role      NUMBER(19, 0) NOT NULL,
	r_type      NUMBER(3, 0),
	PRIMARY KEY (r_num, f_page_inst, f_role)
);

CREATE TABLE a_mt_dmt_prvlg_role_deny (
	r_num   NUMBER(10, 0) NOT NULL,
	f_role  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL,
	r_type  NUMBER(3, 0),
	PRIMARY KEY (r_num, f_role, f_prvlg)
);

CREATE TABLE a_mt_dmt_prvlg_role_perm (
	r_num   NUMBER(10, 0) NOT NULL,
	f_role  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL,
	r_type  NUMBER(3, 0),
	PRIMARY KEY (r_num, f_role, f_prvlg)
);

CREATE TABLE a_mt_dmt_prvlg_user (
	r_num   NUMBER(10, 0) NOT NULL,
	f_user  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL,
	r_type  NUMBER(3, 0),
	PRIMARY KEY (r_num, f_user, f_prvlg)
);

CREATE TABLE a_mt_dmt_user_role (
	r_num  NUMBER(10, 0) NOT NULL,
	f_user NUMBER(19, 0) NOT NULL,
	f_role NUMBER(19, 0) NOT NULL,
	r_type NUMBER(3, 0),
	PRIMARY KEY (r_num, f_user, f_role)
);

CREATE TABLE a_t_dmt_d_page_inst (
	id              NUMBER(19, 0) NOT NULL,
	r_num           NUMBER(10, 0) NOT NULL,
	r_type          NUMBER(3, 0),
	b_in_menu       NUMBER(1, 0),
	f_modifier_user NUMBER(19, 0),
	c_title         VARCHAR2(255 CHAR),
	c_uri           VARCHAR2(255 CHAR),
	PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_dmt_person (
	id              NUMBER(19, 0) NOT NULL,
	r_num           NUMBER(10, 0) NOT NULL,
	r_type          NUMBER(3, 0),
	d_birth_reg     DATE,
	c_email         VARCHAR2(255 CHAR),
	c_first_name    VARCHAR2(255 CHAR),
	b_has_user      NUMBER(1, 0),
	c_last_name     VARCHAR2(255 CHAR),
	c_mobile        VARCHAR2(255 CHAR),
	f_modifier_user NUMBER(19, 0),
	e_mod           NUMBER(10, 0),
	c_sys_number    VARCHAR2(255 CHAR),
	PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_dmt_role (
	id              NUMBER(19, 0) NOT NULL,
	r_num           NUMBER(10, 0) NOT NULL,
	r_type          NUMBER(3, 0),
	b_dynamic       NUMBER(1, 0),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR),
	e_mod           NUMBER(10, 0),
	PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_dmt_user (
	id                  NUMBER(19, 0) NOT NULL,
	r_num               NUMBER(10, 0) NOT NULL,
	r_type              NUMBER(3, 0),
	b_admin             NUMBER(1, 0),
	e_auth_mech         NUMBER(10, 0),
	e_cal_type          NUMBER(10, 0),
	e_date_pattern      NUMBER(10, 0),
	e_date_time_pattern NUMBER(10, 0),
	e_laydir_type       NUMBER(10, 0),
	e_locale            NUMBER(10, 0),
	n_session_timeout   NUMBER(10, 0),
	e_status            NUMBER(10, 0),
	c_username          VARCHAR2(255 CHAR),
	PRIMARY KEY (id, r_num)
);

------------------------
-- CREATE MIDDLE TABLES
------------------------

CREATE TABLE mt_dmt_pageinst_role (
	f_page_inst NUMBER(19, 0) NOT NULL,
	f_role      NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_dmt_prvlg_role_deny (
	f_role  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_dmt_prvlg_role_perm (
	f_role  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_dmt_prvlg_user (
	f_user  NUMBER(19, 0) NOT NULL,
	f_prvlg NUMBER(19, 0) NOT NULL
);

CREATE TABLE mt_dmt_user_role (
	f_user NUMBER(19, 0) NOT NULL,
	f_role NUMBER(19, 0) NOT NULL
);

----------------------
-- CREATE MAIN TABLES
----------------------

CREATE TABLE t_dmt_d_page (
	id             NUMBER(19, 0)      NOT NULL,
	c_base_uri     VARCHAR2(255 CHAR) NOT NULL,
	d_creation     DATE               NOT NULL,
	b_enabled      NUMBER(1, 0)       NOT NULL,
	d_modification DATE,
	c_module       VARCHAR2(255 CHAR) NOT NULL,
	c_type         VARCHAR2(255 CHAR) NOT NULL,
	c_type_alt     VARCHAR2(255 CHAR),
	n_version      NUMBER(10, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_dmt_d_page_inst (
	id              NUMBER(19, 0)      NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	b_in_menu       NUMBER(1, 0)       NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_ref_id        VARCHAR2(255 CHAR),
	c_title         VARCHAR2(255 CHAR) NOT NULL,
	c_uri           VARCHAR2(255 CHAR) NOT NULL,
	n_version       NUMBER(10, 0)      NOT NULL,
	f_page_info     NUMBER(19, 0),
	PRIMARY KEY (id)
);

CREATE TABLE t_dmt_d_task (
	id              NUMBER(19, 0)      NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	b_enabled       NUMBER(1, 0)       NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_module        VARCHAR2(255 CHAR) NOT NULL,
	c_type          VARCHAR2(255 CHAR) NOT NULL,
	n_version       NUMBER(10, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_dmt_d_task_log (
	id      NUMBER(19, 0)      NOT NULL,
	d_end   DATE,
	c_key   VARCHAR2(255 CHAR) NOT NULL,
	d_start DATE               NOT NULL,
	c_state NUMBER(10, 0)      NOT NULL,
	f_task  NUMBER(19, 0),
	PRIMARY KEY (id)
);

CREATE TABLE t_dmt_d_task_schd (
	id              NUMBER(19, 0)      NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	c_cron_expr     VARCHAR2(255 CHAR) NOT NULL,
	b_enabled       NUMBER(1, 0)       NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_ref_id        VARCHAR2(255 CHAR),
	n_version       NUMBER(10, 0)      NOT NULL,
	f_task          NUMBER(19, 0),
	PRIMARY KEY (id)
);

CREATE TABLE t_dmt_file_store (
	id              NUMBER(19, 0)      NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	d_expiration    DATE,
	c_file_id       VARCHAR2(255 CHAR) NOT NULL,
	e_mime_type     NUMBER(10, 0)      NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR) NOT NULL,
	e_status        NUMBER(10, 0)      NOT NULL,
	e_storage       NUMBER(10, 0)      NOT NULL,
	c_tag           VARCHAR2(255 CHAR),
	n_version       NUMBER(10, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_dmt_person (
	id              NUMBER(19, 0) NOT NULL,
	d_birth_reg     DATE,
	d_creation      DATE          NOT NULL,
	f_creator_user  NUMBER(19, 0),
	c_email         VARCHAR2(255 CHAR),
	c_first_name    VARCHAR2(255 CHAR),
	b_has_user      NUMBER(1, 0)  NOT NULL,
	c_last_name     VARCHAR2(255 CHAR),
	c_mobile        VARCHAR2(255 CHAR),
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	e_mod           NUMBER(10, 0) NOT NULL,
	c_sys_number    VARCHAR2(255 CHAR),
	n_version       NUMBER(10, 0) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_dmt_privilege (
	id         NUMBER(19, 0)      NOT NULL,
	d_creation DATE               NOT NULL,
	c_name     VARCHAR2(255 CHAR) NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_dmt_role (
	id              NUMBER(19, 0)      NOT NULL,
	d_creation      DATE               NOT NULL,
	f_creator_user  NUMBER(19, 0)      NOT NULL,
	b_dynamic       NUMBER(1, 0)       NOT NULL,
	d_modification  DATE,
	f_modifier_user NUMBER(19, 0),
	c_name          VARCHAR2(255 CHAR) NOT NULL,
	e_mod           NUMBER(10, 0)      NOT NULL,
	n_version       NUMBER(10, 0)      NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE t_dmt_user (
	id                  NUMBER(19, 0)      NOT NULL,
	b_admin             NUMBER(1, 0)       NOT NULL,
	e_auth_mech         NUMBER(10, 0)      NOT NULL,
	e_cal_type          NUMBER(10, 0),
	e_date_pattern      NUMBER(10, 0),
	e_date_time_pattern NUMBER(10, 0),
	d_last_login        DATE,
	e_laydir_type       NUMBER(10, 0),
	e_locale            NUMBER(10, 0),
	c_password          VARCHAR2(255 CHAR),
	n_session_timeout   NUMBER(10, 0),
	e_status            NUMBER(10, 0)      NOT NULL,
	c_username          VARCHAR2(255 CHAR) NOT NULL,
	PRIMARY KEY (id)
);

-----------------------------
-- CREATE UNIQUE CONSTRAINTS
-----------------------------

ALTER TABLE t_dmt_d_page
ADD CONSTRAINT uk_dmt_page_type UNIQUE (c_type);

ALTER TABLE t_dmt_d_page
ADD CONSTRAINT uk_dmt_page_baseuri UNIQUE (c_base_uri);

ALTER TABLE t_dmt_d_page_inst
ADD CONSTRAINT uk_dmt_pageinst_uri UNIQUE (c_uri);

ALTER TABLE t_dmt_d_task
ADD CONSTRAINT uk_dmt_task_type UNIQUE (c_type);

ALTER TABLE t_dmt_file_store
ADD CONSTRAINT uk_dmt_filestore_fileid UNIQUE (c_file_id);

ALTER TABLE t_dmt_privilege
ADD CONSTRAINT uk_dmt_privilege_name UNIQUE (c_name);

ALTER TABLE t_dmt_role
ADD CONSTRAINT uk_dmt_role_name UNIQUE (c_name);

ALTER TABLE t_dmt_user
ADD CONSTRAINT uk_dmt_user_username UNIQUE (c_username);

----------------------------------
-- CREATE REFERENTIAL CONSTRAINTS
----------------------------------

ALTER TABLE a_mt_dmt_pageinst_role
ADD CONSTRAINT FK_tlf8x1usn0dug9sf5jaq077sr
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_dmt_prvlg_role_deny
ADD CONSTRAINT FK_mqwhogdldx9juleqmc9c185xr
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_dmt_prvlg_role_perm
ADD CONSTRAINT FK_s7m14y23jw3ohc1orsge2ahiu
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_dmt_prvlg_user
ADD CONSTRAINT FK_ksrx5xc36yt315hn996r28tf3
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_mt_dmt_user_role
ADD CONSTRAINT FK_mqjoupr478iv6jchf7be9w2kf
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_dmt_d_page_inst
ADD CONSTRAINT FK_sarg1peou92ejerqwwav35ej1
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_dmt_person
ADD CONSTRAINT FK_g9l38x2ycntsvmn84wl6aqsa9
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_dmt_role
ADD CONSTRAINT FK_l9yemywu9lnbr39qyvstwnget
FOREIGN KEY (r_num)
REFERENCES REVINFO;

ALTER TABLE a_t_dmt_user
ADD CONSTRAINT FK_hkcebwdtmgqd01r4qpw95ebxe
FOREIGN KEY (r_num)
REFERENCES REVINFO;

------------------------------

ALTER TABLE mt_dmt_pageinst_role
ADD CONSTRAINT pageInstRole2role
FOREIGN KEY (f_role)
REFERENCES t_dmt_role;

ALTER TABLE mt_dmt_pageinst_role
ADD CONSTRAINT pageInstRole2pageInst
FOREIGN KEY (f_page_inst)
REFERENCES t_dmt_d_page_inst;

ALTER TABLE mt_dmt_prvlg_role_deny
ADD CONSTRAINT prvlgRoleDeny2prvlg
FOREIGN KEY (f_prvlg)
REFERENCES t_dmt_privilege;

ALTER TABLE mt_dmt_prvlg_role_deny
ADD CONSTRAINT prvlgRoleDeny2role
FOREIGN KEY (f_role)
REFERENCES t_dmt_role;

ALTER TABLE mt_dmt_prvlg_role_perm
ADD CONSTRAINT prvlgRolePerm2prvlg
FOREIGN KEY (f_prvlg)
REFERENCES t_dmt_privilege;

ALTER TABLE mt_dmt_prvlg_role_perm
ADD CONSTRAINT prvlgRolePerm2role
FOREIGN KEY (f_role)
REFERENCES t_dmt_role;

ALTER TABLE mt_dmt_prvlg_user
ADD CONSTRAINT prvlgUser2prvlg
FOREIGN KEY (f_prvlg)
REFERENCES t_dmt_privilege;

ALTER TABLE mt_dmt_prvlg_user
ADD CONSTRAINT prvlgUser2user
FOREIGN KEY (f_user)
REFERENCES t_dmt_user;

ALTER TABLE mt_dmt_user_role
ADD CONSTRAINT userRole2role
FOREIGN KEY (f_role)
REFERENCES t_dmt_role;

ALTER TABLE mt_dmt_user_role
ADD CONSTRAINT userRole2user
FOREIGN KEY (f_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_d_page_inst
ADD CONSTRAINT pageinst_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_d_page_inst
ADD CONSTRAINT pageinst_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_d_page_inst
ADD CONSTRAINT pageinst2pageinfo
FOREIGN KEY (f_page_info)
REFERENCES t_dmt_d_page;

ALTER TABLE t_dmt_d_task
ADD CONSTRAINT task_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_d_task
ADD CONSTRAINT task_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_d_task_log
ADD CONSTRAINT tasklog2task
FOREIGN KEY (f_task)
REFERENCES t_dmt_d_task;

ALTER TABLE t_dmt_d_task_schd
ADD CONSTRAINT taskschd_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_d_task_schd
ADD CONSTRAINT taskschd_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_d_task_schd
ADD CONSTRAINT taskschd2task
FOREIGN KEY (f_task)
REFERENCES t_dmt_d_task;

ALTER TABLE t_dmt_file_store
ADD CONSTRAINT filestore_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_file_store
ADD CONSTRAINT filestore_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_person
ADD CONSTRAINT prsn_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_person
ADD CONSTRAINT prsn_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_role
ADD CONSTRAINT role_crtrusr2user
FOREIGN KEY (f_creator_user)
REFERENCES t_dmt_user;

ALTER TABLE t_dmt_role
ADD CONSTRAINT role_mdfrusr2user
FOREIGN KEY (f_modifier_user)
REFERENCES t_dmt_user;

--------------------
-- CREATE SEQUENCES
--------------------

CREATE SEQUENCE dmt_d_page START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE dmt_d_task START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE dmt_d_task_log START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE dmt_d_task_schd START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE dmt_file_store START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE dmt_person START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE dmt_privilege START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE dmt_role START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE hibernate_sequence START WITH 1 INCREMENT BY 1;

---------------
-- CREATE MISC
---------------

CREATE TABLE z_dmt_sql_apply (
	c_module  VARCHAR2(10 CHAR) NOT NULL,
	c_version VARCHAR2(10 CHAR) NOT NULL,
	c_file    VARCHAR2(255 CHAR) NOT NULL,
	d_apply   DATE NOT NULL,

	PRIMARY KEY (c_version, c_module)
);
