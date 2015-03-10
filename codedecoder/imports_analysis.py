#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os

import StringIO

from pylab import *

from networkx import *

import random



pathDir = "/media/DATA/ICFO/git/graspj/"

dicFiles = {}


lstDir = os.walk(pathDir)

for paths, dirs, files in lstDir:
    
    
    for singleFile in files:
        
        (nombreFichero, extension) = os.path.splitext(singleFile)
        
        pathFile = paths + '/' + singleFile
        key = (pathFile).split(pathDir)[1]
        lstFile = []
        lstFile.append(nombreFichero)
        lstFile.append(extension)
        
        if (extension == ".java" or extension == ".class"):
            
            objFile = open(paths + '/' + singleFile)            
            
            strIO = StringIO.StringIO()
            strIO.write(objFile.read())
            lstFile.append(strIO)
        
        
        dicFiles[key] = lstFile
 

importsGraph = {}
prueba = {}
        
for pathFile, lstFile in dicFiles.iteritems():
    
    #print lstFile    
    extension = lstFile[1]
    #print lstFile[0]+lstFile[1]
    if (extension == ".java"): 
        
        lstLines = lstFile[2].getvalue().split("\n")

        lstImports = []
        

        for readedLine in lstLines:

                
            #print readedLine        
        
            if "package " in readedLine:
            
            
                key = readedLine.split("package ")[1].split(';')[0] + '.' + lstFile[0]
            
            elif "import " in readedLine:
                prueba[lstFile[0]] = readedLine
                lstImports.append(readedLine.split("import ")[1].split(';')[0])
            
        
        if '"' not in key: 
            importsGraph[key] = lstImports
                    

positions = {}

#Trying an 16*16 array elem
ii = 0
jj = 0
      
java_c = 0
brede_c = 0
ij_c = 0
others_c = 0

        
codeGraph = Graph(importsGraph)            



node_colors = []

ij_theta_ref = {}
brede_theta_ref = {}
java_theta_ref = {}
others_theta_ref = {}
radius = 30

for node in codeGraph.nodes():
    
    lstLocations = node.split('.')
    
    if ("ij" or "ij3d") in lstLocations:
        
        node_colors.append('b')
        
        if lstLocations[1] not in ij_theta_ref: 
            ij_theta_ref[lstLocations[1]] = (float(ij_c)/15)*pi/2
            ij_c += 1        
        
                
        theta_ref = ij_theta_ref[lstLocations[1]] 
        positions[node] = (radius*cos(theta_ref) + 4*rand(), radius*sin(theta_ref) + 4*rand())       
        #theta = rand()
        #positions[node] = (1*radius*cos(theta), 1*radius*sin(theta))        
        
        
    elif "brede" in lstLocations: 
        
        node_colors.append('g')
        
        if lstLocations[3] not in brede_theta_ref: 
            brede_theta_ref[lstLocations[3]] = (float(brede_c)/10)*2*pi
            brede_c += 1        
        
                
        theta_ref = brede_theta_ref[lstLocations[3]] 
        positions[node] = (radius*cos(theta_ref)/2 + 4*rand(), radius*sin(theta_ref)/2 + 4*rand())       
        
        
#        positions[node] = (ii-8, jj-8)
#        
#        ii += 1    
#    
#        if ii == 16: 
#            ii = 0
#            jj += 1
        
    elif "java" in lstLocations:
        
        node_colors.append('r')
        
        if lstLocations[1] not in java_theta_ref: 
            java_theta_ref[lstLocations[1]] = (1 + 2*(float(java_c)/9))*pi/2
            java_c += 1
    
        theta_ref = java_theta_ref[lstLocations[1]]            
        # theta = rand()
        # positions[node] = (-1*radius*cos(theta), random.choice([1,-1])*radius*sin(theta))        
        positions[node] = (radius*cos(theta_ref) + 6*rand(), radius*sin(theta_ref) + 6*rand())       
        
    else: 
        
        node_colors.append('k')
        theta = rand()
        loc2 = lstLocations[0] + '.' + lstLocations[1]        
        if loc2 not in others_theta_ref: 
            others_theta_ref[loc2] = (float(others_c)/30)*pi/2
            others_c += 1
    
        theta_ref = others_theta_ref[loc2]
        
        pos = (radius*cos(theta_ref), radius*sin(theta_ref) )
        pos_rot = ( pos[1] + 1*rand(), -pos[0] + 8*rand() )
        positions[node] = pos_rot

edge_colors = []
        
for edge in codeGraph.edges():
    
    lstOrig = edge[0].split('.')
    lstDest = edge[1].split('.')
    
    if (len(lstOrig) > 2) and (len(lstDest) > 2): 
        if (lstOrig[1] == 'brede') and (lstDest[1] == 'brede'):
            edge_colors.append('g')
            
        elif (lstOrig[1] == 'brede') and (lstDest[0] == 'java'):
            edge_colors.append('y')
            
        elif (lstOrig[1] == 'brede') and (lstDest[0] == 'ij'):
            edge_colors.append('y')    
            
        else: 
            edge_colors.append('y')
            
    else:
        edge_colors.append('y')

figure(100)

draw(codeGraph, positions, 
     node_color=node_colors, 
     node_size = 30, 
     font_size = 10,
     edge_color = edge_colors,
     arrows = True,
     with_labels = False)      


write_gexf(codeGraph, './codeGraph.gexf')
        
        





        