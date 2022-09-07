# -*- coding: utf-8 -*-
"""
Created on Sat Nov 27 14:16:32 2021

@author: Karlz
"""
import insGenerateFunctions as gf
import dill

# FILENAME_INSTANCES = 'test1_5.csv'
MAXLAUNCH = 2
M = 10000; # BIG ENOUGH NUMBER
EXPORTFILE = 'testinstance.pkl'

#-------Gain the Raw Data of Order and Node position------ 
# NODES = gf.getCsvData(FILENAME_INSTANCES).values  
NODES = nODES


#-------Generate Proper Feasible Sets From Node----------
#Classify Nodes
[Nc,R,Rd,resupplyNodeSet,Su,SEnode]\
    = gf.classification(NODES)

ts = SEnode[0][0];te = SEnode[0][1];ds = SEnode[1][0];de = SEnode[1][1]
N = Nc|R;Nse = N|{ts,te}

#Generate Launch Nodes
[exNODES,L,head,samelocation,Lmap,Rmap]\
    = gf.extendNODES(NODES,Rd,MAXLAUNCH,SEnode)

Lse = L|{ds,de}

#Generate drone and truck object, calucate lenthMatrix
de1 = gf.drone(2,15)
tk1 = gf.truck(1, M)

Td = gf.calVehicleMatrix(de1,exNODES)    
Tt = gf.calVehicleMatrix(tk1,exNODES)
Td[:,de] = 0

#Generate Curial Set base on the constrain of Drones' range
[Ll,S,Lt] = de1.cal_LS(Td,Rd,Su,L,ds)
A = Nse|Lse|Su

#-----------------------------------------

fd = gf.orderpair(exNODES)
timewindows = gf.timewindows(exNODES)

#--------------------------------------

import numpy as np
aa = np.zeros(len(A))
bb = np.zeros(len(A))
for i in R:
    aa[i] = timewindows[i]
for i in Nc:
    bb[i] = timewindows[i]
    
#--------------------------------------