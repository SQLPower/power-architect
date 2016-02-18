
package ca.sqlpower.architect;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;

import javax.swing.JComboBox;


/**
 * @author Kirti
 *
 */
public class LocaleChooser extends JComboBox<String> implements ItemListener {

    private Locale[] availableLocales;
    private Locale locale;


    /**
     * 
     * @param locale
     */
    public LocaleChooser(Locale locale) {
        super();
        this.locale = locale;

        addItemListener(this);
//    currently power-architect supports only few languages, so not adding all languages
//    availableLocales = NumberFormat.getAvailableavailableLocales();
        availableLocales = new Locale[] { new Locale("en"), new Locale("de"),
                new Locale("ko"), new Locale("ru"),new Locale("pt") }; 

        for (int i = 0; i < availableLocales.length; i++) {
            // Uncomment if need to add a same language for different country, currently adding languages as in common
//        if (availableavailableLocales[i].getCountry().length() > 0) {
        	// adding extra label inside parentheses in English at the end of language name for ease.
            addItem(availableLocales[i].getDisplayLanguage()+"  ("+ availableLocales[i].getDisplayLanguage(new Locale("ENGLISH", "US"))+ ")");
//        }
        }
        for (int i = 0; i < availableLocales.length; i++) {
            if (locale!= null && availableLocales[i].getDisplayLanguage().equals(locale.getDisplayLanguage())) {
                setLocale(availableLocales[i], false);
                break;
            } 
        }

    }


    @Override
    public void itemStateChanged(ItemEvent event) {
    	// extracting the extra label added at the end of language in a parentheses in English
        String item = (String) event.getItem();
        item = item.substring(0, item.indexOf("(")).trim();
        if (item == null || item.isEmpty()) return;
        for (int i = 0; i < availableLocales.length; i++) {
            if (availableLocales[i].getDisplayLanguage().equalsIgnoreCase(item) ) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    setLocale(availableLocales[i],false);
                }
                break;
            }
        }
    }


    /**
     * 
     * @param l
     * @param select
     */
    public void setLocale(Locale l, boolean select) {
        Locale oldLocale = locale;
        locale = l;
        if (select) {
            for (int i = 0; i < availableLocales.length; i++) {
                if (availableLocales[i].getDisplayLanguage().equalsIgnoreCase(locale.getDisplayLanguage())) {
                    setSelectedIndex(i);
                }
            }
        }
        firePropertyChange("locale", oldLocale, locale);
    }


    /**
     * return the Locale
     */
    public Locale getLocale() {
        return locale;
    }
}