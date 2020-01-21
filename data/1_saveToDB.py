#import pymysql
import mysql.connector
import math
import os

def main():
    #db = pymysql.connect("localhost", "root", "19980312", "hyper_simulation")
    db = mysql.connector.connect(host="localhost", user="hangzhi", passwd="hangzhi", database="hyper_simulation")
    mycursor = db.cursor()

    inputFiles = input("input file number: ").split(',')
    numTime = input("start and end of times: ").split(',')
    matrixNum = input("matrix: ").split(",")

    for m in matrixNum:
        for i in range(int(inputFiles[0]), int(inputFiles[1]) + 1):
            countVal, componentVal, curMatrix, inputFile = [], [], "", "in" + str(i) + ".conf"
            for t in range(int(numTime[0]), int(numTime[1]) + 1):
                fitnessVal = []
                fileName = "../out/out" +m+"_"+ str(i) + "_" + str(t) + ".txt"
                if not os.path.exists(fileName): continue
                print(fileName)
                f = open(fileName,"r")
                lines = f.readlines()
                for l in lines:
                    firstChar = l.split("\t")[0]
                    if firstChar == "T":
                        curMatrix = l.split("\t")[1].replace("\n", "")
                    elif firstChar == "C":
                        size = int(l.split("\t")[1])
                        index = 0
                        for c in range(0, size):
                            componentVal.append((t, inputFile, curMatrix, c, index))
                            index += 1
                    elif firstChar == "L":
                        iteration, config, fitness = l.split("\t")[1:]
                        if math.isnan(float(fitness)): fitness = 0
                        fitnessVal.append((t, inputFile, curMatrix, int(iteration), float(fitness), config, -1, -1))
                    elif firstChar == "N":
                        firmId, countExp, countAdd, countDrop, countBorrow, countSwitch, finalFitness = l.split("\t")[1:]
                        if math.isnan(float(finalFitness)): finalFitness = 0
                        countVal.append((
                                        t, inputFile, curMatrix, int(countExp), int(countAdd), int(countDrop), int(countBorrow),
                                        int(countSwitch), int(firmId), float(finalFitness)))
                    else:
                        iteration, firmId, rank, config, fitness = l.split("\t")
                        if math.isnan(float(fitness)): fitness = 0
                        fitnessVal.append(
                            (t, inputFile, curMatrix, int(iteration), float(fitness), config, int(firmId), int(rank)))

                fitnessSql = "INSERT INTO fitness (times, inputFile, matrix, iteration, fitness, fitnessConfig, firmId, firmRank) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
                mycursor.executemany(fitnessSql, fitnessVal)
                os.remove(fileName)
                db.commit()
                f.close()

            countSql = "INSERT INTO count (times, inputFile, matrix, countExp, countAdd, countDrop, countBorrow, countSwitch, firmId, finalFitness) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"
            componentSql = "INSERT INTO component (times, inputFile, matrix, size, CIndex) VALUES (%s, %s, %s, %s, %s)"
            mycursor.executemany(countSql, countVal)
            mycursor.executemany(componentSql, componentVal)
            db.commit()

if __name__ == '__main__':
    main()