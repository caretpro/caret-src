package caret.ui;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import caret.tasks.OtherParameter;
import caret.tasks.Parameter;

public class InputParametersDialog extends Dialog {
	
	private String title;
	private String message;
	private IInputValidator validator = null ;
	private Button okButton;
	private Text errorMessageText;
	private String errorMessage;
	private HashMap<String, Text> parametersText = new HashMap<String, Text>();
	private Parameter [] parameters;

	public InputParametersDialog(Shell parentShell, String dialogTitle,
			String dialogMessage, Parameter [] parameters) {
		super(parentShell);
		this.title = dialogTitle;
		message = dialogMessage;
		this.parameters = parameters;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			for (String key:parametersText.keySet()) {
				if(!parametersText.get(key).getText().equals("")) {
					getParameter(key).setValue(parametersText.get(key).getText());
				}else {
					getParameter(key).setValue(null);
				}
			    
			}
		}
		
		super.buttonPressed(buttonId);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite composite = (Composite) super.createDialogArea(parent);
		// create message
		composite.setLayout( new GridLayout( 2, false ) );
        if (message != null) {
			Label label = new Label(composite, SWT.WRAP);
			label.setText(message);
			GridData data = new GridData(GridData.GRAB_HORIZONTAL
					| GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_CENTER);
			data.horizontalSpan=2;
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(parent.getFont());
		}
        for (Parameter parameter : parameters) {
        	if (parameter.isRequired() || parameter.getParameterType() instanceof OtherParameter) {
        		Label label = new Label(composite, SWT.LEFT);
                label.setText(parameter.getDescription()+":");
                Text textInput = new Text(composite, SWT.SINGLE | SWT.BORDER);
                GridData gridData = new GridData();
                gridData.horizontalAlignment = GridData.FILL;
                gridData.grabExcessHorizontalSpace = true;
                textInput.setLayoutData(gridData);
                if(parameter.getValue()!=null) {
                	textInput.setText(parameter.getValue());
                }
                parametersText.put(parameter.getName(), textInput);
			}
        	
		}

		applyDialogFont(composite);
		return composite;
	}

	@Deprecated
	protected Label getErrorMessageLabel() {
		return null;
	}

	protected Button getOkButton() {
		return okButton;
	}

	/*protected Text getText() {
		return text;
	}*/
	
	protected IInputValidator getValidator() {
		return validator;
	}

	public String getParameterValue(String parameterName) {
		return getParameter(parameterName).getValue();
	}
	
	protected void validateInput() {
		String errorMessage = null;
		if (validator != null) {
			//errorMessage = validator.isValid(text.getText());
		}
		// Bug 16256: important not to treat "" (blank error) the same as null
		// (no error)
		setErrorMessage(errorMessage);
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		if (errorMessageText != null && !errorMessageText.isDisposed()) {
			errorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
			// Disable the error message text control if there is no error, or
			// no error text (empty or whitespace only).  Hide it also to avoid
			// color change.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
			boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
			errorMessageText.setEnabled(hasError);
			errorMessageText.setVisible(hasError);
			errorMessageText.getParent().update();
			// Access the ok button by id, in case clients have overridden button creation.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
			Control button = getButton(IDialogConstants.OK_ID);
			if (button != null) {
				button.setEnabled(errorMessage == null);
			}
		}
	}

	protected int getInputTextStyle() {
		return SWT.SINGLE | SWT.BORDER;
	}
	
	public Parameter getParameter(String parameterName) {
		for (Parameter parameter : parameters) {
			if(parameter.getName().equals(parameterName)) {
				return parameter;
			}
		}
		return null;
	}
}
