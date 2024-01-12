# NOTE this pre-supposes that you have done a full build of QDL and are making the full
# distro for OA4MP's version of QDL.  If you just need to runnable jar, that is created
# using the qdl profile and
# and resides in oa4mp/qdl/target

CILOGON_QDL_ROOT=$NCSA_DEV_INPUT/cilogon/qdl
CILOGON_QDL_DEPLOY=$NCSA_DEV_OUTPUT/cilogon-qdl
DEFAULT_JAR_NAME="cilogon-qdl-installer.jar"

JAR_NAME=${1:-$DEFAULT_JAR_NAME}
cd "$CILOGON_QDL_DEPLOY" || exit

# rm -Rf *
"$CILOGON_QDL_ROOT"/src/main/scripts/create_dirs.sh

echo "changing to $CILOGON_QDL_ROOT"
cd "$CILOGON_QDL_ROOT" || exit
mvn -P qdl package > qdl-maven.log
if [[ $? -ne 0 ]] ; then
    echo "create CILogon QDL failed. See qdl-maven.log"
    exit 1
fi

cp "$CILOGON_QDL_ROOT/target/cilogon-qdl-jar-with-dependencies.jar" $CILOGON_QDL_DEPLOY/lib/qdl.jar
unzip -p "$CILOGON_QDL_ROOT/target/cilogon-qdl-jar-with-dependencies.jar" META-INF/MANIFEST.MF > $CILOGON_QDL_DEPLOY/lib/build-info.txt

cd $CILOGON_QDL_DEPLOY || exit
# Get the actual manifest so that build info is available.
jar cmf installer.mf "$JAR_NAME" edu/uiuc/ncsa/qdl/install/Installer.class version.txt  bin docs etc lib log var examples

