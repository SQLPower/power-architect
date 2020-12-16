/*
 * Copyright (c) 2020, SQL Power Group Inc.
 *
 * This file is part of SQL Power Architect.
 *
 * SQL Power Architect is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SQL Power Architect is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.architect.swingui;

import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.sqlpower.architect.etl.kettle.KettleJob;
import ca.sqlpower.sqlobject.SQLTable;
import ca.sqlpower.swingui.DataEntryPanel;

public class KettleSplitJobPanel implements DataEntryPanel {
    private static final Logger logger = Logger.getLogger(KettleSplitJobPanel.class);
    
    private JPanel panel;
    
    /**
     * Number to split the job 
     */
    private int splitNo;
    
    private KettleJob settings;

    /**
     * List of table in a Playpen
     */
    private List<SQLTable> tableList = new ArrayList<SQLTable>();  
    
    private DefaultListModel<String> listModel = new DefaultListModel<String>();
    
    private Map<String, JList<String>> splitMap = new LinkedHashMap<String, JList<String>>();

 
    public KettleSplitJobPanel(List<SQLTable> tableList, KettleJob settings) {
        this.tableList = tableList;
        this.splitNo = settings.getSplitJobNo();
        this.settings = settings;
        for(SQLTable table: tableList) {
            listModel.addElement(table.getName());
        }
        createGUI();
    }
    
    private void createGUI() {
        FormLayout mainLayout = new FormLayout(
                "2dlu, pref:grow, 2dlu, right:pref, 3dlu, pref:grow",  // columns
                "pref, 3dlu, pref, 3dlu, pref");           // rows

        PanelBuilder mainBuilder = new PanelBuilder(mainLayout);
        panel = mainBuilder.getPanel();   
        
        
        PanelBuilder pb = new PanelBuilder((FormLayout) panel.getLayout(), panel);
        CellConstraints cc = new CellConstraints();
        JList<String> list = new JList<String>(listModel);
        JPanel playPenTablePanel = createSplitList(list,"Playpen Tables");
        pb.add(playPenTablePanel, cc.xy(2, 1)); //$NON-NLS-1$

        FormLayout layout = new FormLayout("right:pref,5dlu");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);

        
        for(int i =1 ; i<= splitNo; i++) {
            DefaultListModel<String> splitListModel = new DefaultListModel<String>();
            JList<String> splitList = new JList<String>(splitListModel);
            splitMap.put("Job_"+i, splitList);
            builder.append(createSplitList(splitList,"Job_"+i));
            builder.nextLine();
        }
        pb.add(builder.getPanel(), cc.xy(4, 1));
        
    }
    
    
    private JPanel createSplitList(JList<String> list, String name) {
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setWheelScrollingEnabled(true);
        list.setDragEnabled(true);
        list.setTransferHandler(new ListTransferHandler());
        JPanel splitPanel = new JPanel(new FlowLayout());
        splitPanel.add(scrollPane);
        splitPanel.setBorder(BorderFactory.createTitledBorder(name));
        return splitPanel;
    }
    /**
     * @return the splitMap
     */
    public Map<String, List<String>> getSplitMap() {
        Map<String, List<String>> splitJobMap =new LinkedHashMap<String, List<String>>();
        for(String key :splitMap.keySet()) {
            JList<String> list = splitMap.get(key);
            DefaultListModel<String> listModel = (DefaultListModel<String>)list.getModel();
            List<String> list1 = IntStream.range(0,listModel.size()).mapToObj(listModel::get).collect(Collectors.toList());
            splitJobMap.put(key, list1);
        }
        return splitJobMap;
    }
    
    @Override
    public boolean applyChanges() {
       settings.setSplitMap(getSplitMap());
       return true;
    }

    @Override
    public void discardChanges() {

    }

    @Override
    public JComponent getPanel() {
        return panel;
    }

    @Override
    public boolean hasUnsavedChanges() {
        return false;
    }

}

/**
 * 
 *
 */
abstract class StringTransferHandler extends TransferHandler {
    
    protected abstract String exportString(JComponent c);
    protected abstract void importString(JComponent c, String str);
    protected abstract void cleanup(JComponent c, boolean remove);
    
    protected Transferable createTransferable(JComponent c) {
        return new StringSelection(exportString(c));
    }
    
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
    
    public boolean importData(JComponent c, Transferable t) {
        if (canImport(c, t.getTransferDataFlavors())) {
            try {
                String str = (String)t.getTransferData(DataFlavor.stringFlavor);
                importString(c, str);
                return true;
            } catch (UnsupportedFlavorException ufe) {
            } catch (IOException ioe) {
            }
        }

        return false;
    }
    
    protected void exportDone(JComponent c, Transferable data, int action) {
        cleanup(c, action == MOVE);
    }
    
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        for (int i = 0; i < flavors.length; i++) {
            if (DataFlavor.stringFlavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }
}

/**
 * 
 * 
 *
 */
class ListTransferHandler extends StringTransferHandler {
    private int[] indices = null;
    private int addIndex = -1; 
    private int addCount = 0; 
            
    //Bundle up the selected items in the list
    //as a single string, for export.
    protected String exportString(JComponent c) {
        JList<String> list = (JList<String>)c;
        indices = list.getSelectedIndices();
        List<String> values = list.getSelectedValuesList();
        
        StringBuffer buff = new StringBuffer();

        for(String value :values) {
            buff.append(value == null ? "" : value.toString());
        }
        buff.append("\n");

        return buff.toString();
    }

    protected void importString(JComponent c, String str) {
        JList<String> target = (JList<String>)c;
        DefaultListModel<String> listModel = (DefaultListModel<String>)target.getModel();
        int index = target.getSelectedIndex();

        if (indices != null && index >= indices[0] - 1 &&
              index <= indices[indices.length - 1]) {
            indices = null;
            return;
        }

        int max = listModel.getSize();
        if (index < 0) {
            index = max;
        } else {
            index++;
            if (index > max) {
                index = max;
            }
        }
        addIndex = index;
        String[] values = str.split("\n");
        addCount = values.length;
        for (int i = 0; i < values.length; i++) {
            listModel.add(index++, values[i]);
        }
        List<String> list1 = IntStream.range(0,listModel.size()).mapToObj(listModel::get).collect(Collectors.toList());

    }

    /**
     * 
     */
    protected void cleanup(JComponent c, boolean remove) {
        if (remove && indices != null) {
            JList<String> source = (JList<String>)c;
            DefaultListModel<String> model  = (DefaultListModel<String>)source.getModel();
            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] > addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
               
            }
            List<String> list1 = IntStream.range(0,model.size()).mapToObj(model::get).collect(Collectors.toList());

        } 
        indices = null;
        addCount = 0;
        addIndex = -1;
    }
}
