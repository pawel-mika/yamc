package pl.wcja.jcommon;

/**
 * 
 * @author <a href="mailto:ketonal80@gmail.com">Pablo</a>, wcja.pl
 *
 */
public enum Unit {
	SAMPLE("Sample", 0),
	SECOND("Second", 1),
	BEAT("Beat", 2);
	
	private String name = "";
	private int value = 0;
	
	private Unit(String name, int value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;  
	}
	
	public int getValue() {
		return value;
	}
}
