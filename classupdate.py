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

