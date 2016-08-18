# JFR-Processor

Example project to view content of JFR file and visualize it as you wish.
It was inspired by [http://hirt.se/blog/?p=446]() and [https://github.com/chrishantha/jfr-flame-graph]() and of course [https://github.com/brendangregg/FlameGraph]()

To run you have to get install JDK 7u40+

1) you need to get a folded stacks from jfr. To get them run command like this (In example below I filter stack by date them acquired and thread name)
 
`java -jar target/processor-1.0-jar-with-dependencies.jar -f /tmp/record.jfr -df "17.08.2016 18:05:36,772" -dt "17.08.2016 18:05:56,772" -tnm "exec-164"`

2) make a flamegraph from output.txt (default file for folded stacks)
`./flamegraph.pl --minwidth 2 output.txt > flame.svg`
