import pandas as pd
import numpy
import matplotlib.pyplot as plt
import scipy

def initResChange(cursor, inputFile,matrixNum, iterationNum):
    # avg, sd, and plotting of firm fitness by firm ID + fitness

    for f in inputFile:
        meanArr = []
        stdArr = []
        for i in range(0, iterationNum):
            cursor.execute("SELECT fitness from fitness " +
                           "where iteration=" + str(i) +
                           #                        " and times=" + str(t) +
                           " and firmId!=" + str(-1) +
                           " and inputFile='in" + f + ".conf'" +
                           " and matrix='matrix" + matrixNum + "'")
            row = numpy.array(cursor.fetchall())
            mean = numpy.mean(row)
            std = numpy.std(row)
            meanArr.append(mean)
            stdArr.append(std)

        result = {'iteration': range(0, iterationNum),
                  'fitness': meanArr,
                  'errorBar': stdArr}
        df = pd.DataFrame(result)

        plt.errorbar(df.iteration, df.fitness,  uplims=True, lolims=True,label='input='+f)#yerr=df.errorBar,

    plt.legend(prop={'size': 10})
    # plt.show()
    plt.savefig("initResresult.png")


def componentSizeChange(cursor, inputFile,matrixNum):
    meanArr = []
    stdArr = []
    for f in inputFile:
        cursor.execute("SELECT fitness from fitness " +
                       "where iteration=50" 
                       " and firmId!=" + str(-1) +
                       " and inputFile='in" + f + ".conf'" +
                       " and matrix='matrix" + matrixNum + "'")
        row = numpy.array(cursor.fetchall())
        mean = numpy.mean(row)
        std = numpy.std(row)


    plt.errorbar(df.iteration, df.fitness,  uplims=True, lolims=True,label='input='+f)#yerr=df.errorBar,

    plt.legend(prop={'size': 10})
    # plt.show()
    plt.savefig("componentResult.png")
