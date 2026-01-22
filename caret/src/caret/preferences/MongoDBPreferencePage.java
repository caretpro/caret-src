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
        // MongoDB username
        addField(new StringFieldEditor(
                PreferenceConstants.P_MONGO_USER, 
                "MongoDB user:", 
                getFieldEditorParent()));

        // MongoDB password (string field, not masked)
        addField(new StringFieldEditor(
                PreferenceConstants.P_MONGO_PASSWORD, 
                "MongoDB password:", 
                getFieldEditorParent()));

        // Cluster host (example: caret-cluster.wefhfh9.mongodb.net)
        addField(new StringFieldEditor(
                PreferenceConstants.P_MONGO_HOST, 
                "Host:", 
                getFieldEditorParent()));

        // appName parameter for connection string
        addField(new StringFieldEditor(
                PreferenceConstants.P_MONGO_APPNAME, 
                "App name:", 
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
