export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
DEPLOY_ROOT=/home/ncsa/dev/temp-deploy
SVN_ROOT=/home/ncsa/dev/ncsa-git
CILOGON_ROOT=$SVN_ROOT/cilogon
CILOGON_OA2_DEPLOY=$DEPLOY_ROOT/cilogon-oa2
CILOGON_OA2_TOOLS=$CILOGON_ROOT/admin

cd $CILOGON_ROOT
mvn clean install

if [[ $? -ne 0 ]] ; then
    echo "CILogon maven build failed, exiting..."
    exit 1
fi

cd $CILOGON_OA2_TOOLS

mvn -P cli package
cp target/cilogon-oa2-cli-jar-with-dependencies.jar $CILOGON_OA2_DEPLOY/cilogon-oa2-cli.jar
cp $CILOGON_ROOT/server/target/oauth2.war $CILOGON_OA2_DEPLOY/
cp $CILOGON_ROOT/client/target/cilogon-oa2-client.war $CILOGON_OA2_DEPLOY/

cp $CILOGON_OA2_TOOLS/src/main/scripts/* $CILOGON_OA2_DEPLOY
cp $CILOGON_OA2_TOOLS/src/main/resources/*.sql $CILOGON_OA2_DEPLOY


cp cilogon-oa2-cli-jar-with-dependencies.jar $CILOGON_OA2_DEPLOY/cilogon-oa2-cli.jar
cd $CILOGON_OA2_DEPLOY

#CILogon QDL support
mkdir CILOGON_OA2_DEPLOY/cil-qdl
cd $CILOGON_ROOT/qdl
mvn -P qdl package
mv target/cil-qdl-jar-with-dependencies.jar target/qdl.jar
#cd target
#cp cil-qdl-jar-with-dependencies.jar $CILOGON_OA2_DEPLOY/cil-qdl.jar
#
# The next command takes the qdl.jar file in qdl_root and creates and installer jar_name in target_dir
# It explodes the qdl.jar file in the target_dir and re-assembles it into the installer.
# create_installer.sh qdl_root target_dir jar_name
/home/ncsa/dev/ncsa-git/qdl/language/src/main/scripts/create_installer.sh $CILOGON_ROOT/qdl $CILOGON_OA2_DEPLOY/cil-qdl cil-qdl-installer.jar

