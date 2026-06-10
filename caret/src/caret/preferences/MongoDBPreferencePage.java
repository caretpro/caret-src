package caret.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import caret.Activator;

public class MongoDBPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public MongoDBPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("MongoDB settings:");
    }

    @Override
    public void createFieldEditors() {
        //connection string
        addField(new StringFieldEditor(
                PreferenceConstants.P_MONGO_URI, 
                "URI (Connection String):", 
                getFieldEditorParent()));
        
        // Optional database name
        addField(new StringFieldEditor(
                PreferenceConstants.P_MONGO_DATABASE, 
                "Database:", 
                getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        // No initialization logic required
    }

    @Override
    protected void performApply() {
        System.out.println("MongoDBPreferencePage: PERFORM APPLY");
        super.performApply();
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        return super.performOk();
    }
}
