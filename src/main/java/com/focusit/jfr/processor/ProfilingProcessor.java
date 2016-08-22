package com.focusit.jfr.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.jrockit.mc.common.IMCFrame;
import com.jrockit.mc.common.IMCMethod;
import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace;
import com.jrockit.mc.flightrecorder.internal.model.FLRThread;
import com.jrockit.mc.flightrecorder.spi.EventOrder;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IView;

/**
 * Created by doki on 18.08.16.
 */
public class ProfilingProcessor
{
    @Parameter(names = { "-f", "--jfrdump" }, description = "Java Flight Recorder Dump", required = true)
    File jfrdump;

    @Parameter(names = { "-i", "--ignore-line-numbers" }, description = "Ignore Line Numbers in Stack Frame")
    boolean ignoreLineNumbers = false;

    @Parameter(names = { "-o", "--output" }, description = "Output file")
    File outputFile;

    @Parameter(names = { "-h", "--help" }, description = "Display Help")
    boolean help = false;

    @Parameter(names = { "-tnm", "--threadnamemask" })
    String threadFilter = "";

    @Parameter(names = { "-jtid", "--jvmthreadid" })
    String jvmThreadId = "";

    @Parameter(names = { "-otid", "--osthreadid" })
    String osThreadId = "";

    @Parameter(names = { "-df", "-datefrom" })
    String from = "";

    @Parameter(names = { "-dt", "-dateto" })
    String to = "";

    public void exportFoldedStacks() throws IOException, IOException, ParseException
    {
        FlightRecording recording = FlightRecordingLoader.loadFile(jfrdump);
        final String EVENT_TYPE = "Method Profiling Sample";
        Map<String, Integer> stackTraceMap = new LinkedHashMap<>();
        IView view = recording.createView();
        view.setOrder(EventOrder.ASCENDING);

        Date fromDate = null;
        Date toDate = null;

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");
        if (from != null && !from.trim().isEmpty())
        {
            fromDate = formatter.parse(from);
        }

        if (to != null && !to.trim().isEmpty())
        {
            toDate = formatter.parse(to);
        }

        for (IEvent event : view)
        {
            // Filter for Method Profiling Sample Events
            if (EVENT_TYPE.equals(event.getEventType().getName()))
            {
                FLRThread thread = (FLRThread)event.getValue("(thread)");
                if (threadFilter != null && thread != null && !threadFilter.trim().isEmpty()
                        && !thread.getName().contains(threadFilter))
                {
                    continue;
                }

                if (fromDate != null)
                {
                    if (event.getStartTimestamp() / 1000000L < fromDate.getTime())
                    {
                        System.out.println("Skipping " + new Date(event.getStartTimestamp() / 1000000L).toString());
                        continue;
                    }
                }

                if (toDate != null)
                {
                    if (event.getStartTimestamp() / 1000000L > toDate.getTime())
                    {
                        System.out.println("Skipping " + new Date(event.getStartTimestamp() / 1000000L).toString());
                        continue;
                    }
                }

                // Get Stack Trace from the event. Field ID was identified from
                // event.getEventType().getFieldIdentifiers()
                FLRStackTrace flrStackTrace = (FLRStackTrace)event.getValue("(stackTrace)");

                Stack<String> stack = new Stack<>();
                for (IMCFrame frame : flrStackTrace.getFrames())
                {
                    StringBuilder methodBuilder = new StringBuilder();
                    IMCMethod method = frame.getMethod();

                    if (method == null)
                    {
                        continue;
                    }

                    methodBuilder.append(method.getHumanReadable(false, true, true, true, true, true));
                    if (!ignoreLineNumbers)
                    {
                        methodBuilder.append(":");
                        methodBuilder.append(frame.getFrameLineNumber());
                    }
                    // Push method to a stack
                    stack.push(methodBuilder.toString());
                }

                // StringBuilder to keep stack trace
                StringBuilder stackTraceBuilder = new StringBuilder();
                boolean appendSemicolon = false;
                while (!stack.empty())
                {
                    if (appendSemicolon)
                    {
                        stackTraceBuilder.append(";");
                    }
                    else
                    {
                        appendSemicolon = true;
                    }
                    stackTraceBuilder.append(stack.pop());
                }
                String stackTrace = stackTraceBuilder.toString();
                Integer count = stackTraceMap.get(stackTrace);
                if (count == null)
                {
                    count = 1;
                }
                else
                {
                    count++;
                }
                stackTraceMap.put(stackTrace, count);
            }
        }

        FileWriter fileWriter;
        if (outputFile == null)
        {
            outputFile = new File("output.txt");
        }
        fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (Map.Entry<String, Integer> entry : stackTraceMap.entrySet())
        {
            bufferedWriter.write(String.format("%s %d%n", entry.getKey(), entry.getValue()));
        }
        bufferedWriter.close();
    }

    public static void main(String[] args) throws ParseException
    {
        ProfilingProcessor proc = new ProfilingProcessor();
        final JCommander jcmdr = new JCommander(proc);
        jcmdr.setProgramName(ProfilingProcessor.class.getSimpleName());

        try
        {
            jcmdr.parse(args);
            proc.exportFoldedStacks();
        }
        catch (ParameterException | IOException e)
        {
            System.out.println(e.getMessage());
        }

        if (proc.help)
        {
            jcmdr.usage();
            return;
        }
    }
}
