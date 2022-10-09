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
            
# # Round version
# for i in A:
#     for j in A:
#         if abs(x[(i,j)].X-1)<1e-5:
#             #print('x:',i,'to:',j,':',round(x[(i,j)].X))
#             print('x:',i,'to:',j)

print('=====================')
for i in A:
    for j in A:
        if abs(u[(i,j)].X-1)<1e-5:
            #print('u:',i,'to:',j,':',round(u[(i,j)].X))
            print('u:',i,'to:',j)

# Sequential
cn = ts #current node: cn
s_r = 'courier route: '
s_r2 = 's_r2: '
while cn != te:
    for j in A:
        if abs(x[(cn,j)].X-1)<1e-5:
            s_r += (str(cn) + '[' + str(round(T[(cn)].X, 1)) + '] -> ')
            s_r2 += (str(cn) + ',')
            cn = j
s_r += str(cn) + '[' + str(round(T[(cn)].X, 1)) + ']'
s_r2 += str(cn)

# for drone
dn = ds #current node: cn
d_r = 'drone route: '
d_r2 = 'd_r2: '
while dn != de:
    old_dn = dn
    for j in A:
        if abs(u[(dn,j)].X-1)<1e-5:
            d_r += (str(dn) + ' -> ')
            d_r2 += (str(dn) + ',')
            dn = j
    if dn == old_dn:
        break
d_r += str(dn)
d_r2 += str(dn)


print(s_r)
print(s_r2)
print(d_r)
print(d_r2)

# model.computeIIS()
# model.write("model.ilp")

# for j in A:
#     if abs(x[(ts,j)].X-1)<1e-5:
#         print(round(x[(ts,j)]))

# for j in A:
#     print(x[(ts,j)])