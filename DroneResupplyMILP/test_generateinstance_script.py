# -*- coding: utf-8 -*-
"""
Created on Sat Nov 27 14:16:32 2021

@author: Karlz
"""
import insGenerateFunctions as gf
import dill

FILENAME_INSTANCES = 'test1_4.csv'
MAXLAUNCH = 3
M = 10000; # BIG ENOUGH NUMBER
EXPORTFILE = 'testinstance.pkl'

# TEST 
NODES = gf.getCsvData(FILENAME_INSTANCES).values  


#-------------------------------------------
[customerSet,restaurantSet,dronebaseResSet,resupplyNodeSet,suppliableNodeSet,SEnode]\
    = gf.classification(NODES)
#S: suppliableNodeSet
# SEnode = [[ts, te],[ds, de]]

ts = SEnode[0][0]
te = SEnode[0][1]
ds = SEnode[1][0]
de = SEnode[1][1]

Nc = customerSet
R = restaurantSet
SU = suppliableNodeSet
Rd = dronebaseResSet

N = Nc|R
Nse = N|{ts,te}


#-------------------------------------------
[exNODES,launcherSet,head,samelocation,res2launcher,lac2restaurant]\
    = gf.extendNODES(NODES,dronebaseResSet,MAXLAUNCH,SEnode)

Lse = launcherSet|{ds,de}
Lmap = res2launcher
Rmap = lac2restaurant


#-------------------------------------------
de1 = gf.drone(2,4.5)
lenthMatrix_d = gf.calVehicleMatrix(de1,exNODES)    
tk1 = gf.truck(1, M)
lenthMatrix_t = gf.calVehicleMatrix(tk1, exNODES)

Td = lenthMatrix_d
Tt = lenthMatrix_t


#-------------------------------------------
[LL,S,Lt] = de1.cal_LS(lenthMatrix_d,dronebaseResSet,suppliableNodeSet,launcherSet,ds)
Ll = LL
A = Nse|Lse|SU
#Lij: ll[i][j]
#Si: ss[i]

#-----------------------------------------


#Nc:
#Nr:
#di: res2cusMap[i]
#ai/bi: timewindows[i]
res2cusMap = gf.orderpair(exNODES)
timewindows = gf.timewindows(exNODES)
fd = res2cusMap

#--------------------------------------

import numpy as np
aa = np.zeros(len(A))
bb = np.zeros(len(A))
for i in R:
    aa[i] = timewindows[i]
for i in Nc:
    bb[i] = timewindows[i]
    

