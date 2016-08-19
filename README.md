# JFR-Processor

Example project to view content of JFR file and visualize/analyze it in my own way.

There is only one example of parsing and analyzing of jfr at the moment.

##FlameGraph generation based on jfr  profiling samples

To run you have to get installed JDK 7u40+

1) you need to get a folded stacks from jfr. To get them, run command like this (In example below I filter stack by date them acquired and thread name)
 
`java -jar target/processor-1.0-jar-with-dependencies.jar -f /tmp/record.jfr -df "17.08.2016 18:05:36,772" -dt "17.08.2016 18:05:56,772" -tnm "exec-164"`

2) make a flamegraph from output.txt (default file for folded stacks)
`./flamegraph.pl --minwidth 2 output.txt > flame.svg`

##Resources
###JFR Parsing
Markus Hirt's blog [http://hirt.se/blog] has a vast information about JFR  and JMC. 

Some posts about parsing jfrs:
* Parsing Flight Recordings – an Example [http://hirt.se/blog/?p=459]
* Using the Flight Recorder Parsers [http://hirt.se/blog/?p=446]

###Flamegraph
* FlameGraph by Brendann Gregg [https://github.com/brendangregg/FlameGraph]
* An example of how to convert JFR stack to folded stack [https://github.com/chrishantha/jfr-flame-graph]
