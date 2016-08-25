package com.focusit.jfr.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.internal.model.FLRThread;
import com.jrockit.mc.flightrecorder.internal.model.FLRType;
import com.jrockit.mc.flightrecorder.spi.EventOrder;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IView;

/**
 * Class that helps analyze exceptions that were created during a recording
 * Created by doki on 22.08.16.
 */
public class ExceptionProcessor
{
    @Parameter(names = { "-f", "--jfrdump" }, description = "Java Flight Recorder Dump", required = true)
    File jfrdump;

    @Parameter(names = { "-h", "--help" }, description = "Display Help")
    boolean help = false;

    @Parameter(names = { "--thread-class-count" })
    boolean threadClassCount;

    @Parameter(names = { "--class-count" })
    boolean classCount;

    public static void main(String args[])
    {
        ExceptionProcessor processor = new ExceptionProcessor();
        JCommander jcmdr = new JCommander(processor);

        jcmdr.setProgramName(ExceptionProcessor.class.getSimpleName());

        try
        {
            jcmdr.parse(args);
            processor.makeExceptionStat();
        }
        catch (ParameterException | IOException e)
        {
            System.out.println(e.getMessage());
        }

        if (processor.help)
        {
            jcmdr.usage();
            return;
        }

    }

    private static class ExceptionInfo
    {
        public String thrownClass;
        public String thread;

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (!(o instanceof ExceptionInfo))
                return false;

            ExceptionInfo that = (ExceptionInfo)o;

            if (thrownClass != null ? !thrownClass.equals(that.thrownClass) : that.thrownClass != null)
                return false;
            return thread != null ? thread.equals(that.thread) : that.thread == null;

        }

        @Override
        public int hashCode()
        {
            int result = thrownClass != null ? thrownClass.hashCode() : 0;
            result = 31 * result + (thread != null ? thread.hashCode() : 0);
            return result;
        }
    }

    private void makeExceptionStat() throws FileNotFoundException
    {
        HashMap<ExceptionInfo, Long> exceptions = new HashMap<>();

        FlightRecording recording = FlightRecordingLoader.loadFile(jfrdump);
        final String EVENT_TYPE = "Java Exception";
        IView view = recording.createView();
        view.setOrder(EventOrder.ASCENDING);

        /**
         * Every event of certain type has specific field set.
         * Get get information about available field you can inspect event.getValue("(eventType)")
         */
        for (IEvent event : view)
        {
            if (EVENT_TYPE.equals(event.getEventType().getName()))
            {
                FLRThread thread = (FLRThread)event.getValue("(thread)");
                FLRType type = (FLRType)event.getValue(("thrownClass"));
                String typeName = type.getPackageName() + "." + type.getTypeName();
                String threadName = thread.getName();

                if (threadClassCount || classCount)
                {
                    ExceptionInfo info = new ExceptionInfo();
                    info.thrownClass = typeName;

                    if (threadClassCount)
                    {
                        info.thread = threadName;
                    }
                    else
                    {
                        info.thread = "";
                    }

                    Long count = exceptions.get(info);
                    if (count == null)
                    {
                        count = new Long(1);
                    }
                    else
                    {
                        count++;
                    }
                    exceptions.put(info, count);
                }
            }
        }

        if (exceptions.size() == 0)
        {
            return;
        }

        try (PrintWriter writer = new PrintWriter("result.csv"))
        {
            for (ExceptionInfo info : exceptions.keySet())
            {
                writer.println(info.thread + ";" + info.thrownClass + ";" + exceptions.get(info));
            }
        }
    }
}
