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
            
        
            
        importsGraph[key] = lstImports
                    

positions = {}

#Trying an 16*16 array elem
ii = 0
jj = 0
      
   
figure(100)
        
codeGraph = Graph(importsGraph)            



node_colors = []


for node in codeGraph.nodes():
    
    lstLocations = node.split('.')
    
    if "ij" in lstLocations:
        
        node_colors.append('b')
        
        theta = rand()
        positions[node] = (1*24*cos(theta), 1*24*sin(theta))        
        
        
    elif "brede" in lstLocations: 
        
        node_colors.append('g')
        
        positions[node] = (ii-8, jj-8)
        
        ii += 1    
    
        if ii == 16: 
            ii = 0
            jj += 1
        
    elif "java" in lstLocations:
        
        node_colors.append('r')
        theta = rand()
        positions[node] = (-1*24*cos(theta), 1*24*sin(theta))        
        
        
    else: 
        
        node_colors.append('k')
        theta = rand()
                
        pos = (1*24*cos(theta), 1*24*sin(theta))
        pos_rot = ( pos[1], -pos[0] )
        positions[node] = pos_rot
        




draw(codeGraph, positions, node_color=node_colors, with_labels=False)           
            
        
        
        





        