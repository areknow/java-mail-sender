//package edu.uci.ics.crawler4j.util;

//import edu.uci.ics.crawler4j.util.DB;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

	/*--------TASKS----------------------
	 *
	 *   change users -> fake users
	 *
	 */

	public static DB db = new DB();

	//main controller
	public static void main(String[] args) throws SQLException, IOException {

		ArrayList<String> IDlist = new ArrayList<String>();
		// build list of ID's
		IDlist = getIDs();
		// count ID list
		int usernum = IDlist.size();
		System.out.println("~~~~~~~~   INITIATING VIZORTEAM MAIL DAEMON");
		System.out.println("~~~~~~~~   There are "+usernum+" users");
		System.out.println("");
		int countr = 0;

		// for the length of the arraylist, send each person an email
		for (int i=0; i < IDlist.size(); i++) { 

			String userid = IDlist.get(i);
			System.out.println("~~~~~~~~   ID: "+userid+" is email: "+ getEmail(userid));
			String DLcount = getDLC(userid);
			System.out.println("~~~~~~~~   Amount of unopened proposals: "+DLcount);

			if (DLcount.equals("0")) {
				System.out.println("~~~~~~~~   No reason to send mail");
				System.out.println("");
			}
			else {
				// send mail to all 'username' in 'users' table..
				sendMail(getEmail(userid), userid);
				countr ++;
			}

		}
		System.out.println("~~~~~~~~   Closing mail session. "+countr+" messages sent.");



		//        ..send test email to a single user..
		//        sendMail("arnaudcrowther@gmail.com"); 

	}





	// return a list of ID's
	public static ArrayList<String> getIDs() throws SQLException, IOException {

		String sql = "SELECT id FROM fakeusers";
		ResultSet rs = db.runSql(sql);
		ArrayList<String> rowValues = new ArrayList<String>();

		// while results are fetched, add ID's to arraylist
		while (rs.next()) {
			rowValues.add(rs.getString(1));
		}
		return rowValues;
	}





	// return a specific email for ID
	public static String getEmail(String ID) throws SQLException, IOException {

		String sql = "SELECT username FROM fakeusers WHERE id="+ID;
		ResultSet rs = db.runSql(sql);
		String email = "";

		// while results are fetched, store email as string
		while (rs.next()) {
			email = rs.getString("username");
		}
		return email;
	}





	// return a count of non-downloaded files
	public static String getDLC(String ID) throws SQLException, IOException {

		ArrayList<String> rowValues = new ArrayList<String>();
		String sql = "SELECT down FROM id"+ID;

		try { // try to get values from down column
			ResultSet rs = db.runSql(sql);
			while (rs.next()) {
				rowValues.add(rs.getString(1));
			}
		} catch (Exception e) { // no values means no proposals
			System.out.println("~~~~~~~~   User has no saved proposals");
		}

		// count the amount of proposals
		String dlc = Integer.toString(rowValues.size());
		return dlc;
	}





	// construct and send emails
	public static void sendMail(String email, String id) throws UnsupportedEncodingException, SQLException, IOException {

		System.out.println("~~~~~~~~   Attempting to send mail");
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		String msgBody = "<h3>You have "+getDLC(id)+" unopened proposal(s) in your OURS profile</h3>" +
				"See them now at the <a href=\"http://vizorteam.com/crawl\">OU Research System</a> Homepage";
		String from = "crawler@vizorteam.com";
		String allias = "OURS Mail System";
		String subject = "OU Research System (New Proposals)";

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from, allias));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			msg.setSubject(subject);
			msg.setContent(msgBody,"text/html");
			Transport.send(msg);
			System.out.println("~~~~~~~~   Successfully sent mail");
			System.out.println("");

		} catch (AddressException e) {
			System.out.println(e);
			System.out.println("~~~~~~~~   Failed to send mail! (address error)");
		} catch (MessagingException e) {
			System.out.println(e);
			System.out.println("~~~~~~~~   Failed to send mail! (transport error)");
		}

	}
}
