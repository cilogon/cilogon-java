/* Creates the database for a test server.

 issue the following commands inside psql:

 create database ncsa;
 \c  ncsa; <<-- connects to database, this is a meta command
 drop schema oauth cascade;
 create schema ciloa2;
 set search_path to ciloa2;
 create user cilogon with password 'XXXXX';
*/


create table ciloa2.:clients  (
    client_id  text PRIMARY KEY,
    public_key text,
    name text,
    home_url text,
    error_url text,
    email text,
    callback_uri text,
    proxy_limited boolean,
    rt_lifetime bigint,
    creation_ts TIMESTAMP);


create table ciloa2.client_approvals(
    client_id text primary key,
    approver text,
    approved boolean,
    approval_ts TIMESTAMP);

create table ciloa2.transactions  (
   temp_token text primary key,
   temp_token_valid boolean,
   callback_uri text,
   certreq text,
   certlifetime bigint,
   client_id text,
   verifier_token text,
   access_token text,
   access_token_valid boolean,
   certificate text,
   refresh_token text,
   refresh_token_valid boolean,
   expires_in bigint,
   myproxyusername text,
   username text);

CREATE UNIQUE INDEX trans_ndx ON ciloa2.transactions (temp_token, refresh_token, access_token, username);



CREATE TABLE ciloa2.user (
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

CREATE TABLE ciloa2.old_user (
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

CREATE TABLE ciloa2.identity_provider (
  idp_uid TEXT PRIMARY KEY
);


CREATE TABLE ciloa2.two_factor (user_uid TEXT NOT NULL, two_factor TEXT) WITHOUT OIDS;

CREATE SEQUENCE ciloa2.uid_seq INCREMENT 1 MINVALUE 1 MAXVALUE 9223372036854775807 START 42 CACHE 1;

CREATE TABLE ciloa2.uid_seq (
  user_id BIGINT NOT NULL DEFAULT nextval('user_id_seq')
);

GRANT ALL PRIVILEGES ON SCHEMA cilogon2 TO cilogon;
GRANT ALL PRIVILEGES ON ciloa2.transactions TO cilogon;
GRANT ALL PRIVILEGES ON ciloa2.client_approvals TO cilogon;
GRANT ALL PRIVILEGES ON ciloa2.clients TO cilogon;
GRANT ALL PRIVILEGES ON ciloa2.user TO cilogon;
GRANT ALL PRIVILEGES ON ciloa2.old_user TO cilogon;
GRANT ALL PRIVILEGES ON ciloa2.identity_provider TO cilogon;
GRANT ALL PRIVILEGES ON ciloa2.uid_seq TO cilogon;
GRANT ALL PRIVILEGES ON ciloa2.two_factor TO cilogon;

COMMIT;
