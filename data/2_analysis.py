# import pymysql
pimport mysql.connector
import os

def main():
    # db = pymysql.connect("localhost", "root", "19980312", "hyper_simulation")
    db = mysql.connector.connect(host="localhost", user="hangzhi", passwd="hangzhi", database="hyper_simulation")
    cursor = db.cursor()
    inputFile = input("input file number: ").split(',')
    matrixNums = input("matrix number: ").split(',')
    iterationNum = int(input("number of iterations: "))
    numTimes = input("start and end of times: ").split(',')

    for matrixNum in matrixNums:
        dir = "t".join(numTimes) + "_m" + matrixNum + "_" + "i".join(inputFile)
        if not os.path.exists("result/" + dir):
            os.makedirs("result/" + dir)
        fitness.polyRegressionTime (cursor, inputFile, matrixNum, dir)
        # fitness.polyRegression(cursor, inputFile, matrixNum, numTimes, dir)
        fitness.firmSizeAndFitness(cursor,inputFile,matrixNum, numTimes, dir)
        fitness.performanceIteration(cursor,inputFile,matrixNum, iterationNum, numTimes, dir)
        fitness.avgRankChange(cursor, inputFile, matrixNum,iterationNum, numTimes, dir)
        count.countNum(cursor,inputFile,matrixNum, numTimes, dir)
        network.centralityAnalysis(cursor,inputFile,matrixNum, numTimes, dir)

    db.close()

if __name__ == '__main__':
    import fitness
    import count
    import network
    main()