# Creates everything for the CILogon database. pipe it into mysql.



# First commands clean out the database and users. Use what you need of these.
# DROP DATABASE oauth;
# REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'cilogon'@'localhost';
# DROP USER 'cilogon'@'localhost';

# NOTE: The database must be made with utf8 and this in turn will strongly limit your
# primary key size to 256 characters (which are internally represented by 4 bytes each).
# Failure to do so will make it impossible to store international user information!
#
# HOW TO USE
#
# (1) Brand spanking new setup. Get this from svn, copy it someplace and replace the
# password in the create user command with the one you want.
#
# (2) If the user already exists, log in to mysql as root and (if needed) drop then recreate everything.
#
# Backup, backup, backup first.


CREATE USER 'cilogon'@'localhost'
  IDENTIFIED BY 'PUT PASSWORD HERE';

CREATE DATABASE ciloa2
DEFAULT CHARACTER SET utf8;
USE ciloa2;

# Set permissions -- must exist before the databases are created.
GRANT ALL PRIVILEGES ON ciloa2.clients TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.transactions TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.client_approvals TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.user TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.old_user TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.identity_provider TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.uid_seq TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.two_factor TO 'cilogon'@'localhost'
WITH GRANT OPTION;


COMMIT;

CREATE TABLE ciloa2.clients (
  client_id     VARCHAR(255) PRIMARY KEY,
  public_key    TEXT,
  name          TEXT,
  home_url      TEXT,
  error_url     TEXT,
  email         TEXT,
  proxy_limited BOOLEAN,
  creation_ts   TIMESTAMP,
  rt_lifetime bigint,
  callback_uri  TEXT
);

CREATE TABLE ciloa2.client_approvals (
  client_id   VARCHAR(255) PRIMARY KEY,
  approver    TEXT,
  approved    BOOLEAN,
  approval_ts TIMESTAMP
);

CREATE TABLE ciloa2.transactions (
  temp_token          VARCHAR(255) PRIMARY KEY,
  temp_token_valid    BOOLEAN,
  callback_uri        TEXT,
  certreq             TEXT,
  certlifetime        BIGINT,
  client_id           TEXT,
  verifier_token      TEXT,
  access_token        TEXT,
  refresh_token       TEXT,
  refresh_token_valid BOOLEAN,
  expires_in          BIGINT,
  certificate         TEXT,
  username            TEXT,
  myproxyUsername     TEXT,
  UNIQUE INDEX verifier (verifier_token(255)),
  UNIQUE INDEX accessToken (access_token(255)),
  UNIQUE INDEX refreshToken (refresh_token(255)),
  UNIQUE INDEX username (username (255))
);

COMMIT;

# NOTE: If you are using the CILogon user tables from another source, you do not need to create them.
# In that case, you need nothing from here on in this file.

CREATE TABLE ciloa2.user (
  user_uid         VARCHAR(255) PRIMARY KEY,
  first_name       TEXT,
  last_name        TEXT,
  idp              TEXT,
  idp_display_name TEXT,
  remote_user      TEXT,
  email            TEXT,
  serial_string    TEXT,
  eppn             TEXT,
  eptid            TEXT,
  open_id          TEXT,
  oidc             TEXT,
  create_time      TIMESTAMP,
  INDEX eppn (eppn(255)),
  INDEX eptid (eptid(255)),
  INDEX oidc (oidc(255)),
  INDEX open_id (open_id(255))
);

CREATE TABLE ciloa2.old_user (
  archived_user_id VARCHAR(255) PRIMARY KEY,
  archive_time     TIMESTAMP,
  user_uid         TEXT NOT NULL,
  first_name       TEXT,
  last_name        TEXT,
  idp              TEXT,
  idp_display_name TEXT,
  remote_user      TEXT,
  email            TEXT,
  serial_string    TEXT,
  eppn             TEXT,
  eptid            TEXT,
  open_id          TEXT,
  oidc             TEXT,
  create_time      TIMESTAMP
);

CREATE TABLE ciloa2.identity_provider (
  idp_uid VARCHAR(255) PRIMARY KEY
);


CREATE TABLE ciloa2.two_factor (
  user_uid   VARCHAR(255) PRIMARY KEY,
  two_factor TEXT
);

CREATE TABLE ciloa2.uid_seq (
  nextval INT PRIMARY KEY AUTO_INCREMENT,
  dummy   TINYINT
)
  AUTO_INCREMENT =42;
COMMIT;
