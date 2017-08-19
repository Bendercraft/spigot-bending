package net.bendercraft.spigot.bending.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.ErrorManager;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class RollingFileHandler extends Handler {

	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	private String fileName;
	private File folder;
	
	private FileHandler handler;
	private Date date;

	public RollingFileHandler(File folder, String fileName) throws IOException, SecurityException {
		this.folder = folder;
		this.fileName = fileName;
		this.date = new Date();
	}

	private String getFileName() {
		return this.folder.getPath()+"/"+fileName.replaceAll("%d", format.format(new Date()));
    }
	
	private FileHandler getHandler() throws SecurityException, IOException {
		Date now = new Date();
		if(now.getTime() - this.date.getTime() >= 86400000) {
			handler.flush();
			handler.close();
			handler = null;
			date = now;
		}
		
		if(handler == null) {
			handler = new FileHandler(getFileName(), true);
			handler.setLevel(getLevel());
			handler.setEncoding(getEncoding());
            handler.setFilter(getFilter());
            handler.setFormatter(getFormatter());
            handler.setErrorManager(getErrorManager());
		}
		return handler;
	}
	
	@Override
    public synchronized void publish(LogRecord r) {
        if (isLoggable(r)) {
            try {
                FileHandler h = getHandler();
                
                h.publish(r);
            } catch (IOException | SecurityException jm) {
                this.reportError(null, jm, ErrorManager.WRITE_FAILURE);
            }
        }
    }

	@Override
	public void close() throws SecurityException {
		super.setLevel(Level.OFF);
		if(handler != null) {
			handler.close();
		}
	}

	@Override
	public void flush() {
		if(handler != null) {
			handler.flush();
		}
	}
}
