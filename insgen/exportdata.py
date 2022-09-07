# -*- coding: utf-8 -*-
"""Output
Created on Mon Mar 14 15:04:56 2022

@author: Karlz
"""

import pandas as pd
pd.DataFrame(exNODES).to_csv('exNODES.csv' ,header=False ,index=False)
pd.DataFrame(L).to_csv('set_L.csv' ,header=False ,index=False)
pd.DataFrame(Lse).to_csv('set_Lse.csv' ,header=False ,index=False)
pd.DataFrame(exNODES).to_csv('exNODES.csv' ,header=False ,index=False)
pd.DataFrame(Tt).to_csv('Tt.csv' ,header=False ,index=False)
Td = Td - 1000 * np.eye(Td.shape[0])
pd.DataFrame(Td).to_csv('Td.csv' ,header=False ,index=False)
aaaa = pd.DataFrame(Ll)