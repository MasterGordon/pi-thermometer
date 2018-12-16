package me.mastergordon.temperaturmesser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class HttpServer extends NanoHTTPD {

	private TemperaturMesser temperaturMesser;
	private static final Pattern PATTERNTEL = Pattern.compile("\\st=\\d*");

	public HttpServer(int port, TemperaturMesser temperaturMesser) throws IOException {
		super(port);
		this.temperaturMesser = temperaturMesser;
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		System.out.println("[Info] HttpServer is starting on " + port);
	}

	@Override
	public Response serve(IHTTPSession session) {
		@SuppressWarnings({ "deprecation", "unused" })
		Map<String, String> parms = session.getParms();

		String path = "html" + session.getUri();

		if (path.equals("html/"))
			path = "html/index.html";

		System.out.println("[HttpServer] Someone tries to load " + path);

		if (path.split("/")[1].equals("temperature")) {
			if (path.split("/").length == 2) {
				String sensorFile;
				try {
					sensorFile = new String(Files
							.readAllBytes(FileSystems.getDefault().getPath(TemperaturMesser.sensorPath1, "w1_slave")));
					Matcher matcherTel = PATTERNTEL.matcher(sensorFile);
					while (matcherTel.find()) {
						return newFixedLengthResponse(sensorFile.substring(matcherTel.start() + 3, matcherTel.end()));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (path.split("/").length == 3) {
				try {
					String labels = "\"labels\": [ ";
					String data1 = "\"data1\": [ ";
					String data2 = "\"data2\": [ ";
					int dataCount = Integer.parseInt(path.split("/")[2]);
					Statement stat = temperaturMesser.getConnection().createStatement();
					String sql = "select * from temperature ORDER BY date DESC limit " + dataCount + ";";
					ResultSet rs = stat.executeQuery(sql);
					while (rs.next()) {
						long dateUnformat = rs.getLong("date");
						double temp1 = rs.getInt("temperature1");
						double temp2 = rs.getInt("temperature2");
						Date date = new Date(dateUnformat);
						DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm.ss");
						formatter.setTimeZone(TimeZone.getTimeZone("UTC+1"));
						labels += "\"" + formatter.format(date) + "\",";
						data1 += "\"" + (temp1 / 1000) + "\",";
						data2 += "\"" + (temp2 / 1000) + "\",";
					}
					labels = labels.substring(0, labels.length() - 1) + "]";
					data1 = data1.substring(0, data1.length() - 1) + "]";
					data2 = data2.substring(0, data2.length() - 1) + "]";

					return newFixedLengthResponse("{" + labels + "," + data1 + "," + data2 + "}");
				} catch (Exception e) {

				}
			}

			if (path.split("/").length == 4) {
				try {
					String labels = "\"labels\": [ ";
					String data1 = "\"data1\": [ ";
					String data2 = "\"data2\": [ ";
					Statement stat = temperaturMesser.getConnection().createStatement();
					String sql = "select * from temperature WHERE DATE BETWEEN " + path.split("/")[2] + " AND "
							+ path.split("/")[3] + " ORDER BY date DESC;";
					ResultSet rs = stat.executeQuery(sql);
					while (rs.next()) {
						long dateUnformat = rs.getLong("date");
						double temp1 = rs.getInt("temperature1");
						double temp2 = rs.getInt("temperature2");
						Date date = new Date(dateUnformat);
						DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm.ss");
						formatter.setTimeZone(TimeZone.getTimeZone("CET"));
						labels += "\"" + formatter.format(date) + "\",";
						data1 += "\"" + (temp1 / 1000) + "\",";
						data2 += "\"" + (temp2 / 1000) + "\",";
					}
					labels = labels.substring(0, labels.length() - 1) + "]";
					data1 = data1.substring(0, data1.length() - 1) + "]";
					data2 = data2.substring(0, data2.length() - 1) + "]";

					return newFixedLengthResponse("{" + labels + "," + data1 + "," + data2 + "}");
				} catch (Exception e) {

				}
			}
		}

		try {
			InputStream is = getClass().getClassLoader()
					.getResourceAsStream("me/mastergordon/temperaturmesser/" + path);
			Response response = newFixedLengthResponse(Status.OK, URLConnection.guessContentTypeFromName(path), is,
					is.available());
			return response;
		} catch (Exception e) {
			return newFixedLengthResponse("ERROR 404");
		}
	}
}
