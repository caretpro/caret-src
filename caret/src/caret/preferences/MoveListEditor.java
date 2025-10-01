package caret.preferences;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public abstract class MoveListEditor extends FieldEditor {

	private List list;
	private Composite buttonBox;
	private Button upButton;
	private Button downButton;
	private SelectionListener selectionListener;

	protected MoveListEditor() {
	}

	protected MoveListEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}
	
	private void addPressed() {
		setPresentsDefaultValue(false);
		String input = getNewInputObject();

		if (input != null) {
			int index = list.getSelectionIndex();
			if (index >= 0) {
				list.add(input, index + 1);
			} else {
				list.add(input, 0);
			}
			selectionChanged();
		}
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) list.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	private void createButtons(Composite box) {
		upButton = createPushButton(box, "ListEditor.up");//$NON-NLS-1$
		downButton = createPushButton(box, "ListEditor.down");//$NON-NLS-1$
	}

	protected abstract String createList(String[] items);

	private Button createPushButton(Composite parent, String key) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(JFaceResources.getString(key));
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button,
				IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	public void createSelectionListener() {
		selectionListener = widgetSelectedAdapter(event -> {
			Widget widget = event.widget;
			if (widget == upButton) {
				upPressed();
			} else if (widget == downButton) {
				downPressed();
			} else if (widget == list) {
				selectionChanged();
			}
		});
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		list = getListControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		list.setLayoutData(gd);

		buttonBox = getButtonBoxControl(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		if (list != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			String[] array = parseString(s);
			for (String element : array) {
				list.add(element);
			}
		}
	}

	@Override
	protected void doLoadDefault() {
		if (list != null) {
			list.removeAll();
			String s = getPreferenceStore().getDefaultString(
					getPreferenceName());
			String[] array = parseString(s);
			for (String element : array) {
				list.add(element);
			}
		}
	}

	@Override
	protected void doStore() {
		String s = createList(list.getItems());
		if (s != null) {
			getPreferenceStore().setValue(getPreferenceName(), s);
		}
	}

	private void downPressed() {
		swap(false);
	}

	public Composite getButtonBoxControl(Composite parent) {
		if (buttonBox == null) {
			buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
			buttonBox.addDisposeListener(event -> {
				upButton = null;
				downButton = null;
				buttonBox = null;
			});

		} else {
			checkParent(buttonBox, parent);
		}

		selectionChanged();
		return buttonBox;
	}

	public List getListControl(Composite parent) {
		if (list == null) {
			list = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
					| SWT.H_SCROLL);
			list.setFont(parent.getFont());
			list.addSelectionListener(getSelectionListener());
			list.addDisposeListener(event -> list = null);
		} else {
			checkParent(list, parent);
		}
		return list;
	}

	protected abstract String getNewInputObject();

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	private SelectionListener getSelectionListener() {
		if (selectionListener == null) {
			createSelectionListener();
		}
		return selectionListener;
	}

	protected Shell getShell() {
		if (upButton == null) {
			return null;
		}
		return upButton.getShell();
	}

	protected abstract String[] parseString(String stringList);

	private void removePressed() {
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		if (index >= 0) {
			list.remove(index);
			list.select(index >= list.getItemCount() ? index - 1 : index);
			selectionChanged();
		}
	}

	protected void selectionChanged() {

		int index = list.getSelectionIndex();
		int size = list.getItemCount();

		upButton.setEnabled(size > 1 && index > 0);
		downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}

	@Override
	public void setFocus() {
		if (list != null) {
			list.setFocus();
		}
	}

	private void swap(boolean up) {
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		int target = up ? index - 1 : index + 1;

		if (index >= 0) {
			String[] selection = list.getSelection();
			Assert.isTrue(selection.length == 1);
			list.remove(index);
			list.add(selection[0], target);
			list.setSelection(target);
		}
		selectionChanged();
	}

	private void upPressed() {
		swap(true);
	}

	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getListControl(parent).setEnabled(enabled);
		upButton.setEnabled(enabled);
		downButton.setEnabled(enabled);
	}

	protected Button getUpButton() {
		return upButton;
	}

	protected Button getDownButton() {
		return downButton;
	}

	protected List getList() {
		return list;
	}
}

