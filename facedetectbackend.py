import os.path
import numpy as np
import cv2
from flask import Flask,request,Response
import uuid
import json
import csv
import classupdate

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
    patientdata = classupdate.update(updatedata)
    csvUpdater(patientdata)

    return Response(response="hello",status=200,mimetype="application/json")


if __name__ == "__main__":
  app.run(debug=True)

#start server
app.run(host="0.0.0.0",port=5000,debug=True)
