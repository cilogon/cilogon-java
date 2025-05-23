# This takes two arguments --
# 1 = the root docs directory of the install where the documents are found
# 2 = the target directory for the output.

# remember that this uses C-style array for the args, so ${args[0]} is the 1st arg
# not the program name!

args=("$@")
cd ${args[1]} || exit

echo "converting CILogon docs to PDF"

lowriter --headless --convert-to pdf ${args[0]}/admin/src/main/docs/CILogon-DB-service.odt     > /dev/null
lowriter --headless --convert-to pdf ${args[0]}/qdl/src/main/docs/cilogon-store.odt            > /dev/null
echo "done converting CILogon docs"
