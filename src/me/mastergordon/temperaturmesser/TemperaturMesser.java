package me.mastergordon.temperaturmesser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
				connection.createStatement().executeUpdate("insert into temperature values ("
						+ System.currentTimeMillis() + ", " + sensorFile1 + ", " + sensorFile2 + ")");
			} catch (IOException | SQLException e) {
				e.printStackTrace();
				System.out.println("[WARN] Sensor konnte nicht erreicht werden.");
			}
			String feinstaub = getFeinstaub();
			feinstaub = feinstaub.replaceAll(" ", "");
			double PM25 = Double.parseDouble(feinstaub.split("X")[0]);
			double PM10 = Double.parseDouble(feinstaub.split("X")[1]);
			try {
				connection.createStatement().executeUpdate("insert into feinstaub values (" + System.currentTimeMillis()
						+ ", " + PM25 + ", " + PM10 + ")");
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(1000 * 29);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	});
	public static double PM25;
	public static double PM10;
	private Thread feinstaubCheck = new Thread(() -> {
		while (true) {
			String feinstaub = "";
			try {
				feinstaub = getFeinstaub();
				feinstaub = feinstaub.replaceAll(" ", "");
				PM25 = Double.parseDouble(feinstaub.split("X")[0]);
				PM10 = Double.parseDouble(feinstaub.split("X")[1]);
			} catch (Exception e) {
			}
			try {
				Thread.sleep(1000 * 4);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	});

	public static String getFeinstaub() {
		String output = "";
		String s;
		Process p;
		try {
			p = Runtime.getRuntime().exec("./staub.py");
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((s = br.readLine()) != null)
				output += s;
			p.waitFor();
			p.destroy();
		} catch (Exception e) {
		}
		return output;
	}

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
		String createFeinstaubTable = "CREATE TABLE if not exists `feinstaub` (	`date` BIGINT, `feinstaub1` INTEGER, `feinstaub2` INTEGER);";

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

		try {
			stat = connection.createStatement();
			stat.executeUpdate(createFeinstaubTable);
			System.out.println("[Info] Feinstaub Table created!");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("[WARN] Feinstaub-Table konnte nicht erstellt werden!\nServer wird beendet...");
			System.exit(0);
		}

		sensorCheck.start();
		feinstaubCheck.start();
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
