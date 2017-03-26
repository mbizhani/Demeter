CREATE TABLE REVINFO (
  REV      INTEGER,
  REVTSTMP BIGINT,
  PRIMARY KEY (REV)
);

CREATE TABLE a_mt_dmt_pageinst_role (
  r_num       INTEGER NOT NULL,
  f_page_inst BIGINT  NOT NULL,
  f_role      BIGINT  NOT NULL,
  r_type      TINYINT,
  PRIMARY KEY (r_num, f_page_inst, f_role)
);

CREATE TABLE a_mt_dmt_user_role (
  r_num  INTEGER NOT NULL,
  f_user BIGINT  NOT NULL,
  f_role BIGINT  NOT NULL,
  r_type TINYINT,
  PRIMARY KEY (r_num, f_user, f_role)
);

CREATE TABLE a_t_dmt_d_page_inst (
  id              BIGINT  NOT NULL,
  r_num           INTEGER NOT NULL,
  r_type          TINYINT,
  b_in_menu       BOOLEAN,
  f_modifier_user BIGINT,
  c_title         VARCHAR(255),
  c_uri           VARCHAR(255),
  PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_dmt_person (
  id              BIGINT  NOT NULL,
  r_num           INTEGER NOT NULL,
  r_type          TINYINT,
  d_birth_reg     DATE,
  c_email         VARCHAR(255),
  c_first_name    VARCHAR(255),
  b_has_user      BOOLEAN,
  c_last_name     VARCHAR(255),
  c_mobile        VARCHAR(255),
  f_modifier_user BIGINT,
  e_mod           INTEGER,
  c_sys_number    VARCHAR(255),
  PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_dmt_role (
  id              BIGINT  NOT NULL,
  r_num           INTEGER NOT NULL,
  r_type          TINYINT,
  d_modification  DATE,
  f_modifier_user BIGINT,
  c_name          VARCHAR(255),
  e_mod           INTEGER,
  PRIMARY KEY (id, r_num)
);

CREATE TABLE a_t_dmt_user (
  id                  BIGINT  NOT NULL,
  r_num               INTEGER NOT NULL,
  r_type              TINYINT,
  b_admin             BOOLEAN,
  e_auth_mech         INTEGER,
  e_cal_type          INTEGER,
  e_date_pattern      INTEGER,
  e_date_time_pattern INTEGER,
  e_laydir_type       INTEGER,
  e_locale            INTEGER,
  n_session_timeout   INTEGER,
  e_status            INTEGER,
  c_username          VARCHAR(255),
  PRIMARY KEY (id, r_num)
);

CREATE TABLE mt_dmt_pageinst_role (
  f_page_inst BIGINT NOT NULL,
  f_role      BIGINT NOT NULL
);

CREATE TABLE mt_dmt_user_role (
  f_user BIGINT NOT NULL,
  f_role BIGINT NOT NULL
);

CREATE TABLE t_dmt_d_page (
  id             BIGINT       NOT NULL,
  c_base_uri     VARCHAR(255) NOT NULL,
  d_creation     DATE         NOT NULL,
  b_enabled      BOOLEAN      NOT NULL,
  d_modification DATE,
  c_module       VARCHAR(255) NOT NULL,
  c_type         VARCHAR(255) NOT NULL,
  c_type_alt     VARCHAR(255),
  n_version      INTEGER      NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE t_dmt_d_page_inst (
  id              BIGINT       NOT NULL,
  d_creation      DATE         NOT NULL,
  f_creator_user  BIGINT,
  b_in_menu       BOOLEAN      NOT NULL,
  d_modification  DATE,
  f_modifier_user BIGINT,
  c_ref_id        VARCHAR(255),
  c_title         VARCHAR(255) NOT NULL,
  c_uri           VARCHAR(255) NOT NULL,
  n_version       INTEGER      NOT NULL,
  f_page_info     BIGINT,
  PRIMARY KEY (id)
);

CREATE TABLE t_dmt_d_task (
  id              BIGINT       NOT NULL,
  d_creation      DATE         NOT NULL,
  f_creator_user  BIGINT,
  b_enabled       BOOLEAN      NOT NULL,
  d_modification  DATE,
  f_modifier_user BIGINT,
  c_module        VARCHAR(255) NOT NULL,
  c_type          VARCHAR(255) NOT NULL,
  n_version       INTEGER      NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE t_dmt_d_task_log (
  id      BIGINT       NOT NULL,
  d_end   DATE,
  c_key   VARCHAR(255) NOT NULL,
  d_start DATE         NOT NULL,
  c_state INTEGER      NOT NULL,
  f_task  BIGINT,
  PRIMARY KEY (id)
);

CREATE TABLE t_dmt_d_task_schd (
  id              BIGINT       NOT NULL,
  d_creation      DATE         NOT NULL,
  f_creator_user  BIGINT,
  c_cron_expr     VARCHAR(255) NOT NULL,
  b_enabled       BOOLEAN      NOT NULL,
  d_modification  DATE,
  f_modifier_user BIGINT,
  c_ref_id        VARCHAR(255),
  n_version       INTEGER      NOT NULL,
  f_task          BIGINT,
  PRIMARY KEY (id)
);

CREATE TABLE t_dmt_file_store (
  id              BIGINT       NOT NULL,
  d_creation      DATE         NOT NULL,
  f_creator_user  BIGINT,
  d_expiration    DATE,
  c_file_id       VARCHAR(255) NOT NULL,
  e_mime_type     INTEGER      NOT NULL,
  d_modification  DATE,
  f_modifier_user BIGINT,
  c_name          VARCHAR(255) NOT NULL,
  e_status        INTEGER      NOT NULL,
  e_storage       INTEGER      NOT NULL,
  c_tag           VARCHAR(255),
  n_version       INTEGER      NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE t_dmt_person (
  id              BIGINT  NOT NULL,
  d_birth_reg     DATE,
  d_creation      DATE    NOT NULL,
  f_creator_user  BIGINT,
  c_email         VARCHAR(255),
  c_first_name    VARCHAR(255),
  b_has_user      BOOLEAN NOT NULL,
  c_last_name     VARCHAR(255),
  c_mobile        VARCHAR(255),
  d_modification  DATE,
  f_modifier_user BIGINT,
  c_sys_number    VARCHAR(255),
  n_version       INTEGER NOT NULL,
  e_mod           INTEGER NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE t_dmt_role (
  id              BIGINT       NOT NULL,
  d_creation      DATE         NOT NULL,
  f_creator_user  BIGINT,
  d_modification  DATE,
  f_modifier_user BIGINT,
  c_name          VARCHAR(255) NOT NULL,
  e_mod           INTEGER      NOT NULL,
  n_version       INTEGER      NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE t_dmt_user (
  id                  BIGINT       NOT NULL,
  b_admin             BOOLEAN      NOT NULL,
  e_auth_mech         INTEGER      NOT NULL,
  e_cal_type          INTEGER,
  e_date_pattern      INTEGER,
  e_date_time_pattern INTEGER,
  d_last_login        DATE,
  e_laydir_type       INTEGER,
  e_locale            INTEGER      NOT NULL,
  c_password          VARCHAR(255),
  n_session_timeout   INTEGER      NOT NULL,
  e_status            INTEGER      NOT NULL,
  c_username          VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

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

ALTER TABLE t_dmt_role
ADD CONSTRAINT uk_dmt_role_rolename UNIQUE (c_name);

ALTER TABLE t_dmt_user
ADD CONSTRAINT uk_dmt_user_username UNIQUE (c_username);

ALTER TABLE a_mt_dmt_pageinst_role
ADD CONSTRAINT FK_tlf8x1usn0dug9sf5jaq077sr
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

ALTER TABLE mt_dmt_pageinst_role
ADD CONSTRAINT pageinstrole2role
FOREIGN KEY (f_role)
REFERENCES t_dmt_role;

ALTER TABLE mt_dmt_pageinst_role
ADD CONSTRAINT pageinstrole2user
FOREIGN KEY (f_page_inst)
REFERENCES t_dmt_d_page_inst;

ALTER TABLE mt_dmt_user_role
ADD CONSTRAINT userrole2role
FOREIGN KEY (f_role)
REFERENCES t_dmt_role;

ALTER TABLE mt_dmt_user_role
ADD CONSTRAINT userrole2user
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

CREATE SEQUENCE dmt_d_page
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE dmt_d_task
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE dmt_d_task_log
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE dmt_d_task_schd
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE dmt_file_store
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE dmt_person
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE dmt_role
    START WITH 1
    INCREMENT BY 1;

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1;
