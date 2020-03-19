# import pymysql
import mysql.connector
import pandas as pd
import numpy as np

def main():
    # db = pymysql.connect("localhost", "root", "19980312", "hyper_simulation")
    db = mysql.connector.connect(host="localhost", user="hangzhi", passwd="hangzhi", database="hyper_simulation")
    cursor = db.cursor()
    inputFiles = ['1','45']
    numTime = ['1','50']
    matrixNum = ['3','6','9','12','15']
    # print(">>> Fitness")
    # fitnessStats(inputFiles, numTime, matrixNum, cursor, db)
    print(">>> Rank")
    rankStats(inputFiles, numTime, matrixNum, cursor, db)
    print("job done!")

def rankStats(inputFiles, numTimes, matrixNums, cursor,db):
    iterationNum = 100
    for f in range(int(inputFiles[0]), int(inputFiles[1]) + 1):
        cursor.execute("SELECT times, firmId, iteration, firmRank, inputFile,matrix from fitness where firmId != -1 and inputFile = 'in"+str(f)+".conf'")
        df = pd.DataFrame(cursor.fetchall(),
                          columns=['times', 'firmId', 'iteration', 'firmRank', 'inputFile','matrix'])
        for matrixNum in matrixNums:
            print(matrixNum, f)
            val = []
            for j in range(int(numTimes[0]), int(numTimes[1]) + 1):
                # checksql = "SELECT count(*) from rankStats where times = "+int(j)+" and matrix = "+int(matrixNum) + " inputFile = in"+str(f)+".conf"
                # cursor.exeute(checksql)
                # if cursor.fetchall() != 0:
                #     continue
                print('r',matrixNum, f, j)
                firmDf = df.loc[(df.iteration == 0)& (df.times == j) & (df.matrix == str(matrixNum))].sort_values('firmId')
                previousRow = firmDf.firmRank.to_numpy()
                sumArr = np.full((50,),0)
                sumArr75 = np.full((50,),0)
                for i in range(1, iterationNum):
                    firmDf = df.loc[(df.iteration == i) & (df.times == j) & (df.matrix == str(matrixNum))].sort_values('firmId')
                    curRow = firmDf.firmRank.to_numpy()
                    minusResultRow = np.absolute(curRow - previousRow)
                    sumArr = np.add(minusResultRow, sumArr)
                    if i >= 75: sumArr75 = np.add(minusResultRow, sumArr75)
                val.append(list(getStats(matrixNum, f, j, sumArr) + getStats(matrixNum, f, j, sumArr75)[3:]))
            sql = "INSERT INTO rankStats (times, inputFile, matrix, meanstats, stdstats, minstats, maxstats, rangestats, mean75, std75, min75, max75, range75) VALUES (%s, %s, %s, %s, %s, %s, %s, %s,%s, %s,%s, %s,%s)"
            cursor.executemany(sql, val)
            db.commit()

def fitnessStats(inputFiles, numTime, matrixNum, cursor, db):
    sizeCalculation = []
    cursor.execute("SELECT inputFile, matrix, fitness, times, fitnessConfig, fSize from fitness where iteration = 99 and firmId != -1");
    df = pd.DataFrame(cursor.fetchall(), columns=['inputFile', 'matrix', 'fitness', 'times', 'fitnessConfig', 'size'])
    for index, row in df.iterrows():
        config = row['fitnessConfig']
        sizeCalculation.append(20 - config.count('.'))
    df['totalSize'] = sizeCalculation;
    df['borrowed'] = df["totalSize"] - df["size"]
    fitnessVal, sizeVal, tSizeVal, bSizeVal = [],[],[],[]
    for m in matrixNum:
        for i in range(int(inputFiles[0]), int(inputFiles[1]) + 1):
            for t in range(int(numTime[0]), int(numTime[1]) + 1):
                print('f',m,i,t)
                rows = df.loc[(df['inputFile'] == 'in' + str(i) + '.conf') & (df['matrix'] == m) & (df['times'] == t)]

                row_fitness = rows.fitness.to_numpy()
                fitnessVal.append(getStats(m, i, t, row_fitness))

                row_size = rows['size'].to_numpy()
                sizeVal.append(getStats(m, i, t, row_size))

                row_tsize = rows.totalSize.to_numpy()
                tSizeVal.append(getStats(m, i, t, row_tsize))

                row_bsize = rows.borrowed.to_numpy()
                bSizeVal.append(getStats(m, i, t, row_bsize))


    fitnessSql = "INSERT INTO fitnessStats (times, inputFile, matrix, meanstats, stdstats, minstats, maxstats, rangestats) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
    sizeSql = "INSERT INTO sizeStats (times, inputFile, matrix, meanstats, stdstats, minstats, maxstats, rangestats) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
    tsizeSql = "INSERT INTO tsizeStats (times, inputFile, matrix, meanstats, stdstats, minstats, maxstats, rangestats) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
    bsizeSql = "INSERT INTO bsizeStats (times, inputFile, matrix, meanstats, stdstats, minstats, maxstats, rangestats) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)"
    cursor.executemany(fitnessSql, fitnessVal)
    cursor.executemany(sizeSql, sizeVal)
    cursor.executemany(tsizeSql, tSizeVal)
    cursor.executemany(bsizeSql, bSizeVal)
    db.commit()

def getStats(m, i, t,row):
    mean = np.mean(row)
    std = np.std(row)
    minval = min(row)
    maxval = max(row)
    range = maxval - minval
    return (int(t), int(i), int(m),float(mean), float(std), float(minval), float(maxval), float(range))

if __name__ == '__main__':
    main()