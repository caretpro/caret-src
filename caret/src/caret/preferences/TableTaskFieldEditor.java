package caret.preferences;

import java.util.HashMap;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import java.util.ArrayList;
import com.google.gson.Gson;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public abstract class TableTaskFieldEditor extends FieldEditor {

    private Table table;
    PTask[] originalTasks;

    protected TableTaskFieldEditor() {
    }

    protected TableTaskFieldEditor(String name, String labelText, Composite parent) {
        init(name, labelText);
        createControl(parent);
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
        Control control = getLabelControl();
        ((GridData) control.getLayoutData()).horizontalSpan = numColumns;
        ((GridData) table.getLayoutData()).horizontalSpan = numColumns - 1;
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        Control label = getLabelControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        label.setLayoutData(gd);

        table = getTableControl(parent);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = numColumns - 1;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        table.setLayoutData(gd);
    }

    @Override
    protected void doLoad() {
        if (table != null) {
            table.removeAll();
            String json = getPreferenceStore().getString(getPreferenceName());

            if (json != null && !json.isEmpty()) {
                Gson gson = new Gson();
                originalTasks = gson.fromJson(json, PTask[].class);
                for (PTask task : originalTasks) {
                    TableItem tableItem = new TableItem(table, SWT.NONE);
                    tableItem.setText(new String[] { "", task.getTaskName() });
                    tableItem.setChecked(task.isEnabled());

                    createButtonValidatorForTableItem(tableItem, task);
                    createButtonContextForTableItem(tableItem, task);
                }
            }
        }
    }

    @Override
    protected void doLoadDefault() {
        if (table != null) {
            table.removeAll();
            String json = getPreferenceStore().getDefaultString(getPreferenceName());

            if (json != null && !json.isEmpty()) {
                Gson gson = new Gson();
                originalTasks = gson.fromJson(json, PTask[].class);
                for (PTask task : originalTasks) {
                    TableItem tableItem = new TableItem(table, SWT.NONE);
                    tableItem.setText(new String[] { "", task.getTaskName() });
                    tableItem.setChecked(task.isEnabled());

                    createButtonValidatorForTableItem(tableItem, task);
                    createButtonContextForTableItem(tableItem, task);
                }
            }
        }
    }

    @Override
    protected void doStore() {
        if (table != null) {
            List<PTask> tasks = new ArrayList<>();
            for (int i = 0; i < table.getItemCount(); i++) {
                TableItem tableItem = table.getItem(i);
                String taskName = tableItem.getText(1);
                boolean isChecked = tableItem.getChecked();
                
                // Create PTask object for each row in the table
                PTask task = new PTask(isChecked, taskName, getValidatorsForTask(taskName), getContextForTask(taskName));
                tasks.add(task);
            }

            Gson gson = new Gson();
            String json = gson.toJson(tasks);
            getPreferenceStore().setValue(getPreferenceName(), json);
        }
    }

    public Table getTableControl(Composite parent) {
        if (table == null) {
            table = new Table(parent, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
            table.setHeaderVisible(true);
            table.setLinesVisible(true);

            TableColumn checkboxColumn = new TableColumn(table, SWT.NONE);
            checkboxColumn.setText("");
            checkboxColumn.setWidth(10);

            TableColumn taskColumn = new TableColumn(table, SWT.NONE);
            taskColumn.setText("Task");
            taskColumn.setWidth(300);

            TableColumn buttonValidatorsColumn = new TableColumn(table, SWT.NONE);
            buttonValidatorsColumn.setText("Cycles");
            buttonValidatorsColumn.setWidth(50);
            
            TableColumn buttonContextColumn = new TableColumn(table, SWT.NONE);
            buttonContextColumn.setText("Context");
            buttonContextColumn.setWidth(50);

            table.addDisposeListener(event -> table = null);
        } else {
            checkParent(table, parent);
        }
        return table;
    }

    private void createButtonValidatorForTableItem(TableItem tableItem, PTask task) {
        TableEditor editor = new TableEditor(table);
        Button button = new Button(table, SWT.PUSH);
        button.setText("...");
        button.setSize(30, 15);
        button.addListener(SWT.Selection, event -> handleButtonValidatorsClick(tableItem, task));

        editor.grabHorizontal = true;
        editor.setEditor(button, tableItem, 2);
    }
    
    private void createButtonContextForTableItem(TableItem tableItem, PTask task) {
        TableEditor editor = new TableEditor(table);
        Button button = new Button(table, SWT.PUSH);
        button.setText("...");
        button.setSize(30, 15);
        button.addListener(SWT.Selection, event -> handleButtonContextClick(tableItem, task));

        editor.grabHorizontal = true;
        editor.setEditor(button, tableItem, 3);
    }

    protected void handleButtonValidatorsClick(TableItem tableItem, PTask task) {
        /*CheckValidatorDialog dialog = new CheckValidatorDialog(Display.getCurrent().getActiveShell(), task.getTaskName(), "Validators to apply:", task.getValidators());
        if (dialog.open() == Dialog.OK) {
            // Update task validators based on dialog result
            HashMap<String, Boolean> updatedValidators = dialog.getValidators();
            task.setValidators(updatedValidators);
            // Update the original tasks array with the new validators
            for (int i = 0; i < originalTasks.length; i++) {
                if (originalTasks[i].getTaskName().equals(task.getTaskName())) {
                    originalTasks[i].setValidators(updatedValidators);
                    break;
                }
            }
        }*/
    	CycleValidatorsDialog dialog = new CycleValidatorsDialog(Display.getCurrent().getActiveShell(), "Improvement cycles: "+task.getTaskName(), "Validators to apply:");
        if (dialog.open() == Dialog.OK) {
        	
        }
    }
    
    protected void handleButtonContextClick(TableItem tableItem, PTask task) {
    	HashMap<String, Boolean> contextInfo = new HashMap<String, Boolean> ();
    	contextInfo.put("Extended Class", null);
    	contextInfo.put("Implemented Interface", null);
    	contextInfo.put("Attribute", null);
    	contextInfo.put("Method parameters", null);
    	contextInfo.put("Local method variables", null);
        CheckValidatorDialog dialog = new CheckValidatorDialog(Display.getCurrent().getActiveShell(), "Context: "+task.getTaskName(), "Context information to send:", task.getContext());
        if (dialog.open() == Dialog.OK) {
            // Update task validators based on dialog result
            HashMap<String, Boolean> updatedValidators = dialog.getValidators();
            task.setValidators(updatedValidators);
            // Update the original tasks array with the new validators
            for (int i = 0; i < originalTasks.length; i++) {
                if (originalTasks[i].getTaskName().equals(task.getTaskName())) {
                    originalTasks[i].setValidators(updatedValidators);
                    break;
                }
            }
        }
    }

    // Replace the simulated method with the actual task validators from originalTasks
    private HashMap<String, Boolean> getValidatorsForTask(String taskName) {
        for (PTask task : originalTasks) {
            if (task.getTaskName().equals(taskName)) {
                return task.getValidators();  // Return the actual validators from originalTasks
            }
        }
        return new HashMap<>();  // Default return in case no task is found (although it should always find a match)
    }

    private HashMap<String, Boolean> getContextForTask(String taskName) {
        for (PTask task : originalTasks) {
            if (task.getTaskName().equals(taskName)) {
                return task.getContext();  // Return the actual validators from originalTasks
            }
        }
        return new HashMap<>();  // Default return in case no task is found (although it should always find a match)
    }
    
    @Override
    public int getNumberOfControls() {
        return 2;
    }

    protected abstract String getNewInputObject();
}
