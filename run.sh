read -p 'Number of inputs: ' inputs
read -p 'Times to repeat: ' times
read -p 'Iterations in one run: ' iterations
read -p 'Influence matrix: ' matrix

for i in $(seq 1 $inputs); do
	for time in $(seq 1 $times); do
		echo $i $time "th run"
		num=${i}_${time}
	    java -jar app-0.0.1-SNAPSHOT.jar in/in$i.conf out/out$num.txt $iterations $time $matrix # > output/output$num.txt
	done
done
