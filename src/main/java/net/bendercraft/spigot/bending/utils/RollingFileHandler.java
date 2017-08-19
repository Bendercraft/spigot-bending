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
	private String current;

	public RollingFileHandler(File folder, String fileName) throws IOException, SecurityException {
		this.folder = folder;
		this.fileName = fileName;
		this.current = getFileName(new Date());
	}

	private String getFileName(Date date) {
		return this.folder.getPath()+"/"+fileName.replaceAll("%d", format.format(new Date()));
    }
	
	private FileHandler getHandler() throws SecurityException, IOException {
		String check = getFileName(new Date());
		if(!check.equals(current)) {
			handler.flush();
			handler.close();
			handler = null;
			current = check;
		}
		
		if(handler == null) {
			handler = new FileHandler(current, true);
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
