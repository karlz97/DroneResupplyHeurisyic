# -*- coding: utf-8 -*-
"""
Created on Sat Nov 27 14:16:32 2021

@author: Karlz
"""
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
    
#--------------------------------------


    
# -*- coding: utf-8 -*-
"""
Created on Fri Nov 26 14:18:25 2021
@title: IE561 Project
@author: Fangyuan Li & Yinding Zhang
"""
import numpy as np
import gurobipy as gp
from gurobipy import GRB
from gurobipy import quicksum

# 添加变量
Su = suppliableNodeSet
B = 1e5
x = {}
u = {}
# avoid loop dummy variable
al_x = {}
al_u = {}
T = {}
TD = {}
w = {}
wd = {}
n = len(A)
Targettime1 = {}
Targettime2 = {}




## creat model and solve it.
model = gp.Model("IE561Project")
# add variables x_ij, u_ij and T_i to the model respectively
for i in range(n):
    T[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "T%f"%i)
    TD[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "TD%f"%i)
    Targettime1[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "TT1%f"%i)
    Targettime2[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "TT2%f"%i)
    w[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "w%d"%i)
    wd[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "wd%d"%i)
    al_x[i] = model.addVar(vtype = GRB.INTEGER, name = "al_x%i"%i )
    al_u[i] = model.addVar(vtype = GRB.INTEGER, name = "al_u%i"%i )
    for j in range(n):
        x[i, j] = model.addVar(vtype = GRB.BINARY, name = "x(%d,%d)" % (i, j))
        u[i, j] = model.addVar(vtype = GRB.BINARY, name = "u(%d,%d)" % (i, j))
model.update()


### add constraints for the courier

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

# add constraints for the drone

##########################################    
# model.addConstr(u[(17,2)] == 1)
# model.addConstr(u[(10,17)] == 1)
#########################################

model.addConstr(quicksum(u[(ds,j)] for j in Lt[ds]) == 1)
model.addConstr(quicksum(u[(ds,j)] for j in A) == 1)
model.addConstr(quicksum(u[(j,ds)] for j in A) == 0)

model.addConstr(quicksum(u[(de,j)] for j in A) == 0)
model.addConstr(quicksum(u[(j,de)] for j in Lse-{ds}-{de}) == 1)
model.addConstr(quicksum(u[(j,de)] for j in A) == 1)


for j in A-{ds,de}:
    model.addConstr(quicksum(u[(i,j)] for i in A) == quicksum(u[(j,i)] for i in A))
    model.addConstr(quicksum(u[(i,j)] for i in A) <= 1)

for i in A:
    model.addConstr(u[(i,i)] == 0)

for i in Rd:
    for j in S[i]:
        model.addConstr(quicksum(u[(j,k)] for k in Ll[i][j]) - u[(i,j)] == 0)

for i in Lse:
    Feasibleset = set.union(set.union(Lt[i],set(Rmap[i])),{de})
    Infeasibleset = set.difference(A,Feasibleset)
    model.addConstr(quicksum(u[(i,j)] for j in Infeasibleset) == 0)

for i in R:
    Feasibleset = set.difference(A,S[i])
    model.addConstr(quicksum(u[(i,j)] for j in Feasibleset) == 0)

################# freeze
#for i in Rd:
#    for j in S[i]:
#        Infeasibleset = set.difference(A,Ll[i][j])
#        model.addConstr(quicksum(u[(j,k)] for k in Infeasibleset) == 0)

NunionS = (N-{ds,de})|Su
pd = len(NunionS)
for i in NunionS:
    for j in NunionS:
        if i != j:
            model.addConstr(al_u[i] - al_u[j] +pd*u[(i,j)] <= (pd-1))

model.update()


# add constraints concerning time
model.addConstr(T[ts] == 0)
model.addConstr(TD[ds] == 0)

B = 1e4
# time for j to reach 
for i in Nse:
    for j in Nse:
        model.addConstr(T[i]+w[i]+Tt[i,j]-B*(1-x[(i,j)]) <= T[j])
for i in A:
    model.addConstr(w[i] >= 0)
    
for i in Su|Lse:
    for j in Su|Lse:
        model.addConstr(TD[i]+wd[i]+Td[i,j]-B*(1-u[(i,j)])<=TD[j])
for i in A:
    if i in Lse:
        model.addConstr(wd[i] >= 0)
    else:
        model.addConstr(wd[i] == 0)

# for j in Su:
#     model.addConstr(T[j]+B*quicksum(u[(i,j)] for i in R) - TD[j] <= B)
#     model.addConstr(T[j]+B*quicksum(u[(i,j)] for i in R) - TD[j] + w[j]>= B)

# # add constraints for pickup and delivery
for j in N:
    model.addConstr(quicksum(x[(i,j)] for i in Nse|Su) + quicksum(u[(i,j)] for i in Lse) == 1)

for i in R:
    model.addConstr(quicksum(u[(i,j)]*T[j] for j in S[i])+T[i] <= T[fd[i]])



for j in A-{ds,de}:
    model.addConstr(B*quicksum(x[(i,j)] for i in A) >= T[j])
model.update()
    
for i in R:
    model.addConstr(quicksum(x[(j,i)]*T[i] for j in N)+quicksum(u[(i,j)]*TD[i] for j in Lse) >= timewindows[i])


# add constraints to form the objective function

### 构造目标函数并求解
for i in range(n):
    model.addConstr(Targettime1[i] == (T[i]-bb[i]))
    model.addGenConstrMax(Targettime2[i],[Targettime1[i],0])

alpha = 1
model.setObjective(alpha*quicksum(Targettime2[i] for i in Nc)+quicksum(T[i] for i in Nc),GRB.MINIMIZE)
model.update()
model.optimize()

# model.computeIIS()
# model.write("model.ilp")

    
    
