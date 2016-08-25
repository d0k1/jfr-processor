# JFR-Processor

Example project to view content of JFR file and visualize/analyze it in my own way.

There is only one example of parsing and analyzing of jfr at the moment.

##FlameGraph generation based on jfr  profiling samples

To run you have to get installed JDK 7u40+

1) you need to get a folded stacks from jfr. To get them, run command like this (In example below I filter stack by date them acquired and thread name)
 
`java -cp target/processor-1.0-jar-with-dependencies.jar:$JAVA_HOME/lib/missioncontrol/plugins/com.jrockit.mc.flightrecorder_5.5.0.165303.jar:$JAVA_HOME/lib/missioncontrol/plugins/com.jrockit.mc.common_5.5.0.165303.jar com.focusit.jfr.processor.ProfilingProcessor -f /tmp/record.jfr -df "17.08.2016 18:05:36,772" -dt "17.08.2016 18:05:56,772" -tnm "exec-164"`

2) make a flamegraph from output.txt (default file for folded stacks)
`./flamegraph.pl --minwidth 2 output.txt > flame.svg`

##Exception statistic

You can view some statistic about recorder exceptions(error) in spreadsheet processor you like. This program can produce a csv file with aggregated info about exeptions.

###List of columns csv file has:

* Exception class
* Thread
* Count

To get that csv you need to run 

`java -cp target/processor-1.0-jar-with-dependencies.jar:$JAVA_HOME/lib/missioncontrol/plugins/com.jrockit.mc.flightrecorder_5.5.0.165303.jar:$JAVA_HOME/lib/missioncontrol/plugins/com.jrockit.mc.common_5.5.0.165303.jar com.focusit.jfr.processor.ExceptionProcessor -f /tmp/record.jfr`

Data will be stored to result.csv

##Resources
###JFR Parsing
Markus Hirt's blog [http://hirt.se/blog] has a vast information about JFR  and JMC. 

Some posts about parsing jfrs:
* Parsing Flight Recordings – an Example [http://hirt.se/blog/?p=459]
* Using the Flight Recorder Parsers [http://hirt.se/blog/?p=446]

###Flamegraph
* FlameGraph by Brendann Gregg [https://github.com/brendangregg/FlameGraph]
* An example of how to convert JFR stack to folded stack [https://github.com/chrishantha/jfr-flame-graph]
