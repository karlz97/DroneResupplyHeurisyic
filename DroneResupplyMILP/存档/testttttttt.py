# -*- coding: utf-8 -*-
"""
Created on Sat Nov 27 14:16:32 2021

@author: Karlz
"""
import insGenerateFunctions as gf
import dill

FILENAME_INSTANCES = 'test0.csv'
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


### model for feasiblibility
# 添加变量
Su = suppliableNodeSet
B = 1e5
model = gp.Model()
x = {}
Targettime1 = {}
Targettime2 = {}
T = {}
w = {}
al_x = {}
n = len(A)
#p = len(Nse|Su)

for i in range(n):
    Targettime1[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "TT%f"%i)
    Targettime2[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "TT%f"%i)
    T[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "T%f"%i)
    w[i] = model.addVar(vtype = GRB.CONTINUOUS)
    al_x[i] = model.addVar(vtype = GRB.INTEGER, name = "u%i"%i )
    for j in range(n):
        x[i, j] = model.addVar(vtype = GRB.BINARY, name = "x(%d,%d)" % (i, j))
        
        
# 约束开始

### Truck routing constraints
# 起始点和终点
model.addConstr(quicksum(x[(ts,j)] for j in N|Su) == 1)
model.addConstr(quicksum(x[(j,ts)] for j in A) == 0)
model.addConstr(quicksum(x[(ts,j)] for j in A) == 1)
model.addConstr(quicksum(x[(j,te)] for j in N|Su) == 1)
model.addConstr(quicksum(x[(te,j)] for j in A) == 0)
model.addConstr(quicksum(x[(j,te)] for j in A) == 1)

# 不成环的约束
p = len(Su)
for i in N|Su:
    for j in N|Su:
        if j != i:
            model.addConstr(al_x[i] - al_x[j] + p*x[(i,j)] <= (p-1))
            

# 不能自己到自己
for i in A:
    model.addConstr(x[(i,i)] == 0)
    
    #model.addConstr(quicksum(x[(i,j)] for j in Lse) ==0 )

# 一进一出的约束    
for j in N|Su:
    model.addConstr(quicksum(x[(i,j)] for i in A) == quicksum(x[(j,i)] for i in A))
    model.addConstr(quicksum(x[(i,j)] for i in A) <= 1)

# truck的非可行点不可进出
Artset = set.difference(A,Nse|Su)
for i in Artset:
    for j in A:
        model.addConstr(x[(i,j)] == 0)
        model.addConstr(x[(j,i)] == 0)
        


### Synchronization & Time Constraints
for i in range(n):
    model.addConstr(w[i] >= 0)
    
model.addConstr(T[ts] == 0)


for i in Nse:
    for j in Nse:
        model.addConstr(T[i]+w[i]+Tt[i][j]-B*(1-x[(i,j)]) <= T[j])

### Pickup & delivery constraints [未完全同步]
for j in N:
    model.addConstr(quicksum(x[(i,j)] for i in (Nse|SU)) == 1)

for i in R:
    model.addConstr(T[i] <= T[fd[i]])
    
for i in R:
    model.addConstr(quicksum(x[(j,i)]*T[i] for j in Nse|Su) >= aa[i])

    
model.update()

### 构造目标函数并求解
for i in range(n):
    model.addConstr(Targettime1[i] == (T[i]-bb[i]))
    model.addGenConstrMax(Targettime2[i],[Targettime1[i],0])

alpha = 1
model.setObjective(alpha*quicksum(Targettime2[i] for i in Nc)+quicksum(T[i] for i in Nc),GRB.MINIMIZE)
model.update()
model.optimize()
    
#for i in range(n):
#    model.addGenConstrMax(Targettime1[i],[T[i],bb[i]])
#model.update()
#
#
#alpha = 1
#model.setObjective(alpha*quicksum((Targettime1[i] - bb[i]) for i in Nc)+quicksum(T[i] for i in Nc),GRB.MINIMIZE)
#model.update()
#model.optimize()
