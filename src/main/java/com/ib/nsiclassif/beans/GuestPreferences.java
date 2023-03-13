/*
 * Copyright 2009-2014 PrimeTek.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ib.nsiclassif.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.ib.indexui.utils.JSFUtils;

@Named
@SessionScoped
public class GuestPreferences implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5644709981503440640L;

	private String layoutPrimaryColor = "blue";

    private String componentTheme = "blue";
    
    private String menuTheme = "blue";
    
    private String menuColor = "light";
    
    private String inputStyle = "outlined";
    
    private String selectedMenuColor;

    private boolean horizontalMenu;
    
    private boolean darkMode;

    private List<ComponentTheme> componentThemes = new ArrayList<>();
    
    private Map<String, List<MenuTheme>> menuColors = new HashMap<>();
    
    private String containerPosition = ""; //auto
    
    @PostConstruct
    public void init() {        
        
        //------------------------------------------------------------------------
        // изчитане от бисквитка какви за били избраните настройки от потребителят 
        String val = JSFUtils.readCookie("user_theme_mod");
        darkMode =  val.equals("true")?true:false;
        // System.out.println("user_theme_mod---> "+val);
        val = JSFUtils.readCookie("user_menu_mod");
        horizontalMenu = (val.isEmpty() || val.equals("true")) ? true : false;
    //  System.out.println("user_menu_mod---> "+val);
        val = JSFUtils.readCookie("user_menu_theme");
        menuTheme = val.isEmpty()?"blue":val;
    //  System.out.println("user_menu_theme---> "+val);
        val = JSFUtils.readCookie("user_component_theme");
        componentTheme = val.isEmpty()?"blue":val;
   //   System.out.println("user_component_theme---> "+val);
        val = JSFUtils.readCookie("user_menu_color");
        menuColor = val.isEmpty()?"light":val;
     // System.out.println("user_menu_color---> "+val);
        
         //------------------------------------------------------------------------
        this.selectedMenuColor = menuColor;

        componentThemes.add(new ComponentTheme("Blue", "blue", "#5E81AC"));
        componentThemes.add(new ComponentTheme("Green", "green", "#99CE6B"));
        componentThemes.add(new ComponentTheme("Yellow", "yellow", "#EBCB8B"));
        componentThemes.add(new ComponentTheme("Cyan", "cyan", "#88C0D0"));
        componentThemes.add(new ComponentTheme("Purple", "purple", "#B48EAD"));
        componentThemes.add(new ComponentTheme("Orange", "orange", "#D08770"));
        componentThemes.add(new ComponentTheme("Teal", "teal", "#88D0BD"));
        componentThemes.add(new ComponentTheme("Magenta", "magenta", "#BD69AE"));
        componentThemes.add(new ComponentTheme("Lime", "lime", "#B9BE7F"));
        componentThemes.add(new ComponentTheme("Brown", "brown", "#BE9B7F"));
        componentThemes.add(new ComponentTheme("Red", "red", "#F28686"));
        componentThemes.add(new ComponentTheme("Indigo", "indigo", "#8886F2"));

        /************** Light ********************/
        List<MenuTheme> menuThemes = new ArrayList<>();
        menuThemes.add(new MenuTheme("Blue", "blue", "#5e81ac"));
        menuThemes.add(new MenuTheme("Green", "green", "#A3BE8C"));
        menuThemes.add(new MenuTheme("Yellow", "yellow", "#EBCB8B"));
        menuThemes.add(new MenuTheme("Cyan", "cyan", "#88C0D0"));
        menuThemes.add(new MenuTheme("Purple", "purple", "#B48EAD"));
        menuThemes.add(new MenuTheme("Orange", "orange", "#D08770"));
        menuThemes.add(new MenuTheme("Teal", "teal", "#88D0BD"));
        menuThemes.add(new MenuTheme("Magenta", "magenta", "#BD69AE"));
        menuThemes.add(new MenuTheme("Lime", "lime", "#B9BE7F"));
        menuThemes.add(new MenuTheme("Brown", "brown", "#BE9B7F"));
        menuThemes.add(new MenuTheme("Red", "red", "#F28686"));
        menuThemes.add(new MenuTheme("Indigo", "indigo", "#8886F2"));
        menuColors.put("light", menuThemes);
       
        /************** Dark ********************/
        menuThemes = new ArrayList<>();
        menuThemes.add(new MenuTheme("Blue", "blue", "#5e81ac"));
        menuThemes.add(new MenuTheme("Green", "green", "#A3BE8C"));
        menuThemes.add(new MenuTheme("Yellow", "yellow", "#EBCB8B"));
        menuThemes.add(new MenuTheme("Cyan", "cyan", "#88C0D0"));
        menuThemes.add(new MenuTheme("Purple", "purple", "#B48EAD"));
        menuThemes.add(new MenuTheme("Orange", "orange", "#D08770"));
        menuThemes.add(new MenuTheme("Teal", "teal", "#88D0BD"));
        menuThemes.add(new MenuTheme("Magenta", "magenta", "#BD69AE"));
        menuThemes.add(new MenuTheme("Lime", "lime", "#B9BE7F"));
        menuThemes.add(new MenuTheme("Brown", "brown", "#BE9B7F"));
        menuThemes.add(new MenuTheme("Red", "red", "#F28686"));
        menuThemes.add(new MenuTheme("Indigo", "indigo", "#8886F2"));
        menuColors.put("dark", menuThemes);
        
    }
    
    public void onMenuTypeChange() {
        PrimeFaces.current().executeScript("PrimeFaces.MirageConfigurator.changeMenuToHorizontal(" + horizontalMenu +")");
    }

    public void onColorSchemeChange() {
        PrimeFaces.current().executeScript("PrimeFaces.MirageConfigurator.changeLayout('" + layoutPrimaryColor + "', '" + componentTheme + "', " + darkMode + ")");
    }

    public void onMenuColorChange() {
        PrimeFaces.current().executeScript("PrimeFaces.MirageConfigurator.changeMenuTheme('" + menuColor + "', '" + menuTheme + "', " + darkMode + ")");
    }

    public String getInputStyleClass() {
        return this.inputStyle.equals("filled") ? "ui-input-filled" : "";
    }

    public String getInputStyle() {
        return inputStyle;
    }

    public void setInputStyle(String inputStyle) {
        this.inputStyle = inputStyle;
    }
    
    public String getLayoutConfig() {
        StringBuilder sb = new StringBuilder();
        String color = "";
        if ("custom".equals(this.menuColor)) {
            color = this.menuTheme;
        }
        else {
            color = this.isDarkMode() ? "dark" : "light";
        }
        sb.append("layout-menu-").append(color);

        if (this.isHorizontalMenu()) {
            sb.append(" layout-horizontal");
        }

        return sb.toString();
    }

    public String getLayout() {
        return "layout-" + this.layoutPrimaryColor + (this.darkMode ? "-dark" : "-light");
    }
    
    public String getTheme() {
        return this.darkMode ? this.componentTheme + "-dark" : this.componentTheme + "-light";
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        
        this.menuColor = darkMode ? "dark" : "light";
        this.selectedMenuColor = this.menuColor;
        this.menuTheme = ((MenuTheme)this.menuColors.get(this.menuColor).get(0)).getFile();
        
    }

    public String getLayoutPrimaryColor() {
        return layoutPrimaryColor;
    }

    public void setLayoutPrimaryColor(String layoutPrimaryColor) {
        this.layoutPrimaryColor = layoutPrimaryColor;
    }

    public String getComponentTheme() {
        return componentTheme;
    }

    public void setComponentTheme(String componentTheme) {
        this.componentTheme = componentTheme;
    }

    public boolean isHorizontalMenu() {
        return horizontalMenu;
    }

    public void setHorizontalMenu(boolean horizontalMenu) {
        this.horizontalMenu = horizontalMenu;
//        if (horizontalMenu) {  // за превключване на позицията на дилтъра дали да е по средата на страницата или не при хоризонталното меню (не се използва за сега но го има заложено по страниците)
//        	containerPosition = "auto";
//        } else {
//        	containerPosition = "";
//        }
    }

    public String getMenuTheme() {
        return menuTheme;
    }

    public void setMenuTheme(String menuTheme) {
        this.menuTheme = menuTheme;
    }

    public List<ComponentTheme> getComponentThemes() {
        return componentThemes;
    }

    public void setComponentThemes(List<ComponentTheme> componentThemes) {
        this.componentThemes = componentThemes;
    }

    public String getMenuColor() {
        return menuColor;
    }

    public void setMenuColor(String menuColor) {
        this.menuColor = menuColor;
        
        this.selectedMenuColor = menuColor;
        this.menuTheme = ((MenuTheme)this.menuColors.get(menuColor).get(0)).getFile();
    }

    public Map<String, List<MenuTheme>> getMenuColors() {
        return menuColors;
    }

    public void setMenuColors(Map<String, List<MenuTheme>> menuColors) {
        this.menuColors = menuColors;
    }

    public String getSelectedMenuColor() {
        return selectedMenuColor;
    }

    public void setSelectedMenuColor(String selectedMenuColor) {
        this.selectedMenuColor = selectedMenuColor;
    }

	public class MenuTheme {

        String name;
        String file;
        String color;

        public MenuTheme(String name, String file, String color) {
            this.name = name;
            this.file = file;
            this.color = color;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public String getColor() {
            return this.color;
        }
    }
	
	public String getContainerPosition() {
		return containerPosition;
	}

	public void setContainerPosition(String containerPosition) {
		this.containerPosition = containerPosition;
	}

    public class ComponentTheme {

        String name;
        String file;
        String color;

        public ComponentTheme(String name, String file, String color) {
            this.name = name;
            this.file = file;
            this.color = color;
        }

        public String getName() {
            return this.name;
        }

        public String getFile() {
            return this.file;
        }

        public String getColor() {
            return this.color;
        }
    }
}
