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


CREATE USER 'cilogon'@'localhost' IDENTIFIED BY 'PUT PASSWORD HERE';

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
GRANT ALL PRIVILEGES ON ciloa2.adminClients TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.permissions TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.user TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.old_user TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.identity_provider TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.uid_seq TO 'cilogon'@'localhost'
WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.two_factor TO 'cilogon'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.tx_records TO 'cilogon'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON ciloa2.virtual_organizations TO 'cilogon'@'localhost' WITH GRANT OPTION;

COMMIT;

CREATE TABLE ciloa2.clients (
  at_lifetime               bigint DEFAULT NULL,
  at_max_Lifetime           bigint DEFAULT NULL,
  audience                  text,
  callback_uri              TEXT,
  cfg                       TEXT,
  client_id                 VARCHAR(255) PRIMARY KEY,
  client_type               int DEFAULT NULL,
  creation_ts               timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  debug_on                  tinyint(1) DEFAULT NULL,
  description               text,
  df_interval               bigint DEFAULT NULL,
  df_lifetime               bigint DEFAULT NULL,
  email                     TEXT,
  error_url                 TEXT,
  ersatz_client             BOOLEAN DEFAULT NULL,
  ersatz_inherit_id_token   BOOLEAN DEFAULT NULL,
  extended_attributes       text,
  extends_provisioners      BOOLEAN DEFAULT NULL,
  forward_scopes_to_proxy   BOOLEAN DEFAULT NULL,
  home_url                  TEXT,
  idt_lifetime              bigint DEFAULT NULL,
  idt_max_Lifetime          bigint DEFAULT NULL,
  issuer                    TEXT,
  jwks                      text,
  last_accessed             bigint DEFAULT NULL,
  last_modified_ts          timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  ldap                      TEXT,
  name                      TEXT,
  prototypes                text,
  proxy_claims_list         text,
  proxy_claims              text,
  proxy_request_scopes      text,
  proxy_limited             BOOLEAN,
  public_client             BOOLEAN,
  public_key                TEXT,
  rfc7523_client_users      text,
  rfc7523_client            BOOLEAN DEFAULT NULL,
  rt_grace_period           bigint DEFAULT NULL,
  rt_max_Lifetime           bigint DEFAULT NULL,
  rt_lifetime               bigint,
  scopes                    TEXT,
  sign_tokens               BOOLEAN,
  skip_server_scripts       BOOLEAN DEFAULT NULL,
  strict_scopes             BOOLEAN DEFAULT NULL
  );



CREATE TABLE ciloa2.client_approvals (
  approval_ts timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  approved    BOOLEAN,
  approver    TEXT,
  client_id   VARCHAR(255) PRIMARY KEY,
  description TEXT,
  status      TEXT,
);


CREATE TABLE ciloa2.adminClients (
  admin_id                  VARCHAR(255) PRIMARY KEY,
  allow_custom_ids          BOOLEAN DEFAULT NULL,
  allow_qdl                 BOOLEAN DEFAULT NULL,
  config                    TEXT,
  creation_ts               timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  debug_on                  BOOLEAN DEFAULT NULL,
  description               TEXT,
  email                     TEXT,
  generate_ids              BOOLEAN DEFAULT NULL,
  id_start                  TEXT,
  issuer                    TEXT,
  jwks                      text,
  last_accessed             bigint DEFAULT NULL,
  last_modified_ts          timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  list_users_other_clients  BOOLEAN DEFAULT NULL,
  list_users                BOOLEAN DEFAULT NULL,
  max_clients               BIGINT,
  name                      TEXT,
  new_client_notify         BOOLEAN DEFAULT NULL,
  secret                    TEXT,
  use_timestamps_in_ids     BOOLEAN DEFAULT NULL,
  vo                        TEXT,
  vo_uri                    TEXT
);

`vo_uri` text,
`vo` text,

CREATE TABLE ciloa2.permissions (
  admin_id       VARCHAR(255),
  can_approve    BOOLEAN,
  can_create     BOOLEAN,
  can_read       BOOLEAN,
  can_remove     BOOLEAN,
  can_substitute BOOLEAN DEFAULT NULL,
  can_write      BOOLEAN,
  client_id      VARCHAR(255),
  creation_ts    TIMESTAMP
  description    text,
  ersatz_id      text,
  permission_id  VARCHAR(255) PRIMARY KEY,
);


CREATE TABLE ciloa2.transactions (
  access_token              TEXT,
  access_token_valid        BOOLEAN DEFAULT NULL,
  affiliation               TEXT,
  at_jwt                    TEXT,
  auth_grant                TEXT,
  authz_grant_lifetime      bigint DEFAULT NULL,
  auth_time                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  callback_uri              TEXT,
  certificate               TEXT,
  certlifetime              BIGINT,
  certreq                   TEXT,
  client_id                 TEXT,
  display_name              TEXT,
  expires_in                BIGINT,
  id_token_identifier       text,
  id_token_lifetime         bigint DEFAULT NULL,
  is_rfc_8628               BOOLEAN DEFAULT NULL,
  loa                       text,
  myproxyUsername           TEXT,
  nonce                     text,
  ou                        TEXT,
  proxy_id                  TEXT,
  refresh_token             TEXT,
  refresh_token_expires_at  bigint DEFAULT NULL,
  refresh_token_lifetime    bigint DEFAULT NULL,
  refresh_token_valid       BOOLEAN,
  req_state                 text,
  rt_jwt                    text,
  scopes                    text,
  states                    TEXT,
  temp_token_valid          BOOLEAN,
  user_code                 text,
  user_uid                  text,
  username                  TEXT,
  verifier_token            TEXT,
  validated_scopes          text,
  temp_token                VARCHAR(255) PRIMARY KEY,
  UNIQUE INDEX verifier (verifier_token(255)),
  UNIQUE INDEX accessToken (access_token(255)),
  UNIQUE INDEX refreshToken (refresh_token(255))
);


create table ciloa2.virtual_organizations
(
    at_issuer       text,
    created         bigint,
    default_key_id  text,
    description     text,
    discovery_path  text,
    issuer          text,
    json_web_keys   text,
    last_accessed   bigint DEFAULT NULL,
    last_modified   bigint,
    resource        text,
    title           text,
    valid           boolean,
    vo_id           VARCHAR(255) PRIMARY KEY,
    UNIQUE INDEX parents (vo_id(255)),
    INDEX discovery_path (discovery_path(255))
);



COMMIT;

# NOTE: If you are using the CILogon user tables from another source, you do not need to create them.
# In that case, you need nothing from here on in this file.

CREATE TABLE ciloa2.user (
  affiliation        TEXT,
  attr_json          TEXT,
  create_time        TIMESTAMP,
  description        TEXT,
  display_name       TEXT,
  email              TEXT,
  eppn               TEXT,
  eptid              TEXT,
  first_name         TEXT,
  idp                TEXT,
  idp_display_name   TEXT,
  last_accessed      BIGINT DEFAULT NULL,
  last_modified_ts   TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  last_name          TEXT,
  loa                TEXT,
  oidc               TEXT,
  open_id            TEXT,
  ou                 TEXT,
  pairwise_id        TEXT,
  remote_user        TEXT,
  serial_string      TEXT,
  state              TEXT,
  subject_id         TEXT,
  us_idp             BOOLEAN,
  user_uid           VARCHAR(255) PRIMARY KEY,
  INDEX eppn (eppn(255)),
  INDEX eptid (eptid(255)),
  INDEX oidc (oidc(255)),
  INDEX open_id (open_id(255))
);


CREATE TABLE ciloa2.old_user (
   affiliation        text,
   archive_time       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
   archived_user_id   varchar(255) PRIMARY KEY,
   attr_json          text,
   create_time        timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
   description        text,
   display_name       text,
   email              text,
   eppn               text,
   eptid              text,
   first_name         text,
   idp_display_name   text,
   idp                text,
   last_accessed      bigint DEFAULT NULL,
   last_modified_ts   timestamp NULL DEFAULT CURRENT_TIMESTAMP,
   last_name          text,
   loa                text,
   oidc               text,
   open_id            text,
   ou                 text,
   pairwise_id        text,
   remote_user        text,
   serial_string      text,
   state              text,
   subject_id         text,
   us_idp             BOOLEAN DEFAULT NULL,
   user_uid           text NOT NULL,
    );

CREATE TABLE ciloa2.identity_provider (
  idp_uid           VARCHAR(255) PRIMARY KEY,
  description       TEXT,
  creation_ts       TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  last_accessed     BIGINT DEFAULT NULL,
  last_modified_ts  TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
);


CREATE TABLE ciloa2.two_factor (
  description  text,
  user_uid     VARCHAR(255) PRIMARY KEY,
  two_factor   TEXT
);


CREATE TABLE ciloa2.uid_seq (
  nextval INT PRIMARY KEY AUTO_INCREMENT,
  dummy   TINYINT
)   AUTO_INCREMENT =5;

CREATE TABLE ciloa2.tx_records
(
    audience     text,
    description  text,
    expires_at   bigint,
    issued_at    bigint,
    issuer       text,
    lifetime     bigint,
    parent_id    text,
    resource     text,
    scopes       text,
    stored_token text,
    token        text,
    token_id     VARCHAR(255) PRIMARY KEY,
    token_type   text,
    valid        boolean,
    INDEX parents (parent_id(255))
);


COMMIT;

