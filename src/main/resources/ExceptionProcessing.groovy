import com.jrockit.mc.flightrecorder.internal.model.FLRThread
import com.jrockit.mc.flightrecorder.internal.model.FLRType
import com.jrockit.mc.flightrecorder.spi.IEvent

import java.text.SimpleDateFormat

SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss,SSS");

Date fromDate = formatter.parse("01.01.1970 00:00:00,00");
Date toDate = formatter.parse("01.01.1970 00:00:30,00");
String threadFilter = null;

String EVENT_TYPE = "Java Exception";

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

        FLRType type = (FLRType) event.getValue(("thrownClass"));
        String typeName = type.getPackageName() + "." + type.getTypeName();
        String threadName = thread.getName();
    }
}
