import matplotlib.pyplot as plt
import numpy
import pandas as pd

def countNum(cursor,i,matrixNum, numTimes, dir):

    meanArr = []
    stdArr = []
    collect = [{}, {}, {}, {}, {}]

    countArr = ["Exp", "Add", "Drop", "Borrow", "Switch"]
    # for i in inputFile:
    for j in range(0, 5):
        cursor.execute("SELECT count" + countArr[j] + " from count " +
                       "where inputFile='in" + i + ".conf'" +
                       " and matrix='"+matrixNum +"'"+
                       " and times >= " + numTimes[0] +
                       " and times <= " + numTimes[1])
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
    # print(df)

    # fig, axs = plt.subplots(1, 5, figsize=(15, 3), sharey=true)
    for j in range(0, 5):
        x, y = [], []
        for k in sorted(collect[j].keys()):
            x.append(k)
            y.append(collect[j][k])

        # plt.text(0.5, 0.01, "Occurence of a " + countArr[j] + " Decision in 100 iterations (1 run)", ha='center')
        # plt.text(0.01, 0.5, "Between Times " + numTimes[0] + "-" + numTimes[1] + " Number of Firms", va='center',
        #          rotation='vertical')
        plt.xlim(0,50)
        plt.ylim(0,900)
        plt.tight_layout()
        plt.bar(x, y)
        plt.savefig("result/" + dir + "/" + countArr[j] + ".png")
        plt.clf()
        plt.close()



