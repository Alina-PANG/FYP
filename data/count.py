# import mysql.connector
import matplotlib.pyplot as plt
import numpy
import pymysql
import pandas as pd

def countNum(cursor,inputFile,matrixNum):

    meanArr = []
    stdArr = []
    collect = [{}, {}, {}, {}, {}]

    countArr = ["Exp", "Add", "Drop", "Borrow", "Switch"]
    for i in inputFile:
        for j in range(0, 5):
            cursor.execute("SELECT count" + countArr[j] + " from count " +
                           "where inputFile='in" + i + ".conf'" +
                           " and matrix='matrix" + matrixNum + "'")
            tempRow = cursor.fetchall()
            row = numpy.array(tempRow)
            mean = numpy.mean(row)
            std = numpy.std(row)
            meanArr.append(mean)
            stdArr.append(std)
            for r in tempRow:
                k = r[0]
                if k in collect[j].keys():
                    collect[j][k] = collect[j][k] + 1
                else:
                    collect[j][k] = 1

        result = {'avg': meanArr,
                  'std': stdArr}
        df = pd.DataFrame(result)
        print(df)

        fig, axs = plt.subplots(1, 5, figsize=(15, 3), sharey=True)

        for j in range(0, 5):
            x, y = [], []
            for k in sorted(collect[j].keys()):
                x.append(k)
                y.append(collect[j][k])
            axs[j].bar(x, y)

        # plt.show()
        plt.savefig("countResult_"+i+".png")
