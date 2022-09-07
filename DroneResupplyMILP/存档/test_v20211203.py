# -*- coding: utf-8 -*-

import insGenerateFunctions as gf
import dill

FILENAME_INSTANCES = 'test1_5.csv'
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
[LL,S,Lt] = de1.cal_LS(lenthMatrix_d,dronebaseResSet,suppliableNodeSet,launcherSet)
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



"""
Created on Tue Nov 30 23:00:53 2021

@author: 19043
"""
import numpy as np
import pandas as pd
import gurobipy as gp
from gurobipy import GRB
from gurobipy import quicksum

import os


### test for feasiblibility
# 添加变量
SU = suppliableNodeSet
B = 1e5
test = gp.Model()
x1 = {}
Targettime1 = {}
Targettime2 = {}
T1 = {}
w1 = {}
u1 = {}
n = len(A)
#p = len(Nse|SU)

for i in range(n):
    Targettime1[i] = test.addVar(vtype = GRB.CONTINUOUS, name = "TT%f"%i)
    Targettime2[i] = test.addVar(vtype = GRB.CONTINUOUS, name = "TT%f"%i)
    T1[i] = test.addVar(vtype = GRB.CONTINUOUS, name = "T%f"%i)
    w1[i] = test.addVar(vtype = GRB.CONTINUOUS)
    u1[i] = test.addVar(vtype = GRB.INTEGER, name = "U%i"%i )
    for j in range(n):
        x1[i, j] = test.addVar(vtype = GRB.BINARY, name = "x(%d,%d)" % (i, j))
        
        
# 约束开始

### Truck routing constraints
# 起始点和终点
test.addConstr(quicksum(x1[(ts,j)] for j in N|SU) == 1)
test.addConstr(quicksum(x1[(j,ts)] for j in A) == 0)
test.addConstr(quicksum(x1[(ts,j)] for j in A) == 1)
test.addConstr(quicksum(x1[(j,te)] for j in N|SU) == 1)
test.addConstr(quicksum(x1[(te,j)] for j in A) == 0)
test.addConstr(quicksum(x1[(j,te)] for j in A) == 1)

#test.addConstr(quicksum(x1[(ts,j)] for j in N|SU) == 1)
#test.addConstr(quicksum(x1[(j,ts)] for j in N|SU) == 0)
#test.addConstr(quicksum(x1[(ts,j)] for j in A) == 1)
#test.addConstr(quicksum(x1[(j,te)] for j in N|SU) == 1)
#test.addConstr(quicksum(x1[(te,j)] for j in N|SU) == 0)
#test.addConstr(quicksum(x1[(j,te)] for j in A) == 1)


# 不成环的约束
p = len(SU)
for i in N|SU:
    for j in N|SU:
        if j != i:
            test.addConstr(u1[i] - u1[j] + p*x1[(i,j)] <= (p-1))
            

# 不能自己到自己
for i in A:
    test.addConstr(x1[(i,i)] == 0)
    
    #test.addConstr(quicksum(x1[(i,j)] for j in Lse) ==0 )

# 一进一出的约束    
for j in N|SU:
    test.addConstr(quicksum(x1[(i,j)] for i in A) == quicksum(x1[(j,i)] for i in A))
    test.addConstr(quicksum(x1[(i,j)] for i in A) <= 1)
test.update()

# truck的非可行点不可进出
Artset = set.difference(A,Nse|SU)
for i in Artset:
    for j in A:
        test.addConstr(x1[(i,j)] == 0)
        test.addConstr(x1[(j,i)] == 0)

### Synchronization & Time Constraints
for i in range(n):
    test.addConstr(w1[i] >= 0)
    
test.addConstr(T1[ts] == 0)


for i in Nse:
    for j in Nse:
        test.addConstr(T1[i]+w1[i]+Tt[i][j]-B*(1-x1[(i,j)]) <= T1[j])

### Pickup & delivery constraints
for j in N:
    test.addConstr(quicksum(x1[(i,j)] for i in Nse) == 1)

for i in R:
    test.addConstr(T1[i] <= T1[fd[i]])
    
for i in R:
    test.addConstr(quicksum(x1[(j,i)]*T1[i] for j in Nse|SU) >= aa[i])
    

### 构造目标函数并求解
for i in range(n):
    test.addConstr(Targettime1[i] == (T1[i]-bb[i]))
    test.addGenConstrMax(Targettime2[i],[Targettime1[i],0])

alpha = 1
test.setObjective(alpha*quicksum(Targettime2[i] for i in Nc)+quicksum(T1[i] for i in Nc),GRB.MINIMIZE)
test.update()
test.optimize()
    
#for i in range(n):
#    test.addGenConstrMax(Targettime1[i],[T1[i],bb[i]])
#test.update()
#
#
#alpha = 1
#test.setObjective(alpha*quicksum((Targettime1[i] - bb[i]) for i in Nc)+quicksum(T1[i] for i in Nc),GRB.MINIMIZE)
#test.update()
#test.optimize()
