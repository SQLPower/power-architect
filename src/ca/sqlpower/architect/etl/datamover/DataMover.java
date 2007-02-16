package ca.sqlpower.architect.etl.datamover;
import java.io.UnsupportedEncodingException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DataMover {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String srcDriverClass = "oracle.jdbc.OracleDriver";
		String srcJdbcURL = "jdbc:oracle:thin:@ford:1521:orcl";
		String srcJdbcUser = "spforum";
		String srcPassword = "spforum";

		String dstDriverClass = "org.postgresql.Driver";
		String dstJdbcURL = "jdbc:postgresql://dhcp-119/sqlpower_forum";
		String dstJdbcUser = "sqlpower_forum";
		String dstPassword = "forum";

		Class.forName(srcDriverClass);
		Class.forName(dstDriverClass);

		Connection srcCon = DriverManager.getConnection(srcJdbcURL, srcJdbcUser, srcPassword);
		Connection dstCon = DriverManager.getConnection(dstJdbcURL, dstJdbcUser, dstPassword);

		//copyPosts(srcCon, dstCon);
		//copyPrivmsgs(srcCon, dstCon);
		updateSequences(srcCon, dstCon);
		
		dstCon.commit();
		
		dstCon.close();
		srcCon.close();
	}
	
	private static void updateSequences(Connection srcCon, Connection dstCon) throws SQLException {
		Statement srcStmt = srcCon.createStatement();
		Statement dstStmt = dstCon.createStatement();
		
		ResultSet rs = srcStmt.executeQuery(
				"SELECT sequence_name, last_number " +
				"FROM user_sequences");
		
		while(rs.next()) {
			String sequenceName = rs.getString(1);
			int lastNumber = rs.getInt(2);
			
			dstStmt.executeUpdate("ALTER SEQUENCE "+sequenceName+" RESTART WITH "+lastNumber);
		}
		
		dstStmt.close();
		rs.close();
		srcStmt.close();
	}

	private static void copyPosts(Connection srcCon, Connection dstCon) throws SQLException, UnsupportedEncodingException {
		Statement srcStmt = srcCon.createStatement();
		long totalPostBytes = 0;
		long startTime = System.currentTimeMillis();
		int rowCount = 0;
		
		ResultSet rs = srcStmt.executeQuery(
				"SELECT post_id, post_Text, post_subject" +
				" FROM jforum_posts_text");
		PreparedStatement pstmt = dstCon.prepareStatement(
				"INSERT INTO jforum_posts_text" +
				" (post_id, post_Text, post_subject)" +
				" VALUES (?, ?, ?)");
		
		while (rs.next()) {
			int id = rs.getInt(1);
			Blob blob = rs.getBlob(2);
			String subject = rs.getString(3);
			
			totalPostBytes += blob.length();
			String text = new String(blob.getBytes(1, (int) blob.length()), "utf-16");
			
			pstmt.setInt(1, id);
			pstmt.setString(2, text);
			pstmt.setString(3, subject);
			pstmt.execute();
			
			rowCount++;
		}
		pstmt.close();
		srcStmt.close();
		rs.close();
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Copied "+rowCount+" posts ("+totalPostBytes+" bytes) in "+elapsedTime+"ms");
	}

	private static void copyPrivmsgs(Connection srcCon, Connection dstCon) throws SQLException, UnsupportedEncodingException {
		Statement srcStmt = srcCon.createStatement();

		long totalMsgBytes = 0;
		long startTime = System.currentTimeMillis();
		int rowCount = 0;

		ResultSet rs = srcStmt.executeQuery(
				"SELECT privmsgs_id, privmsgs_Text" +
				" FROM jforum_privmsgs_text");
		PreparedStatement pstmt = dstCon.prepareStatement(
				"INSERT INTO jforum_privmsgs_text" +
				" (privmsgs_id, privmsgs_Text)" +
				" VALUES (?, ?)");
		
		while (rs.next()) {
			int id = rs.getInt(1);
			Blob blob = rs.getBlob(2);
			
			totalMsgBytes += blob.length();
			
			String text = new String(blob.getBytes(1, (int) blob.length()), "utf-16");
			
			pstmt.setInt(1, id);
			pstmt.setString(2, text);
			pstmt.execute();
			rowCount++;
		}
		pstmt.close();
		srcStmt.close();
		rs.close();
		
		long elapsedTime = System.currentTimeMillis() - startTime;
		System.out.println("Copied "+rowCount+" messages ("+totalMsgBytes+" bytes) in "+elapsedTime+"ms");

	}
}
