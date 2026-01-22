package caret.preferences;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
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

public class AgentsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	
	public AgentsPreferencePage() {
		
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Settings:");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new MoveListEditor(PreferenceConstants.P_LIST_TASK_CLASSIFIER_AGENTS, "Task Classifier Agents:", getFieldEditorParent()) {
	        @Override
	        protected String[] parseString(String str) {
	            // TODO Auto-generated method stub
	            return str.split(",");
	        }

	        @Override
	        protected String createList(String[] str) {
	            // TODO Auto-generated method stub
	            return String.join(",", str);
	        }

			@Override
			protected String getNewInputObject() {
				// TODO Auto-generated method stub
				return null;
			}
	    });
		MoveListEditor processingAgents = new MoveListEditor(PreferenceConstants.P_LIST_TASK_PROCESSING_AGENTS, "Task Processing Agents:", getFieldEditorParent()) {
	        @Override
	        protected String[] parseString(String str) {
	            // TODO Auto-generated method stub
	            return str.split(",");
	        }

	        @Override
	        protected String createList(String[] str) {
	            // TODO Auto-generated method stub
	            return String.join(",", str);
	        }

			@Override
			protected String getNewInputObject() {
				// TODO Auto-generated method stub
				return null;
			}
	    };
	    addField(processingAgents);
		
	    BooleanFieldEditor dynamicMode = new BooleanFieldEditor(
	            PreferenceConstants.P_AGENTS_DINAMIC,
	            "Dynamic mode",
	            getFieldEditorParent());
	    addField(dynamicMode);

	    dynamicMode.setPropertyChangeListener(event -> {
	        if (BooleanFieldEditor.VALUE.equals(event.getProperty())) {
	            boolean enabled = dynamicMode.getBooleanValue();
	            processingAgents.setEnabled(!enabled);
	        }
	    });

	    processingAgents.setEnabled(!dynamicMode.getBooleanValue());
		
		addField(new ListEditor(PreferenceConstants.P_LIST_CONTENT_ASSISTANT_AGENTS, "Content Assistant Agents:", getFieldEditorParent()) {
	        @Override
	        protected String[] parseString(String str) {
	            // TODO Auto-generated method stub
	            return str.split(",");
	        }

	        @Override
	        protected String createList(String[] str) {
	            // TODO Auto-generated method stub
	            return String.join(",", str);
	        }

			@Override
			protected String getNewInputObject() {
				// TODO Auto-generated method stub
				return null;
			}
	    });
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
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