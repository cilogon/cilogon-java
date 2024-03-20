# This takes two arguments --
# 1 = the root docs directory of the install where the documents are found
# 2 = the target directory for the output.

# remember that this uses C-style array for the args, so ${args[0]} is the 1st arg
# not the program name!

args=("$@")
cd ${args[1]} || exit

echo "converting CILogon docs to PDF"

lowriter --headless --convert-to pdf ${args[0]}/website/docs/CILogon_db_servlet.odt        > /dev/null
echo "done converting CILogon docs"
