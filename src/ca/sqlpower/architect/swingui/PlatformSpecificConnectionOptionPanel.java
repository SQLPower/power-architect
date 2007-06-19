package ca.sqlpower.architect.swingui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectDataSourceType;

public class PlatformSpecificConnectionOptionPanel {

    private class JDBCURLUpdater implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updateUrlFromFields();
        }

        public void removeUpdate(DocumentEvent e) {
            updateUrlFromFields();
        }

        public void changedUpdate(DocumentEvent e) {
            updateUrlFromFields();
        }
    }
    
    private static class PlatformOptionsLayout implements LayoutManager {

        /** The number of pixels to leave before each label except the first one. */
        int preLabelGap = 10;

        /** The number of pixels to leave between every component. */
        int gap = 5;

        public void addLayoutComponent(String name, Component comp) {
            // nothing to do
        }

        public void removeLayoutComponent(Component comp) {
            // nothing to do
        }

        public Dimension preferredLayoutSize(Container parent) {
            int height = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                height = Math.max(height, c.getPreferredSize().height);
            }
            return new Dimension(parent.getWidth(), height);
        }

        public Dimension minimumLayoutSize(Container parent) {
            int height = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                height = Math.max(height, c.getMinimumSize().height);
            }
            return new Dimension(parent.getWidth(), height);
        }

        public void layoutContainer(Container parent) {

            // compute total width of all labels
            int labelSize = 0;
            int labelCount = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);
                if (c instanceof JLabel) {
                    if (i > 0) labelSize += preLabelGap;
                    labelSize += c.getPreferredSize().width;
                    labelCount += 1;
                }
            }

            int gapSize = gap * (parent.getComponentCount() - 1);

            // compute how wide each non-label component should be (if there are any non-labels)
            int nonLabelWidth = 0;
            if (parent.getComponentCount() != labelCount) {
                nonLabelWidth = (parent.getWidth() - labelSize - gapSize) / (parent.getComponentCount() - labelCount);
            }

            // impose a minimum so the non-labels at least show up when we're tight on space
            if (nonLabelWidth < 20) {
                nonLabelWidth = 20;
            }

            // lay out the container
            int x = 0;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                Component c = parent.getComponent(i);

                if (i > 0) x += gap;

                if (c instanceof JLabel) {
                    if (i > 0) x += preLabelGap;
                    c.setBounds(x, 0, c.getPreferredSize().width, parent.getHeight());
                    x += c.getPreferredSize().width;
                } else {
                    c.setBounds(x, 0, nonLabelWidth, parent.getHeight());
                    x += nonLabelWidth;
                }
            }
        }
    }
    
    private static Logger logger = Logger.getLogger(PlatformSpecificConnectionOptionPanel.class);
    
    private JDBCURLUpdater urlUpdater = new JDBCURLUpdater();

    private boolean updatingUrlFromFields = false;
    private boolean updatingFieldsFromUrl = false;
    private JTextField dbUrlField;
    private JPanel platformSpecificOptionPanel;
    private ArchitectDataSourceType template;
    
    public PlatformSpecificConnectionOptionPanel(JTextField dbUrlField) {
        platformSpecificOptionPanel = new JPanel();
        platformSpecificOptionPanel.setLayout(new PlatformOptionsLayout());
        platformSpecificOptionPanel.setBorder(BorderFactory.createEmptyBorder());
        platformSpecificOptionPanel.add(new JLabel("(No options for current driver)"));

        this.dbUrlField = dbUrlField;
  

        dbUrlField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                updateFieldsFromUrl();
            }

            public void removeUpdate(DocumentEvent e) {
                updateFieldsFromUrl();
            }

            public void changedUpdate(DocumentEvent e) {
                updateFieldsFromUrl();
            }
        });
    }
    
    /**
     * Copies the values from the platform-specific url fields into the main
     * url.
     */
    private void updateUrlFromFields() {
        if (updatingFieldsFromUrl) return;

        if (template == null || template.getJdbcUrl() == null) return;
        try {
            updatingUrlFromFields = true;
            StringBuffer newUrl = new StringBuffer();
            Pattern p = Pattern.compile("<(.*?)>");
            Matcher m = p.matcher(template.getJdbcUrl());
            while (m.find()) {
                String varName = m.group(1);
                if (varName.indexOf(':') != -1) {
                    varName = varName.substring(0, varName.indexOf(':'));
                }
                String varValue = getPlatformSpecificFieldValue(varName);
                m.appendReplacement(newUrl, varValue);
            }
            m.appendTail(newUrl);
            dbUrlField.setText(newUrl.toString());
        } finally {
            updatingUrlFromFields = false;
        }
    }
    
    /**
     * Retrieves the named platform-specific option by looking it up in the
     * platformSpecificOptionPanel component.
     */
    private String getPlatformSpecificFieldValue(String varName) {
        // we're looking for the contents of the JTextField that comes after a JLabel with the same text as varName
        for (int i = 0; i < platformSpecificOptionPanel.getComponentCount(); i++) {
            if (platformSpecificOptionPanel.getComponent(i) instanceof JLabel
                    && ((JLabel) platformSpecificOptionPanel.getComponent(i)).getText().equals(varName)
                    && platformSpecificOptionPanel.getComponentCount() >= i+1) {
                return ((JTextField) platformSpecificOptionPanel.getComponent(i+1)).getText();
            }
        }
        return "";
    }
    
    
    
    /**
     * Parses the main url against the current template (if possible) and fills in the
     * individual fields with the values it finds.
     */
    private void updateFieldsFromUrl() {
        if (updatingUrlFromFields) return;
        try {
            updatingFieldsFromUrl = true;

            for (int i = 0; i < platformSpecificOptionPanel.getComponentCount(); i++) {
                platformSpecificOptionPanel.getComponent(i).setEnabled(true);
            }

            Map<String, String> map = template.retrieveURLParsing(dbUrlField.getText());
            if (!map.isEmpty()) {
                platformSpecificOptionPanel.setEnabled(true);
                for (int g = 0; g < map.size(); g++) {
                    ((JTextField) platformSpecificOptionPanel.getComponent(2*g+1)).setText((String)map.values().toArray()[g]);
                }
            } else {
                for (int i = 0; i < platformSpecificOptionPanel.getComponentCount(); i++) {
                    platformSpecificOptionPanel.getComponent(i).setEnabled(false);
                }
            }
        } finally {
            updatingFieldsFromUrl = false;
        }
    }
    

    
 
    
    /**
     * Sets up the platformSpecificOptionPanel component to contain labels and
     * text fields associated with each variable in the current template.
     */
    private void createFieldsFromTemplate() {
        for (int i = 0; i < platformSpecificOptionPanel.getComponentCount(); i++) {
            Component c = platformSpecificOptionPanel.getComponent(i);
            if (c instanceof JTextField) {
                ((JTextField) c).getDocument().removeDocumentListener(urlUpdater);
            }
        }
        platformSpecificOptionPanel.removeAll();

        if (template != null) {
            Map<String, String> map = template.retrieveURLDefaults();

            for(String key : map.keySet()) {
                String var = key;
                String def = map.get(key);

                platformSpecificOptionPanel.add(new JLabel(var));
                JTextField field = new JTextField(def);
                platformSpecificOptionPanel.add(field);
                field.getDocument().addDocumentListener(urlUpdater);
                logger.debug("The default value is: " + def);
            }


        } else {
            platformSpecificOptionPanel.add(new JLabel("Unknown driver class.  Fill in URL manually."));

        }

        platformSpecificOptionPanel.revalidate();
        platformSpecificOptionPanel.repaint();
    }

    public JPanel getPanel() {
        return platformSpecificOptionPanel;
    }

    public ArchitectDataSourceType getTemplate() {
        return template;
    }

    public void setTemplate(ArchitectDataSourceType template) {
        this.template = template;
        createFieldsFromTemplate();
        updateUrlFromFields();
    }
    
}
