import pandas as pd
import numpy
import matplotlib.pyplot as plt


def performanceIteration(cursor, inputFile,matrixNum, iterationNum, numTimes):
    # avg, sd, and plotting of firm fitness by firm ID + fitness
    for f in inputFile:
        meanArr = []
        stdArr = []
        cursor.execute("SELECT iteration, fitness from fitness " +
                       "where firmId!=" + str(-1) +
                       " and inputFile='in" + f + ".conf'" +
                       " and matrix='"+matrixNum +"'"+
                       " and times >= " + numTimes[0] +
                       " and times <= " + numTimes[1])
        df = pd.DataFrame(cursor.fetchall(), columns=['iteration', 'fitness'])
        for i in range(0, iterationNum):
            row = df.loc[df['iteration'] == i].fitness.to_numpy()
            mean = numpy.mean(row)
            std = numpy.std(row)
            meanArr.append(mean)
            stdArr.append(std)

        line = plt.errorbar(range(0, iterationNum), meanArr,  uplims=True, lolims=True,label='input='+f)#yerr=df.errorBar,
        plt.annotate("input="+f, xy=(0, meanArr[0]), xytext=(6,0),  color=line[0].get_color(), textcoords="offset points", va="center")
        # plt.annotate("input=" + f, xy=(iterationNum-1, df.fitness.iloc[-1]), xytext=(6, 0), color=line[0].get_color(),
        #              textcoords="offset points", va="center")

    plt.legend(prop={'size': 10})
    # plt.show()
    plt.savefig("performance"+matrixNum+"_".join(inputFile)+".png")


def componentSizeChange(cursor, inputFile, matrixNum, numTimes):
    iteration = [24,49,74,99]
    for i in iteration:
        meanArr = []
        stdArr = []
        for f in inputFile:
            cursor.execute("SELECT fitness from fitness " +
                           "where iteration=" + str(i) +
                           " and firmId!=" + str(-1) +
                           " and inputFile='in" + f + ".conf'" +
                           " and matrix='"+matrixNum +"'"+
                           " and times >= " + numTimes[0] +
                           " and times <= " + numTimes[1])
            row = numpy.array(cursor.fetchall())
            mean = numpy.mean(row)
            std = numpy.std(row)
            meanArr.append(mean)
            stdArr.append(std)

        line = plt.errorbar(range(1, 7), meanArr, uplims=True, lolims=True,
                            label='input=' + f)  # yerr=stdArr,
        plt.annotate("iteration=" + str(i), xy=(6, meanArr[-1]), xytext=(6, 0), color=line[0].get_color(),
                     textcoords="offset points", va="center")
        # plt.annotate("iteration=" + str(i), xy=(0, meanArr[-1]), xytext=(6, 0), color=line[0].get_color(),
        #              textcoords="offset points", va="center")

    plt.legend(prop={'size': 10})
    plt.show()
    # plt.savefig("componentResult.png")

def avgRankChange(cursor, inputFile, matrixNum,iterationNum, numTimes):
    firms = range(0, 20)
    for f in inputFile:
        meanArr = []
        stdArr = []
        cursor.execute("SELECT times, firmId, iteration, firmRank from fitness " +
                       "where firmId!=-1" +
                       " and inputFile='in" + f + ".conf'" +
                       " and matrix='"+matrixNum +"'"+
                       " and times >= " + numTimes[0] +
                       " and times <= " + numTimes[1])
        df = pd.DataFrame(cursor.fetchall(), columns=['times', 'firmId', 'iteration', 'firmRank'])
        for firm in firms:
            print('input=' + f, 'firm=' + str(firm))
            firmDf = df.loc[(df.firmId == firm) & (df.iteration == 0)].sort_values('times').drop_duplicates()
            tempMeanArr = []
            tempStdArr = []
            previousRow = firmDf.firmRank.to_numpy()
            for i in range(1, iterationNum):
                firmDf = df.loc[(df.firmId == firm) & (df.iteration == i)].sort_values('times').drop_duplicates()
                curRow = firmDf.firmRank.to_numpy()
                minusResultRow = numpy.subtract(curRow, previousRow)
                mean = numpy.mean(minusResultRow)
                std = numpy.std(minusResultRow)
                tempMeanArr.append(mean)
                tempStdArr.append(std)
                previousRow = curRow
            meanArr.append(tempMeanArr)
            stdArr.append(tempStdArr)
        meanArr = numpy.mean(numpy.array(meanArr), axis=0)
        stdArr = numpy.std(numpy.array(stdArr), axis=0)
        line = plt.errorbar(range(0, iterationNum - 1), meanArr, uplims=True, lolims=True,
                            label='input=' + f)  # yerr=stdArr,
        plt.annotate("input=" + f, xy=(6, meanArr[0]), xytext=(6, 0), color=line[0].get_color(),
                     textcoords="offset points", va="center")
        # plt.annotate("iteration=" + str(i), xy=(0, meanArr[-1]), xytext=(6, 0), color=line[0].get_color(),
        #              textcoords="offset points", va="center")

    plt.legend(prop={'size': 10})
    # plt.show()
    plt.savefig("rankResult"+matrixNum+"_".join(inputFile)+".png")


