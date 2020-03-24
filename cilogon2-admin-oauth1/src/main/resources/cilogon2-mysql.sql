# Creates everything for the CILgon database. pipe it into mysql.

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

CREATE DATABASE oauth
  DEFAULT CHARACTER SET utf8;
USE oauth;

# Set permissions -- must exist before the databases are created.
GRANT ALL PRIVILEGES ON oauth.clients TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON oauth.transactions TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON oauth.client_approvals TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON oauth.user TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON oauth.old_user TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON oauth.identity_provider TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON oauth.uid_seq TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON oauth.two_factor TO 'cilogon'@'localhost'
WITH GRANT OPTION;


COMMIT;

CREATE TABLE oauth.clients (
  oauth_consumer_key  VARCHAR(255) PRIMARY KEY,
  oauth_client_pubkey TEXT,
  name                TEXT,
  home_url            TEXT,
  error_url           TEXT,
  email               TEXT,
  proxy_limited       BOOLEAN,
  creation_ts         TIMESTAMP
);



CREATE TABLE oauth.client_approvals (
  oauth_consumer_key VARCHAR(255),
  approver           TEXT,
  approved           BOOLEAN,
  approval_ts        TIMESTAMP
);

CREATE TABLE oauth.transactions (
  temp_token         VARCHAR(255) PRIMARY KEY,
  temp_token_ss      TEXT,
  temp_token_valid   BOOLEAN,
  oauth_callback     TEXT,
  certreq            TEXT,
  certlifetime       BIGINT,
  oauth_consumer_key TEXT,
  oauth_verifier     TEXT,
  access_token       TEXT,
  access_token_ss    TEXT,
  access_token_valid BOOLEAN,
  certificate        TEXT,
  username           TEXT,
  cilogon_info       TEXT,
  complete           BOOLEAN,
  loa                TEXT,
  INDEX verifier (oauth_verifier(255)),
  INDEX accessToken (access_token(255))
);

CREATE TABLE user (
  user_uid         VARCHAR(255) PRIMARY KEY,
  first_name       TEXT,
  last_name        TEXT,
  idp              TEXT,
  idp_display_name TEXT,
  remote_user      TEXT,
  email            TEXT,
  serial_string    TEXT,
  subject_id       TEXT,
  pairwise_id      TEXT,
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

CREATE TABLE oauth.old_user (
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

CREATE TABLE oauth.identity_provider (
  idp_uid VARCHAR(255) PRIMARY KEY
);


CREATE TABLE oauth.two_factor (
  user_uid   VARCHAR(255) PRIMARY KEY,
  two_factor TEXT
);

CREATE TABLE oauth.uid_seq (
  nextval INT PRIMARY KEY AUTO_INCREMENT,
  dummy   TINYINT
)
  AUTO_INCREMENT =42;
COMMIT;
