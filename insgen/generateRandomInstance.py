# -*- coding: utf-8 -*-
"""
Created on Mon Dec  6 19:16:07 2021

@author: Karlz
"""
import os
import sys
import shutil
import time
import numpy as np
import random
import pandas as pd
import xlwings as xw 

droneNumber = 1
courierNumber = 1 
# orderNumber = 5
orderNumber = 5
droneBaseNumber = 2
supplynodeNumber = 0 
# mapLenth = 25
mapLenth = 20
# timewindosType = 'Dense'
timewindosType = 'Loose'

instanceName = 'RI_'+str(courierNumber)+str(droneNumber)\
    +'_o'+str(orderNumber)+'_b'+str(droneBaseNumber)+'_s'+str(supplynodeNumber)+'_'+timewindosType
FILENAME = instanceName 

seNodeNumber4d = 2*droneNumber 
seNodeNumber4t = 2*courierNumber

### functions
def generateOrderNode(number,droneBaseNumber,timewindosType):
    tempNODES = np.zeros([2*number,5])
    # Generate frist 4 columns 
    for i in range(number):
        tempNODES[i,0] = random.randint(0, mapLenth)
        tempNODES[i,1] = random.randint(0, mapLenth)
        tempNODES[i,2] = 101
        tempNODES[i,3] = i
        tempNODES[i+number,0] = random.randint(0, mapLenth)
        tempNODES[i+number,1] = random.randint(0, mapLenth)
        tempNODES[i+number,2] = 201
        tempNODES[i+number,3] = i
    # Generate Timewindows columns
    timegap = mapLenth
    tempTime = 0
    if timewindosType == 'Dense':
        timeInterval = 1
    if timewindosType == 'Loose':
        timeInterval = 10
    for i in range(number):
        tempNODES[i,4] = tempTime
        tempNODES[i+number,4] = tempTime + timegap
        tempTime = tempTime + timeInterval

    # Generate DroneBase Set
    droneBaseSet = set()
    while len(droneBaseSet) < droneBaseNumber:
        rand = random.randint(0, number-1)
        tempNODES[rand,2] = 103
        droneBaseSet.add(rand)
    return tempNODES,droneBaseSet


def generateSENode(droneNumber,courierNumber,droneBaseSet,NODES):
    tempNODES = np.zeros([2*(droneNumber + courierNumber),5])
    #find drone base
    droneBaseNumber = len(droneBaseSet)
    if droneBaseNumber == 0:  
        droneBaseNumber = 1
        snode = 0
        print('No drone base')
    else:    
        j = random.randint(0,droneBaseNumber-1)
        snode = list(droneBaseSet)[j]
    # drone start at a randomly picked from one of drone base
    for i in range(droneNumber):
        tempNODES[i,0] = NODES[snode,0]
        tempNODES[i,1] = NODES[snode,1]
        tempNODES[i,2] = -1001
        tempNODES[i,3] = -1
        tempNODES[i,4] = -1    
        tempNODES[i+droneNumber,0] = NODES[snode,0]
        tempNODES[i+droneNumber,1] = NODES[snode,1]
        tempNODES[i+droneNumber,2] = -1002
        tempNODES[i+droneNumber,3] = -1
        tempNODES[i+droneNumber,4] = -1
    # courier always start at (1,1) position
    for i in range(courierNumber):
        tempNODES[droneNumber*2+i,0] = 1
        tempNODES[droneNumber*2+i,1] = 1
        tempNODES[droneNumber*2+i,2] = -2001
        tempNODES[droneNumber*2+i,3] = -1
        tempNODES[droneNumber*2+i,4] = -1        
        tempNODES[droneNumber*2+courierNumber+i,0] = 1
        tempNODES[droneNumber*2+courierNumber+i,1] = 1
        tempNODES[droneNumber*2+courierNumber+i,2] = -2002
        tempNODES[droneNumber*2+courierNumber+i,3] = -1
        tempNODES[droneNumber*2+courierNumber+i,4] = -1
    return tempNODES
        

### generate orders
nodeNum = 2*orderNumber + supplynodeNumber + seNodeNumber4d + seNodeNumber4t
nODES = np.zeros([nodeNum,5])
[nODES[0:orderNumber*2,:],droneBaseSet] = \
    generateOrderNode(orderNumber,droneBaseNumber,timewindosType)
nODES[orderNumber*2:1+(orderNumber+droneNumber+courierNumber)*2,:] = \
    generateSENode(droneNumber,courierNumber,droneBaseSet,nODES)
#NODES[]


    
        
        
        
        
        
        
        
        
        
        
            
        
        

