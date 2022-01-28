
   
import numpy as np
import time
import pika
import json




# connect to rabbitmq

auth = pika.PlainCredentials('admin','admin')
connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost', port='5672', virtual_host='/', credentials=auth))

channel = connection.channel()
# channel.queue_declare(queue='CheckVehicleType')








exitFilePath = "/hdd/data/1101/exitwaste.csv"



def stringToTimestamp(string):
    flowtime = string
    time_array = time.strptime(flowtime,"%Y-%m-%d %H:%M:%S")
    timestamp = time.mktime(time_array)*1000
    return int(timestamp)



# send exit data
print("send exitwaste......")
gf = open(exitFilePath)       
line = gf.readline() 


i = 0


stime = time.time()
while line:

        
    line = gf.readline()
    linesplit = line.split("€", line.count("€"))
    
    i += 1


    if i%50000 == 0:
        etime = time.time()
        print("------------ %s ------------ total time : %.2f sec" % (i,etime-stime))


    try:

        timestamp = stringToTimestamp(linesplit[34])
        # select attributes
   
        exitrecord = {'EXITID' : linesplit[0], 
                  'EXTIME' : timestamp, 
                  'PASSID' : linesplit[124], 
                  'EXTOLLSTATIONID' : linesplit[32],
                  'VEHICLEID' : linesplit[48] + '-' + linesplit[49],

                  'VEHICLETYPE': linesplit[53], 
                  'TRANSPAYTYPE':  linesplit[162],

                 }


        
        channel.basic_publish(exchange='', routing_key='CheckVehicleType',body=str(exitrecord))

    
    except:
        continue


etime = time.time()
print("------------ process exitwaste total time : %.2f sec ------------ " % (etime-stime))



