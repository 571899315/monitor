package monitor.utils;

public class NumberUtil {

	
	public static double nullToZero(Number number) {
		return number == null?0:number.doubleValue();
	}
	
}
