#!/usr/bin/python3
import os.path
#import numpy as np
#import cv2
from flask import Flask, request, Response
import uuid
import json
import csv
#import classupdate

#Function detect face from image
# def faceDetect(img):
#     face_cascade = cv2.CascadeClassifier('face_detect_cascade.xml')
#     gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
#     faces = face_cascade.detectMultiScale(gray,1.3,5) #org tutorial 1.3,5
#     for (x,y,w,h) in faces:
#         img =cv2.rectangle(img,(x,y),(x+w,y+h),(0,255,0))
#     #save file
#     path_file=('static/%s.jpg' %uuid.uuid4().hex)
#     cv2.imwrite(path_file,img)
#     return json.dumps(path_file) #return image file name

#csv upload

class update():
    def __init__(self,info):
        '''
        :param info: a list containing : [situation,uuid,date]
        '''
        self.situation = info[0]
        self.uuid = info[1]
        self.date = info[2]
    def getsituation(self):
        return self.situation
    def getuuid(self):
        return self.uuid
    def getdate(self):
        return self.date

def csvUpdater(patientdata):
    '''
    :param patientdata: classupdate, use get methods to retrieve fields
    :return: True
    '''
    status = patientdata.getsituation()
    uuid = patientdata.getuuid()
    date = patientdata.getdate()
    with open('static/cases.csv', 'a') as csvfile:
        writer = csv.writer(csvfile)
        #writer.writerow(['uuid','statuslog','datalog'])
        writer.writerow([uuid,status,date])
    return True

#NOT USED
def csvuuid():
    with open('static/uuid.csv', 'r') as csvfile:
        csvfile.seek(0,2)
        fsize = csvfile.tell()
        csvfile.seek(max(fsize-50,0),0)
        lines = csvfile.readlines()
        print(lines)

        increaser = False
        #processing the lines and adding one
        def uuidincrease(lines):
            '''
            :param lines: a list ['lastestuseduuidfromcsv']
            :return: newuuid (only uses numbers, adds 1 to the uuids, will think of another method if we run out)
            '''
            counter = 0
            currentuuid = lines[0]
            newuuid = currentuuid
            reversedcurrentuuid = currentuuid[::-1]
            for x in range(0,len(reversedcurrentuuid)): #verified to be correct len. No need to minus/add any ofset to len(currentuuid)
                intuuidchar = reversedcurrentuuid[x]
                #print(intuuidchar)
                try:
                    counter += 1
                    intuuidchar=int(intuuidchar)
                    intuuidchar += 1
                    #print(intuuidchar)
                    if intuuidchar == 10:
                        increaser = True
                    else:
                        increaser = False
                        adjustedcounter= 36 - counter
                        newuuid = newuuid[:adjustedcounter] + str(intuuidchar) + newuuid[(adjustedcounter+1):]
                        #print(newuuid)
                except(ValueError):
                    continue
            return newuuid

        newuuid = uuidincrease(lines)

        def writtingtocsv(newuuid):
            with open('static/uuid.csv', 'w') as csvfile:
                #writer = csv.writer(csvfile,delimiter=',')
                #writer.writerow([newuuid])
                print(newuuid + "newuuid")
                fieldnames = ['uuid']
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                #writer.writeheader()
                writer.writerow({'uuid':newuuid})
            return True

        writtingtocsv(newuuid)
    return newuuid


def uuidincrease(currentuuid):
    '''
    :param currentuuid: a string consisting of the 36 characters of the uuid including dashes
    :return: newuuid (only uses numbers, adds 1 to the uuids, will think of another method if we run out)
    '''
    counter = 0
    newuuid = currentuuid
    reversedcurrentuuid = currentuuid[::-1]
    increaser=True
    while increaser==True:
        for x in range(0, len(
                reversedcurrentuuid)):  # verified to be correct len. No need to minus/add any ofset to len(currentuuid)
            intuuidchar = reversedcurrentuuid[x]
            # print(intuuidchar)
            try:
                counter += 1
                intuuidchar = int(intuuidchar)
                intuuidchar += 1
                # print(intuuidchar)
                if intuuidchar == 10:
                    intuuidchar=0
                    increaser = True
                    adjustedcounter = 36 - counter
                    newuuid = newuuid[:adjustedcounter] + str(intuuidchar) + newuuid[(adjustedcounter + 1):]
                else:
                    adjustedcounter = 36 - counter
                    newuuid = newuuid[:adjustedcounter] + str(intuuidchar) + newuuid[(adjustedcounter + 1):]
                    # print(newuuid)
                    increaser = False
                    break
            except(ValueError):
                continue
    return newuuid

def new_uuid_major_minor_increase(currentuuid,major,minor):
    '''
    :param currentuuid: current uuid
    :param major: last distributed major
    :param minor: last distributed minor
    :return: new uuid (first 36 digits) - (37-42 inclusive )Major - (44-49 inclusive) Minor
    '''
    def putbackdigits(original):
        '''
            :param original: string between 1 to 5 characters long
            :return: string that is now 1 character longer by adding a zero , a special type of recurring loops. Calls itself over and over again
        '''
        fixed_digits = str(original)
        while len(fixed_digits)<5:
            fixed_digits = '0' + fixed_digits # concatinating string by adding zeros in front
            print(fixed_digits)
        return fixed_digits

    counter = 0
    newuuidmajor = int(major)
    newuuidminor = int(minor)+1
    if newuuidminor>66535: #incase more than 66535 people install our app
        newuuidmajor += 1
        newuuidminor = 0
    # Failsafe
    if newuuidmajor>66535:
        print('UNABLE TO FUNCTION. NUMBER OF USERS EXCEED 65535*65535')
        raise UserWarning
    newuuidmajor = putbackdigits(newuuidmajor)
    newuuidminor = putbackdigits(newuuidminor)
    newuuid = str(currentuuid) + '-' + str(newuuidmajor) + '-' + str(newuuidminor) #concatinating
    print(newuuid)
    return newuuid





def txtclearandupdate(content):
    '''
    Function used by txtuuid
    :param content: a string consisting of what to put into the txt
    :return: True/False (Bool)
    '''
    uuidtxtw = open("static/uuid.txt", 'w')
    uuidtxtw.write(content)
    uuidtxtw.close() #Close the file to avoid confusion
    #to allow for debugging
    print(open("static/uuid.txt", "r").readlines())
    return True

def new_txtclearandupdate(content):
    '''
    Function used by txtuuid
    :param content: a string consisting of what to put into the txt
    :return: True/False (Bool)
    '''
    #uuid
    uuidtxtw = open("static/uuid.txt", 'w')
    uuidtxtw.write(content[:36])
    uuidtxtw.close() #Close the file to avoid confusion
    #major
    majortxtw = open("static/newuuidmajor.txt", 'w')
    majortxtw.write(content[37:42])
    majortxtw.close()  # Close the file to avoid confusion
    # minor
    minortxtw = open("static/newuuidminor.txt", 'w')
    minortxtw.write(content[43:48])
    minortxtw.close()  # Close the file to avoid confusion
    #to allow for debugging
    print(open("static/uuid.txt", "r").readlines())
    return True

def txtuuid():
    '''
    Replacement function for csvuuid - simplifies UUID distrubution process
    :return: True/False (Boolean)
    '''
    #readingprevioussuuid
    uuidtxtr = open("static/uuid.txt",'r')
    lastuuid = uuidtxtr.readlines()[-1] #uuidtxt.readlines returns a list of strings. Choosing the last line
    uuidtxtr.close() #Closing the file. Not needed anymore
    print(lastuuid) #printing old UUID
    newuuid = uuidincrease(lastuuid) #printing new uuid
    #Writing new uuid to txt
    txtclearandupdate(newuuid)
    return newuuid

def newtxtuuid():
    '''
    Replacement function for csvuuid - simplifies UUID distrubution process
    :return: True/False (Boolean)
    '''
    #readingprevioussuuid
    #uuid
    uuidtxtr = open("static/uuid.txt",'r')
    lastuuid = uuidtxtr.readlines()[-1] #uuidtxt.readlines returns a list of strings. Choosing the last line
    uuidtxtr.close() #Closing the file. Not needed anymore
    #major
    majortxtr = open("static/newuuidmajor.txt",'r')
    lastmajor = majortxtr.readlines()[-1] #uuidtxt.readlines returns a list of strings. Choosing the last line
    majortxtr.close()
    #minor
    minortxtr = open("static/newuuidminor.txt", 'r')
    lastminor = minortxtr.readlines()[-1]  # uuidtxt.readlines returns a list of strings. Choosing the last line
    minortxtr.close()


    print(lastuuid+lastmajor+lastminor) #printing old UUID
    newuuid = new_uuid_major_minor_increase(lastuuid,lastmajor,lastminor) #printing new uuid
    #Writing new uuid to txt
    new_txtclearandupdate(newuuid)
    return newuuid

#API
app = Flask(__name__)

#route http post to this method
@app.route('/api/upload',methods=['POST'])
def upload():
    #retrieving upload data from client
    updatedata = request.files['hello'].read()
    updatedata = str(updatedata, 'utf-8')
    print(updatedata)
    updatedata = updatedata.split(",")
    patientdata = update(updatedata)
    csvUpdater(patientdata)

    return Response(response="static/cases.csv",status=200,mimetype="application/json")

@app.route('/')
def home():
    return Response(response="hello",status=200,mimetype="application/json")

@app.route('/uuid/new')
def uuidnew():
    #csvuuid(
    newuuid = txtuuid()
    return Response(response=newuuid,status=200,mimetype="application/json")

def create_app(config_filename):
    app = Flask(__name__)
    app.config.from_pyfile(config_filename)
    return app

#app = create_app(app)
@app.route('/uuid/officalnew')
def newuuidnew():
    newuuid = newtxtuuid()
    return Response(response=newuuid, status=200, mimetype="application/json")

if __name__ == "__main__":
  app.run(debug=False)

#start server
#app.run(host="0.0.0.0",port=5000,debug=True)
