package processing.core;

public class PApplet {

	public static void println(String string) {
		System.out.println(string);
	}

	public void registerMethod(String string, Object callback) {
		System.out.println("registerMethod " + string + " " + callback);
	}
}
