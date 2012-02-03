package pl.wcja.yamc.frame;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public interface Undoable {

	public void undo();
	
	public void redo();
	
	public void getDescription();
	
}
