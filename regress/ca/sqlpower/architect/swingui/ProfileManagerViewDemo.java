package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ProfileChangeEvent;
import ca.sqlpower.architect.profile.ProfileManagerInterface;
import ca.sqlpower.architect.profile.TableProfileResult;

public class ProfileManagerViewDemo {

    static ProfileManagerView view;
    static SQLTable mockTable;
    static TableProfileResult tableProfileResult;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JFrame jf = new JFrame("ProfileManagerViewDemo");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final List<TableProfileResult> mockData = new ArrayList<TableProfileResult>();

        mockTable = new SQLTable();
        mockTable.setName("Customers");
        tableProfileResult = new TableProfileResult(mockTable) {
            @Override
            public boolean isFinished() {
                return true;
            }
        };
        tableProfileResult.setCreateStartTime(System.currentTimeMillis());
        mockData.add(tableProfileResult);

        mockTable = new SQLTable();
        mockTable.setName("Suppliers");
        tableProfileResult = new TableProfileResult(mockTable);
        tableProfileResult.setCreateStartTime(System.currentTimeMillis());
        mockData.add(tableProfileResult);

        ProfileManagerInterface mock = new ProfileManagerInterface() {

            public List<TableProfileResult> getTableResults() {
                return mockData;
            }

            public void remove(TableProfileResult victim) throws ArchitectException {
                System.out.println("Mock Manager asked to remove " + victim);
                mockData.remove(victim);
                view.profileRemoved(new ProfileChangeEvent(mockTable, tableProfileResult));
            }

            public void clear() {
                System.out.println("Mock Manager asked to clear");
                mockData.clear();
            }
        };

        view = new ProfileManagerView(mock);
        jf.setContentPane(view);
        jf.pack();
        jf.setVisible(true);

        Thread.sleep(4000);
        System.out.println("Adding another table");
        mockTable = new SQLTable();
        mockTable.setName("Yet Another Table");
        tableProfileResult = new TableProfileResult(mockTable);
        tableProfileResult.setCreateStartTime(System.currentTimeMillis());

        view.profileAdded(new ProfileChangeEvent(mockTable, tableProfileResult));
    }

}
