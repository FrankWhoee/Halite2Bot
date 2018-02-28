package hlt;

public class Turn {
	private static int turnNum = 0;
	
	public static int getTurn() {
		return turnNum;
	}
	
	public static void incrementTurn(int incrementation) {
		turnNum += incrementation;
	}
}
