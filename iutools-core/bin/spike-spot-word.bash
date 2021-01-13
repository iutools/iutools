#!/bin/bash

# Quick spike to experiment with calling the portage word aligner through
# ssh.
#

EN_SENTS="Premier Joe Savikataaq was selected by his colleagues in the 5th Legislative Ass
embly of Nunavut to lead Nunavut on June 14, 2018.\n  Prior to that, Premier Savikataaq was first elected in the general election held
 on October 28, 2013, to represent the constituency of Arviat South in the 4th L
egislative Assembly of Nunavut.\n  He became the Minister of Environment and Minister of Community and Government
 Services in November 2015."

IU_SENTS="ᓯᕗᓕᖅᑎ ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᓚᐅᖕᒪᑦ ᒪᓕᒐᓕᐅᖅᑎᐅᖃᑎᖏᓐᓄᑦ ᑕᓪᓕᒪᒋᓕᖅᑕᖓᓐᓂᑦ ᒪᓕᕆᓕᐅᕐᕕᖕᒥᑦ ᓄᓇᕗᒻᒥᑦ ᓯᕗᓕᖅᑎᐅᓂᐊ
ᓕᖅᖢᓂ ᓄᓇᕗᒻᒧᑦ ᔫᓐ 14, 2018−ᖑᑎᓪᓗᒍ.\nᓯᕗᙵᓂᑦ, ᓯᕗᓕᖅᑎ ᓴᕕᑲᑖᖅ ᓯᕗᓪᓕᖅᐹᒥᑦ ᓂᕈᐊᖅᑕᐅᓚᐅᖅᓯᒪᔪᖅ ᓂᕈᐊᖕᓇᐅᑎᓪᓗᒍ ᐅᑐᐱᕆ 28, 2013−ᒥᑦ, ᑭᒡᒐᖅᑐᐃᓂᐊᖅ
ᖢᓂ ᐊᕐᕕᐊᑦ ᓂᒋᐊᓂᑦ ᑎᓴᒪᒋᓕᖅᑕᖓᓐᓂᑦ ᒪᓕᒐᓕᐅᕐᕕᖕᒥᑦ ᓄᓇᕗᒻᒥᑦ.\n  ᒥᓂᔅᑕᙳᓚᐅᖅᓯᒪᔪᖅ ᐊᕙᑎᓕᕆᔨᒃᑯᓐᓄᑦ ᐊᒻᒪᓗ ᓄᓇᓕᖕᓂᑦ ᒐᕙᒪᒃᑯᓐᓂᓪᓗ ᐱᔨᑦᑎᕋᖅᑎᒃᑯᓐᓄᑦ ᓄᕕᐱᕆ 2015−ᒥᑦ.\n ᓄᕕᐱᕆ 2017−ᒥᑦ, ᑎᒃᑯᐊᖅᑕᐅᓚᐅᖅᑐᖅ ᐱᓕᕆᐊᖃᓕᖅᖢᓂ ᓯᕗᓕᖅᑎᐅᑉ ᑐᒡᓕᐊᓂᑦ ᒥᓂᔅᑕᐅᓕᖅᖢᓂᓗ ᐃᓄᓕᕆᔨᒃᑯᓐᓄᑦ; ᒫᑦᓯ
2018−ᒥᑦ ᑎᓕᔭᐅᒃᑲᓐᓂᓚᐅᖅᑐᖅ ᒥᓂᔅᑕᒧᑦ ᐱᕙᓪᓕᐊᔪᓕᕆᔨᒃᑯᓐᓄᑦ ᐃᖏᕐᕋᔪᓕᕆᔨᒃᑯᓐᓄᓪᓗ ᐊᒻᒪᓗ ᐊᕙᑎᓕᕆᔨᒃᑯᓐᓄᑦ, ᐊᒻᒪ
ᓗ 2019-ᒥ ᐱᔭᑦᓴᖅᑖᖅᑎᑕᐅᓚᐅᖅᑐᖅ ᒥᓂᔅᑕᒧᑦ ᑲᒪᒋᔭᖃᕐᓂᕐᒧᑦ ᑲᓇᑕᐅᑉ ᓯᓚᑖᓃᖔᖅᑐᓕᕆᓂᕐᒧᑦ."

IUTOOLS_WORKSPACE=/Users/desilets/Temp/iutools/workspace
PORTAGE_HOST=132.246.128.43
PORTAGE_WORKSPACE=/home/desiletsa/iutools/workspace/job0001
PORTAGE_USER=desiletsa

echo "IUTOOLS_WORKSPACE=$IUTOOLS_WORKSPACE"
echo "EN_SENTS=$EN_SENTS"
echo "IU_SENTS=$IU_SENTS"

# Dump sentence files to local IUTOOLS workspace
echo $EN_SENTS > $IUTOOLS_WORKSPACE/en.sents
echo $IU_SENTS > $IUTOOLS_WORKSPACE/iu.sents

# Make sure the ssh-agent is running
/Users/desilets/bin/ssh-agent-wrap.bash

# SCP the sentence files from loacl IUTOOLS workspace to remote
# PORTAGE workspace
#
#scp $IUTOOLS_WORKSPACE/*.sents $PORTAGE_USER@PORTAGE_HOST:$PORTAGE_WORKSPACE
scp $IUTOOLS_WORKSPACE/*.sents $PORTAGE_USER@132.246.128.43:$PORTAGE_WORKSPACE