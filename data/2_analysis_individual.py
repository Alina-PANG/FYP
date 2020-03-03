# import pymysql
import mysql.connector
import os

def main():
    # db = pymysql.connect("localhost", "root", "19980312", "hyper_simulation")
    db = mysql.connector.connect(host="localhost", user="hangzhi", passwd="hangzhi", database="hyper_simulation")
    cursor = db.cursor()
    inputFile = input("input file number: ").split(',')
    matrixNums = input("matrix number: ").split(',')
    iterationNum = 100
    numTimes = ['1','50']

    dict = {"poly":[], "firmSize":[], "performance":[], "rank":[], "coun":[], "network":[]}
    for matrixNum in matrixNums:
        for i in range(int(inputFile[0]), int(inputFile[1]) + 1):
            dir = matrixNum + "_" + str(i)
            if not os.path.exists("result/" + dir):
                os.makedirs("result/" + dir)
            print(dir)

            # fitness.polyRegression (cursor, str(i), matrixNum,numTimes,dir)
            # fitness.firmSizeAndFitness(cursor,str(i),matrixNum, numTimes, dir)
            # fitness.performanceIteration(cursor,str(i),matrixNum, iterationNum, numTimes, dir)
            # fitness.avgRankChange(cursor, str(i), matrixNum,iterationNum, numTimes, dir)
            count.countNum(cursor,str(i),matrixNum, numTimes, dir)
            # network.centralityAnalysis(cursor, str(i), matrixNum, numTimes, dir)

    print(dict)

    db.close()

if __name__ == '__main__':
    import fitness_individual as fitness
    import count_individual as count
    import network_individual as network
    main()