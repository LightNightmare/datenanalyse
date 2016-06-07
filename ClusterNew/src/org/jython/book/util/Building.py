#!/usr/bin/python2
# A python module that implements a Java interface to
# create a building object

from org.jython.book.interfaces import BuildingType

public_url='http://skyserver.sdss3.org/public/en/tools/search/x_sql.aspx'

default_url=public_url
default_fmt='csv'

    
class Building(BuildingType):
    def __init__(self, query):

        self.query = query


    
    def getQueryRes(self):
        return self.query
    
    def params(self):
        return ''
    
    def queryEx(self, url=default_url):
        return ''
    