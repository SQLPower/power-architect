package ca.sqlpower.architect.profile;

import java.util.List;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.profile.TableProfileResult;

public interface ProfileManagerInterface {

    List<TableProfileResult> getTableResults();

    public void remove(TableProfileResult victim) throws ArchitectException;

    public void clear();
}
