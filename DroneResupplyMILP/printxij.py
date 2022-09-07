# -*- coding: utf-8 -*-
"""
Created on Tue Nov 30 23:02:33 2021

@author: 19043
"""
# # Accurate version
# for i in A:
#     for j in A:
#         if x[(i,j)].X==1:
#             print(x[(i,j)])

# for i in A:
#     for j in A:
#         if u[(i,j)].X==1:
#             print(u[(i,j)])
            
# Round version
for i in A:
    for j in A:
        if abs(x[(i,j)].X-1)<1e-5:
            #print('x:',i,'to:',j,':',round(x[(i,j)].X))
            print('x:',i,'to:',j)

print('=====================')
for i in A:
    for j in A:
        if abs(u[(i,j)].X-1)<1e-5:
            #print('u:',i,'to:',j,':',round(u[(i,j)].X))
            print('u:',i,'to:',j)




# for j in A:
#     if abs(x[(ts,j)].X-1)<1e-5:
#         print(round(x[(ts,j)]))

# for j in A:
#     print(x[(ts,j)])