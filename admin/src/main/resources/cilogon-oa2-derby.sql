/*
   This comment section tells how to set up an Apache Derby database to
   work with OA4MP.

   Create the directory to where you want derby to create the database.
   This directory should be empty, since Derby will create everything on your
   behalf -- and might refuse to do anything if the directory has content.
   Make the following substitutions below:

         DB_NAME - the entire path to this database,
     DB_PASSWORD - password to the database
       USER_NAME - name of the user (created below)
   USER_PASSWORD - password for user

   Note 1: If you want your database to live in

       /opt/oauth2/var/derby/oa4mp

   you would create

      /opt/oauth2/var/derby

   with nothing in it and the DB_NAME is then

      /opt/oauth2/var/derby/oa4mp

   I.e., the last directory in this path is what Derby creates.

   Note 2: In Derby, the database lives in a directory. This means that unless
   certain precautions are taken, it is completely insecure. The setup below
   mitigates this.

   1. Puts a password on the entire database so it cannot be read from the disk
   2. Sets a user and password to access the database.
      These are stored in the database, hence step 1 to lock the whole thing down.
   3. All database access from OA4MP is via the so-called embedded driver, so
      no network traffic is needed.

   One-time install instructions
   ----------------------------
   Install derby, probably with a package manager like synaptic or yum.
   Note that outdented lines are to be pasted into the command line

   Start derby with

ij

   Then issue the following. This sets up the database and will create the user above
   (Note that the user name and password are set as properties, so do substitute).
   Even though the user does not exist yet, you must  connect with the
   user name so that they are the owner of the database.

connect 'jdbc:derby:DB_NAME;create=true;dataEncryption=true;bootPassword=DB_PASSWORD;user=USER_NAME';
call syscs_util.syscs_set_database_property('derby.connection.requireAuthentication', 'true');
call syscs_util.syscs_set_database_property('derby.authentication.provider', 'BUILTIN');
call syscs_util.syscs_set_database_property('derby.user.USER_NAME', 'USER_PASSWORD');
call syscs_util.syscs_set_database_property('derby.database.propertiesOnly', 'true');
call syscs_util.syscs_set_database_property('derby.database.sqlAuthorization', 'true');

   Optional test:
   If you want be sure it works, create the schema as follows:

create schema ciloa2;
show schemas;

   And a bunch of schemas will be displayed, including ciloa2. This means everything
   worked. You don't need to issue the create schema command below.

   Note that if you do not set the schema, then the default schema will be whatever
   username you connected as, which will own the tables.

   At this point, exit Derby. Initial setup is done. You must connect again as the user
   that runs this because creating the tables below will automatically assign the
   current user as the table owner, so no other permissions (which can get complicated)
   are needed.

exit;

   Now connect to it with the following from the command line after restarting ij:

connect 'jdbc:derby:DB_NAME;user=USER_NAME;password=USER_PASSWORD;bootPassword=DB_PASSWORD';

   and either paste in the rest of this file OR just run the whole thing from inside ij

run '/full/path/to/oauth2-derby.qdl';

   At this point, your database is ready for use.


*/

/* Uncomment this if you did not do the test above and have already created the schema.
CREATE SCHEMA ciloa2;
*/

CREATE TABLE ciloa2.clients
(
    client_id        VARCHAR(255) PRIMARY KEY,
    public_key       CLOB,
    name             CLOB,
    home_url         CLOB,
    error_url        CLOB,
    issuer           CLOB,
    ldap             CLOB,
    email            CLOB,
    scopes           CLOB,
    proxy_limited    BOOLEAN,
    public_client    BOOLEAN,
    creation_ts      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_ts timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rt_lifetime      bigint,
    callback_uri     CLOB,
    sign_tokens      BOOLEAN,
    cfg              CLOB
);

CREATE TABLE ciloa2.adminClients
(
    admin_id         VARCHAR(255) PRIMARY KEY,
    name             CLOB,
    secret           CLOB,
    email            CLOB,
    creation_ts      timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_ts timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vo               CLOB,
    max_clients      BIGINT,
    issuer           CLOB,
    config           CLOB
);


CREATE TABLE ciloa2.permissions
(
    permission_id VARCHAR(255) PRIMARY KEY,
    admin_id      VARCHAR(255),
    client_id     VARCHAR(255),
    can_approve   BOOLEAN,
    can_create    BOOLEAN,
    can_read      BOOLEAN,
    can_remove    BOOLEAN,
    can_write     BOOLEAN,
    creation_ts   TIMESTAMP
);

CREATE TABLE ciloa2.client_approvals
(
    client_id   VARCHAR(255) PRIMARY KEY,
    approver    CLOB,
    approved    BOOLEAN,
    status      CLOB,
    approval_ts TIMESTAMP
);

CREATE TABLE ciloa2.transactions
(
            temp_token VARCHAR(255) PRIMARY KEY,
      temp_token_valid BOOLEAN,
          certlifetime BIGINT,
             client_id VARCHAR(1024),
        verifier_token VARCHAR(1024),
          access_token VARCHAR(1024),
         refresh_token VARCHAR(1024),
   refresh_token_valid BOOLEAN,
            expires_in BIGINT,
              username VARCHAR(1024),
     access_token_valid BOOLEAN DEFAULT NULL,
              auth_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
             auth_grant varchar(1024),
 refresh_token_lifetime BIGINT,
   authz_grant_lifetime BIGINT,
            is_rfc_8628 BOOLEAN,
            affiliation varchar(1024),
              user_code VARCHAR(1024),
                    loa VARCHAR(1014),
                     ou VARCHAR(1014),
           display_name VARCHAR(1014),
           callback_uri CLOB,
                certreq CLOB,
                states CLOB,
           certificate CLOB,
       myproxyUsername CLOB,
                 nonce CLOB,
                scopes CLOB,
            req_state CLOB
    );

   CREATE INDEX access_token on ciloa2.transactions (access_token);
   CREATE INDEX refresh_token on ciloa2.transactions (refresh_token);

CREATE TABLE ciloa2.tx_records
(
     token_id VARCHAR(255) PRIMARY KEY,
      lifetime bigint,
     issued_at bigint,
    expires_at bigint,
     parent_id VARCHAR(1024),
    token_type CLOB,
         valid boolean,
        scopes CLOB,
      audience CLOB,
        issuer CLOB,
      resource CLOB);
   CREATE INDEX  parents on ciloa2.tx_records (parent_id);


CREATE TABLE ciloa2.virtual_organizations
(
             vo_id VARCHAR(255) PRIMARY KEY,
           created bigint,
    default_key_id CLOB,
    discovery_path VARCHAR(1024),
            issuer CLOB,
         at_issuer CLOB,
     json_web_keys CLOB,
     last_modified bigint,
             title CLOB,
          resource CLOB,
             valid boolean);
  create  INDEX discovery_path on ciloa2.virtual_organizations (discovery_path);

CREATE TABLE ciloa2.users
(
        user_uid VARCHAR(255) PRIMARY KEY,
      first_name VARCHAR(1024),
       last_name VARCHAR(1024),
             idp VARCHAR(1024),
idp_display_name VARCHAR(1024),
     remote_user VARCHAR(1024),
           email VARCHAR(1024),
   serial_string VARCHAR(1024),
     affiliation CLOB,
       attr_json CLOB,
    display_name VARCHAR(1024),
              ou VARCHAR(1024),
             loa VARCHAR(1024),
     pairwise_id VARCHAR(1024),
      subject_id VARCHAR(1024),
            eppn VARCHAR(1024),
           eptid VARCHAR(1024),
         open_id VARCHAR(1024),
          us_idp BOOLEAN,
            oidc VARCHAR(1024),
           state CLOB,
     create_time TIMESTAMP
);
create  INDEX eppn    on ciloa2.users (eppn);
create  INDEX eptid   on ciloa2.users (eptid);
create  INDEX oidc    on ciloa2.users (oidc);
create  INDEX open_id on ciloa2.users (open_id);

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
  idp_uid VARCHAR(255) PRIMARY KEY
);


CREATE TABLE ciloa2.two_factor (
  user_uid   VARCHAR(255) PRIMARY KEY,
  two_factor CLOB
);


CREATE TABLE ciloa2.uid_seq
(     nextval INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    dummy int
);

/*
 Useful commands
 ij - starts the command line tool (once installed)

 To connect to the database
 ij> connect 'jdbc:derby:/home/ncsa/temp/oa4mp2/derby;create=true'

 To run (possibly this scripts). argument is the full path to the script.
 ij> run 'myscript.sql';


 */