# -*- coding: utf-8 -*-
"""
Created on Mon November 26 2021

@author: Karlz
"""

import os
import sys
import shutil
import time
import numpy as np
import pandas as pd
import xlwings as xw
import matplotlib.pyplot as plt
import math

    
def getExcelData(excelLocation, sheet_name=0, index_col=0):
    data = pd.read_excel(excelLocation, sheet_name, header=0, index_col=index_col)
    return data

def getCsvData(csvLocation, sheet_name=0):
    data = pd.read_csv(csvLocation)
    return data

def verifyNODES(NODES):
    n = NODES.shape[1] #the number of nodes
    customerNo = sum(NODES[:,2]==201) + sum(NODES[:,2]==2)
    restaurantNo = sum(NODES[:,2]==3) + sum(NODES[:,2]==4)
    return customerNo == restaurantNo 

def calNorm2(position1,position2):
    return np.around(math.sqrt((position1[0]-position2[0])*(position1[0]-position2[0])+\
    (position1[1]-position2[1])*(position1[1]-position2[1])),decimals=3)

def calNorm1(position1,position2):
    return np.around(abs((position1[0]-position2[0])) + abs((position1[1]-position2[1])),decimals=3)

def classify(node):
    if node[2]  == 201 or node[2]  == 202:
        c1 = 'customer'
    elif node[2]  == 101 or node[2]  == 102 or node[2]  == 103:
        c1 = 'restaurant'
    elif node[2] == 301:
        c1 = 'resupplyonly'
    elif node[2]  == -2001:
        c1 = 'truckstart'
    elif node[2]  == -2002:
        c1 = 'truckend'
    elif node[2]  == -1001:
        c1 = 'dronestart'
    elif node[2]  == -1002:
        c1 = 'droneend'     
    elif node[2]  == 0:
        c1 = 'lauchnode'
    else:
        print('unknow classify value')
        c1 = 'unknow'
    
    if node[2]  == 103:
        c2 = 'dronebaseRestaurant'
    else:
        c2 = 'normal'
    
    if  node[2]  in {201, 101, 103, 301}:
        c3 = 'resuppliable'
    else:
        c3 = 'nonresuppliable'
    
    return c1,c2,c3
    

def classification(NODES):
    n = NODES.shape[0] #the number of nodes
    #initialize
    customerSet = set()
    restaurantSet = set()
    dronebaseResSet = set()
    resupplyNodeSet = set()
    ts = -1; te = -1; ds= -1; de = -1
    for i in range(n):
        if NODES[i,2] == 201 or NODES[i,2] == 202:
            customerSet.add(i)
        elif NODES[i,2] == 101 or NODES[i,2] == 102 or NODES[i,2] == 103:
            restaurantSet.add(i)
        elif NODES[i,2] == 301:
            resupplyNodeSet.add(i)
            
        elif NODES[i,2] == -1001:
            ds = i
        elif NODES[i,2] == -1002:
            de = i
        elif NODES[i,2] == -2001:
            ts = i
        elif NODES[i,2] == -2002:
            te = i        
            
        else:
            print('unknow classify value')
        if NODES[i,2] == 103:
            dronebaseResSet.add(i)
    SEnode = [[ts, te],[ds, de]]
    suppliableNodeSet = customerSet | restaurantSet | resupplyNodeSet
    return customerSet,restaurantSet,dronebaseResSet,resupplyNodeSet,suppliableNodeSet,SEnode

def extendNODES(NODES,dronebaseResSet,MAXLAUNCH,SEnode):
    res2launcher = {'restaurantNodes':'launcherNodes'}
    lac2restaurant = {'launcherNodes':'restaurantNodes'}
    samelocation = {'head':'all'}
    head = set()
    launcherSet = set()
    N = NODES.shape[0]
    exNODES = NODES
    
    dronebaseResSet_t = dronebaseResSet.copy()
    
    ## Find dronebaseNodes that are the same location in real
    # set : head
    # dic(map) : samelocation
    for i in dronebaseResSet:    # 这里需要多check一下
        temp =[]
        if i in dronebaseResSet_t:
            for j in dronebaseResSet:
                if calNorm1(NODES[i], NODES[j])==0:
                        temp.append(j) 
                        dronebaseResSet_t.remove(j)                
            samelocation[i] = temp
            head.add(i)
        
    ## Generate a mapping from dronebase restaurant to launcherNodes
    for i in head:
        
        #launcherNodes that belong to Node i
        launcher4i = np.arange(N, N+MAXLAUNCH, 1) 
        N = N+MAXLAUNCH
        
        # Add those Nodes to exNODES
        for j in launcher4i:
            exNODES = np.append(exNODES,[[NODES[i,0],NODES[i,1],0,-1,-1]],0)
            #exNODES.append([NODES[i,1],NODES[i,2],5])
            launcherSet.add(j)  
            #build the mapping of lac2restaurant
            lac2restaurant[j] = samelocation[i]
        
        #build the mapping of res2launcher
        for j in samelocation[i]:
            res2launcher[j] = launcher4i
        
           #?why I write code below?
        for j in (set(SEnode[0])|set(SEnode[1])):
            res2launcher[j] = [j]
            lac2restaurant[j] = [j]
                    
    return exNODES,launcherSet,head,samelocation,res2launcher,lac2restaurant


def plotMap(NODES): 
    n = NODES.shape[0] #the number of nodes
    for i in range(n):
        if NODES[i,2]==201:    #customer
            plt.scatter(NODES[i,0],NODES[i,1],color = 'b')
            
        if NODES[i,2]==101 or NODES[i,2]==102 or NODES[i,2]==103: #restaurant
            plt.scatter(NODES[i,0],NODES[i,1],color = 'r')
            
        if NODES[i,2]==301: #resupply only
            plt.scatter(NODES[i,0],NODES[i,1],color = 'g')
            
def calVehicleMatrix(vehicle,NODES):   
    #Notice: Here Should plugin exNODES instead of NODES
    M = 1000
    n = NODES.shape[0] #the number of nodes
    lenthMatrix = np.zeros([n,n])    
    for i in range(n):
        for j in range(n):
            lenthMatrix[i,j] = vehicle.calNorm(NODES[i], NODES[j])/vehicle.speed
        lenthMatrix[i,i] = M
        
    return lenthMatrix    


class vehicle:
    def __init__(self,speed,maxrange):
        self.speed = speed
        self.maxrange = maxrange
    def calNorm(self,NODES):
        print('not defined yet.')
        return -1
    
class drone(vehicle):
    def calNorm(self,position1,position2):       
        return calNorm2(position1,position2)
        
    def cal_LS(self,lenthMatrix,dronebaseResSet,suppliableNodeSet,launcherSet,ds):
        #calculate Lij and Si
        n = lenthMatrix.shape[0] #the number of nodes
        LL = [[set() for _ in range(n)] for _ in range (n)] #initialize LL matrix
        for i in dronebaseResSet:
            for j in suppliableNodeSet:
                leftrange = self.maxrange - lenthMatrix[i,j]
                # print('j:',j)
                # print('leftrange:',leftrange)
                for k in launcherSet:
                    if leftrange >= lenthMatrix[j,k]:
                        # print('k:',k)
                        # print('len:',lenthMatrix[j][k])
                        LL[i][j].add(k)
        SS = [set() for _ in range(n)] 
        for i in dronebaseResSet:
            for j in suppliableNodeSet:
                if bool(LL[i][j]):
                    SS[i].add(j)
        
        Lt = [set() for _ in range(n)]
        for i in (launcherSet|{ds}):  #notice that i conclude drone start point
            for j in launcherSet:
                if self.maxrange >= lenthMatrix[i,j]:
                    Lt[i].add(j)
        return LL, SS, Lt
    
    def test(self):
        print(self.speed)
        
            
class truck(vehicle):
    def calNorm(self,position1,position2): 
        ##return calNorm1(position1,position2)
        return calNorm1(position1,position2)


def orderpair(NODES):
    num = int(max(NODES[:,3]))
    res2cusMap = {}
    for i in range(num+1):
        son = np.where(NODES[:,3] == i)[0] #same order node
        #exam 
        if len(son) != 2:
            print('warning: multi-order-node or empty order')
        else:
            for j in son:
                if classify(NODES[j])[0] == 'restaurant':
                    r = j
                elif classify(NODES[j])[0] == 'customer':
                    c = j
                else:
                    print('unexpected class')
            res2cusMap[r] = c
    return res2cusMap
            

def timewindows(NODES):
    return NODES[:,4]
            
