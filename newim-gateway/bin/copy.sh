#!/bin/sh

source /etc/profile
export LC_ALL=C

folder="$1"
name="$2"
dest="$3"

echo "$folder"
echo "$name"
echo "$dest"

user="$4"
host="$5"

# e.g. /data/in/zhenai/t_fw_00005/20140918
cd "$folder"
scp -q "$name" "$user"@"$host":"$dest"

# e.g. /data/in/zhenai/t_fw_00005/20140918
# to  /data/up/zhenai/t_fw_00005/20140918
up="${folder/\/in\///up/}"
echo "up=$up"
mkdir -p "$up"

touch "$name.check"
check="${dest/\/in//check}"
echo "check=$check"
scp -q "$name.check" "$user"@"$host":"$check"

rm -f "$name.check"

mv "$name" "$up"

echo "SUCCESS"