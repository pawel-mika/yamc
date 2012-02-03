package pl.wcja.jcommon;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 
 * @author <a href="mailto:pawel.mika@geomar.pl">Pawe³ Mika</a>, Geomar SA
 * 
 */
public class OneLineFormatter extends Formatter {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	@Override
	public String format(LogRecord record) {
		String className = null;
		String methodName = null;
		if(record.getSourceClassName() != null) {
			className = record.getSourceClassName();
			methodName = record.getSourceMethodName();
		}
		
		String sDate = String.format("%s", new Date(record.getMillis()));
		
		String logMessage = String.format("%s %s %s %s %s %s", 
				getMsgInBrackets(sDate), 
				getMsgInBrackets(record.getLevel().getLocalizedName()), 
				getMsgInBrackets(className),
				getMsgInBrackets(methodName),
				getMsgInBrackets(formatMessage(record)),
				LINE_SEPARATOR);

		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				logMessage += sw.toString();
			} catch (Exception ex) {
				// ignore
			}
		}

		return logMessage;
	}
	
	/**
	 * 
	 * @param msg
	 * @return
	 */
	private String getMsgInBrackets(String msg) {
		return String.format("[%s]", msg);
	}
}
