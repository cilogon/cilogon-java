/* Creates the database for a test server.

 issue the following commands inside psql:

 create database ncsa;
 \connect database ncsa;
 drop schema oauth cascade;
 create schema cilogon2;
 set search_path to cilogon2;
 create user cilogon with password 'XXXXX';
*/

CREATE TABLE cilogon2.clients (
  oauth_consumer_key  TEXT PRIMARY KEY,
  oauth_client_pubkey TEXT,
  name                TEXT,
  home_url            TEXT,
  error_url           TEXT,
  email               TEXT,
  proxy_limited       BOOLEAN,
  creation_ts         TIMESTAMP);


CREATE TABLE cilogon2.client_approvals (
  oauth_consumer_key TEXT PRIMARY KEY,
  approver           TEXT,
  approved           BOOLEAN,
  approval_ts        TIMESTAMP);

CREATE UNIQUE INDEX c_a_ndx ON cilogon2.client_approvals (oauth_consumer_key);

/*
  This is a bit of a mess... This is because this transaction table MUST be shared by cilogon 1 code running
  on the servers, therefore, newer OA4MP column names are not used (e.g. temp_cred rather than temp_token),
  and this has to have some extra columns that OA4MP doesn't use. These are custom set in the class
  CILPGTransactionKeys for use.
 */
CREATE TABLE cilogon2.transactions (
  temp_cred          TEXT NOT NULL,
  temp_cred_ok       BOOLEAN,
  oauth_callback     TEXT,
  temp_cred_ss       TEXT,
  callback_uri       TEXT,
  certreq            TEXT,
  lifetime           BIGINT,
  oauth_consumer_key TEXT,
  verifier           TEXT,
  access_token       TEXT,
  access_token_ss    TEXT,
  access_token_ok    BOOLEAN,
  certificate        TEXT,
  userid             TEXT,
  cilogon_info       TEXT,
  complete           BOOLEAN,
  loa                TEXT
);

CREATE TABLE cilogon2.user (
  user_uid         TEXT PRIMARY KEY,
  first_name       TEXT,
  last_name        TEXT,
  idp              TEXT,
  idp_display_name TEXT,
  remote_user      TEXT,
  eppn             TEXT,
  eptid            TEXT,
  open_id          TEXT,
  oidc             TEXT,
  email            TEXT,
  serial_string    TEXT,
  create_time      TIMESTAMP
);

CREATE TABLE cilogon2.old_user (
  archived_user_id TEXT PRIMARY KEY,
  archive_time     TIMESTAMP,
  user_uid         TEXT NOT NULL,
  first_name       TEXT,
  last_name        TEXT,
  idp              TEXT,
  idp_display_name TEXT,
  remote_user      TEXT,
  email            TEXT,
  eppn             TEXT,
  eptid            TEXT,
  open_id          TEXT,
  oidc             TEXT,
  serial_string    TEXT,
  create_time      TIMESTAMP
);

CREATE TABLE cilogon2.identity_provider (
  idp_uid TEXT PRIMARY KEY
);


CREATE INDEX trans_ndx ON cilogon2.transactions (temp_token, oauth_verifier, access_token);

CREATE TABLE cilogon2.two_factor (user_uid TEXT NOT NULL, two_factor TEXT) WITHOUT OIDS;

CREATE SEQUENCE cilogon2.uid_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 42 CACHE 1;

CREATE TABLE cilogon2.uid_seq (
  user_id BIGINT NOT NULL DEFAULT nextval('user_id_seq')
);

GRANT ALL PRIVILEGES ON SCHEMA cilogon2 TO cilogon;
GRANT ALL PRIVILEGES ON cilogon2.transactions TO cilogon;
GRANT ALL PRIVILEGES ON cilogon2.client_approvals TO cilogon;
GRANT ALL PRIVILEGES ON cilogon2.clients TO cilogon;
GRANT ALL PRIVILEGES ON cilogon2.user TO cilogon;
GRANT ALL PRIVILEGES ON cilogon2.old_user TO cilogon;
GRANT ALL PRIVILEGES ON cilogon2.identity_provider TO cilogon;
GRANT ALL PRIVILEGES ON cilogon2.uid_seq TO cilogon;
GRANT ALL PRIVILEGES ON cilogon2.two_factor TO cilogon;

COMMIT;
