package caret.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import caret.Activator;
import caret.ChatView;
import caret.agent.AgentInterface;
import caret.agent.Response;
import caret.preferences.MoveListEditor;
import caret.preferences.PreferenceConstants;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class TasksPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	
	public TasksPreferencePage() {
		
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Settings");
	}
	
	public void createFieldEditors() {
		addField(new TableTaskFieldEditor(PreferenceConstants.P_TABLE_TASKS, "Tasks:", getFieldEditorParent()) {
			@Override
			protected String getNewInputObject() {
				return null;
			}
	    });
		
	}

	public void init(IWorkbench workbench) {
		
	}
	
	@Override
	protected void performApply() {
		System.out.println("PERFORM APPLY");
	}

	@Override
	public boolean performOk() {
		System.out.println("PERFORM OK");
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return super.performOk();
	}
}