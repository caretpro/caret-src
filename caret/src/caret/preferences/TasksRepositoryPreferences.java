package caret.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import caret.Activator;

public class TasksRepositoryPreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public TasksRepositoryPreferences() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Tasks repository settings:");
    }

    @Override
    public void createFieldEditors() {
    	
        addField(new StringFieldEditor(
                PreferenceConstants.P_TASKS_UPLOAD_URL, 
                "URL (Tasks plugin upload):", 
                getFieldEditorParent()));
        
        addField(new StringFieldEditor(
                PreferenceConstants.P_TASKS_UPLOAD_TOKEN, 
                "Tasks upload bearer-token:", 
                getFieldEditorParent()));
        
        addField(new StringFieldEditor(
                PreferenceConstants.P_TASKS_P2_REPOSITORY_URL, 
                "URL (Tasks P2 Repository):", 
                getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        
    }
    
    @Override
	protected void performApply() {
		System.out.println("PERFORM APPLY");
	}

	@Override
	public boolean performOk() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return super.performOk();
	}
}
