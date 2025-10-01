package caret.validator.test.property;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import caret.validator.test.Activator;

public class TestPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

    private IProject project;
    private ScopedPreferenceStore preferenceStore;
    private DirectoryFieldEditor testFolderEditor;
    private StringFieldEditor testRegexEditor;
    private FileFieldEditor testClassEditor;

    private CheckboxFieldEditor testFolderCheckbox;
    private CheckboxFieldEditor testRegexCheckbox;
    private CheckboxFieldEditor testClassCheckbox;
    
    public TestPropertyPage() {
        super(GRID);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(new ProjectScope(project), Activator.PLUGIN_ID);
        setPreferenceStore(preferenceStore);
        //setPreferenceStore(Activator.getDefault().getPreferenceStore());
        
    }

    @Override
    protected void createFieldEditors() {
        // Create a field editor for a string input
    	testFolderCheckbox = new CheckboxFieldEditor(TestPropertyConstants.PROPERTY_ENABLE_TEST_FOLDER, "Enable Test Folder", getFieldEditorParent());
    	addField(testFolderCheckbox);
    	testFolderEditor = new DirectoryFieldEditor(TestPropertyConstants.PROPERTY_TEST_FOLDER, "Test folder:", getFieldEditorParent());
        addField(testFolderEditor);

    	testRegexCheckbox = new CheckboxFieldEditor(TestPropertyConstants.PROPERTY_ENABLE_TEST_REGEX, "Enable Test Regex", getFieldEditorParent());
    	addField(testRegexCheckbox);
    	testRegexEditor = new StringFieldEditor(TestPropertyConstants.PROPERTY_TEST_REGEX, "Regular Expression:", getFieldEditorParent());
        addField(testRegexEditor);
    	
    	testClassCheckbox = new CheckboxFieldEditor(TestPropertyConstants.PROPERTY_ENABLE_TEST_CLASS, "Enable Test Class", getFieldEditorParent());
    	addField(testClassCheckbox);
        testClassEditor = new FileFieldEditor(TestPropertyConstants.PROPERTY_TEST_CLASS, "Test class:", getFieldEditorParent());
        addField(testClassEditor);
    }

    
    @Override
    public void setElement(IAdaptable element) {
        project = (IProject) element.getAdapter(IProject.class);
        if (project != null) {
            preferenceStore = new ScopedPreferenceStore(new ProjectScope(project), Activator.PLUGIN_ID);
            setPreferenceStore(preferenceStore);
        }
    }

    @Override
    public IAdaptable getElement() {
        return project;
    }
    
    
    @Override
	public void propertyChange(PropertyChangeEvent event) {
    	
    	if(event.getSource() instanceof CheckboxFieldEditor) {
    		System.out.println("###change-boolean:");
    		CheckboxFieldEditor checkboxFieldEditor = (CheckboxFieldEditor)event.getSource();
    		String nameFieldEditor = checkboxFieldEditor.getPreferenceName();
    		System.out.println("###change-sw1:"+nameFieldEditor);
    		switch (nameFieldEditor) {
			case TestPropertyConstants.PROPERTY_ENABLE_TEST_FOLDER:
				if(testFolderCheckbox.getBooleanValue()) {
					testFolderEditor.setEnabled(true, getFieldEditorParent());
					testRegexCheckbox.setEnabled(true, getFieldEditorParent());
					testRegexEditor.setEnabled(true, getFieldEditorParent());
					testRegexCheckbox.setValue(false);
					testClassEditor.setEnabled(false, getFieldEditorParent());
					testClassCheckbox.setValue(false);
				}else {
					testFolderEditor.setEnabled(false, getFieldEditorParent());
					testRegexCheckbox.setEnabled(false, getFieldEditorParent());
					testRegexEditor.setEnabled(false, getFieldEditorParent());
					testRegexCheckbox.setValue(false);
				}
				break;
			case TestPropertyConstants.PROPERTY_ENABLE_TEST_REGEX:
				testRegexEditor.setEnabled(testRegexCheckbox.getBooleanValue(), getFieldEditorParent());
				break;
			case TestPropertyConstants.PROPERTY_ENABLE_TEST_CLASS:
				
				if(testClassCheckbox.getBooleanValue()) {
					testFolderEditor.setEnabled(false, getFieldEditorParent());
					testFolderCheckbox.setValue(false);
					testRegexCheckbox.setEnabled(false, getFieldEditorParent());
					testRegexEditor.setEnabled(false, getFieldEditorParent());
					testRegexCheckbox.setValue(false);
					testClassEditor.setEnabled(true, getFieldEditorParent());
				}else {
					testClassEditor.setEnabled(false, getFieldEditorParent());
				}
				break;

			default:
				break;
			}
    	}
	}
    
    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        
        try {
            preferenceStore.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
    }
}
