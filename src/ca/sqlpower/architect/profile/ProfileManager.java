package ca.sqlpower.architect.profile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLObject;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.architect.swingui.Monitorable;

public class ProfileManager {

    private static final Logger logger = Logger.getLogger(ProfileManager.class);

    private final Map<SQLObject, ProfileResult> results =
        new HashMap<SQLObject, ProfileResult>();

    private boolean findingMin = true;

    private boolean findingMax = true;

    private boolean findingAvg = true;

    private boolean findingMinLength = true;

    private boolean findingMaxLength = true;

    private boolean findingAvgLength = true;

    private boolean findingDistinctCount = true;

    private boolean findingNullCount = true;

    private boolean findingTopTen = true;

    private ThreadLocal<Integer> jobSize = new ThreadLocal<Integer>();

    private int progress;

    private boolean finished;

    private ThreadLocal<String> currentProfilingTable =
        new ThreadLocal<String>();

    private boolean userCancel;

    private int topNCount = 10;

    public void putResult(ProfileResult profileResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("[instance "+hashCode()+"]" +
                    " Adding new profile result for "+profileResult.getProfiledObject().getName()+
                    " existing profile count: "+results.size());
        }
        results.put(profileResult.getProfiledObject(), profileResult);
    }

    public ProfileResult getResult(SQLObject sqlObject) {
        return results.get(sqlObject);
    }

    /**
     * Creates a new profile object for the given SQL Object.
     *
     * @param obj The database object you want to profile.
     * @throws ArchitectException
     * @throws SQLException
     */
    public Monitorable asynchCreateProfiles(final Collection<SQLTable> tables) throws SQLException, ArchitectException {
        Monitorable m = new Monitorable() {

            public Integer getJobSize() throws ArchitectException {
                return jobSize.get();
            }

            public String getMessage() {
                return "Profiling: " + currentProfilingTable.get();
            }

            public int getProgress() throws ArchitectException {
                return progress;
            }

            public boolean hasStarted() throws ArchitectException {
                return true;
            }

            public boolean isFinished() throws ArchitectException {
                // TODO Auto-generated method stub
                return false;
            }

            public void setCancelled(boolean cancelled) {
                // TODO Auto-generated method stub
            }

        };
        new Thread() {
            public void run() {
                try {
                    createProfiles(tables);
                } catch (SQLException e) {
                    e.printStackTrace(); // XXX save me
                } catch (ArchitectException e) {
                    e.printStackTrace(); // XXX save me
                }
            }
        }.start();
        return m;
    }

    /**
     * Creates a new profile object for the given SQL Object.
     *
     * @param tables The database table(s) you want to profile.
     * @throws ArchitectException
     * @throws SQLException
     */
     public synchronized void createProfiles(Collection<SQLTable> tables) throws SQLException, ArchitectException {
         int objCount = 0;
         for (SQLTable t : tables) {
             objCount += 1;
             objCount += t.getColumns().size();
         }
         jobSize.set(Integer.valueOf(objCount));
         finished = false;
         progress = 0;
         userCancel = false;
         logger.debug("Job Size:"+jobSize.get()+"    progress="+progress);

        try {
            for (SQLTable t : tables) {
                    currentProfilingTable.set(t.getName());

                    if (userCancel)
                        break;

                    ProfileResult tableResult = new TableProfileResult(t);
                    tableResult.populate();
                    putResult(tableResult);
                    progress++;
                    logger.debug("Job Size:"+jobSize.get()+"    progress="+progress);

                    List<SQLColumn> columns = t.getColumns();
                    if ( columns.size() == 0 )
                        return;
                    DDLGenerator ddlg = getDDLGenerator(columns.get(0));
                    for (SQLColumn col : columns ) {
                        if (userCancel) {
                            remove(col.getParentTable());
                            return;
                        }
                        ProfileResult columnResult = new ColumnProfileResult(col, this, ddlg);
                        columnResult.populate();
                        putResult(columnResult);
                        progress++;
                    }
            }
        } finally {
            finished = true;
            jobSize.set(null);

            fireProfileAddedEvent(new ProfileChangeEvent(this, null));
        }
    }

    public boolean isCancelled() {
        return userCancel;
    }

    public void setCancelled(boolean userCancel) {
        this.userCancel = userCancel;
    }

    private DDLGenerator getDDLGenerator(SQLColumn col1) throws ArchitectException {
        DDLGenerator ddlg = null;

        try {
            ddlg = (DDLGenerator) DDLUtils.createDDLGenerator(
                    col1.getParentTable().getParentDatabase().getDataSource());
        } catch (InstantiationException e1) {
            throw new ArchitectException("problem running Profile Manager", e1);
        } catch ( IllegalAccessException e1 ) {
            throw new ArchitectException("problem running Profile Manager", e1);
        }
        return ddlg;
    }

    public void clear(){
        results.clear();
        fireProfileRemovedEvent(new ProfileChangeEvent(this, null));
    }

    public void remove(SQLObject sqo) throws ArchitectException{
        results.remove(sqo);

        if ( sqo instanceof SQLTable ) {
            for ( SQLColumn col: ((SQLTable)sqo).getColumns()) {
                results.remove(col);
            }
        }
        else if ( sqo instanceof SQLColumn ) {
            SQLTable table = ((SQLColumn)sqo).getParentTable();
            boolean allColumnDeleted = true;
            for ( SQLColumn col: table.getColumns()) {
                if ( getResult(col) != null ) {
                    allColumnDeleted = false;
                    break;
                }
            }
            if ( allColumnDeleted ){
                results.remove(table);
            }
        }
        fireProfileRemovedEvent(new ProfileChangeEvent(this, null));
    }

    public boolean isFindingAvg() {
        return findingAvg;
    }

    public void setFindingAvg(boolean findingAvg) {
        this.findingAvg = findingAvg;
    }

    public boolean isFindingAvgLength() {
        return findingAvgLength;
    }

    public void setFindingAvgLength(boolean findingAvgLength) {
        this.findingAvgLength = findingAvgLength;
    }

    public boolean isFindingDistinctCount() {
        return findingDistinctCount;
    }

    public void setFindingDistinctCount(boolean findingDistinctCount) {
        this.findingDistinctCount = findingDistinctCount;
    }

    public boolean isFindingMax() {
        return findingMax;
    }

    public void setFindingMax(boolean findingMax) {
        this.findingMax = findingMax;
    }

    public boolean isFindingMaxLength() {
        return findingMaxLength;
    }

    public void setFindingMaxLength(boolean findingMaxLength) {
        this.findingMaxLength = findingMaxLength;
    }

    public boolean isFindingMin() {
        return findingMin;
    }

    public void setFindingMin(boolean findingMin) {
        this.findingMin = findingMin;
    }

    public boolean isFindingMinLength() {
        return findingMinLength;
    }

    public void setFindingMinLength(boolean findingMinLength) {
        this.findingMinLength = findingMinLength;
    }

    public boolean isFindingNullCount() {
        return findingNullCount;
    }

    public void setFindingNullCount(boolean findingNullCount) {
        this.findingNullCount = findingNullCount;
    }

    public boolean isfindingTopTen() {
        return findingTopTen;
    }

    public int getTopNCount() {
        return topNCount;
    }

    public void setTopNCount(int topNCount) {
        this.topNCount = topNCount;
    }

    public void setTopNCount(String topNCount) {
        this.topNCount = Integer.valueOf(topNCount);
    }

    public Map<SQLObject, ProfileResult> getResults() {
        return results;
    }

    //==================================
    // ProfileManagerListeners
    //==================================
    List<ProfileChangeListener> listeners = new ArrayList<ProfileChangeListener>();

    public void addProfileChangeListener(ProfileChangeListener listener){
        listeners.add(listener);
    }

    public void removeProfileChangeListener(ProfileChangeListener listener){
        listeners.remove(listener);
    }

    private void fireProfileAddedEvent(ProfileChangeEvent event){
        for (ProfileChangeListener listener: listeners){
            listener.profileAdded(event);
        }
    }

    private void fireProfileRemovedEvent(ProfileChangeEvent event){
        for (ProfileChangeListener listener: listeners){
            listener.profileRemoved(event);
        }
    }


}
