# cilogon-java
The java extension to OA4MP for CILogon.

[![NSF-1547268](https://img.shields.io/badge/NSF-1547268-blue.svg)](https://nsf.gov/awardsearch/showAward?AWD_ID=1547268)

# [CILogon](https://www.cilogon.org) extensions to [OA4MP](https://oa4mp.org)

This is the set of extensions to OA4MP for CILogon. It consists of the user management and authorization
modules. the only change is customizing the first leg of the OAuth exchange, the rest of the functionality
of OA4MP is the same. The user interface code for this extension is written in PHP and available [here](https://github.com/cilogon/service).

# License

Please see the [NCSA license](https://github.com/cilogon/oauth2-cilogon/blob/master/LICENSE) for details

## Getting the sources

You may check out the source from [GitHub](https://github.com/ncsa/cilogon-java). This is
cloned into `$NCSA_DEV_INPUT`. At the end of the cloning, you should have `$NCSA_DEV_INPUT/cilogon`.
A typical sequence would be
```
$>export NCSA_DEV_INPUT=/path/to/ncsa/git
$>cd $NCSA_DEV_INPUT
$>git clone (whatever you want)
$>cd cilogon
```
The information at [OA4MP](https://github.com/ncsa/oa4mp) on building that system is identical, except
the top level is cilogon, not oa4mp. The only real difference comes down to which artifacts are produced.

# Artifacts

If you run the `build.sh` script (which compiles everything and produces artifacts in the `$NCSA_DEV_OUTPUT/cilogon`
directory)

| Name                   | Description                                                                                        |
|------------------------|----------------------------------------------------------------------------------------------------|
| cilogon-oa2-cli        | script to run the CLI (command line interface), the chief admin interface                          |
 | cilogon-oa2-cli.jar    | runnable jar for the CLI                                                                           |
 | cilogon-oa2-X.sql      | SQL creation scripts for database X, e.g., mysql, derby, etc.                                      |
 | cilogon-oa2-client.war | the CIlogon client. This is functionally the same as the OA4MP client, just with CILogon branding. |
 | oauth2.war             | deployable war for Tomcat                                                                          |
 | qdl.jar                | runnable jar that includes QDL and all the extensions for both OA4MP and CILogon                   |

 # About build-all.sh

This script will build _every_ project for CILogon, which includes the NCSA security library, QDL, and OA4MP
proper. It is intended to be one-stop shopping for someone who needs to do wide-ranging updates on the
entire source tree, and assumes you have checked out all of the source code for these projects.