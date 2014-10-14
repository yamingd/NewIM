#!/bin/bash

source /etc/profile
export LC_ALL=zh_CN.utf8

function scpfile()
{
  local source_file="$1/$2"
  local source_name="$2"
  local target_dir="$3"
  local target_name="$4"

  #判断是否存在源文件，存在则建立check文件
  if [ -f "$source_file" ]; then
    touch "$source_file.check"
    if [ $? -eq 0 ]; then
      scp -q  "$source_file" "$target_dir/in/$target_name"
      if [ $? -eq 0 ]; then
        scp -q "$source_file.check" "$target_dir/check/$target_name.check"
        rm "$source_file.check"
        [ $? -eq 0 ] && return 0 || return 1
      fi
    else
      return 1
    fi
  else
    return 1
  fi
}

#参数 source_file: 源文件目录
#参数 source_name: 源文件名称
#在上传成功后，把文件移动到上传成功目录
function mvfile()
{
   local source_file="$1"
   local source_name="$2"

   upfolder="${source_file/\/in\///up/}"
   mkdir -p "$upfolder"

   mv "$source_file/$source_name" "$upfolder"

}

function main()
{
  local src_file="$1"
  local src_name="$2"
  local tar_dir="$3"
  local tar_name="$4"
  local user="$5"
  local tar_ip="$6"
  
  scpfile "$src_file" "$src_name" "$user@$tar_ip:$tar_dir" "$tar_name"
  ret=$?
  echo "$ret"

  if [ $ret -eq 0 ]; then
     mvfile "$src_file" "$src_name"
     echo "SUCCESS"
  else
     echo "FAILED"
  fi

  [ $ret -eq 0 ] && return 0 || return 1
}

main "$1" "$2" "$3" "$4" "$5" "$6"
