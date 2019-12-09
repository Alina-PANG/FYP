# **inf** folder

- Influence matrix files should be stored in this folder
- The config file (experimental setup) will only specify filename (e.g., matrix9.txt); package will look for file in this (inf) folder -- ./inf/matrix9.txt
- Influence matrix file is a comma-delimited text file with x's and o's.  x's denote interdependence, o's denote independence
- The number of columns and rows must match
- the diagonal entry must be x
- number of x across rows for each column is not necessarily K

- Example (for N=20 and K=6 setup)
```
x,x,o,x,o,o,x,x,o,o,o,o,o,o,o,x,o,o,o,x
x,x,o,o,o,x,x,x,o,o,x,o,o,o,o,x,o,o,o,o
o,o,x,o,o,o,x,x,o,x,o,x,x,o,o,o,o,o,x,o
o,o,x,x,x,o,o,x,o,x,o,o,o,o,x,o,o,x,o,o
o,o,o,o,x,o,o,o,o,x,x,x,o,o,x,x,o,x,o,o
o,o,o,o,o,x,o,o,x,o,o,x,o,x,x,o,x,o,x,o
x,o,o,x,o,x,x,o,o,o,x,o,o,o,o,x,x,o,o,o
o,o,o,o,o,o,o,x,x,o,o,x,o,x,x,o,o,o,x,x
o,x,o,o,o,o,o,x,x,o,o,o,x,o,o,x,x,o,o,x
x,x,o,o,x,x,o,o,o,x,o,o,o,o,o,o,x,o,x,o
o,o,o,x,o,o,o,o,o,x,x,x,o,x,x,o,o,o,x,o
o,o,x,o,o,x,o,o,x,o,o,x,x,o,x,o,x,o,o,o
x,o,o,x,x,o,x,o,o,o,x,o,x,o,x,o,o,o,o,o
o,o,o,o,o,o,x,o,o,x,x,o,x,x,o,o,o,x,o,x
o,o,x,o,x,o,o,x,x,o,o,o,o,o,x,o,x,x,o,o
x,x,x,o,x,o,o,o,o,x,o,x,o,o,o,x,o,o,o,o
o,o,x,x,o,x,o,o,o,o,o,o,x,o,o,o,x,x,x,o
o,x,x,x,o,o,o,o,x,x,o,o,o,o,o,o,o,x,o,x
o,x,o,o,x,x,o,o,o,o,x,o,o,x,o,o,o,o,x,x
x,o,o,o,o,o,x,o,x,o,o,o,x,x,o,o,o,x,o,x
```