ALTER TABLE a_t_dmt_role
DROP COLUMN b_dynamic;

ALTER TABLE a_t_dmt_role
ADD e_role_mode NUMBER(10, 0);

-- ---------------

ALTER TABLE t_dmt_role
DROP COLUMN b_dynamic;

ALTER TABLE t_dmt_role
ADD e_role_mode NUMBER(10, 0);

UPDATE t_dmt_role
SET e_role_mode = 1;

ALTER TABLE t_dmt_role
MODIFY e_role_mode NUMBER(10, 0) NOT NULL;

-- ---------------

ALTER TABLE mt_dmt_pageinst_role
ADD CONSTRAINT uk_dmt_mtPageInstRole UNIQUE (f_page_inst, f_role);

ALTER TABLE mt_dmt_prvlg_role_deny
ADD CONSTRAINT uk_dmt_mtPrvlgRoleDeny UNIQUE (f_role, f_prvlg);

ALTER TABLE mt_dmt_prvlg_role_perm
ADD CONSTRAINT uk_dmt_mtPrvlgRolePerm UNIQUE (f_role, f_prvlg);

ALTER TABLE mt_dmt_prvlg_user_deny
ADD CONSTRAINT uk_dmt_mtPrvlgUserDeny UNIQUE (f_user, f_prvlg);

ALTER TABLE mt_dmt_prvlg_user_perm
ADD CONSTRAINT uk_dmt_mtPrvlgUserPerm UNIQUE (f_user, f_prvlg);

ALTER TABLE mt_dmt_user_role
ADD CONSTRAINT uk_dmt_mtUserRole UNIQUE (f_user, f_role);