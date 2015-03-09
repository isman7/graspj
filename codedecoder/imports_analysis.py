#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os

import StringIO

pathDir = "/media/DATA/ICFO/git/graspj/graspj"

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
       
        
for pathFile, lstFile in dicFiles.iteritems():
    
    print lstFile[0]+lstFile[1]
    if (extension == ".java" or extension == ".class"): 
        
        readedLine = lstFile[2].readline()

        lstImports = []
        
        if "package" in readedLine:
            
            key = readedLine.split("package ")[1].split(';')[0]
            
        elif "import" in readedLine:
            
            lstImports.append(readedLine.split("import ")[1].split(';')[0])
            
        if key in imports_graph: 
            
            lstImportsNew = imports_graph[key] + lstImports
            imports_graph[key] = lstImportsNew
            
        else: 
            
            imports_graph[key] = lstImports            
        
        
        
        





        