#!/bin/bash
set -e

function die()
{
	echo "$1"
	exit 1
}

function check_variable()
{
	eval val='$'$1
	[ "$val" != "" ] || die "$1 not defined"
}

check_variable RED_EXPERT_VERSION

rm -rf tmp dist
mkdir tmp tmp/config dist

cp target/RedExpert.jar tmp
cp target/RedExpert.exe tmp
cp eq.sh tmp/RedExpert.sh
cp RedExpert.bat tmp
cp config/fbtrace.conf tmp/config
cp config/gss.login.conf tmp/config

cp -r red_expert.{png,ico} \
	  target/lib \
	  docs \
	  license \
	  LICENSE \
	  tmp

cd tmp
ARCHIVE_PREFIX=RedExpert-$RED_EXPERT_VERSION
tar --transform "s|^.|RedExpert-$RED_EXPERT_VERSION|" -czf ../dist/RedExpert-$RED_EXPERT_VERSION.tar.gz .
zip -9 -r -y  ../dist/RedExpert-$RED_EXPERT_VERSION.zip . > /dev/null
