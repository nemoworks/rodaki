import numpy as np
import time
import pika
import json




# read files

Times = []

print("Read data ...")
print("process entry :")
file_name = './entryfile.csv'
with open(file_name,"r",encoding="GB18030") as f:
    entryfile1 = f.readlines()[1:]
    print(len(entryfile1))
    for i in range(1,len(entryfile1),2):
        items = entryfile1[i].split(",")
        timeArray = time.strptime(items[22], "%Y-%m-%d %H:%M:%S")
        ts = time.mktime(timeArray)
        Times.append([0,i,ts])

        
print("process gantry :")
file_name = './gantryfile.csv'
with open(file_name,"r",encoding="GB18030") as f:
    gantryfile1 = f.readlines()[1:]
    print(len(gantryfile1))
    for i in range(1,len(gantryfile1),2):
        try:
            items = gantryfile1[i].split(",")
            timeArray = time.strptime(items[8], "%Y-%m-%d %H:%M:%S")
            ts = time.mktime(timeArray)
            Times.append([1,i,ts])
        except:
            continue

            
    
print("process exit :")
file_name = './exitfile.csv'
with open(file_name,"r",encoding="GB18030") as f:
    exitfile1 = f.readlines()[1:]
    print(len(exitfile1))
    for i in range(1,len(exitfile1),2):
        items = exitfile1[i].split(",")

        timeArray = time.strptime(items[34], "%Y-%m-%d %H:%M:%S")
        ts = time.mktime(timeArray)
        Times.append([2,i,ts])
    
station = np.loadtxt("广场信息.csv",skiprows=2, delimiter=',', dtype=str)[:,[1,2,4,5]]

for i,s in enumerate(station):
    station[i][0] = s[0][:14]
    
station_location = station[:,0]
# print(station.shape)

gantry = np.loadtxt("收费门架信息.csv",skiprows=2, delimiter=',', dtype=str)[:,[1,2,3,4]]
gantry_location = gantry[:,0]
# print(gantry.shape)




# sort

Times = np.array(Times)

messageOrder = Times[np.argsort(Times[:,2])].astype(int)




# connect to rabbitmq

auth = pika.PlainCredentials('admin','admin')
connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost', port='5672', virtual_host='/', credentials=auth))

channel_re = connection.channel()
channel_re.queue_declare(queue='record')


channel_pay = connection.channel()
channel_pay.queue_declare(queue='payrecord')

channel_inv = connection.channel()
channel_inv.queue_declare(queue='invrecord')

channel_veh = connection.channel()
channel_veh.queue_declare(queue='vehicle')

channel_gan = connection.channel()
channel_gan.queue_declare(queue='gantry')



print("Send data ...")

# publish message

for index in messageOrder:
    if index[0] == 0 :
        
        items = entryfile1[index[1]].split(",")

        MEDIATYPE = items[23]
        MEDIAID = ''
        if items[23] == '1':
            MEDIATYPE = 'OBU'
            MEDIAID = items[26]
        elif items[23] == '2':
            MEDIATYPE = 'CPC'
            MEDIAID = items[33]
            
        try:
            p = np.where(station_location == items[20])[0][0]
            
            STATIONNAME = station[p,1]
            LONGITUDE = station[p,3]
            LATITUDE = station[p,2]

        except:
            STATIONNAME = "null"
            LONGITUDE = "-1"
            LATITUDE = "-1"
            
        entryrecord = {'ENTRYID' : items[0], 
                  'ENTIME' : items[22], 
                  'PASSID' : items[65], 
                  'ENTOLLSTATIONID' : items[20],
                  'ENTOLLLANEID' : items[21],
                  'SHIFT': items[5], 
                  'OPERID': items[9],
                  'VEHICLEID' : items[39] + '-' + items[40],
                  'MEDIATYPE' : MEDIATYPE,
                  'MEDIAID' : MEDIAID,
                  'ENWEIGHT' : items[48],
                  'ENIDENTIFY' : items[41] + '-' + items[42],
                  'LONGITUDE' : LONGITUDE,     
                  'LATITUDE' : LATITUDE,
                  'STATIONNAME' : STATIONNAME,
                  'STATIONTYPE' : '1'
                 }
        
        
        
        vehicle = {'VEHICLEID' : items[39] + '-' + items[40],
                  'VEHICLETYPE' : items[43],
                  'AXISINFO' : items[49],
                  'LIMITWEIGHT' : items[50],
                  'STATIONTYPE' : '1'
                 }
        

        
        channel_re.basic_publish(exchange='', routing_key='record',body=str(entryrecord))
        channel_veh.basic_publish(exchange='', routing_key='vehicle',body=str(vehicle))         

        
#         print(entryrecord)
#         print(vehicle)
        
        
    elif index[0] == 1 :

        items = gantryfile1[index[1]].split(",")

        MEDIATYPE = items[13]
        MEDIAID = ''
        if items[13] == '1':
            MEDIATYPE = 'OBU'
            MEDIAID = items[48]
        elif items[13] == '2':
            MEDIATYPE = 'CPC'
            MEDIAID = items[48]
            
        try:
            p = np.where(gantry_location == items[1])[0][0]

            STATIONNAME = gantry[p,1]
            LONGITUDE = gantry[p,3]
            LATITUDE = gantry[p,2]  

        except:
            STATIONNAME = "null"
            LONGITUDE = "-1"
            LATITUDE = "-1"

    
        try:
            gantryrecord = {'TRADEID' : items[0], 
                      'TRANSTIME' : items[8], 
                      'PASSID' : items[43], 
                      'GANTRYID' : items[1],
                      'VEHICLEID' : items[21] + '-' + items[22],
                      'MEDIATYPE' : MEDIATYPE,
                      'MEDIAID' : MEDIAID,
                      'FEE' : items[10],
                      'FEEMILEAGE' : items[146],
                      'LONGITUDE' : LONGITUDE,     
                      'LATITUDE' : LATITUDE,
                      'STATIONNAME' : STATIONNAME,
                      'STATIONTYPE' : '2'
                     }
        except:
            continue

            
        vehicle = {'VEHICLEID' : items[21] + '-' + items[22],
                  'VEHICLESEAT' : items[58],
                  'VEHICLELENGTH' : items[61],
                  'VEHICLEWIDTH' : items[62],
                  'VEHICLEHIGHT' : items[63],
                  'AXLECOUNT' : items[59],
                  'STATIONTYPE' : '2'
                 }

        gan= {'GANTRYID' : items[1],
                  'GANTRYNAME' : STATIONNAME,
                  'LONGITUDE' : LONGITUDE,   
                  'LATITUDE' : LATITUDE,   
                  'GANTRYTYPE' : items[119]
                 }
        
        channel_re.basic_publish(exchange='', routing_key='record',body=str(gantryrecord))
        channel_veh.basic_publish(exchange='', routing_key='vehicle',body=str(vehicle))       
        channel_gan.basic_publish(exchange='', routing_key='gantry',body=str(gan))               
        

        
        
        
    elif index[0] == 2 :

        items = exitfile1[index[1]].split(",")
        
        MEDIATYPE = items[37]
        MEDIAID = ''
        if items[37] == '1':
            MEDIATYPE = 'OBU'
            MEDIAID = items[38]
        elif items[37] == '2':
            MEDIATYPE = 'CPC'
            MEDIAID = items[38]

        try:
            p = np.where(station_location == items[32])[0][0]

            STATIONNAME = station[p,1]
            LONGITUDE = station[p,3]
            LATITUDE = station[p,2]

        except:
            STATIONNAME = "null"
            LONGITUDE = "-1"
            LATITUDE = "-1"
    
            
        exitrecord = {'EXITID' : items[0], 
                  'EXTIME' : items[34], 
                  'PASSID' : items[124], 
                  'EXTOLLSTATIONID' : items[32],
                  'EXTOLLLANEID' : items[33],
                  'SHIFT': items[5], 
                  'OPERID': items[9], 
                  'VEHICLEID' : items[48] + '-' + items[49],
                  'MEDIATYPE' : MEDIATYPE,
                  'MEDIAID' : MEDIAID,
                  'PAYID' : items[48] + '-' + items[49] + '-' + items[124], 
                  'FEE' : items[76],
                  'FEEMILEAGE' : items[163],
                  'EXWEIGHT' : items[56],
                  'EXIDENTIFY' : items[50] + '-' + items[51],
                  'LONGITUDE' : LONGITUDE,     
                  'LATITUDE' : LATITUDE,
                  'STATIONNAME' : STATIONNAME,
                  'STATIONTYPE' : '3'
                 }
        
        payrecord = {'PAYID' : items[48] + '-' + items[49] + '-' + items[124], 
                  'TIME' : items[34], 
                  'PASSID' : items[124], 
                  'VEHICLEID' : items[48] + '-' + items[49],
                  'PAYTYPE' : items[96],
                  'FEE' : items[76],
                  'FEEMILEAGE' : items[163],
                  'INVOICEID' : items[102]
                 }
        
        invrecord = {'INVOICEID' : items[102],
                  'TIME' : items[34], 
                  'INVOICETYPE' : items[100], 
                  'INVOICECNT' : items[103]
                 }
            
        vehicle = {'VEHICLEID' : items[48] + '-' + items[49],
                  'VEHICLETYPE' : items[53],
                  'AXISINFO' : items[57],
                  'LIMITWEIGHT' : items[58],
                  'STATIONTYPE' : '3',
                 }
            
        channel_re.basic_publish(exchange='', routing_key='record',body=str(exitrecord))
        channel_pay.basic_publish(exchange='', routing_key='payrecord',body=str(payrecord))
        channel_inv.basic_publish(exchange='', routing_key='invrecord',body=str(invrecord))
        channel_veh.basic_publish(exchange='', routing_key='vehicle',body=str(vehicle))
                
