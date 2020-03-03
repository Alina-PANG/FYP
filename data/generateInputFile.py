

def main():
    innovation = ['0.5','0.75','1']
    cost = ['0.01','0.25','0.5']
    componentSize = [['2','2'],['3','3'],['4','4'],['5','5'],['2','5']]
    counter = 1
    record = open("../in/record.txt", "w+")
    record.write("{\n")
    for c in cost:
        for i in innovation:
            for cs in componentSize:
                f = open("../in/in"+str(counter)+".conf", "w+")
                f.write("N=20\nnumConfig=14\n")
                f.write("firms=50,5,"+i+",1,1,"+c+",abs,"+c+","+i+","+c+","+i+","+c+","+i+","+c+"\n")
                f.write("minComponentSize="+cs[0]+"\n")
                f.write("maxComponentSize=" + cs[1])
                f.close()
                record.write(str(counter)+":["+i+","+c+","+cs[0]+","+cs[1]+"],\n")
                counter += 1
    record.write("}")
    record.close()

if __name__ == '__main__':
    main()