import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static String url = "jdbc:mysql://127.0.0.1:3306/myDB";
	static String user = "remote";
	static String password = "1234";
	static Connection connection = null;

	public MyServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		connection = null;
		try {
			connection = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			System.out.println(url);
			e.printStackTrace();
			return;
		}
		printHTMLStart(response.getWriter());
		if (request.getParameter("pressed") != null) {
			if (request.getParameter("pressed").equals("Add")) {
				// we are adding something to the SQL table
				String sqlCode = "INSERT INTO myTable (USERN, ASSIGNM, WHICH, DAY, TIME) VALUES (?, ?, ?, ?, ?)";
				try {
					PreparedStatement preparedStatement = connection.prepareStatement(sqlCode);
					preparedStatement.setString(1, request.getParameter("user"));
					preparedStatement.setString(2,  request.getParameter("assignment") );
					if (request.getParameter("weekDue").equals("thisW")) {
						preparedStatement.setString(3, "0");
					} else {
						preparedStatement.setString(3, "1");
					}
					preparedStatement.setString(4,  request.getParameter("dayDue"));
					preparedStatement.setString(5, request.getParameter("timeDue"));
					preparedStatement.executeQuery();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// we are printing the schedule to the window
				if (request.getParameter("userGen") != null) {
					try {
						String selectSQL = "SELECT * FROM myTable WHERE USERN ='";
						selectSQL += request.getParameter("userGen") + "';";
						PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
						ResultSet rs = preparedStatement.executeQuery();
						int[] array = new int[14];
						for (int i = 0; i < array.length; i++) {
							array[i] = 6;
						}
						while (rs.next()) {
							String assign = rs.getString("ASSIGNM");
							String which = rs.getString("WHICH");
							String day = rs.getString("DAY");
							String time = rs.getString("TIME");
							int[] aHours = new int[14];
							boolean overdone = populate(array, aHours, assign, which, day, time);
							printRow(response.getWriter(), assign, aHours, overdone);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
			printHTMLEnd(response.getWriter());
		}
	}

	private void printRow(PrintWriter writer, String assign, int[] aHours, boolean overdone) {
		writer.println("<tr>");
		writer.println("<td>" + assign + "</td>");
		for (int i = 0; i < aHours.length; i++) {
			writer.println("<td>");
			if (aHours[i] == 0) {
			} else if (overdone) {
				writer.println("!" + aHours + "hr");
			} else {
				writer.println("<span style=\"display:inline-block; width: 5px;\"></span>" + aHours + "hr");
			}
			writer.println("</td>");
		}
		writer.println("</tr>");
	}

	private boolean populate(int[] array, int[] aHours, String assign, String which, String day, String timeS) {

		int time;
		try {
			time = Integer.parseInt(timeS);
		} catch (NumberFormatException e) {
			return false;
		}
		// first get the index
		int index = getIndex(which, day);
		if (index == -1) {
			return false;
		}
		boolean overbooked = false;
		for (int i = index; time > 0; i--) {
			if (checkEmpty(array, index)) {
				overbooked = true;
			}
			if (i < 0) {
				i = index;
			}
			if (array[i] > 0) {
				if (time >= 1) {
					array[i]--;
					aHours[i]++;
					time--;
				} else {
					array[i] -= time;
					aHours[i] = time;
					time = 0;
				}
			} else {
				if (time >= 1) {
					aHours[i]++;
					time--;
				} else {
					aHours[i] = time;
					time = 0;
				}
			}
		}
		return overbooked;

	}

	private boolean checkEmpty(int[] array, int index) {
		boolean empty = true;
		for (int i = 0; i < index; i++) {
			if (array[i] > 0) {
				empty = false;
				break;
			}
		}
		return empty;
	}

	private int getIndex(String which, String day) {
		int index = 0;
		try {
			if (Integer.parseInt(which) == 1) {
				index = 7;
			}
		} catch (NumberFormatException e) {
			return -1;
		}
		switch (day) {
		case "Mon":
			break;
		case "Tue":
			index += 1;
			break;
		case "Wed":
			index += 2;
			break;
		case "Thu":
			index += 3;
			break;
		case "Fri":
			index += 4;
			break;
		case "Sat":
			index += 5;
			break;
		case "Sun":
			index += 6;
			break;
		}
		return index;
	}

	private void printHTMLStart(PrintWriter printWriter) {

		printWriter.println("<!DOCTYPE html>");
		printWriter.println("<html>");
		printWriter.println("<head>");
		printWriter.println("<meta charset=\"ISO-8859-1\">");
		printWriter.println("<title>Scheduler Pro</title>");
		printWriter.println("</head>");
		printWriter.println("<body style=\"background-color:#46064c;\">");
		printWriter.println("<div style=\"color: #FFFFFF; font-size: 30px;\">");
		printWriter.println("Scheduler Pro </div>");
		printWriter.println("<br>");
		printWriter.println("<div style=\"color: #FFFFFF; font-size: 15px\">Add an assignment: </div>");
		printWriter.println("<form action=\"/shedule/MyServlet\" method=\"POST\">");
		printWriter.println("<table style=\"background-color:#e293dc\">");
		printWriter.println("<tr>");
		printWriter.println("<td>User:  <input id=\"user\" name=\"user\" type=\"text\" value=\"\" /></td>");
		printWriter.println(
				"<td>Assignment:   <input id=\"assignment\" name=\"assignment\" type=\"text\" value=\"\" /></td>");
		printWriter.println("</tr>");
		printWriter.println("<tr>");
		printWriter.println("<td>Week due: <input name=\"weekDue\" type=\"radio\" id=\"thisW\" value=\"thisW\"/>");
		printWriter.println("<label for=\"thisW\">This Week</label>");
		printWriter.println("<input name=\"weekDue\" type=\"radio\" id=\"nextW\" value=\"nextW\"/>");
		printWriter.println("<label for=\"nextW\">Next Week</label>");
		printWriter.println("</td>");
		printWriter.println("</tr>");
		printWriter.println("<tr>");
		printWriter.println("<td>Day Due:   <select name = \"dayDue\" id=\"dayDue\">");
		printWriter.println("<option value=\"Mon\">Monday</option>");
		printWriter.println("<option value=\"Tue\">Tuesday</option>");
		printWriter.println("<option value=\"Wed\">Wednesday</option>");
		printWriter.println("<option value=\"Thu\">Thursday</option>");
		printWriter.println("<option value=\"Fri\">Friday</option>");
		printWriter.println("<option value=\"Sat\">Saturday</option>");
		printWriter.println("<option value=\"Sun\">Sunday</option>");
		printWriter.println("</select>");
		printWriter.println("</td>");
		printWriter.println("<td>");
		printWriter.println("Time Needed:  <input id=\"timeDue\" name=\"timeDue\" type=\"text\" value=\"\" />");
		printWriter.println("</td>");
		printWriter.println("</tr>");
		printWriter.println("<tr><td>");
		printWriter.println("<input name = \"pressed\" type=\"submit\" value=\"Add\">");
		printWriter.println("</td>");
		printWriter.println("</tr>");
		printWriter.println("</table>");
		printWriter.println("</form>");
		printWriter.println("<br><br>");
		printWriter.println("<form action = \"/shedule/MyServlet\" method = \"POST\">");
		printWriter.println(
				"<div style=\"color: #FFFFFF; font-size: 15px\">Get schedule for User: <input id=\"userGen\" name=\"userGen\" type=\"text\" value=\"\" />");
		printWriter.println("<input name = \"pressed\" type=\"submit\" value=\"Generate\">");
		printWriter.println("</div>");
		printWriter.println("</form>");
		printWriter.println("<br><br><br>");
		printWriter.println("<div style=\"color: #FFFFFF; font-size: 20px\">Schedule:</div>");
		printWriter.println("<table border= \"1\" style=\"background-color:#e293dc\">");
		printWriter.println("<tr><td><span style=\"display:inline-block; width: 120px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Mon<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Tue<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Wed<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Thu<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Fri<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Sat<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Sun<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Mon<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Tue<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Wed<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Thu<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Fri<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Sat<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println(
				"<td><span style=\"display:inline-block; width: 5px;\"></span>Sun<span style=\"display:inline-block; width: 5px;\"></span></td>");
		printWriter.println("</tr>");
	}

	private void printHTMLEnd(PrintWriter response) {
		response.println("</table>");
		response.println("</body>");
		response.println("</html>");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	private String writeAssign(String user, String assign, String day, String thisOrNext, String timeS) {
		// String user, assign, day, thisOrNext, timeS;
		if (user == null || assign == null || day == null || thisOrNext == null || timeS == null) {
			return null;
		}
		int time;
		try {
			time = Integer.parseInt(timeS);
		} catch (NumberFormatException e) {
			return null;
		}

		String sqlCode = "INSERT INTO myTable (USERN, ASSIGNM, WHICH, DAY, TIME)";
		sqlCode += "VALUES (";
		sqlCode += user.substring(0, 31);
		sqlCode += assign.substring(0, 31);
		if (thisOrNext.equals("thisW")) {
			sqlCode += "0";
		} else {
			sqlCode += "1";
		}
		sqlCode += day;
		sqlCode += time;
		sqlCode += ");";

		return sqlCode;
	}

}