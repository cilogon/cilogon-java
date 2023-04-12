#
# Run this AFTER build.sh or it will fail.
#
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
CILOGON_ROOT=/home/ncsa/dev/ncsa-git/cilogon
# Output of everything goes to WEBSITE_ROOT
WEBSITE_ROOT=$CILOGON_ROOT/docs

cd $GITHUB_ROOT/pdf

echo "converting docs to PDF"
lowriter --headless --convert-to pdf ~/dev/ncsa-git/cilogon/website/docs/CILogon_db_servlet.odt
echo "done converting PDFs"


cd $CILOGON_ROOT
mvn clean javadoc:javadoc javadoc:aggregate
cd $CILOGON_ROOT/website
mvn clean site
# Note the source directory in the next command has no apidocs subdirectory, so this overlays
# without overwriting.
cp -r $CILOGON_ROOT/website/target/site/* $WEBSITE_ROOT # copy maven site
cp -r $CILOGON_ROOT/target/site/* $WEBSITE_ROOT   # copy javadoc in toto
