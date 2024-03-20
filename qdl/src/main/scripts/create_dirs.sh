# This is where the brains of the install reside. It knits together various distros to
# make the full distro for CILogon

QDL_ROOT=$NCSA_DEV_INPUT/qdl
OA4MP_ROOT=$NCSA_DEV_INPUT/oa4mp
CILOGON_ROOT=$NCSA_DEV_INPUT/cilogon

CILOGON_QDL_DEPLOY=$NCSA_DEV_OUTPUT/cilogon-qdl

cd $OA4MP_ROOT/qdl/src/main/scripts  || exit
./create_dirs.sh $QDL_ROOT $CILOGON_QDL_DEPLOY

$CILOGON_ROOT/website/convert-docs.sh $CILOGON_ROOT $CILOGON_QDL_DEPLOY/docs
