import pandas as pd
import numpy
import matplotlib.pyplot as plt
from scipy import stats
from matplotlib import cm
from matplotlib.ticker import LinearLocator, FormatStrFormatter, StrMethodFormatter
from mpl_toolkits.mplot3d import Axes3D

def heatmap(data, row_labels, col_labels, ax=None,
            cbar_kw={}, cbarlabel="", **kwargs):

    if not ax:
        ax = plt.gca()

    # Plot the heatmap
    im = ax.imshow(data, **kwargs)

    # Create colorbar
    cbar = ax.figure.colorbar(im, ax=ax, **cbar_kw)
    cbar.ax.set_ylabel(cbarlabel, rotation=-90, va="bottom")

    # We want to show all ticks...
    ax.set_xticks(numpy.arange(data.shape[1]))
    ax.set_yticks(numpy.arange(data.shape[0]))
    # ... and label them with the respective list entries.
    ax.set_xticklabels(col_labels)
    ax.set_yticklabels(row_labels)

    # Let the horizontal axes labeling appear on top.
    ax.tick_params(top=True, bottom=False,
                   labeltop=True, labelbottom=False)

    # Rotate the tick labels and set their alignment.
    plt.setp(ax.get_xticklabels(), rotation=-30, ha="right",
             rotation_mode="anchor")

    # Turn spines off and create white grid.
    for edge, spine in ax.spines.items():
        spine.set_visible(False)

    ax.set_xticks(numpy.arange(data.shape[1]+1)-.5, minor=True)
    ax.set_yticks(numpy.arange(data.shape[0]+1)-.5, minor=True)
    ax.grid(which="minor", color="w", linestyle='-', linewidth=3)
    ax.tick_params(which="minor", bottom=False, left=False)
    ax.set_title("x - Borrowed, y - Owned",y=1.08)

    return im, cbar


def annotate_heatmap(im, data=None, valfmt="{x:.2f}",
                     textcolors=["black", "white"],
                     threshold=None, **textkw):

    if not isinstance(data, (list, numpy.ndarray)):
        data = im.get_array()

    # Normalize the threshold to the images color range.
    if threshold is not None:
        threshold = im.norm(threshold)
    else:
        threshold = im.norm(data.max())/2.

    kw = dict(horizontalalignment="center",
              verticalalignment="center")
    kw.update(textkw)

    texts = []
    for i in range(data.shape[0]):
        for j in range(data.shape[1]):
            kw.update(color=textcolors[int(im.norm(data[i, j]) > threshold)])
            text = im.axes.text(j, i, data[i, j], **kw)
            texts.append(text)

    return texts

def firmSizeAndFitness(cursor,inputFile,matrixNum, numTimes, dir):
    for i in inputFile:
        cursor.execute("SELECT fitnessConfig, fitness, fSize, firmRank, fitnessNorm from fitness" +
                       " where inputFile='in" + i + ".conf'" +
                       " and matrix='" + matrixNum + "'" +
                       " and times >= " + numTimes[0] +
                       " and times <= " + numTimes[1] +
                       " and iteration=99")

        df = pd.DataFrame(cursor.fetchall(), columns=['FitnessConfig', 'Fitness', 'Size', 'Rank', 'FitnessNorm'])
        sizeCalculation = []
        for index, row in df.iterrows():
            config = row['FitnessConfig']
            sizeCalculation.append(20 - config.count('.'))
        df['TotalSize'] = sizeCalculation;
        df['Borrowed'] = df["TotalSize"] - df["Size"]

        ### graph 1
        df.plot.scatter(x='Size',y='Fitness',c='#65B9E3')
        slope, intercept, r_value, p_value, std_err = stats.linregress(df['Size'], df['Fitness'])
        a = plt.gca()
        x_vals = numpy.array(a.get_xlim())
        y_vals = intercept + slope * x_vals
        plt.plot(x_vals, y_vals, '--')
        plt.xlabel('Firm Size')
        plt.ylabel('Firm Fitness')
        # plt.title('Firm Size VS Firm Fitness')
        text = 'slope='+str(slope)+", intercept="+str(intercept)+", r_value="+str(r_value)+", p_value="+str(p_value)+", std_err="+str(std_err)
        plt.text(df.min(axis=0)['Size'], 0.9, text, wrap=True)
        print(text)
        plt.savefig("result/" + dir + "/sizeVSfitness_" +i + ".png")
        plt.close()

        ### graph 2
        df.plot.scatter(x='Size', y='FitnessNorm', c='#65B9E3')
        slope, intercept, r_value, p_value, std_err = stats.linregress(df['Size'], df['FitnessNorm'])
        a = plt.gca()
        x_vals = numpy.array(a.get_xlim())
        y_vals = intercept + slope * x_vals
        plt.plot(x_vals, y_vals, '--')
        plt.xlabel('Firm Size')
        plt.ylabel('Firm Fitness Efficiency(FitnessNorm)')
        # plt.title('Firm Size VS Firm Fitness')
        text = 'slope=' + str(slope) + ", intercept=" + str(intercept) + ", r_value=" + str(
            r_value) + ", p_value=" + str(p_value) + ", std_err=" + str(std_err)
        plt.text(df.min(axis=0)['Size'], 0.9, text, wrap=True)
        print(text)
        plt.savefig("result/" + dir + "/sizeVSefficiency_" + i + ".png")
        plt.close()

        ### graph 3
        df.plot.scatter(x='Size', y='Rank', c='#65B9E3')
        slope, intercept, r_value, p_value, std_err = stats.linregress(df['Size'], df['Rank'])
        a = plt.gca()
        x_vals = numpy.array(a.get_xlim())
        y_vals = intercept + slope * x_vals
        plt.plot(x_vals, y_vals, '--')
        plt.xlabel('Firm Size')
        plt.ylabel('Firm Ranking')
        text = 'slope=' + str(slope) + ", intercept=" + str(intercept) + ", r_value=" + str(
            r_value) + ", p_value=" + str(p_value) + ", std_err=" + str(std_err)
        plt.text(df.min(axis=0)['Size'], 0.9, text, wrap=True)
        print(text)
        plt.savefig("result/" + dir + "/sizeVSranking_" + i + ".png")
        plt.clf()
        plt.close()

        ### graph 4
        df.plot.scatter(x='Borrowed', y='Rank', c='#65B9E3')
        slope, intercept, r_value, p_value, std_err = stats.linregress(df['Borrowed'], df['Rank'])
        a = plt.gca()
        x_vals = numpy.array(a.get_xlim())
        y_vals = intercept + slope * x_vals
        plt.plot(x_vals, y_vals, '--')
        plt.xlabel('Firm Number of Borrowed Resources')
        plt.ylabel('Firm Ranking')
        text = 'slope=' + str(slope) + ", intercept=" + str(intercept) + ", r_value=" + str(
            r_value) + ", p_value=" + str(p_value) + ", std_err=" + str(std_err)
        plt.text(df.min(axis=0)['Borrowed'], 0.9, text, wrap=True)
        print(text)
        plt.savefig("result/" + dir + "/borrowedVSranking_" + i + ".png")
        plt.clf()
        plt.close()

        ### graph 5
        df.plot.scatter(x='TotalSize', y='Rank', c='#65B9E3')
        slope, intercept, r_value, p_value, std_err = stats.linregress(df['TotalSize'], df['Rank'])
        a = plt.gca()
        x_vals = numpy.array(a.get_xlim())
        y_vals = intercept + slope * x_vals
        plt.plot(x_vals, y_vals, '--')
        plt.xlabel('Firm Entire Size')
        plt.ylabel('Firm Ranking')
        text = 'slope=' + str(slope) + ", intercept=" + str(intercept) + ", r_value=" + str(
            r_value) + ", p_value=" + str(p_value) + ", std_err=" + str(std_err)
        plt.text(df.min(axis=0)['TotalSize'], 0.9, text, wrap=True)
        print(text)
        plt.savefig("result/" + dir + "/totalVSranking_" + i + ".png")
        plt.clf()
        plt.close()

        ### graph 6
        sizeData = {}
        for k in df['Borrowed'].values:
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
        plt.title("inputFile = " + i)
        plt.xlabel("Firm Size")
        plt.ylabel("Occurence")
        plt.tight_layout()
        plt.savefig("result/" + dir + "/firmSizeOccurence_" + i + ".png")
        plt.clf()
        plt.close()

        ### graph 7
        countSizeFirmNumber = {}
        for index, row in df.iterrows():
            key = (row['Borrowed'], row['Size'])
            if key in countSizeFirmNumber.keys():
                countSizeFirmNumber[key] += 1
            else:
                countSizeFirmNumber[key] = 1

        x, y, z = [], [], []
        for k in countSizeFirmNumber.keys():
            x.append(k[0])
            y.append(k[1])
            z.append(countSizeFirmNumber[k])

        fig = plt.figure()
        ax = Axes3D(fig)
        ax.scatter(x,y,z, cmap=cm.viridis)
        ax.set_xlabel('Number of Res Borrowed')
        ax.set_ylabel('Number of Res Owned')
        ax.set_zlabel('Number of Firms')
        plt.tight_layout()
        plt.savefig("result/" + dir + "/3dfSizeScatter" + i + ".png")
        plt.clf()
        plt.close()

        ### graph 8
        x = numpy.arange(start=0, stop=20)
        y = numpy.arange(start=0, stop=20)
        z = []
        for t in x:
            k = []
            for j in y:
                if (t, j) in countSizeFirmNumber.keys():
                    k.append(countSizeFirmNumber[(t,j)])
                else:
                    k.append(0)
            z.append(k)

        fig, ax = plt.subplots(figsize=(10,10))

        im, cbar = heatmap(numpy.array(z), x, y, ax=ax,
                           cmap="YlGn", cbarlabel="Number of Firms")
        texts = annotate_heatmap(im, valfmt="{x:.1f} t",size=10)

        fig.tight_layout()
        plt.savefig("result/" + dir + "/3dfSizeHeatMap" + i + ".png")
        plt.clf()
        plt.close()

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

        line = plt.errorbar(range(0, iterationNum), meanArr,  uplims=True, lolims=True,label='inumpyut='+f)#yerr=df.errorBar,
        plt.annotate("inumpyut="+f, xy=(len(meanArr) - 1, meanArr[-1]), xytext=(6,0),  color=line[0].get_color(), textcoords="offset points", va="center")
        # plt.annotate("inumpyut=" + f, xy=(iterationNum-1, df.fitness.iloc[-1]), xytext=(6, 0), color=line[0].get_color(),
        #              textcoords="offset points", va="center")

    plt.legend(prop={'size': 10})
    plt.ylabel("Firm Fitness")
    plt.xlabel("Iterations")
    plt.title("Firm Performance for Matrix="+matrixNum)
    # plt.show()
    plt.tight_layout()
    plt.savefig("result/"+dir+"/performance.png")
    plt.clf()
    plt.close()

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
            print('inumpyut=' + f, 'firm=' + str(firm))
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
                            label='inumpyut=' + f)  # yerr=stdArr,
        plt.annotate("inumpyut=" + f, xy=(len(meanArr) - 1, meanArr[-1]), xytext=(6, 0), color=line[0].get_color(),
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
    plt.close()



