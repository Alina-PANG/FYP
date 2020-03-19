import statsmodels.formula.api as sm
import numpy as np
import pandas as pd
import mysql.connector


def main():
    db = mysql.connector.connect(host="localhost", user="hangzhi", passwd="hangzhi", database="hyper_simulation")
    cursor = db.cursor()

    cursor.execute("SELECT inputFile, matrix, fitness, times from fitness " +
                   "where iteration = 99 ")
    df_fit = pd.DataFrame(cursor.fetchall(), columns=['inputFile', 'matrix', 'fitness','times'])

    meanArr, stdArr = [], []
    inputFileArr, matrixArr = [], []
    # (df_fit['inputFile'] == 'in3.conf') &

    # row = df_fit.loc[(df_fit['inputFile'] == 'in3.conf') &(df_fit['matrix'] == '6')].fitness.to_numpy()

    for i in range(1, 46):
        for m in ['3','6','9','12','15']:
            for t in range(1,51):
                row = df_fit.loc[(df_fit['inputFile'] == 'in'+str(i)+'.conf') & (df_fit['matrix'] == m)  & (df_fit['times'] == t)].fitness.to_numpy()
                mean = np.mean(row)
                std = np.std(row)
                meanArr.append(mean)
                stdArr.append(std)
                inputFileArr.append(i)
                matrixArr.append(int(m))

    df = pd.DataFrame(list(zip(inputFileArr, matrixArr, meanArr, stdArr)), columns=['input', 'matrix', 'fitness', 'std'])
    df['innovation'], df['cost'], df['minSize'], df['maxSize'] = inputFileSetting(inputFileArr)

    # X = df[['innovation', 'cost', 'minSize', 'maxSize', 'matrix']]
    # y1 = df['fitness']
    # y2 = df['std']
    # df = pd.DataFrame({"A": [10, 20, 30, 40, 50], "B": [20, 30, 10, 40, 50], "C": [32, 234, 23, 23, 42523]})
    formula = "fitness ~ innovation + cost + minSize + maxSize + matrix + innovation:cost + innovation:minSize + innovation:maxSize + innovation:matrix + cost:minSize + cost:maxSize + cost:matrix + minSize:maxSize + minSize:matrix + maxSize:matrix"
    # formula = "fitness ~ innovation + cost + minSize + maxSize + matrix"
    est = sm.ols(formula=formula, data=df).fit()
    # est2 = sm.ols(formula="std ~ innovation + cost + minSize + maxSize + matrix", data=df).fit()

    ## fit a OLS model with intercept on TV and Radio
    # est = sm.ols(y1, X).fit()
    print(est.summary())
    # est2 = sm.OLS(y2, X).fit()
    # print(est2.summary())

    # text_file = open("output.txt", "w")
    # text_file.write(est.summary)
    # text_file.write(est2.summary)
    # text_file.close()

def inputFileSetting(inputFileArr):
    # innovation, cost, minSize, maxSize
    dict = {
        1: [0.5, 0.01, 2, 2],
        2: [0.5, 0.01, 3, 3],
        3: [0.5, 0.01, 4, 4],
        4: [0.5, 0.01, 5, 5],
        5: [0.5, 0.01, 2, 5],
        6: [0.75, 0.01, 2, 2],
        7: [0.75, 0.01, 3, 3],
        8: [0.75, 0.01, 4, 4],
        9: [0.75, 0.01, 5, 5],
        10: [0.75, 0.01, 2, 5],
        11: [1, 0.01, 2, 2],
        12: [1, 0.01, 3, 3],
        13: [1, 0.01, 4, 4],
        14: [1, 0.01, 5, 5],
        15: [1, 0.01, 2, 5],
        16: [0.5, 0.25, 2, 2],
        17: [0.5, 0.25, 3, 3],
        18: [0.5, 0.25, 4, 4],
        19: [0.5, 0.25, 5, 5],
        20: [0.5, 0.25, 2, 5],
        21: [0.75, 0.25, 2, 2],
        22: [0.75, 0.25, 3, 3],
        23: [0.75, 0.25, 4, 4],
        24: [0.75, 0.25, 5, 5],
        25: [0.75, 0.25, 2, 5],
        26: [1, 0.25, 2, 2],
        27: [1, 0.25, 3, 3],
        28: [1, 0.25, 4, 4],
        29: [1, 0.25, 5, 5],
        30: [1, 0.25, 2, 5],
        31: [0.5, 0.5, 2, 2],
        32: [0.5, 0.5, 3, 3],
        33: [0.5, 0.5, 4, 4],
        34: [0.5, 0.5, 5, 5],
        35: [0.5, 0.5, 2, 5],
        36: [0.75, 0.5, 2, 2],
        37: [0.75, 0.5, 3, 3],
        38: [0.75, 0.5, 4, 4],
        39: [0.75, 0.5, 5, 5],
        40: [0.75, 0.5, 2, 5],
        41: [1, 0.5, 2, 2],
        42: [1, 0.5, 3, 3],
        43: [1, 0.5, 4, 4],
        44: [1, 0.5, 5, 5],
        45: [1, 0.5, 2, 5],
    }
    sortedDict = [[],[],[],[]]
    for i in inputFileArr:
        for j in range(0, 4):
            sortedDict[j].append(dict[i][j])
    return sortedDict

if __name__ == '__main__':
    main()