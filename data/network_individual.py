import networkx as nx
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import os

def main():
    # import pymysql
    # db = pymysql.connect("localhost", "root", "19980312", "hyper_simulation")
    import mysql.connector
    db = mysql.connector.connect(host="localhost", user="hangzhi", passwd="hangzhi", database="hyper_simulation")
    cursor = db.cursor()
    inputFile = input("input file number: ").split(',')
    matrixNum = input("matrix number: ")
    numTimes = input("times: ").split(',')

    # for i in inputFile:
    result = pd.DataFrame(columns=['Times','InDegree', 'OutDegree', 'DCen', 'CCen', 'BCen'])
    for t in numTimes:
        if not os.path.exists("network/t" + t):
            os.makedirs("network/t" + t)
        plt.figure(figsize=(10, 10))
        cursor.execute("SELECT bFirm, lFirm, CIndex from borrowing" +
                       " where inputFile='in" + i + ".conf'" +
                       " and matrix='" + matrixNum + "'" +
                       " and times = " + t)
        borrowing = cursor.fetchall()
        G = nx.DiGraph()
        edge_color = []
        count = 0
        for row in borrowing:
            count += 1
            # duplicates
            if not G.has_edge(row[1], row[0]):
                G.add_edge(row[1], row[0])
                edge_color.append(row[2])

        print("\n===== Input = "+i+" & Time = "+t+" =====")
        print("Number of Different Edges: ", count)
        print("Total number of nodes: ", int(G.number_of_nodes()))
        print("Total number of edges: ", int(G.number_of_edges()))


        df1 = pd.DataFrame([dict(G.in_degree()), dict(G.out_degree),
                      nx.algorithms.centrality.degree_alg.degree_centrality(G),
                      nx.algorithms.centrality.closeness.closeness_centrality(G),
                      nx.algorithms.centrality.betweenness_centrality(G)])
        df1_transposed = df1.T
        df1_transposed.columns = ['InDegree', 'OutDegree', 'DCen', 'CCen', 'BCen']
        df1_transposed['Times'] = [t] * int(G.number_of_nodes())
        result = pd.concat([result, df1_transposed], sort=False)

        node_size = [100 * G.degree(v) for v in G]
        nx.draw_networkx(G, node_size=node_size,
                         node_color='#A0CBE2', alpha=0.7, edge_color=edge_color,
                         with_labels=True, width=1, cmap=plt.cm.Blues,
                         font_size=12, font_weight='bold')
        plt.axis('off')
        # plt.legend()
        plt.title("Firm Network at Iteration=100, Times="+t+", Matrix="+matrixNum+", InputFile="+i)
        plt.tight_layout()
        plt.savefig("network/t"+t+"/input"+i+"_m"+matrixNum)
        # plt.show()
        plt.clf()

    result.to_csv("network/input"+i+"_m"+matrixNum+".csv", encoding='utf-8')

def centralityAnalysis(cursor,i,matrixNum, numTimes, dir):
    meanNodes, meanEdges, meanDCen, meanCCen, meanBCen = [],[],[],[],[]
    stdNodes, stdEdges,  stdDCen, stdCCen, stdBCen = [], [], [],  [], []
    # for i in inputFile:
    tempmeanNodes, tempmeanEdges, tempmeanDCen, tempmeanCCen, tempmeanBCen = [], [], [],  [], []
    cursor.execute("SELECT bFirm, lFirm, CIndex, times from borrowing" +
                   " where inputFile='in" + i + ".conf'" +
                   " and matrix='" + matrixNum + "'" +
                   " and times >= " + numTimes[0] +
                   " and times <= " + numTimes[1])
    tempDf = pd.DataFrame(cursor.fetchall(), columns=['bFirm', 'lFirm', 'CIndex', 'times'])
    for t in range(int(numTimes[0]), int(numTimes[1])+1):
        tDf = tempDf.loc[(tempDf.times == t)]
        G = nx.DiGraph()
        for index, row in tDf.iterrows():
            G.add_edge(row[1], row[0])
        tempmeanNodes.append(G.number_of_nodes())
        tempmeanEdges.append(G.number_of_edges())
        for i in G.nodes:
            tempmeanDCen.append(nx.algorithms.centrality.degree_alg.degree_centrality(G)[i])
            tempmeanCCen.append(nx.algorithms.centrality.closeness.closeness_centrality(G)[i])
            tempmeanBCen.append(nx.algorithms.centrality.betweenness_centrality(G)[i])

    meanNodes.append(np.mean(tempmeanNodes))
    meanEdges.append(np.mean(tempmeanEdges))
    meanDCen.append(np.mean(tempmeanDCen))
    meanCCen.append(np.mean(tempmeanCCen))
    meanBCen.append(np.mean(tempmeanBCen))

    stdNodes.append(np.std(tempmeanNodes))
    stdEdges.append(np.std(tempmeanEdges))
    stdDCen.append(np.std(tempmeanDCen))
    stdCCen.append(np.std(tempmeanCCen))
    stdBCen.append(np.std(tempmeanBCen))


    multplier = 1
    data = {
            'Type': ['Nodes']*multplier+['Edges']*multplier+['DCen']*multplier+['CCen']*multplier+['BCen']*multplier,
            'Mean': meanNodes+meanEdges+meanDCen+meanCCen+meanBCen,
            "Std": stdNodes+stdEdges+stdDCen+stdCCen+stdBCen}

    df = pd.DataFrame(data, columns=['Type', 'Mean', 'Std'])
    df.to_csv("result/"+dir+"/centrality.csv", encoding='utf-8')

if __name__ == '__main__':
    main()