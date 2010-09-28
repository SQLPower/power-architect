package ca.sqlpower.architect.etl;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * PLUtils is a collection of utility methods for interfacing with the Power*Loader.
 */
public class PLUtils {

	private static final Logger logger = Logger.getLogger(PLUtils.class);

	/**
	 * Maximum length (in characters) of a PL identifier (transaction
	 * name, job name, etc).
	 */
	public static final int MAX_PLID_LENGTH = 80;


	/**
	 * Store the last time we loaded PL.INI from disk
	 */
	private static java.util.Date plLastReadTimestamp = new Date(0);

	/** PLUtils is a non-instantiable class. */
	private PLUtils() {
        // this method doesn't get called
    }

	/**
	 * Creates a list of PLConnectionSpec objects from the database
	 * connections described in the PL.INI file at the given path.
	 */
	public static List parsePlDotIni(String plDotIniPath)
		throws FileNotFoundException, IOException {

		List plSpecs = new ArrayList();
		PLConnectionSpec currentSpec = null;
		File inputFile = new File(plDotIniPath);
		plLastReadTimestamp = new Date(inputFile.lastModified());		
		BufferedReader in = new BufferedReader(new FileReader(inputFile));
		String line = null;

		while ((line = in.readLine()) != null) {
			if (line.startsWith("[Databases")) {
				currentSpec =  new PLConnectionSpec();
				plSpecs.add(currentSpec);
			} else if (currentSpec != null) {
				int equalsIdx = line.indexOf('=');
				if (equalsIdx > 0) {
					String key = line.substring(0, equalsIdx);
					String value = line.substring(equalsIdx+1, line.length());
					currentSpec.setProperty(key, value);
					logger.debug("key="+key+",val="+value);
				} else {
					logger.debug("pl.ini entry lacks = sign: "+line);
				}
			} else {
				logger.debug("Skipping "+line);
			}
		}
		in.close();
		return plSpecs;
	}

	/**
	 * Mangles the given string into a valid PL identifier (no spaces,
	 * at most 80 characters long, all uppercase).
	 */
	public static String toPLIdentifier(String text) {
		if (text.length() > MAX_PLID_LENGTH) text = text.substring(0, MAX_PLID_LENGTH);
		StringBuffer plid = new StringBuffer(text.toUpperCase());
		for (int i = 0, n = plid.length(); i < n; i++) {
			if (Character.isWhitespace(plid.charAt(i))) {
				plid.setCharAt(i, '_');
			}
		}
		return plid.toString();
	}

	/**
	 * Decrypts a PL.INI password.  The correct argument for
	 * <code>number</code> is 9.
	 */
	public static String decryptPlIniPassword(int number, String encryptedPassword) {
		StringBuffer password = new StringBuffer(encryptedPassword.length());
		
		for (int i = 0, n = encryptedPassword.length(); i < n; i++) {
			logger.debug("input char = "+encryptedPassword.charAt(i)+"(" + (int)encryptedPassword.charAt(i) + ")" );
			int temp = ((encryptedPassword.charAt(i) & 0x00ff) ^ (10 - number));

			if (i % 2 == 1) {
				temp += number;
			} else {
				temp -= number;
			}
			logger.debug("output char = " + (char) temp + "(" + temp + ")");
			password.append((char) temp);
		}

		return password.toString();
	}

	public static boolean plDotIniHasChanged(String plDotIniPath) {
		File inputFile = new File(plDotIniPath);
		boolean retVal = false;
		// logger.debug("last mod=" + new Date(inputFile.lastModified()) + ", currTimestamp=" + plLastReadTimestamp);
		if (inputFile.lastModified() > plLastReadTimestamp.getTime()) {
			retVal = true;
		}
		inputFile = null; // is this necessary?
		return retVal;
	}
}
