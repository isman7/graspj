#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os



pathDir = "/media/DATA/ICFO/git/graspj/graspj/"

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
            
            lstFile.append(objFile.read())
        
        
        dicFiles[key] = lstFile