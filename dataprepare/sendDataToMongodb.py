import string
import time
import pymongo
from pymongo import MongoClient, InsertOne, DeleteOne, ReplaceOne, UpdateOne
import json





# connect to mongodb, Tutorial: https://pymongo.readthedocs.io/en/stable/tutorial.html
client = MongoClient('mongodb://localhost:27017/')

# db in mongodb
db = client['sortdata']

# collection in mongodb
collection = db["1101_original"]


# paths of three files
entryFilePath = "/hdd/data/1101/enwaste.csv"
gntryFilePath = "/hdd/data/1101/gantrywaste_fix.csv"
exitFilePath = "/hdd/data/1101/exitwaste.csv"











#   province boundary gantryids

entrys = [
"G000237001001010090",
"G000237001001010100",
"G000237006000820080",
"G000237006000820090",
"G000237006000820230",
"G000237006000820240",
"G000337001000110010",
"G000337001000110030",
"G000337001000110050",
"G000337001000110070",
"G000337008000920090",
"G000337008000920100",
"G001537007000820080",
"G001537007000820090",
"G001837013000110010",
"G001837013000110210",
"G002037007000920100",
"G002037007000920110",
"G002237008000820080",
"G002237008000820220",
"G002537004000920100",
"G002537004000920160",
"G003537002000820070",
"G003537002000820080",
"G032137002000110010",
"G032137002000110020",
"G032137004001020100",
"G032137004001020200",
"G032137006000610010",
"G032137006000610020",
"G032137009001220110",
"G032137009001220120",
"G151137004000620080",
"G151137004000620090",
"G251637004000620060",
"G251637004000620120",
"G351137002000920020",
"G351137002000920030",
"S000137002001120110",
"S000137002001120220",
"S001237002001020100",
"S001237002001020220",
"S002837001000320090",
"S002837001000320220",
"S003037002001520080",
"S003037002001520100",
"S003337002001320060",
"S003337002001320130"
]





exits = [

"G000237001000120010",
"G000237001000120020",
"G000237006000710060",
"G000237006000710070",
"G000237006000710210",
"G000237006000710220",
"G000337001001220020",
"G000337001001220040",
"G000337001001220060",
"G000337001001220080",
"G000337008000610070",
"G000337008000610080",
"G001537007000710060",
"G001537007000710070",
"G001837013002020200",
"G001837013002020220",
"G002037007000810080",
"G002037007000810090",
"G002237008000710070",
"G002237008000710210",
"G002537004000810070",
"G002537004000810150",
"G003537002000910090",
"G003537002000910100",
"G032137002000220030",
"G032137002000220040",
"G032137004000910090",
"G032137004000910190",
"G032137006000520110",
"G032137006000520120",
"G032137009001110090",
"G032137009001110100",
"G151137004000510060",
"G151137004000510070",
"G251637004000510050",
"G251637004000510110",
"G351137002000210040",
"G351137002000210050",
"S000137002001010100",
"S000137002001010210",
"S001237002000910090",
"S001237002000910210",
"S002837001000210020",
"S002837001000210210",
"S003037002001410070",
"S003037002001410090",
"S003337002000810070",
"S003337002000810140"
]






def stringToTimestamp(string):
    flowtime = string
    time_array = time.strptime(flowtime,"%Y-%m-%d %H:%M:%S")
    timestamp = time.mktime(time_array)*1000
    return int(timestamp)






# send entry data
print("process enwaste......")
gf = open(entryFilePath)       
line = gf.readline() 



requests = []

i = 0


stime = time.time()
while line:
    
    
        
    line = gf.readline()
    linesplit = line.split("€", line.count("€"))
    
    i += 1
        
    if len(requests) == 1000:
        collection.bulk_write(requests)
        requests = []


    if i%50000 == 0:
        etime = time.time()
        print("------------ %s ------------ total time : %.2f sec" % (i,etime-stime))

    try:

        timestamp = stringToTimestamp(linesplit[22])

        # select attributes
        requests.append(InsertOne({"FLOWTYPE":1,
                                    "TIME":timestamp,
                                    "STATIONID":linesplit[20],
                                    "VLP":linesplit[39],
                                    "VLPC":linesplit[40],
                                    "VEHICLETYPE":linesplit[43],
                                    "PASSID":linesplit[65],
                                    
                                    "TIMESTRING":linesplit[22],
                                    "ORIGINALFLAG": None,
                                    "PROVINCEBOUND": None,

                                    "MEDIATYPE": linesplit[23],
                                    "TRANSCODE": linesplit[1],
                                    "SPECIALTYPE": linesplit[61],
                                    "LANESPINFO": None,
                                    "ACTUALFEECLASS": None
                                    }))

    
    except:
        continue

collection.bulk_write(requests)
etime = time.time()
print("------------ process enwaste total time : %.2f sec ------------ " % (etime-stime))












# send gantry data
print("process gantrywaste......")
gf = open(gntryFilePath)          

line = gf.readline() 




requests = []

i = 0


stime = time.time()
while line:

    insertdata = {}
        
    line = gf.readline()
    linesplit = line.split("€", line.count("€"))
    
    i += 1
        
    if len(requests) >= 1000:
        collection.bulk_write(requests)
        requests = []


    if i%50000 == 0:
        etime = time.time()
        print("------------ %s ------------ total time : %.2f sec" % (i,etime-stime))

    try:

        timestamp = stringToTimestamp(linesplit[8])
        # check if current gantryid is in province boundary gantryids list
        if linesplit[1] in entrys:
            requests.append(InsertOne({"FLOWTYPE":2,
                                        "TIME":timestamp,
                                        "STATIONID":linesplit[1],
                                        "VLP":linesplit[21],
                                        "VLPC":linesplit[22],
                                        "VEHICLETYPE":linesplit[23],
                                        "PASSID":linesplit[43],
                                        
                                        "TIMESTRING":linesplit[8],
                                        "ORIGINALFLAG": linesplit[2],
                                        "PROVINCEBOUND": 1,
                                        
                                        "MEDIATYPE": linesplit[13],
                                        "TRANSCODE": None,
                                        "SPECIALTYPE": linesplit[87],
                                        "LANESPINFO": linesplit[107],
                                        "ACTUALFEECLASS": None
                                        }))


        elif linesplit[1] in exits:
            requests.append(InsertOne({"FLOWTYPE":2,
                                        "TIME":timestamp,
                                        "STATIONID":linesplit[1],
                                        "VLP":linesplit[21],
                                        "VLPC":linesplit[22],
                                        "VEHICLETYPE":linesplit[23],
                                        "PASSID":linesplit[43],
                                        
                                        "TIMESTRING":linesplit[8],
                                        "ORIGINALFLAG": linesplit[2],
                                        "PROVINCEBOUND": 2,

                                        "MEDIATYPE": linesplit[13],
                                        "TRANSCODE": None,
                                        "SPECIALTYPE": linesplit[87],
                                        "LANESPINFO": None,
                                        "ACTUALFEECLASS": None
                                        }))

        else:

            requests.append(InsertOne({"FLOWTYPE":2,
                                        "TIME":timestamp,
                                        "STATIONID":linesplit[1],
                                        "VLP":linesplit[21],
                                        "VLPC":linesplit[22],
                                        "VEHICLETYPE":linesplit[23],
                                        "PASSID":linesplit[43],
                                        
                                        "TIMESTRING":linesplit[8],
                                        "ORIGINALFLAG": linesplit[2],
                                        "PROVINCEBOUND": 0,

                                        "MEDIATYPE": linesplit[13],
                                        "TRANSCODE": None,
                                        "SPECIALTYPE": linesplit[87],
                                        "LANESPINFO": None,
                                        "ACTUALFEECLASS": None
                                        }))


    except:
        continue

collection.bulk_write(requests)
etime = time.time()
print("------------ process gantrywaste total time : %.2f sec ------------ " % (etime-stime))
    







# send exit data
print("process exitwaste......")
gf = open(exitFilePath)       
line = gf.readline() 



requests = []

i = 0


stime = time.time()
while line:
    
    
        
    line = gf.readline()
    linesplit = line.split("€", line.count("€"))
    
    i += 1
        
    if len(requests) == 1000:
        collection.bulk_write(requests)
        requests = []


    if i%50000 == 0:
        etime = time.time()
        print("------------ %s ------------ total time : %.2f sec" % (i,etime-stime))

    try:

        timestamp = stringToTimestamp(linesplit[34])
        # select attributes
        requests.append(InsertOne({"FLOWTYPE":3,
                                    "TIME":timestamp,
                                    "STATIONID":linesplit[32],
                                    "VLP":linesplit[48],
                                    "VLPC":linesplit[49],
                                    "VEHICLETYPE":linesplit[53],
                                    "PASSID":linesplit[124],
                                    
                                    "TIMESTRING":linesplit[34],
                                    "ORIGINALFLAG": None,
                                    "PROVINCEBOUND": None,

                                    "MEDIATYPE": linesplit[37],
                                    "TRANSCODE": linesplit[3],
                                    "SPECIALTYPE": linesplit[106],
                                    "LANESPINFO": linesplit[107],
                                    "ACTUALFEECLASS": linesplit[147]
                                    }))


    
    except:
        continue

collection.bulk_write(requests)
etime = time.time()
print("------------ process exitwaste total time : %.2f sec ------------ " % (etime-stime))


