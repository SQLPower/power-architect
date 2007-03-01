package ca.sqlpower.architect.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.profile.ProfileManagerInterface;
import ca.sqlpower.architect.profile.TableProfileResult;

public class ProfileManagerViewDemo {

    /**
     * @param args
     */
    public static void main(String[] args) {
        JFrame jf = new JFrame("ProfileManagerViewDemo");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final List<TableProfileResult> mockData = new ArrayList<TableProfileResult>();

        SQLTable mockTable = new SQLTable();
        mockTable.setName("Customers");
        TableProfileResult tableProfileResult = new TableProfileResult(mockTable);
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
        };

        jf.setContentPane(new ProfileManagerView(mock));
        jf.pack();
        jf.setVisible(true);
    }

}
