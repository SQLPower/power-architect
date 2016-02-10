/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*Architect.
 *
 * Power*Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package ca.sqlpower.architect.etl.kettle;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.UserInfo;
import org.pentaho.di.trans.TransMeta;

import ca.sqlpower.architect.TestingArchitectSessionContext;
import ca.sqlpower.sql.JDBCDataSource;
import ca.sqlpower.sql.JDBCDataSourceType;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.util.UserPrompter.UserPromptResponse;

import com.enterprisedt.util.debug.Logger;

public class KettleJobOutputToRepoTest extends TestCase {

    /**
     * This is a wrapper class for a repository to track what is being done with it.
     */
    public class KettleRepositoryWrapper extends Repository{
        
        int transformationsSaved = 0;
        int jobsSaved = 0;
        
        public KettleRepositoryWrapper(LogWriter log, RepositoryMeta repinfo, UserInfo userinfo) {
            super(log, repinfo, userinfo);
        }
        
        /**
         * This method does not actually disconnect from the connection!
         */
        public int getNumTransformationsSaved() {
            return transformationsSaved;
        }
        
        public int getNumJobsSaved() {
            return jobsSaved;
        }
        
        @Override
        public synchronized void insertJob(JobMeta jobMeta) throws KettleException {
            jobsSaved++;
            super.insertJob(jobMeta);
        }
        
        @Override
        public synchronized void insertTransformation(TransMeta transMeta) throws KettleException {
            transformationsSaved++;
            super.insertTransformation(transMeta);
        }
        
        @Override
        public long getTransformationID(String s, long l) {
            return getValFromString(s);
        }

        @Override
        public long getNextTransformationID() {
            return 1;
        }

        @Override
        public synchronized long getJobID(String name, long id_directory) throws KettleException {
            return getValFromString(name);
        }

        @Override
        public long getNextJobID() {
            return 1;
        }

        @Override
        public long getDatabaseID(String s) {
            return getValFromString(s);
        }
        
        private long getValFromString(String s) {
            try {
                return new Integer(s).intValue();
            } catch (NumberFormatException e) {
                return 1;
            }
        }
    }
    
    private static Logger logger = Logger.getLogger(KettleJobOutputToRepoTest.class);
    
    private SQLDatabase target;
    private KettleJob kettleJob;
    private JDBCDataSource ds;
    private JDBCDataSourceType dsType;
    private TransMeta transMeta;
    private JobMeta job;
    private KettleRepositoryWrapper krw;
    private ArchitectSessionWithFileValidator session;
    private List<TransMeta> transList;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        session = new ArchitectSessionWithFileValidator(new TestingArchitectSessionContext());
        
    }
    
    public void createRepo(int n) {
        // We are using a real HSQLDB database to test this functionality because
        // we need to format the repository and actually write to it.
        target = new SQLDatabase();
        target.setName("Target for Testing");
        ds = session.getContext().getPlDotIni().getDataSource("regression_test");
        target.setDataSource(ds);
        ds.setUser("sa");
        dsType = ds.getParentType();
        dsType.putProperty(KettleOptions.KETTLE_CONNECTION_TYPE_KEY, "Hypersonic");
        ds.setUrl("jdbc:hsqldb:file:testKettleOutputToRepository" + n);
        
           
        transMeta = createTransMeta();
        job = createJobMeta();
        
        transList = new ArrayList<TransMeta>();
        transList.add(transMeta);


        kettleJob = new KettleJob(session);
        kettleJob.setSavingToFile(false);
        kettleJob.setJobName("jobName");
        
        kettleJob.setRepository(ds);

        Object ret[] = kettleJob.createTestRepository();
        krw = new KettleRepositoryWrapper((LogWriter)ret[0],
                (RepositoryMeta)ret[1],
                (UserInfo)ret[2]);
        
        // This adds a whole bunch of tables needed to store the ETL metadata in the
        // target database.
        logger.debug("Creating connection and setting up database");
        try {
            kettleJob.createStraightConnection(krw);
            krw.createRepositorySchema(null, false, new ArrayList<String>(), false);
        } catch (KettleException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void testOutputToRepositoryOverwrite() throws Exception {
        createRepo(1);
        session.setReponse(UserPromptResponse.OK);
        
        kettleJob.outputToRepository(job, transList, krw);
        assertEquals(1, krw.getNumTransformationsSaved());
        assertEquals(1, krw.getNumJobsSaved());
        
        deleteTestDatabase(1);
    }
    
    public void testOutputToRepositoryDontOverwrite() throws Exception {
        createRepo(2);
        session.setReponse(UserPromptResponse.NOT_OK);
        
        kettleJob.outputToRepository(job, transList, krw);
        assertEquals(0, krw.getNumTransformationsSaved());
        assertEquals(0, krw.getNumJobsSaved());
        
        deleteTestDatabase(2);
    }
    
    public void testOutputToRepositoryCancel() throws Exception {
        createRepo(3);
        session.setReponse(UserPromptResponse.CANCEL);
        
        kettleJob.outputToRepository(job, transList, krw);
        assertEquals(0, krw.getNumTransformationsSaved());
        assertEquals(0, krw.getNumJobsSaved());
        
        deleteTestDatabase(3);
    }
    
    private void deleteTestDatabase(int n) throws SQLException {
        logger.debug("Deleting test database");
        File f = new File("testKettleOutputToRepository" + n + ".log");
        f.delete();
        f = new File("testKettleOutputToRepository" + n + ".properties");
        f.delete();
        f = new File("testKettleOutputToRepository" + n + ".lck");
        f.delete();
    }
    
    private JobMeta createJobMeta() {
        LogWriter lw = LogWriter.getInstance();
        JobMeta job = new JobMeta(lw);
        job.setName("jobName");
        job.addNote(new NotePadMeta("original job note", 0, 150, 125, 125));
        return job;
    }

    private TransMeta createTransMeta() {
        TransMeta transMeta = new TransMeta();
        transMeta.setName("tableName");
        transMeta.addNote(new NotePadMeta("original trans meta note", 0, 150, 125, 125));
        return transMeta;
    }
}
