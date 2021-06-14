package org.apache.bookkeeper.bookie;
import java.util.Scanner;



public class DebugTools {
	
	private DebugTools() {}
	
	public static void waitInput(){
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
		scan.next(); //qui il programma attende l'immissione dei dati
	}
}
