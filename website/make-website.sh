#
# Run this AFTER build.sh or it will fail.
#
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
CILOGON_ROOT=/home/ncsa/dev/ncsa-git/cilogon
# Output of everything goes to WEBSITE_ROOT
WEBSITE_ROOT=$CILOGON_ROOT/docs

$CILOGON_ROOT/website/convert-docs.sh $CILOGON_ROOT $WEBSITE_ROOT/pdf

cd $CILOGON_ROOT  || exit
mvn clean javadoc:javadoc javadoc:aggregate
cd $CILOGON_ROOT/website || exit
mvn  site
# Note the source directory in the next command has no apidocs subdirectory, so this overlays
# without overwriting.
cp -r $CILOGON_ROOT/website/target/site/* $WEBSITE_ROOT # copy maven site
cp -r $CILOGON_ROOT/target/site/* $WEBSITE_ROOT   # copy javadoc in toto
