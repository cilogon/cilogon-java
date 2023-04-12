# Top-level build script for all websites. This builds NCSA, QDL, OA4MP then CILogon.

/home/ncsa/dev/ncsa-git/security-lib/website/make-website.sh
if [ $? -ne 0 ]; then
    echo "NCSA build failed. Exiting build"
    exit
fi
/home/ncsa/dev/ncsa-git/qdl/website/make-website.sh
if [ $? -ne 0 ]; then
    echo "QDL build failed. Exiting build"
    exit
fi
/home/ncsa/dev/ncsa-git/oa4mp/website/make-website.sh
if [ $? -ne 0 ]; then
    echo "OA4MP build failed. Exiting build"
    exit
fi

/home/ncsa/dev/ncsa-git/cilogon/website/make-website.sh
if [ $? -ne 0 ]; then
    echo "CILogon build failed. Exiting build"
    exit
fi
