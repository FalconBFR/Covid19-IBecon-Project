# Covid19-IBecon-Project
Both Android and IOS phones are able to act as I-Beacon Transmitters. Every single citizen will install this app onto their phones. A database will be created on each phone locally for cost contacts with the fields : 'I-Beacon ID, Date, Time, Signal Strength, Signal Duration'. If someone is to be infected and confirmed, the I-Beacon ID will be recorded onto the server. All apps from the phone will check with the server and compare those lists of IDs with the close contact I-Beacon IDs. If a person is deemed a close contact, the date and time of contact will be uploaded to the server database as close contact, ring 1. All phones will also compare their list of close contacts with the 'close contact database' on the server to find close contact 1, 2 , 3, and so on in no time to prevent further spread. This app will NOT record the location of citizens and thus there will be no worry of security issues. Idea by a 14 Years Old Boy living in HKSAR, China. Ti

Branches:
Android App V1: Android Studio Java Android App Project
csvmanagement: Python Flask Server code 

Notes on limitations:
Current hosting is not ideal - it is not hosted on a proper nginx/whatever server but instead on the flask development server. It will sometimes crash. Need to fix that
Confirmed Patients must be imported in the correct format or it will causes crashes on the phone - 
            solution(s): 
                1: Add tolerence on App
                2: Add checking when doing csv input through api
