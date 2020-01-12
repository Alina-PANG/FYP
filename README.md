### Input Format
Start Java Program
```buildoutcfg
hangzhi@server:~$ sh run.sh
Start of inputs: 10
End of inputs:15
Starting Time: 1
Times to repeat: 50
Iterations in one run: 100
Influence matrix: matrix12
```
Save To DB Program
```buildoutcfg
hangzhi@server:~/data$ python3 1_saveToDB.py
input file number: 10,15
number of times: 50
```
Start Analysis Program
```buildoutcfg
hangzhi@server:~/data$ python3 2_analysis.py
input file number: 1,2,3,4,5,6,7,8,9
matrix number: 12
number of iterations: 100
```