package me.mastergordon.temperaturmesser;

import java.io.IOException;

public class Main {
	
	public static void main(String[] args) throws IOException {
		System.out.println(System.currentTimeMillis());
		new TemperaturMesser();
	}

}
