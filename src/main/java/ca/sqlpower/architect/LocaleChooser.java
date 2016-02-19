/*
 * Copyright (c) 2016, SQL Power Group Inc.
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

package ca.sqlpower.architect;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;

import javax.swing.JComboBox;

/**
 * This class used to change default Locale.
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
//      currently power-architect supports only few languages, so not adding all languages
//      availableLocales = NumberFormat.getAvailableavailableLocales();
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
     * @param l, Locale to set to 
     * @param select, flag to select or not  
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
     * return the current Locale
     */
    public Locale getLocale() {
        return locale;
    }
}