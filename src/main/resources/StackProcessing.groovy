import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace
import com.jrockit.mc.flightrecorder.internal.model.FLRThread
import com.jrockit.mc.flightrecorder.spi.IEvent

import java.text.SimpleDateFormat

SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

Date fromDate = formatter.parse("01.01.1970 00:00:00,00");
Date toDate = formatter.parse("01.01.1970 00:00:30,00");
String threadFilter = null;

String EVENT_TYPE = "Method Profiling Sample";

for (IEvent event : view) {
    if (EVENT_TYPE.equals(event.getEventType().getName())) {
        FLRThread thread = (FLRThread) event.getValue("(thread)");
        if (threadFilter != null && thread != null && !threadFilter.trim().isEmpty()
                && !thread.getName().contains(threadFilter)) {
            continue;
        }

        if (fromDate != null) {
            if (event.getStartTimestamp() / 1000000L < fromDate.getTime()) {
                long skipMs = event.getStartTimestamp() / 1000000;
                System.out.println("Skipping " + new Date(skipMs).toString());
                continue;
            }
        }

        if (toDate != null) {
            if (event.getStartTimestamp() / 1000000L > toDate.getTime()) {
                long skipMs = event.getStartTimestamp() / 1000000;
                System.out.println("Skipping " + new Date(skipMs).toString());
                continue;
            }
        }

        // Get Stack Trace from the event. Field ID was identified from
        // event.getEventType().getFieldIdentifiers()
        FLRStackTrace flrStackTrace = (FLRStackTrace) event.getValue("(stackTrace)");
    }
}