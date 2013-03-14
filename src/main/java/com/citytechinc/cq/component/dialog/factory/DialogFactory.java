package com.citytechinc.cq.component.dialog.factory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import org.codehaus.plexus.util.StringUtils;

import com.citytechinc.cq.component.annotations.Component;
import com.citytechinc.cq.component.annotations.DialogField;
import com.citytechinc.cq.component.dialog.Dialog;
import com.citytechinc.cq.component.dialog.DialogElement;
import com.citytechinc.cq.component.dialog.Widget;
import com.citytechinc.cq.component.dialog.exception.InvalidComponentClassException;
import com.citytechinc.cq.component.dialog.exception.InvalidComponentFieldException;
import com.citytechinc.cq.component.dialog.impl.SimpleDialog;
import com.citytechinc.cq.component.dialog.impl.SimpleTab;
import com.citytechinc.cq.component.dialog.impl.WidgetCollection;

public class DialogFactory {

	public static Dialog make(CtClass componentClass, Map<Class<?>, String> xtypeMap, ClassLoader classLoader) throws InvalidComponentClassException, InvalidComponentFieldException, ClassNotFoundException, CannotCompileException, NotFoundException {

		Component componentAnnotation = (Component) componentClass.getAnnotation(Component.class);

		if (componentAnnotation == null) {
			throw new InvalidComponentClassException();
		}

		Map<String, List<DialogElement>> tabMap = new LinkedHashMap<String, List<DialogElement>>();

		/*
		 * Get dialog title
		 */
		String dialogTitle = getDialogTitleForComponent(componentAnnotation);

		/*
		 * Setup Tabs from Component tab list if one exists
		 */
		List<String> tabsList = Arrays.asList(componentAnnotation.tabs());

		for (String curTab : tabsList) {
			tabMap.put(curTab, new ArrayList<DialogElement>());
		}

		List<CtField> fields = Arrays.asList(componentClass.getDeclaredFields());

		/*
		 * Iterate through all fields establishing proper widgets for each
		 */
		for (CtField curField : fields) {
			DialogField dialogProperty = (DialogField) curField.getAnnotation(DialogField.class);

			if (dialogProperty != null) {
				Widget builtFieldWidget = WidgetFactory.make(componentClass, curField, xtypeMap, classLoader);

				String tabString = getTabStringForField(curField, dialogProperty);

				if (!tabMap.containsKey(tabString)) {
					tabMap.put(tabString, new ArrayList<DialogElement>());
				}

				tabMap.get(tabString).add(builtFieldWidget);

			}
		}

		List<DialogElement> tabList = new ArrayList<DialogElement>();

		for (String curMapKey : tabMap.keySet()) {
			tabList.add(new SimpleTab(curMapKey, new WidgetCollection(tabMap.get(curMapKey))));
		}

		return new SimpleDialog(tabList, dialogTitle);
	}

	private static final String getDialogTitleForComponent(Component component) {

		String title = component.title();

		if (StringUtils.isNotEmpty(title)) {
			return title;
		}

		return "Dialog";

	}

	private static final String getTabStringForField(CtField field, DialogField dialogProperty) {

		String tabString = dialogProperty.tab();

		if (StringUtils.isNotEmpty(tabString)) {
			return tabString;
		}

		return "Tab";
	}

}
