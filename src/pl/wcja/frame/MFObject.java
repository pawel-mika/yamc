package pl.wcja.frame;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import pl.wcja.jcommon.OneLineFormatter;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public abstract class MFObject {
	
	protected IMainFrame mf = null;
	protected String logFileName = "YAB";
	private Logger mfLogger;
	private boolean loggingEnabled = true;
	
	public MFObject(IMainFrame mf) {
		this.mf = mf;
	}
	
	public void enableLogging(boolean enable) {
		loggingEnabled = enable;
	}
	
	protected void Log(Level level, String message) {
		if(mfLogger == null) {
			ConfigureLogger();
		}
		mfLogger.logp(level, getLoggerName(), Thread.currentThread().getStackTrace()[2].getMethodName(), message);
	}
	
	private void ConfigureLogger() {
		try {
			String loggerName = getLoggerName();
			LogManager.getLogManager().addLogger(Logger.getLogger(loggerName));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss");
			mfLogger = java.util.logging.LogManager.getLogManager().getLogger(loggerName);
			String cb = System.getProperty("user.dir");
			FileHandler fh = 
				new FileHandler(String.format("%s/%s %s.log", cb, logFileName, sdf.format(Calendar.getInstance().getTime())));
			OneLineFormatter formatter = new OneLineFormatter();
			fh.setFormatter(formatter);
			mfLogger.addHandler(fh);
			mfLogger.setLevel(Level.ALL);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected abstract String getLoggerName();
}
