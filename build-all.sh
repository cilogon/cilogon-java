# Top-level build script for everything. This build NCSA, QDL, OA4MP then CILogon.

CILOGON_OA2_DEPLOY=$NCSA_DEV_OUTPUT/cilogon
start_time=$(date +%s)

echo "cleaning out old deploy in " $NCSA_DEV_OUTPUT
if [ -d "$CILOGON_OA2_DEPLOY" ]
  then
    echo "cleaning $CILOGON_OA2_DEPLOY ..."
    cd "$CILOGON_OA2_DEPLOY"
    rm -Rf *
   else
    echo "creating output directory $CILOGON_OA2_DEPLOY ..."
    mkdir "$CILOGON_OA2_DEPLOY"
fi

if [ $? -ne 0 ]; then
    echo "Remove failed. Exiting build"
    exit
fi


if [ -d "$NCSA_DEV_INPUT/security-lib/" ]
then
     "$NCSA_DEV_INPUT/security-lib/build.sh"
  if [ $? -ne 0 ]; then
    echo "NCSA build failed. Exiting build"
    exit
  fi
fi

if [ -d "$NCSA_DEV_INPUT/qdl/" ]
then
   $NCSA_DEV_INPUT/qdl/build.sh
  if [ $? -ne 0 ]; then
      echo "QDL build failed. Exiting build"
      exit
  fi

fi
if [ -d "$NCSA_DEV_INPUT/oa4mp/" ]
then
    $NCSA_DEV_INPUT/oa4mp/build-all.sh
    if [ $? -ne 0 ]; then
        echo "OA4MP build failed. Exiting build. See "$NCSA_DEV_INPUT/oa4mp/oa4mp-maven.log""
        exit
    fi
fi

if [ -d "$NCSA_DEV_INPUT/cilogon/" ]
then
   $NCSA_DEV_INPUT/cilogon/build.sh
  if [ $? -ne 0 ]; then
    echo "CILogon build failed. Exiting build. see $NCSA_DEV_INPUT/cilogon/cilogon-maven.log"
    exit
  fi
fi

end_time=$(date +%s)

elapsed=$((end_time - start_time))
echo "Total elapsed time: $elapsed seconds"

current_time=$(date +%T)
echo "build ended at: $current_time"

