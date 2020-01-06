import pymysql
# import mysql.connector

def main():
    db = pymysql.connect("localhost", "root", "19980312", "hyper_simulation")
    # db = mysql.connector.connect(host="localhost", user="hangzhi", passwd="hangzhi", database="hyper_simulation")
    cursor = db.cursor()
    inputFile = input("input file number: ").split(',')
    matrixNum = input("matrix number: ")
    iterationNum = int(input("number of iterations: "))
    fitness.initResChange(cursor,inputFile,matrixNum, iterationNum)
    count.countNum(cursor,inputFile,matrixNum)
    db.close()

if __name__ == '__main__':
    import fitness
    import count
    main()