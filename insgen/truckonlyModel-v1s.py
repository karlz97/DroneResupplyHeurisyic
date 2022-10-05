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
    # TD[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "TD%f"%i)
    Targettime1[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "TT1%f"%i)
    Targettime2[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "TT2%f"%i)
    w[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "w%d"%i)
    # wd[i] = model.addVar(vtype = GRB.CONTINUOUS, name = "wd%d"%i)
    al_x[i] = model.addVar(vtype = GRB.INTEGER, name = "al_x%i"%i )
    # al_u[i] = model.addVar(vtype = GRB.INTEGER, name = "al_u%i"%i )
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

# # ONLY FOR DEBUG, Specify a route
# Xseq = []
# routeSeq = [12,0,1,4,6,9,2,7,5,3,8,13]
# routeSeq = [12,2,7,1,4,6,3,9,8,0,5,13]
# for i in range(len(routeSeq)-1):
#     Xseq.append((routeSeq[i],routeSeq[i+1]))
# model.addConstrs(x[(i,j)] == 1 for (i,j) in Xseq)


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

#########################################  
## fix drone route instance ##
# model.addConstr(u[(10,17)] == 1)  
# model.addConstr(u[(17,2)] == 1)
# model.addConstr(u[(2,8)] == 1)
# model.addConstr(u[(8,18)] == 1)
# model.addConstr(u[(18,11)] == 1)
#########################################

# model.addConstr(quicksum(u[(ds,j)] for j in Lt[ds]) == 1)
# model.addConstr(quicksum(u[(ds,j)] for j in A) == 1)
# model.addConstr(quicksum(u[(j,ds)] for j in A) == 0)

# model.addConstr(quicksum(u[(de,j)] for j in A) == 0)
# model.addConstr(quicksum(u[(j,de)] for j in Lse-{ds}-{de}) == 1)
# model.addConstr(quicksum(u[(j,de)] for j in A) == 1)


# for j in A-{ds,de}:
#     model.addConstr(quicksum(u[(i,j)] for i in A) == quicksum(u[(j,i)] for i in A))
#     model.addConstr(quicksum(u[(i,j)] for i in A) <= 1)

# for i in A:
#     model.addConstr(u[(i,i)] == 0)

# # 无人机在i∈R_d取订单，补货到j∈S_i处后，受限于航程，可行的降落点集合为L_ij
# for i in Rd:
#     for j in S[i]:
#         model.addConstr(quicksum(u[(j,k)] for k in Ll[i][j]) - u[(i,j)] >= 0)
##freeze
## for i in Rd:
##     for j in S[i]:
##         Infeasibleset = set.difference(A,Ll[i][j])
##         model.addConstr(quicksum(u[(j,k)] for k in Infeasibleset) == 0)



# 无人机从i ∈L出发，只能去对应的R_i，以及航程范围内的其他可行发射基地L ̂_i
# for i in Lse:
#     Feasibleset = set.union(set.union(Lt[i],set(Rmap[i])),{de})
#     Infeasibleset = set.difference(A,Feasibleset)
#     model.addConstr(quicksum(u[(i,j)] for j in Infeasibleset) == 0)
    
    

# 无人机在i∈Rd处取订单后，受限于航程只能补货去S_i
## v2    
# for i in Rd:
#     Infeasibleset = set.difference(A,S[i])
#     for j in Infeasibleset:
#         model.addConstr(u[(i,j)] == 0)




# 不成环的约束
# pd = len(A)
# for i in A:
#     for j in A:
#         if i != j:
#             model.addConstr(al_u[i] - al_u[j] +pd*u[(i,j)] <= (pd-1))

# model.update()


# add constraints concerning time
model.addConstr(T[ts] == 0)
# model.addConstr(TD[ds] == 0)

B = 1e4

##########################################  
## fix route instance ##
# model.addConstr(wd[0] == 0)  
# model.addConstr(wd[1] == 1)
# model.addConstr(wd[2] == 1)
# model.addConstr(wd[3] == 1)
# model.addConstr(wd[4] == 1)
# model.addConstr(w[0] == 0)  
#########################################

# time for j to reach 
#计算truck到达j点的时间:
##v1
for i in Nse:
    for j in Nse:
        model.addConstr(T[i]+w[i]+Tt[i,j]-B*(1-x[(i,j)]) <= T[j])
        model.addConstr(T[i]+w[i]+Tt[i,j]+B*(1-x[(i,j)]) >= T[j])
for i in A:
    model.addConstr(w[i] >= 0)
    

# #计算drone到达j点的时间:
# for i in Su|Lse:
#     for j in Su|Lse:
#         model.addConstr(TD[i]+wd[i]+Td[i,j]-B*(1-u[(i,j)])<=TD[j])
#         model.addConstr(TD[i]+wd[i]+Td[i,j]+B*(1-u[(i,j)])>=TD[j])
# for i in A:
#     if i in Lse:
#         model.addConstr(wd[i] >= 0)
#     else:
#         model.addConstr(wd[i] == 0)




# Resupply时human courier必须比drone先到达，且需等待drone到达后才能离开去下一个节点
## v1
# for j in Su:
#     model.addConstr(T[j]+B*quicksum(u[(i,j)] for i in R) - TD[j] + 1 <= B)
#     model.addConstr(T[j]-B*quicksum(u[(i,j)] for i in R) - TD[j] + w[j] >= -B)
## v2
# for j in Su:
#     model.addConstr(T[j]+B*quicksum(u[(i,j)] for i in R) - TD[j] == B)
#     model.addConstr(T[j]-B*quicksum(u[(i,j)] for i in R) - TD[j] + w[j] == -B) 



# # add constraints for pickup and delivery
# 所有订单 仅可pickup/delivery一次 
for j in N:
    model.addConstr(quicksum(x[(i,j)] for i in Nse|Su) == 1)

# Truck在delivery之前必须先pick up 
for i in R:
    model.addConstr(T[i] <= T[fd[i]])

for j in A-{ds,de}:
    model.addConstr(B*quicksum(x[(i,j)] for i in A) >= T[j])


# 在必须备餐完毕才可取餐    
for i in R:
    model.addConstr(B-B*quicksum(x[(j,i)] for j in (Nse|Su)) + T[i] >= aa[i])


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

    
    
