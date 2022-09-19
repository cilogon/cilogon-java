# Top-level build script for everything. This build NCSA, QDL, OA4MP then CILogon.

DEPLOY_ROOT=/home/ncsa/dev/temp-deploy
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
SVN_ROOT=/home/ncsa/dev/ncsa-git
CILOGON_OA2_DEPLOY=$DEPLOY_ROOT/cilogon-oa2
OA4MP_CLIENT_DEPLOY=$DEPLOY_ROOT/client
OA4MP_SERVER_DEPLOY=$DEPLOY_ROOT/server

echo "cleaning out old deploy in " $DEPLOY_ROOT
if [ ! -d "$DEPLOY_ROOT" ]; then
    mkdir "$DEPLOY_ROOT"
fi
cd $DEPLOY_ROOT
rm -Rf *

if [ $? -ne 0 ]; then
    echo "Remove failed. Exiting build"
    exit
fi
echo "Files removed, making target directories"

mkdir "$OA4MP_CLIENT_DEPLOY"
mkdir "$OA4MP_SERVER_DEPLOY"
mkdir "$CILOGON_OA2_DEPLOY"
mkdir "$DEPLOY_ROOT/oa2-qdl"

echo "Target directories created"

#/home/ncsa/dev/ncsa-git/crypto-java8/build.sh
#if [ $? -ne 0 ]; then
#    echo "Crypto Java 8 build failed. Exiting build"
#    exit
#fi

/home/ncsa/dev/ncsa-git/security-lib/build.sh
if [ $? -ne 0 ]; then
    echo "NCSA build failed. Exiting build"
    exit
fi
/home/ncsa/dev/ncsa-git/qdl/build.sh
if [ $? -ne 0 ]; then
    echo "QDL build failed. Exiting build"
    exit
fi
/home/ncsa/dev/ncsa-git/oa4mp/build.sh
if [ $? -ne 0 ]; then
    echo "OA4MP build failed. Exiting build"
    exit
fi

/home/ncsa/dev/ncsa-git/cilogon/build.sh
if [ $? -ne 0 ]; then
    echo "CILogon build failed. Exiting build"
    exit
fi
