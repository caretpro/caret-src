package caret.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

import caret.ChatView;
import caret.agent.Response;
import caret.data.Agent;
import caret.data.Context;
import caret.data.Interaction;
import caret.data.Result;
import caret.tasks.Task;
import caret.tool.Util;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ResponseDialog extends Dialog {
    private String title;
    private String oldCode;
    private String newCode;
    private Button backButton;
    private Button nextButton;
    private Button otherButton;
    private StyledText leftStyledText;
    private StyledText rightStyledText;
    private int selectedIndex = -1;
    private ChatView chatView;
    private Task task;
    private Context context;
    private Response lastResponse;
    private Label rightLabel;
    private List<Response> cacheResponseCode  = new ArrayList<Response> ();
    private int currentCacheResponseIndex = 0;

    public ResponseDialog(Shell parentShell, String title, String oldCode, Response response, Task task, Context context) {
        super(parentShell);
        this.title = title;
        this.oldCode = oldCode;
        this.newCode = Util.codeToDialog(response.getCode());
        lastResponse = response;
        cacheResponseCode.add(response);
        this.chatView = ChatView.getInstance();
        this.task = task;
        this.context = context;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
    	Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, true));
        
        Label leftLabel = new Label(container, SWT.NONE);
        leftLabel.setText("Original code:");
        leftLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        rightLabel = new Label(container, SWT.NONE);
        rightLabel.setText("Code suggestion ("+lastResponse.getAgentId()+"):");
        rightLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

        leftStyledText = new StyledText(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        leftStyledText.setEditable(false);
        leftStyledText.setText(oldCode); 
        GridData leftGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        leftStyledText.setLayoutData(leftGridData);

        rightStyledText = new StyledText(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        rightStyledText.setEditable(false);
        rightStyledText.setText(newCode); 
        GridData rightGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        rightStyledText.setLayoutData(rightGridData);

        Point leftSize = leftStyledText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Point rightSize = rightStyledText.computeSize(SWT.DEFAULT, SWT.DEFAULT);

        int maxWidth = Math.max(leftSize.x, rightSize.x);
        int maxHeight = Math.max(leftSize.y, rightSize.y);

        leftGridData.widthHint = maxWidth;
        rightGridData.widthHint = maxWidth;
        leftGridData.heightHint = maxHeight;
        rightGridData.heightHint = maxHeight;

        container.layout(true, true);
        
        Util.applySyntaxHighlighting(leftStyledText);
        Util.applySyntaxHighlighting(rightStyledText);
        Util.highlightDifferences( leftStyledText, rightStyledText);
        return container;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
   
    	backButton = createButton(parent, 3, "<Back", false);
    	backButton.setVisible(false);
        backButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	int maxIndex = cacheResponseCode.size() - 1;
            	if(currentCacheResponseIndex>0 && currentCacheResponseIndex <= maxIndex) {
            		currentCacheResponseIndex--;
            		uodateViewCodeSuggestion(currentCacheResponseIndex);
            		if(currentCacheResponseIndex == 0) {
            			backButton.setEnabled(false);
            		}
            		nextButton.setEnabled(true);
            	}
            }
        });
        nextButton = createButton(parent, 4, "Next>", false);
        nextButton.setVisible(false);
        nextButton.setEnabled(false);
        nextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	int maxIndex = cacheResponseCode.size() - 1;
            	if(currentCacheResponseIndex>=0 && currentCacheResponseIndex < maxIndex) {
            		currentCacheResponseIndex++;
            		uodateViewCodeSuggestion(currentCacheResponseIndex);
            		if(currentCacheResponseIndex == maxIndex) {
            			nextButton.setEnabled(false);
            		}
            		backButton.setEnabled(true);
            	}
            }
        });
    	otherButton = createButton(parent, 5, "Other>", false);
        otherButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	String [] listAgents = chatView.getTaskProcessingAgents();
				List<String> agents = Arrays.asList(listAgents);
                Menu menu = crearMenu(getParentShell(), agents);
                Point p = otherButton.getLocation();
                Point shellPoint = otherButton.getParent().toDisplay(p.x, p.y);
                menu.setLocation(shellPoint.x, shellPoint.y - (20 * menu.getItemCount())); 
                menu.setVisible(true);
            }
        });
        
        createButton(parent, OK, "OK", true);
        createButton(parent, CANCEL, "Cancel", false);

    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }
    
    private Menu crearMenu(Shell shell, List<String> agents) {
        Menu menu = new Menu(shell, SWT.POP_UP);

        for (int i = 0; i < agents.size(); i++) {
            String agentId = agents.get(i);
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText(agentId);

            int index = i; 
            item.addListener(SWT.Selection, e -> {
                System.out.println("Índex: " + index + ", Agent: " + agentId);
                process(agentId);
            });
        }

        return menu;
    }
    
    private void process(String agentId) {
    	String messageInput = "You are a code Assistant that helps a software developer (User) in programming tasks. Our previous conversation is the following:\n"
				+chatView.getInteractions(task.getCode())+".\n"
				+"Now, try again another response. "+task.getDescription()+" (You answer only java code)";
		messageInput = Util.codeToLine(messageInput, true);
		Interaction userInteraction = new Interaction();
		userInteraction.setRole(ChatView.USER);
		userInteraction.setText("Try again. Give a different answer");
		userInteraction.setCode(null);
		userInteraction.setTaskCode(task.getCode());
		userInteraction.setContext(context);
		chatView.addInteraction(userInteraction);
		
	    Response response = chatView.processMessage(agentId, messageInput, ChatView.TEMPERATURE_HIGH);
	    Interaction botInteraction = new Interaction();
		botInteraction.setRole(ChatView.BOT);
		botInteraction.setTaskCode(task.getCode());
		botInteraction.setContext(context);
		botInteraction.setResult(new Result(new Agent(chatView.getCurrentAgent().getName(), chatView.getCurrentAgent().getTechnology(), chatView.getCurrentAgent().hasIntent()),false, false));
		if(response != null) {
			lastResponse = response;
			botInteraction.setText(response.getText());
			if(response.getCode() != null) {
		    	botInteraction.setCode(response.getCode());
				cacheResponseCode.add(response);
				currentCacheResponseIndex = cacheResponseCode.size()-1;
		    	uodateViewCodeSuggestion(currentCacheResponseIndex);
		    	backButton.setVisible(true);
		    	nextButton.setVisible(true);
		    	backButton.setEnabled(true);
		    	nextButton.setEnabled(false);
			}else {
				System.out.println("* Not suggestion");
			}
	    }
		chatView.addInteraction(botInteraction);
		
    }
    
    public void uodateViewCodeSuggestion(int index) {
    	Response response = cacheResponseCode.get(index);
		rightLabel.setText("Code suggestion ("+response.getAgentId()+"):");
		updateLabel(rightLabel);
		rightStyledText.setText(Util.trim(response.getCode()));
		Util.applySyntaxHighlighting(rightStyledText);
		Util.highlightDifferences( leftStyledText, rightStyledText);
    }
    
    
    public Response getResponse() {
    	return cacheResponseCode.get(currentCacheResponseIndex);
    }
    
    public void updateLabel(Label label) {
    	Point newSize = rightLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);

    	// Update layout data if using a layout manager
    	Object layoutData = rightLabel.getLayoutData();
    	if (layoutData instanceof GridData) {
    	    GridData gridData = (GridData) layoutData;
    	    gridData.widthHint = newSize.x;
    	    rightLabel.setLayoutData(gridData);
    	}

    	// Force layout and redraw
    	rightLabel.getParent().layout(true, true); // Update the parent layout
    	rightLabel.update(); // Redraw the label

    }
}
