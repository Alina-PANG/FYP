import statsmodels.formula.api as sm
import numpy as np
import pandas as pd
import mysql.connector
import csv

def generateFormula(y):
    return (
            y+" ~ innovation + cost + cSize + matrix + innovation:cost + innovation:cSize + innovation:matrix + cost:cSize + cost:matrix + cSize:matrix",
            y+" ~ innovation + cost + cSize + matrix")

def writeSummaryToCSV(df, has75,type):
    failed = []
    innovation, cost, cSize = [],[],[]
    for index, row in df.iterrows():
        try:
            i, c, mi, ma = inputFileSetting(row.inputFile)
            innovation.append(i)
            cost.append(c)
            cSize.append(mi)

        except:
            failed.append(row)
            df.drop(index, inplace=True)

    df['innovation'] = innovation
    df['cost'] = cost
    df['cSize'] = cSize
    print(df.shape)
    formulas = generateFormula('meanstats') + generateFormula('stdstats') + generateFormula('minstats') + generateFormula('maxstats') + generateFormula('rangestats')
    output = ''
    csvoutput = ''
    for f in formulas:
        est = sm.ols(formula=f, data=df).fit()
        output += est.summary().as_text()
        csvoutput += est.summary().as_csv()
    with open(type + '_summary.csv', 'w', newline='') as csvfile:
        csvfile.write(csvoutput)
        csvfile.close()

    if has75:
        formulas = generateFormula('meanstats75') + generateFormula('stdstats75') + generateFormula(
            'minstats75') + generateFormula('maxstats75') + generateFormula('rangestats75')

        csvoutput = ''
        for f in formulas:
            est = sm.ols(formula=f, data=df).fit()
            output += est.summary().as_text()
            csvoutput += est.summary().as_csv()
        with open(type + '75_summary.csv', 'w', newline='') as csvfile:
            csvfile.write(csvoutput)
            csvfile.close()

    print(failed)

def fitnessAndSizeAnalysis(cursor, type):
    cursor.execute("SELECT inputFile, matrix, meanstats, stdstats,minstats,maxstats,rangestats from "+type+"Stats")
    df = pd.DataFrame(cursor.fetchall(), columns=['inputFile', 'matrix', 'meanstats', 'stdstats', 'minstats', 'maxstats', 'rangestats'])
    writeSummaryToCSV(df, False, type)

def rankAnalysis(cursor):
    cursor.execute(
        "SELECT inputFile, matrix, meanstats, stdstats,minstats,maxstats,rangestats, mean75,std75,min75,max75,range75 from rankStats")
    df = pd.DataFrame(cursor.fetchall(),
                      columns=['inputFile', 'matrix', 'meanstats', 'stdstats', 'minstats', 'maxstats',
                               'rangestats', 'meanstats75', 'stdstats75', 'minstats75', 'maxstats75',
                               'rangestats75'])
    writeSummaryToCSV(df, True,'rank')

def countAnalysis(cursor):
    failed = []
    cursor.execute(
        "SELECT inputFile, matrix, times, countExp, countAdd,countDrop,countBorrow,countSwitch from count")
    df_raw = pd.DataFrame(cursor.fetchall(),
                      columns=['inputFile', 'matrix', 'times','countExp', 'countAdd','countDrop','countBorrow','countSwitch'])

    inputArr, matrixArr, mean, std, minval, maxval, rangeval = [],[],[[],[],[],[],[]],[[],[],[],[],[]],[[],[],[],[],[]],[[],[],[],[],[]],[[],[],[],[],[]]

    for i in range(1, 46):
        for m in [3,6,9,12,15]:
            for t in range(1,51):
                print(i,m,t)
                rows = df_raw.loc[(df_raw['inputFile'] == 'in'+str(i)+'.conf') & (df_raw['matrix'] == str(m))  & (df_raw['times'] == t)]
                try:
                    meantemp, stdtemp, minvaltemp, maxvaltemp, rangetemp = getStats(rows.countExp.to_numpy())
                    mean[0].append(meantemp)
                    std[0].append(stdtemp)
                    minval[0].append(minvaltemp)
                    maxval[0].append(maxvaltemp)
                    rangeval[0].append(rangetemp)

                    meantemp, stdtemp, minvaltemp, maxvaltemp, rangetemp = getStats(rows.countAdd.to_numpy())
                    mean[1].append(meantemp)
                    std[1].append(stdtemp)
                    minval[1].append(minvaltemp)
                    maxval[1].append(maxvaltemp)
                    rangeval[1].append(rangetemp)

                    meantemp, stdtemp, minvaltemp, maxvaltemp, rangetemp = getStats(rows.countDrop.to_numpy())
                    mean[2].append(meantemp)
                    std[2].append(stdtemp)
                    minval[2].append(minvaltemp)
                    maxval[2].append(maxvaltemp)
                    rangeval[2].append(rangetemp)

                    meantemp, stdtemp, minvaltemp, maxvaltemp, rangetemp = getStats(rows.countBorrow.to_numpy())
                    mean[3].append(meantemp)
                    std[3].append(stdtemp)
                    minval[3].append(minvaltemp)
                    maxval[3].append(maxvaltemp)
                    rangeval[3].append(rangetemp)

                    meantemp, stdtemp, minvaltemp, maxvaltemp, rangetemp = getStats(rows.countSwitch.to_numpy())
                    mean[4].append(meantemp)
                    std[4].append(stdtemp)
                    minval[4].append(minvaltemp)
                    maxval[4].append(maxvaltemp)
                    rangeval[4].append(rangetemp)

                    inputArr.append(i)
                    matrixArr.append(m)
                except:
                    print(i,m,t,'failed!')
                    failed.append((i,m,t))
    print(failed)

    counts = ['exp','add','drop','borrow','switch']
    for i in range(0,5):
        df = pd.DataFrame(list(zip(inputArr, matrixArr, mean[i], std[i], minval[i], maxval[i], rangeval[i])),
                          columns=['inputFile', 'matrix', 'meanstats', 'stdstats', 'minstats', 'maxstats', 'rangestats'])
        writeSummaryToCSV(df, False, counts[i])

def getStats(row):
    mean = np.mean(row)
    std = np.std(row)
    minval = min(row)
    maxval = max(row)
    range = maxval - minval
    return (mean, std, minval, maxval, range)

def main():
    db = mysql.connector.connect(host="localhost", user="hangzhi", passwd="hangzhi", database="hyper_simulation")
    cursor = db.cursor()
    # fitness and size series
    # fitnessAndSizeAnalysis(cursor, 'fitness')
    # fitnessAndSizeAnalysis(cursor, 'size')
    # fitnessAndSizeAnalysis(cursor, 'tsize')
    # fitnessAndSizeAnalysis(cursor, 'bsize')
    # rank
    rankAnalysis(cursor)
    # count series
    countAnalysis(cursor)

def inputFileSetting(inputFile):
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
    return dict[inputFile]

if __name__ == '__main__':
    main()