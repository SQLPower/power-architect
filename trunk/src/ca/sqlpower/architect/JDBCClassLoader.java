package ca.sqlpower.architect;

import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URI;
import org.apache.log4j.Logger;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.sql.Connection;

public class JDBCClassLoader extends ClassLoader {
	private static final Logger logger = Logger.getLogger(JDBCClassLoader.class);

	/**
	 * This is the DriverManagerDiplomat class which was created by
	 * this classloader.  It is required by the getConnection method
	 * for getting around funny DriverManager limitations.
	 */
	private static Class diplomat;

	/**
	 * The ArchitectSession object that this ClassLoader gets its
	 * search path from.
	 */
	protected ArchitectSession session;

	/**
	 * Creates a new ClassLoader for the given ArchitectSession.
	 * ArchitectSessions and JDBCClassLoaders have a 1:1 relationship.
	 * This constructor is protected; the normal way to get an
	 * instance of JDBCClassLoader is via the ArchitectSession object.
	 */
	protected JDBCClassLoader(ArchitectSession session) {
		super(session.getClass().getClassLoader());
		this.session = session;
	}
	
	/**
	 * Searches the jar files listed in session.getDriverJarList() for the
	 * named class.  Throws ClassNotFoundException if the class can't
	 * be located.
	 */
	protected Class findClass(String name)
		throws ClassNotFoundException {
		System.out.println("Looking for class "+name);

		Iterator it = session.getDriverJarList().iterator();
		while (it.hasNext()) {
			try {
				File listedFile = new File((String) it.next());
				if (!listedFile.exists()) {
					logger.debug("Skipping non-existant JAR file "+listedFile.getPath());
					continue;
				}
				JarFile jf = new JarFile(listedFile);
				ZipEntry ent = jf.getEntry(name.replace('.','/')+".class");
				if (ent == null) {
					jf.close();
					continue;
				}
				byte[] buf = new byte[(int) ent.getSize()];
				InputStream is = jf.getInputStream(ent);
				int start = 0, n = 0;
				while ( (n = is.read(buf, start, buf.length-start)) > 0) {
					start += n;
				}
				if (start+n != ent.getSize()) {
					logger.warn("What gives?  ZipEntry "+ent.getName()+" is "+ent.getSize()+" bytes long, but we only read "+(start+n)+" bytes!");
				}
				jf.close();
				return defineClass(name, buf, 0, buf.length);
			} catch (IOException ex) {
				throw new ClassNotFoundException("IO Exception reading class from jar file", ex);
			}
		}
		throw new ClassNotFoundException
			("Could not locate class "+name
			 +" in any of the JDBC Driver JAR files "+session.getDriverJarList());
	}

	/**
	 * Attempts to locate the named file in the same JAR files that
	 * classes are loaded from.
	 */
	protected URL findResource(String name) {
		logger.debug("Looking for resource "+name);
		Iterator it = session.getDriverJarList().iterator();
		while (it.hasNext()) {
			File listedFile = new File((String) it.next());
			try {
				if (!listedFile.exists()) {
					logger.debug("Skipping non-existant JAR file "+listedFile.getPath());
					continue;
				}
				JarFile jf = new JarFile(listedFile);
				if (jf.getEntry(name) != null) {
					URI jarUri = listedFile.toURI();
					return new URL("jar:"+jarUri.toURL()+"!/"+name);
				}
			} catch (IOException ex) {
				logger.warn("IO Exception while searching "+listedFile.getPath()
							+" for resource "+name+". Continuing...", ex);
			}
		}
		return null;
	}

	/**
	 * This is a special method for getting a connection from a driver
	 * that may have been loaded by this classloader.  The regular
	 * java.sql.DriverManager doesn't allow connections if the
	 * caller's classloader can't find the driver class.
	 */
	public synchronized Connection getConnection(String url, String user, String password)
		throws SQLException {
		try {
			if (diplomat == null) {
				InputStream is = getResourceAsStream("ca/sqlpower/architect/altclassloader/DriverManagerDiplomat.altclass");
				byte[] buf = new byte[4000]; // XXX: I just assume this will be enough!
				int start = 0, n = 0;
				while ( (n = is.read(buf, start, buf.length-start)) > 0) {
					start += n;
				}
				
				diplomat = defineClass("ca.sqlpower.architect.altclassloader.DriverManagerDiplomat", buf, 0, start);
			}
			Object dmdInstance = diplomat.newInstance();
			Method getConnection = diplomat.getDeclaredMethod
				("getConnection", new Class[] { String.class, String.class, String.class } );
			Connection con = (Connection) getConnection.invoke
				(dmdInstance, new Object[] {url, user, password});
			return con;
		} catch (IllegalAccessException ex) {
			logger.warn("Couldn't access DriverManagerDiplomat via reflection", ex);
			SQLException sex = new SQLException("Couldn't create connection");
			sex.initCause(ex);
			throw sex;
		} catch (InvocationTargetException ex) {
			logger.warn("Couldn't create connection", ex);
			SQLException sex = new SQLException("Couldn't create connection");
			sex.initCause(ex.getTargetException());
			throw sex;
		} catch (InstantiationException ex) {
			logger.warn("Couldn't create connection", ex);
			SQLException sex = new SQLException("Couldn't create connection");
			sex.initCause(ex);
			throw sex;
		} catch (NoSuchMethodException ex) {
			logger.warn("Couldn't create connection", ex);
			SQLException sex = new SQLException("Couldn't create connection");
			sex.initCause(ex);
			throw sex;
		} catch (IOException ex) {
			logger.warn("Couldn't create connection", ex);
			SQLException sex = new SQLException("Couldn't create connection");
			sex.initCause(ex);
			throw sex;
		}
	}
}
