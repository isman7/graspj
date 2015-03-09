#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os

import StringIO

pathDir = "/media/DATA/ICFO/git/graspj/graspj/src/"

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
 

imports_graph = {}
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
            
        
            
        imports_graph[key] = lstImports
        
        if lstFile[0] == 'LaunchPlugin':
            print key, lstImports, pathFile
            print imports_graph[key]
            
            
            
            
            
            
        
        
        





        