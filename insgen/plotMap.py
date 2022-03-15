# -*- coding: utf-8 -*-
"""
Created on Sun Nov 28 13:00:35 2021

@author: Karlz
"""

import matplotlib.pyplot as plt
def plotMap(NODES,suppliableNodeSet,customerSet,restaurantSet,dronebaseResSet,SEnode):
    # 创建画布
    fig = plt.figure()
    ax = fig.add_subplot(111)
    ax.dpi = 300
    
    # Xmax = max(NODES[:,0])
    # Ymax = max(NODES[:,1])
    Xmax = 26
    Ymax = 26
    ax.set_xlim(xmin = -2);ax.set_xlim(xmax = Xmax)
    ax.set_ylim(ymin = -2);ax.set_ylim(ymax = Ymax)
    dy = Ymax/50
    dx = Xmax/50
    NODES[:,3] = NODES[:,3].astype(int)
    
    ts = SEnode[0][0]
    te = SEnode[0][1]
    ds = SEnode[1][0]
    de = SEnode[1][1]
    for i in range(len(NODES)):
        ax.scatter(NODES[i,0],NODES[i,1],color='k',marker='.')
        if i == ds:
            ax.text(NODES[i,0]-2*dx,NODES[i,1]+1*dy, 'ds')
        if i == de:
            ax.text(NODES[i,0]-4*dx,NODES[i,1]+1*dy, 'de')
        if i == ts:
            ax.text(NODES[i,0]-2*dx,NODES[i,1]-1*dy, 'ts')
        if i == te:
            ax.text(NODES[i,0]-4*dx,NODES[i,1]-1*dy, 'te')
    
    
    
    for i in suppliableNodeSet:
        ax.scatter(NODES[i,0],NODES[i,1],color='k',marker='.')
    for i in customerSet:
        ax.scatter(NODES[i,0],NODES[i,1],color = 'b')
        ax.text(NODES[i,0]+dx,NODES[i,1]-1.5*dy, NODES[i,3].astype(int))
        # ax.text(NODES[i,0]+dx,NODES[i,1]+dy, i,color='b')
    for i in restaurantSet:
        ax.scatter(NODES[i,0],NODES[i,1],color = 'r')
        ax.text(NODES[i,0]+dx,NODES[i,1]-1.5*dy, NODES[i,3].astype(int))
        # ax.text(NODES[i,0]+dx,NODES[i,1]+dy, i,color='b')
    for i in dronebaseResSet:
        ax.scatter(NODES[i,0],NODES[i,1],color = 'k',marker='*')
    return fig,ax


def drawArrow(nmap, A, B, acolor):
    nmap.annotate("", xy=(B[0], B[1]), xytext=(A[0], A[1]),arrowprops=dict(arrowstyle="->",color = acolor))    
def drawLine(nmap,A, B, acolor):
    #nmap.arrow(A[0],B[0],A[1]-A[0],B[1]-B[0],color = acolor)
    nmap.arrow(A[0],A[1],B[0]-A[0],B[1]-A[1],color = acolor)
         

[fig,nmap] = plotMap(exNODES,Su,Nc,R,Rd,SEnode)
nmap.dpi = 300
for i in A:
    for j in A:
        if j in set(SEnode[0]):
            pass
        else:    
            if x[(i,j)].X > 0.9:   # 决策变量 x == 1
                a = [exNODES[i,0],exNODES[i,1]]
                ab = [exNODES[j,0],exNODES[i,1]]
                acolor = 'black'
                if abs(exNODES[i,1] - exNODES[j,1])>0.1:
                    drawLine(nmap,a,ab, acolor)
                    drawArrow(nmap,a,ab, acolor)
                else:
                    print('aaaaaaa:',i,j)
                    drawLine(nmap,a,ab, acolor)
                    drawArrow(nmap, a, ab, acolor)

                ab = [exNODES[j,0],exNODES[i,1]]
                b = [exNODES[j,0],exNODES[j,1]]
                drawArrow(nmap, ab, b, acolor)
                drawLine(nmap,ab,b, acolor)
            if u[(i,j)].X > 0.9:
                if i in R:
                    acolor = 'red'
                    a = [exNODES[i,0]+0.1,exNODES[i,1]]
                    b = [exNODES[j,0]+0.1,exNODES[j,1]]
                else:
                    acolor = 'blue'
                    a = [exNODES[i,0]-0.1,exNODES[i,1]]
                    b = [exNODES[j,0]-0.1,exNODES[j,1]]
                drawArrow(nmap, a, b, acolor)
            
fig.show()            
