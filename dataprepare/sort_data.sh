#!/bin/bash


sourcePath="/hdd/data"
targetPath="None"
days=1


# parse param
while [ $# -ge 2 ] ; do
        case "$1" in
                -i) sourcePath=$2; shift 2;;
                -o) targetPath=$2; shift 2;;
                -d) days=$2; shift 2;;
                *) echo "unknown parameter $1." ; exit 1 ; break;;
        esac
done


# if targetPath is None(default), exit
if [ $targetPath = "None" ]
then
   echo "Need Output Path..."
exit ; fi

# if targetPath not exist, create dir
if [ ! -d "$targetPath" ]; then
mkdir $targetPath
fi

echo "Input file path: $sourcePath, Output file path: $targetPath"


for ((i=1101; i<1101+$days; i++))
do


now=`date +'%Y-%m-%d %H:%M:%S'`
start_time=$(date --date="$now" +%s)

echo -n process $i .... 

# process gantry
# select attributes: FLOWTYPE,TIME,STATIONID,VLP,VLPC,VEHICLETYPE,PASSID,TIMESTRING,ORIGINALFLAG,PROVINCEBOUND,MEDIATYPE,TRANSCODE,SPECIALTYPE,LANESPINFO,ACTUALFEECLASS
# and sort by STATIONID

awk -F€ '{ if (NR > 1){print \
                        2 "," \
                        mktime(substr($9,0,4)" "substr($9,6,2)" "substr($9,9,2)" "substr($9,12,2)" "substr($9,15,2)" "substr($9,18,2))-8*3600 "," \
                        $2 "," \
                        $22 "," \
                        $23 "," \
                        $24 "," \
                        $44 "," \
                        $9 "," \
                        $3 "," \
                        0 "," \
                        $14 "," \
                        None "," \
                        $88 "," \
                        $108 "," \
                        None }}' $sourcePath/$i/gantrywaste_fix.csv | \
                        sort -t ',' -k 3,3 > $targetPath/original_${i}_gantry.csv

# create new file, write header info
echo FLOWTYPE,TIME,STATIONID,VLP,VLPC,VEHICLETYPE,PASSID,TIMESTRING,ORIGINALFLAG,PROVINCEBOUND,MEDIATYPE,TRANSCODE,SPECIALTYPE,LANESPINFO,ACTUALFEECLASS > $targetPath/original_${i}.csv


# join with provincesort.csv on STATIONID, 1-province entry, 2-province exit, 0-nonprovinceboundry,
join -a1 -1 3 -2 1 -t ',' $targetPath/original_${i}_gantry.csv provincesort.csv -o 1.1,1.2,1.3,1.4,1.5,1.6,1.7,1.8,1.9,2.2,1.11,1.12,1.13,1.14,1.15 | \
                                    awk -F, '{if($10!=None){print $0} \
                                    else{print $1 ","  $2 ","  $3 ","  $4 ","  $5 ","  $6 ","  $7 ","  $8 ","  $9 ",0,"  $11 ","  $12 ","  $13 ","  $14 ","  $15}}' \
                                    > $targetPath/original_${i}.csv




# process entry
# select attributes: FLOWTYPE,TIME,STATIONID,VLP,VLPC,VEHICLETYPE,PASSID,TIMESTRING,ORIGINALFLAG,PROVINCEBOUND,MEDIATYPE,TRANSCODE,SPECIALTYPE,LANESPINFO,ACTUALFEECLASS



awk -F€ '{ if (NR > 1){print \
                        1 "," \
                        mktime(substr($23,0,4)" "substr($23,6,2)" "substr($23,9,2)" "substr($23,12,2)" "substr($23,15,2)" "substr($23,18,2))-8*3600 "," \
                        $21 "," \
                        $40 "," \
                        $41 "," \
                        $44 "," \
                        $66 "," \
                        $23 "," \
                        None "," \
                        None "," \
                        $24 "," \
                        $2 "," \
                        $62 "," \
                        None "," \
                        None }}' $sourcePath/$i/enwaste.csv >> $targetPath/original_$i.csv
                                        



# process exit
# select attributes: FLOWTYPE,TIME,STATIONID,VLP,VLPC,VEHICLETYPE,PASSID,TIMESTRING,ORIGINALFLAG,PROVINCEBOUND,MEDIATYPE,TRANSCODE,SPECIALTYPE,LANESPINFO,ACTUALFEECLASS


awk -F€ '{ if (NR > 1){print \
                        3 "," \
                        mktime(substr($35,0,4)" "substr($35,6,2)" "substr($35,9,2)" "substr($35,12,2)" "substr($35,15,2)" "substr($35,18,2))-8*3600 "," \
                        $33 "," \
                        $49 "," \
                        $50 "," \
                        $54 "," \
                        $125 "," \
                        $35 "," \
                        None "," \
                        None "," \
                        $38 "," \
                        $4 "," \
                        $107 "," \
                        $108 "," \
                        $148 }}' $sourcePath/$i/exitwaste.csv >> $targetPath/original_$i.csv
                                        

# sort data
sort -t ',' -k 2,2 -k 1,1 $targetPath/original_$i.csv > $targetPath/sort_1101.csv

# delete temp files
rm $targetPath/original_${i}_gantry.csv
rm $targetPath/original_$i.csv



now=`date +'%Y-%m-%d %H:%M:%S'`
end_time=$(date --date="$now" +%s)

echo " done! used time:"$((end_time-start_time))"s"

done
