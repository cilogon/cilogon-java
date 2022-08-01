export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
DEPLOY_ROOT=/home/ncsa/dev/temp-deploy
SVN_ROOT=/home/ncsa/dev/ncsa-git
CILOGON_ROOT=$SVN_ROOT/cilogon
CILOGON_OA2_DEPLOY=$DEPLOY_ROOT/cilogon-oa2
CILOGON_OA2_TOOLS=$CILOGON_ROOT/cilogon-admin

cd $CILOGON_ROOT
mvn clean install

if [[ $? -ne 0 ]] ; then
    echo "CILogon maven build failed, exiting..."
    exit 1
fi

cd $CILOGON_OA2_TOOLS

mvn -P cli package
cp target/cilogon-oa2-cli-jar-with-dependencies.jar $CILOGON_OA2_DEPLOY/cilogon-oa2-cli.jar
cp $CILOGON_ROOT/cilogon-server/target/oauth2.war $CILOGON_OA2_DEPLOY/
cp $CILOGON_ROOT/cilogon-client/target/cilogon-oa2-client.war $CILOGON_OA2_DEPLOY/

cp $CILOGON_OA2_TOOLS/src/main/scripts/* $CILOGON_OA2_DEPLOY
cp $CILOGON_OA2_TOOLS/src/main/resources/*.sql $CILOGON_OA2_DEPLOY


cp cilogon-oa2-cli-jar-with-dependencies.jar $CILOGON_OA2_DEPLOY/cilogon-oa2-cli.jar
cd $CILOGON_OA2_DEPLOY

#CILogon QDL support

cd $CILOGON_ROOT/cilogon-qdl
mvn -P qdl package
cd target
cp cil-qdl-jar-with-dependencies.jar $CILOGON_OA2_DEPLOY/cil-qdl.jar
