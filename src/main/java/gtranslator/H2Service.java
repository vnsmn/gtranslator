package gtranslator;

import gtranslator.annotation.Singelton;

import java.io.File;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.h2.jdbcx.JdbcConnectionPool;

//@Singelton
public class H2Service implements Configurable {
	static final Logger logger = Logger.getLogger(H2Service.class);
	private static final String DB_DRIVER = "org.h2.Driver";

	public static void main(String[] args) throws Exception {
		// DeleteDbFiles.execute("~/gtranslator", null, false);
		/*
		 * H2Helper.INSTANCE.createDb();
		 * 
		 * System.out.println(H2Helper.INSTANCE.addDic("1", "test1"));
		 * System.out.println(H2Helper.INSTANCE.addDic("2", "test2"));
		 * System.out.println(H2Helper.INSTANCE.addDic("3", "test3"));
		 * System.out.println(H2Helper.INSTANCE.addDic("4", "test4"));
		 * System.out.println(H2Helper.INSTANCE.addDic("5", "test5"));
		 * H2Helper.INSTANCE.print(); H2Helper.INSTANCE.deleteDic("3");
		 * H2Helper.INSTANCE.print();
		 * System.out.println(H2Helper.INSTANCE.getDic("5"));
		 * H2Helper.INSTANCE.updateDic("3", "test3"); H2Helper.INSTANCE.print();
		 * H2Helper.INSTANCE.updateDic("5", "test5test5");
		 * H2Helper.INSTANCE.print(); H2Helper.INSTANCE.close();
		 * H2Helper.INSTANCE.print();
		 */
	}

	private JdbcConnectionPool cp;

	private String urlConnect;

	private H2Service() {
	}

	@Singelton
	public static void createSingelton() {
		Registry.INSTANCE.add(new H2Service());
	}

	public boolean addDic(String key, String value) {
		PreparedStatement insertPreparedStatement = null;
		boolean result = false;

		String insertQuery = "INSERT INTO DICTIONARY(phrase_key, phrase_value) values(?,?)";
		try (Connection connection = getConnection()) {
			connection.setAutoCommit(false);
			String phrase_key = StringUtils.defaultIfBlank(key, "").trim()
					.toLowerCase();

			if (!existsDic(phrase_key)) {
				insertPreparedStatement = connection
						.prepareStatement(insertQuery);
				insertPreparedStatement.setString(1, phrase_key);
				insertPreparedStatement.setString(2, value);
				result = insertPreparedStatement.executeUpdate() > 0;
				insertPreparedStatement.close();
			}
			connection.commit();
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}

	@Override
	public void close() {
		cp.dispose();
	}

	public boolean deleteDic(String key) {
		PreparedStatement deletePreparedStatement = null;
		boolean result = false;

		String insertQuery = "DELETE FROM DICTIONARY WHERE phrase_key = ?";
		try (Connection connection = getConnection()) {
			connection.setAutoCommit(false);
			String phrase_key = StringUtils.defaultIfBlank(key, "").trim()
					.toLowerCase();

			deletePreparedStatement = connection.prepareStatement(insertQuery);
			deletePreparedStatement.setString(1, phrase_key);
			result = deletePreparedStatement.executeUpdate() > 0;
			deletePreparedStatement.close();
			connection.commit();
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}

	public boolean existsDic(String key) {
		return !StringUtils.isBlank(getDic(key));
	}

	public Connection getConnection() throws SQLException {
		return cp.getConnection();
	}

	public String getDic(String key) {
		String selectQuery = "select * from DICTIONARY where phrase_key = ?";
		String result = null;

		try (Connection connection = getConnection()) {
			String phrase_key = StringUtils.defaultIfBlank(key, "").trim()
					.toLowerCase();

			PreparedStatement selectPreparedStatement = connection
					.prepareStatement(selectQuery);
			selectPreparedStatement.setString(1, phrase_key);
			ResultSet rs = selectPreparedStatement.executeQuery();
			if (rs.next()) {
				result = rs.getString("phrase_value");
			}
			selectPreparedStatement.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}

	public Map<String, String> getsDic() {
		String selectQuery = "select * from DICTIONARY";
		Map<String, String> result = new HashMap<>();

		try (Connection connection = getConnection()) {
			PreparedStatement selectPreparedStatement = connection
					.prepareStatement(selectQuery);
			ResultSet rs = selectPreparedStatement.executeQuery();
			while (rs.next()) {
				result.put(rs.getString("phrase_key"),
						rs.getString("phrase_value"));
			}
			selectPreparedStatement.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}

	@Override
	public void init(AppProperties appProperties) {
		try {
			File dirDb = Paths.get(appProperties.getDictionaryDirPath(), "db")
					.toFile();
			if (dirDb.exists()) {
				dirDb.mkdirs();
			}
			urlConnect = "jdbc:h2:" + dirDb.getAbsolutePath()
					+ "/gtranslator;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE;";
			try {
				Class.forName(DB_DRIVER);
			} catch (ClassNotFoundException e) {
				System.out.println(e.getMessage());
			}
			cp = JdbcConnectionPool.create(urlConnect, "sa", "sa");
			createDb();
		} catch (SQLException ex) {
			System.exit(-1);
		}
	}

	public void print() throws SQLException {
		Map<String, String> tabs = getsDic();
		System.out.println("**********");
		TreeMap<String, String> treeMap = new TreeMap<String, String>(tabs);
		for (Entry<String, String> ent : treeMap.entrySet()) {
			System.out.println(String.format("%s = %s", ent.getKey(),
					ent.getValue()));
		}
	}

	public boolean updateDic(String key, String value) {
		PreparedStatement updatePreparedStatement = null;
		boolean result = false;

		String insertQuery = "UPDATE DICTIONARY SET phrase_value = ? WHERE phrase_key = ?";
		try (Connection connection = getConnection()) {
			connection.setAutoCommit(false);
			String phrase_key = StringUtils.defaultIfBlank(key, "").trim()
					.toLowerCase();

			updatePreparedStatement = connection.prepareStatement(insertQuery);
			updatePreparedStatement.setString(1, value);
			updatePreparedStatement.setString(2, phrase_key);
			result = updatePreparedStatement.executeUpdate() > 0;
			updatePreparedStatement.close();
			connection.commit();
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}
	
	public boolean addPhon(String word, String phon) {
		PreparedStatement insertPreparedStatement = null;
		boolean result = false;

		String insertQuery = "INSERT INTO PHONETICS(word, phon) values(?,?)";
		try (Connection connection = getConnection()) {			
			if (getPhon(word) == null) {
				connection.setAutoCommit(false);
				insertPreparedStatement = connection
						.prepareStatement(insertQuery);
				insertPreparedStatement.setString(1, word);
				insertPreparedStatement.setString(2, phon);
				result = insertPreparedStatement.executeUpdate() > 0;
				insertPreparedStatement.close();
				connection.commit();
			} else {
				updatePhon(word, phon);
			}			
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}
	
	public boolean updatePhon(String word, String phon) {
		PreparedStatement updatePreparedStatement = null;
		boolean result = false;

		String insertQuery = "UPDATE PHONETICS SET phon = ? WHERE word = ?";
		try (Connection connection = getConnection()) {
			connection.setAutoCommit(false);
				updatePreparedStatement = connection.prepareStatement(insertQuery);
			updatePreparedStatement.setString(1, phon);
			updatePreparedStatement.setString(2, word);
			result = updatePreparedStatement.executeUpdate() > 0;
			updatePreparedStatement.close();
			connection.commit();
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}

	public String getPhon(String word) {
		String selectQuery = "select * from PHONETICS where word = ?";
		String result = null;

		try (Connection connection = getConnection()) {
			PreparedStatement selectPreparedStatement = connection
					.prepareStatement(selectQuery);
			selectPreparedStatement.setString(1, word);
			ResultSet rs = selectPreparedStatement.executeQuery();
			if (rs.next()) {
				result = rs.getString("phon");
			}
			selectPreparedStatement.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return result;
	}
	
	private void createDb() throws SQLException {
		Connection connection = getConnection();
		PreparedStatement createPreparedStatement = null;

		String createPhonTableQuery = "CREATE TABLE IF NOT EXISTS PHONETICS(id identity primary key, word varchar UNIQUE, phon varchar)";
		String createTableQuery = "CREATE TABLE IF NOT EXISTS DICTIONARY(id identity primary key, phrase_key varchar UNIQUE, phrase_value varchar)";
		String createIndexQuery = "CREATE INDEX IF NOT EXISTS IDX_DICTIONARY ON DICTIONARY(phrase_key)";
		try {
			connection.setAutoCommit(false);

			createPreparedStatement = connection
					.prepareStatement(createTableQuery);
			createPreparedStatement.executeUpdate();
			createPreparedStatement.close();

			createPreparedStatement = connection
					.prepareStatement(createIndexQuery);
			createPreparedStatement.executeUpdate();
			createPreparedStatement.close();
			
			createPreparedStatement = connection
					.prepareStatement(createPhonTableQuery);
			createPreparedStatement.executeUpdate();
			createPreparedStatement.close();

			connection.commit();
		} catch (SQLException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			connection.close();
		}
	}
}
