import pandas as pd
import numpy
import matplotlib.pyplot as plt
from scipy import stats

def firmSizeAndFitness(cursor,inputFile,matrixNum, numTimes, dir):
    for i in inputFile:
        cursor.execute("SELECT fitnessConfig, fitness from fitness" +
                       " where inputFile='in" + i + ".conf'" +
                       " and matrix='" + matrixNum + "'" +
                       " and times >= " + numTimes[0] +
                       " and times <= " + numTimes[1] +
                       " and iteration=99")

        df = pd.DataFrame(cursor.fetchall(), columns=['FitnessConfig', 'Fitness'])
        sizeCalculation = []
        for index, row in df.iterrows():
            config = row['FitnessConfig']
            sizeCalculation.append(20 - config.count('.'))
        df['Size'] = sizeCalculation;
        df.plot.scatter(x='Size',y='Fitness',c='#65B9E3')
        slope, intercept, r_value, p_value, std_err = stats.linregress(df['Size'], df['Fitness'])
        axes = plt.gca()
        x_vals = numpy.array(axes.get_xlim())
        y_vals = intercept + slope * x_vals
        plt.plot(x_vals, y_vals, '--')
        plt.xlabel('Firm Size')
        plt.ylabel('Firm Fitness')
        # plt.title('Firm Size VS Firm Fitness')
        text = 'slope='+str(slope)+", intercept="+str(intercept)+", r_value="+str(r_value)+", p_value="+str(p_value)+", std_err="+str(std_err)
        plt.text(df.min(axis=0)['Size'], 0.9, text, wrap=True)
        print(text)
        plt.savefig("result/" + dir + "/sizeVSfitness_" +i + ".png")
        plt.clf()

        sizeData = {}
        for k in sizeCalculation:
            if k in sizeData.keys():
                sizeData[k] += 1
            else:
                sizeData[k] = 1
        x,y = [],[]
        for k in sizeData.keys():
            x.append(k)
            y.append(sizeData[k])
        plt.bar(x, y)

        # plt.show()
        print(i)
        plt.title("InputFile = " + i)
        plt.xlabel("Firm Size")
        plt.ylabel("Occurence")
        plt.tight_layout()
        plt.savefig("result/" + dir + "/firmSizeOccurence_" + i + ".png")
        plt.clf()


def performanceIteration(cursor, inputFile,matrixNum, iterationNum, numTimes, dir):
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
        plt.annotate("input="+f, xy=(len(meanArr) - 1, meanArr[-1]), xytext=(6,0),  color=line[0].get_color(), textcoords="offset points", va="center")
        # plt.annotate("input=" + f, xy=(iterationNum-1, df.fitness.iloc[-1]), xytext=(6, 0), color=line[0].get_color(),
        #              textcoords="offset points", va="center")

    plt.legend(prop={'size': 10})
    plt.ylabel("Firm Fitness")
    plt.xlabel("Iterations")
    plt.title("Firm Performance for Matrix="+matrixNum)
    # plt.show()
    plt.tight_layout()
    plt.savefig("result/"+dir+"/performance.png")
    plt.clf()

def avgRankChange(cursor, inputFile, matrixNum,iterationNum, numTimes, dir):
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
                minusResultRow = numpy.absolute(curRow - previousRow)
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
        plt.annotate("input=" + f, xy=(len(meanArr) - 1, meanArr[-1]), xytext=(6, 0), color=line[0].get_color(),
                     textcoords="offset points", va="center")

        # plt.annotate("iteration=" + str(i), xy=(0, meanArr[-1]), xytext=(6, 0), color=line[0].get_color(),
        #              textcoords="offset points", va="center")

    plt.legend(prop={'size': 10})
    # plt.show()
    plt.ylabel("Absolute Rank Changes")
    plt.xlabel("Iterations")
    plt.title("Firm Ranking Changes for Matrix=" + matrixNum)
    plt.tight_layout()
    plt.savefig("result/"+dir+"/rank.png")
    plt.clf()



