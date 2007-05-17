package ca.sqlpower.architect.profile;

import java.io.OutputStream;
import java.util.List;

/**
 * The general contract of a class that can format a profile results.
 * Implementations may format the profile results in HTML, XML, PDF, CSV, etc.
 * @author ian
 */
public interface ProfileFormat {

    /**
     * Format a set of profile results to an open OutputStream.
     * The "out" parameter is an OutputStream not a Writer since some
     * of the formats (e.g., PDF) are binary formats.
     * An outline of one possible algorithm is:
     * <pre>
     * // Generate headers ...
     * for (ProfileResult res : profile) {
     *      for (ProfileColumn pc : ProfileColumn.values()) {
     *          switch (pc) {
     *              // format each column here
     *      }
     * }
     * @param out   The file to write to.
     * @param profile The list of SQL Tables
     * @param pm    The ProfileManager which generated this Profile
     */
    public void format(OutputStream out, List<ProfileResult> profile) throws Exception;
}
