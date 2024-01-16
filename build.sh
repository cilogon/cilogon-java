if [ -z ${NCSA_DEV_INPUT+x} ]
  then
    echo "no sources, skipping..."
    exit 1
fi

if [ -z ${NCSA_DEV_OUTPUT+x} ]
  then
    echo "no output directory, skipping..."
    exit 1
fi

CILOGON_ROOT=$NCSA_DEV_INPUT/cilogon
CILOGON_OA2_DEPLOY=$NCSA_DEV_OUTPUT/cilogon
CILOGON_OA2_TOOLS=$CILOGON_ROOT/admin

if [ ! -d "$CILOGON_OA2_DEPLOY" ]
  then
    mkdir "$CILOGON_OA2_DEPLOY"
  else
    echo "  deploy target directory exists, cleaning..."
    cd "$CILOGON_OA2_DEPLOY"
    rm -Rf *
fi

cd $CILOGON_ROOT
echo "building CILogon"
mvn clean install > maven.log

if [[ $? -ne 0 ]] ; then
    echo "CILogon maven build failed, exiting..."
    exit 1
fi
echo "         done!"
echo "building CILogon tools"

cd $CILOGON_OA2_TOOLS

mvn -P cli package >  cli.log
if [[ $? -ne 0 ]] ; then
    echo "CILogon maven build for CLI failed, exiting..."
    exit 1
fi
echo "         done!"
echo "deploying CILogon tools"
cp "$CILOGON_OA2_TOOLS/target/cilogon-oa2-cli-jar-with-dependencies.jar" $CILOGON_OA2_DEPLOY/cilogon-oa2-cli.jar
cp $CILOGON_ROOT/server/target/oauth2.war $CILOGON_OA2_DEPLOY/
cp $CILOGON_ROOT/client/target/cilogon-oa2-client.war $CILOGON_OA2_DEPLOY/

cp $CILOGON_OA2_TOOLS/src/main/scripts/cilogon-oa2-cli $CILOGON_OA2_DEPLOY
cp $CILOGON_OA2_TOOLS/src/main/resources/*.sql $CILOGON_OA2_DEPLOY

cd $CILOGON_OA2_DEPLOY
echo "         done!"

#CILogon QDL support
cd $CILOGON_ROOT/qdl
mvn -P qdl package > qdl.log
if [[ $? -ne 0 ]]
 then
    echo "CILogon maven build for QDL extensions failed, exiting..."
    exit 1
fi

cp target/cilogon-qdl-jar-with-dependencies.jar $CILOGON_OA2_DEPLOY/qdl.jar
echo "     ... done!"
#cd target
#cp cil-qdl-jar-with-dependencies.jar $CILOGON_OA2_DEPLOY/cil-qdl.jar
#
# The next command takes the qdl.jar file in qdl_root and creates and installer jar_name in target_dir
# It explodes the qdl.jar file in the target_dir and re-assembles it into the installer.
# create_installer.sh qdl_root target_dir jar_name
# /home/ncsa/dev/ncsa-git/qdl/language/src/main/scripts/create_installer.sh $CILOGON_ROOT/qdl $CILOGON_OA2_DEPLOY/cil-qdl cil-qdl-installer.jar

 cd "$CILOGON_ROOT/qdl/src/main/scripts"
 echo "building CILogon QDL installer"
 ./create_installer.sh
 if [[ $? -ne 0 ]] ; then
     echo "CILogon create installer failed"
     exit 1
 fi
echo "         done!"
 