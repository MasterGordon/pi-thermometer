package me.mastergordon.temperaturmesser;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemperaturMesser {

	private Connection connection;
	static String sensorPath1 = "/sys/bus/w1/devices/28-0517b11476ff";
	static String sensorPath2 = "/sys/bus/w1/devices/28-0517b11000ff";
	private static final Pattern PATTERNTEL = Pattern.compile("\\st=\\d*");
	private Thread sensorCheck = new Thread(() -> {
		while (true) {
			try {
				String sensorFile2 = new String(
						Files.readAllBytes(FileSystems.getDefault().getPath(sensorPath2, "w1_slave")));
				String sensorFile1 = new String(
						Files.readAllBytes(FileSystems.getDefault().getPath(sensorPath1, "w1_slave")));
				Matcher matcherSensor1 = PATTERNTEL.matcher(sensorFile1);
				Matcher matcherSensor2 = PATTERNTEL.matcher(sensorFile2);
				while (matcherSensor1.find()) {
					sensorFile1 = sensorFile1.substring(matcherSensor1.start() + 3, matcherSensor1.end());
				}
				while (matcherSensor2.find()) {
					sensorFile2 = sensorFile2.substring(matcherSensor2.start() + 3, matcherSensor2.end());
				}
				connection.createStatement()
				.executeUpdate("insert into temperature values (" + System.currentTimeMillis() + ", "
						+ sensorFile1 + ", "+sensorFile2+")");
			} catch (IOException | SQLException e) {
				e.printStackTrace();
				System.out.println("[WARN] Sensor konnte nicht erreicht werden.");
			}
			try {
				Thread.sleep(1000 * 30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	});

	public TemperaturMesser() throws IOException {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:temperature.db");
			System.out.println("[Info] Connected to Database!");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(
					"[WARN] Verbindung zur Datenbank konnte nicht hergestellt werden!\nServer wird beendet...");
			System.exit(0);
		}
		String createTemperatureTable = "CREATE TABLE if not exists `temperature` (	`date` BIGINT, `temperature1` INTEGER, `temperature2` INTEGER);";

		Statement stat;
		try {
			stat = connection.createStatement();
			stat.executeUpdate(createTemperatureTable);
			System.out.println("[Info] Temperature Table created!");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("[WARN] Temperature-Table konnte nicht erstellt werden!\nServer wird beendet...");
			System.exit(0);
		}

		sensorCheck.start();
		new HttpServer(8080, this);
		try {
			String command = "python /home/pi/termo/drawip.py";
			Runtime.getRuntime().exec(command);
		} catch (Exception e1) {
			System.out.println("Can't show IP");
			e1.printStackTrace();
		}
	}

	public Connection getConnection() {
		return connection;
	}
}
