# This is where the brains of the install reside. It knits together various distros to
# make the full distro for CILogon

QDL_ROOT=$NCSA_DEV_INPUT/qdl
OA4MP_ROOT=$NCSA_DEV_INPUT/oa4mp
CILOGON_ROOT=$NCSA_DEV_INPUT/cilogon

CILOGON_QDL_DEPLOY=$NCSA_DEV_OUTPUT/cilogon-qdl

cd $OA4MP_ROOT/qdl/src/main/scripts  || exit
./create_dirs.sh $QDL_ROOT $CILOGON_QDL_DEPLOY
cp $CILOGON_ROOT/qdl/src/main/resources/cfg-cilogon.xml $CILOGON_QDL_DEPLOY/etc/cfg-cilogon.xml
cp $CILOGON_ROOT/qdl/src/main/resources/cilogon-boot.qdl $CILOGON_QDL_DEPLOY/etc/cilogon-boot.qdl
cp $CILOGON_ROOT/qdl/src/main/scripts/qdl $CILOGON_QDL_DEPLOY/bin/qdl

$CILOGON_ROOT/website/convert-docs.sh $CILOGON_ROOT/website/docs $CILOGON_QDL_DEPLOY/docs
