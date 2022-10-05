# -*- coding: utf-8 -*-
"""
Created on Mon Oct  3 15:21:06 2022

@author: Karlz
"""
import pandas as pd
exNODES = pd.read_csv('exNODES.csv', header=None).to_numpy()
nODES = exNODES[exNODES[:,2] != 0]
L = set(pd.read_csv('set_L.csv', header=None).squeeze().unique())


Lse = set(pd.read_csv('set_Lse.csv', header=None).squeeze().unique()) 
Tt = pd.read_csv('Tt.csv', header=None).to_numpy()
#Td = pd.read_csv('Td_origin.csv').to_numpy()
#Td = set(pd.read_csv('Td_origin.csv').squeeze().unique()) 

