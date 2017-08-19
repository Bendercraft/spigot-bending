package net.bendercraft.spigot.bending.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class BendingAbilityLogFormatter extends Formatter {
	
	private final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
    @Override
    public String format(LogRecord log) {
        StringBuilder builder = new StringBuilder();
        Throwable thrown = log.getThrown();

        builder.append(date.format(log.getMillis()));
        builder.append(" [" + log.getLevel().getLocalizedName().toUpperCase() + "] ");
        builder.append(formatMessage(log));
        builder.append('\n');

        if (thrown != null) {
            StringWriter writer = new StringWriter();
            thrown.printStackTrace(new PrintWriter(writer));
            builder.append(writer);
        }

        return builder.toString();
    }

}
