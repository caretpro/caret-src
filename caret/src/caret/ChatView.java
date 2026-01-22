package caret;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import caret.agent.AgentInterface;
import caret.agent.Response;
import caret.agent.ResponseJSON;
import caret.contentAssist.Suggestions;
import caret.data.Agent;
import caret.data.AnnotationRestorer;
import caret.data.ChatData;
import caret.data.Context;
import caret.data.ContextConversation;
import caret.data.Interaction;
import caret.data.LogData;
import caret.data.Result;
import caret.data.MongoDB;
import caret.preferences.PTask;
import caret.preferences.PreferenceConstants;
import caret.preferences.PreferenceInitializer;
import caret.project.Resource;
import caret.project.java.ASTMethodExtractor;
import caret.project.java.JavaProject;
import caret.project.java.MethodReplacer;
import caret.service.CodeAnalizer;
import caret.stats.AgentsStatistics;
import caret.stats.StatisticsQuery;
import caret.stats.StatisticsResponse;
import caret.tasks.ITasksGroup;
import caret.tasks.JavaConcept;
import caret.tasks.JavaParameter;
import caret.tasks.OtherParameter;
import caret.tasks.Parameter;
import caret.tasks.Task;
import caret.tasks.TasksGroup;
import caret.tasks.TasksManager;
import caret.tool.ClassInfoExtractor;
import caret.tool.ClasspathLoader;
import caret.tool.Hash;
import caret.tool.InMemoryJavaCompiler;
import caret.tool.JavaSourceCompiler;
import caret.tool.Log;
import caret.tool.MethodPosition;
import caret.tool.Parser;
import caret.tool.SyntaxValidator;
import caret.tool.Tuple;
import caret.tool.Util;
import caret.ui.InputParametersDialog;
import caret.ui.PopupDialog;
import caret.ui.ResponseDialog;
import caret.validator.Validation;
import caret.validator.ValidatorInterface;
import caret.vcs.GitUser;

import java.util.UUID;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.core.runtime.Path;

public class ChatView  {
	private Text textInput;
	private String messageInput;
    private String textChat = "";
    private String sessionId = UUID.randomUUID().toString();
    private static ChatView chatView;
    public String lastProjectName = null;
    private String lastPackageName = null;
    private JavaProject javaProject = null;
    private String preferenceAgent = "";
    private IPreferenceStore store;
    private AgentInterface currentAgent = null;
    private AgentInterface workingAgent = null;
    private Display display;
    private Composite compositeChat;
    public static String USER = "User";
    public static String BOT = "CARET";
    public static String SYSTEM = "System";
    private ScrolledComposite scrolledComposite;
    private String clipboard;
    private ChatData chatData;
    private String timesession = Util.getDateFormat("yyyyMMdd-HHmmss");
    private String pathWorkspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
    private boolean FirstLLM = true;
    private int textInputMaxLines = 3;
    private int textInputLines = 1;
    private ArrayList<Task> tasks = new ArrayList<Task>();
    private HashMap<Integer, Button> buttonsGoTo = new HashMap<Integer, Button>();
    public static final int NOT_INDEX = -1;
	private static final String EXTENSION_ID = null;
	private static final String NO_CLASSIFICATION = "NO_CLASSIFICATION";
	private Task currentTask = null;
	private ITasksGroup currentITaskGroup = null;
	
	
	public static final String EXTENSION_POINT_TASKS = "caret.TaskGroup";
	public static final String EXTENSION_POINT_AGENT = "caret.extagent";
	public static final String EXTENSION_POINT_VALIDATOR = "caret.Validator";
	public static final String FULL_QUALIFIED_NAME_GENERATED = "caret.annotation.Generated";
	public static final String JAVADOC_LINE_CARET = "Generated code by CARET Assistant"; 	
	public static final String COMPILATION_VALIDATOR = "Compilation Validator";
	public static final String TEST_VALIDATOR = "Test Validator";
	
	public static final int EVENT_TYPE_MESSAGE = 0;
	public static final int EVENT_TYPE_MENU = 1;
	public int currentEventType = -1;
	
	public static final float TEMPERATURE_MAXIMUM = 2.0F;
	public static final float TEMPERATURE_HIGH = 1.4F;		
	public static final float TEMPERATURE_INTERMEDIATE = 1.0F;
	public static final float TEMPERATURE_LOW = 0.2F;
	public static final float TEMPERATURE_MINIMUM = 0.0F;
	
	public static final String DEFAULT_AGENT = null;
	
	public static final String GREETING_MESSAGE = "GREETING_MESSAGE";
	public static final String GOODBYE_MESSAGE = "GOODBYE_MESSAGE";
	public static final String JAVA_PROGRAMMING = "JAVA_PROGRAMMING";
	
	String testsPath= "/Users/Albert/runtime-EclipseApplication/MyProject/src/test/java";
	
	public IResource resource;
	public ContextConversation contextConversation;
	public int maxMessages = 20;
	
	public Task lastTask = null;
	
	public StatisticsView statisticsView = StatisticsView.getInstance();
	
	public Response currentResponse = null;
	public Interaction currentInteraction;
	private GitUser gitUser;
	
	String mongoUser;
	String mongoPassword;
	String mongoHost;
	String mongoDatabase;
	String mongoAppName;
	
	@PostConstruct
    public void createPartControl(Composite parent){
    	this.chatView = this;
    	chatData = new ChatData();
    	chatData.setSessionId(sessionId);
    	contextConversation = new ContextConversation();
    	contextConversation.setSessionId(sessionId);
    	display = parent.getDisplay();
    	store = Activator.getDefault().getPreferenceStore();
    	parent.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        scrolledComposite = new ScrolledComposite( parent, SWT.V_SCROLL );
		compositeChat = new Composite( scrolledComposite, SWT.NONE | SWT.BORDER);
	    compositeChat.setLayout( new GridLayout( 2, false ) );   
	    compositeChat.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
	    scrolledComposite.setContent( compositeChat );
	    scrolledComposite.setExpandVertical( true );
	    scrolledComposite.setExpandHorizontal( true );
	    scrolledComposite.addListener( SWT.Resize, event -> {
	      int width = scrolledComposite.getClientArea().width;
	      scrolledComposite.setMinSize( compositeChat.computeSize( width, SWT.DEFAULT ) );
	    } );
	    
	    parent.setLayout( new GridLayout( 2, false ) );
        GridData gridData = new GridData( SWT.FILL, SWT.FILL, true, true );
        gridData.horizontalSpan=2;
        scrolledComposite.setLayoutData( gridData );
        
        Label label = new Label(parent, SWT.LEFT);
        label.setText("Message:");
        textInput = new Text(parent, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        textInput.setLayoutData(gridData);
        textInput.addListener(SWT.KeyUp, new Listener() {
        	public void handleEvent(Event e) {
	      		if(e.keyCode == 13 && !textInput.getText().equals("")) {
	      			messageInput=textInput.getText();
	      			textInput.setText("");
	      			addMessage(USER, messageInput, null, NOT_INDEX);
	      	    	Display.getDefault().asyncExec(new Runnable() {
	      	    	    public void run() {
	      	    	        sendpost();
	      	    	    }
	      	    	});
	      		}
      		}
      	});
        
        init();
    }
    
    public void init() {
    	AgentsStatistics agentsStatistics = new AgentsStatistics();
    	if(getCurrentProject()!=null) {
    		agentsStatistics.print(getCurrentProject().getLocation().toString());
    	}
    	gitUser = new GitUser();
        gitUser.loadGitUser();
        gitUser.printGitUser();
    	checkAgents();
    	
    	mongoUser     = store.getString(PreferenceConstants.P_MONGO_USER);
    	mongoPassword = store.getString(PreferenceConstants.P_MONGO_PASSWORD);
    	mongoHost     = store.getString(PreferenceConstants.P_MONGO_HOST);
    	mongoDatabase = store.getString(PreferenceConstants.P_MONGO_DATABASE);
    	mongoAppName  = store.getString(PreferenceConstants.P_MONGO_APPNAME);
    	
    }
    
    public void restoreAnnotations() {
    	if(getCurrentProject() == null) {
    		return;
    	}
    	List<Interaction> interactions = LogData.getIteractionsJSON(getCurrentProject());
    	for (Interaction interaction : interactions) {
         	if(interaction.getRole().equals("CARET") && interaction.isPassedPreValidations()) {
         		String projectName= interaction.getContext().getResource().getProjectName();
         		IProject project = getProject(projectName);
         		String filename = interaction.getContext().getResource().getFileName();
         		String className = filename.replaceFirst("\\.java$", "");
         		String methodName = interaction.getContext().getResource().getCodeFragment().getMethodName();
         		String agent = interaction.getResult().getAgent().getTechnology();
                String task = interaction.getTaskCode();
                String id = String.valueOf(interaction.getTimestamp());
                String timestamp = Util.getDateFormat("yyyy-MM-dd HH:mm:ss", interaction.getTimestamp());
         		try {
					boolean added = AnnotationRestorer.addGeneratedAnnotationIfMissing(project, className, methodName, agent, task, id, timestamp);
					if(added) {
						addMessage(BOT,"Annotation restored: "+projectName+"->"+className+"->"+methodName+"(...)", null, NOT_INDEX);
					}
         		} catch (CoreException e) {
					e.printStackTrace();
				}
         	}
         }
	}

	public void runCompiler() {
    	String projectPath = getCurrentProject().getLocation().toString();
    	Log.d(projectPath);
    	String className = "AgentsStatistics";
    	String code =
    	        "\n"
    	        + "import java.util.ArrayList;\n"
    	        + "import java.util.HashMap;\n"
    	        + "import java.util.List;\n"
    	        + "import java.util.Map;\n"
    	        + "\n"
    	        + "import com.google.gson.Gson;\n"
    	        + "import com.google.gson.reflect.TypeToken;\n"
    	        + "\n"
    	        + "import caret.data.Extractor;\n"
    	        + "import caret.data.Interaction;\n"
    	        + "import caret.tool.Util;\n"
    	        + "\n"
    	        + "public class AgentsStatistics implements Extractor{\n"
    	        + "	\n"
    	        + "	public AgentsStatistics (){\n"
    	        + "		\n"
    	        + "	}\n"
    	        + "\n"
    	        + "	public List<Interaction> getIteractionsJSON(String projectPath) {\n"
    	        + "		List<Interaction> totalInteractions = new ArrayList<Interaction> ();\n"
    	        + "		List <String> listInteractionsJSON = Util.readFilesFromDirectory(projectPath+\"/.log\", \".json\");\n"
    	        + "		Gson gson = new Gson();\n"
    	        + "		for (String interactionsJSON : listInteractionsJSON) {\n"
    	        + "			List<Interaction> interactions = gson.fromJson(interactionsJSON, new TypeToken<List<Interaction>>() {}.getType());\n"
    	        + "	        totalInteractions.addAll(interactions);\n"
    	        + "		}\n"
    	        + "		return totalInteractions;\n"
    	        + "	}\n"
    	        + "  \n"
    	        + "    /*\n"
    	        + "     * Get best agent based on pre-validations\n"
    	        + "     */\n"
    	        + "	@Override\n"
    	        + "	public String getData(String projectPath) {\n"
    	        + "        if (projectPath == null) {\n"
    	        + "            System.err.println(\"No project found.\");\n"
    	        + "            return null;\n"
    	        + "        }\n"
    	        + "\n"
    	        + "        List<Interaction> interactions = getIteractionsJSON(projectPath);\n"
    	        + "        if (interactions.isEmpty()) {\n"
    	        + "            System.err.println(\"No interactions found in \" + projectPath);\n"
    	        + "            return null;\n"
    	        + "        }\n"
    	        + "\n"
    	        + "        Map<String, int[]> agentStats = new HashMap<>();\n"
    	        + "        // [0] = total interactions, [1] = passed pre-validations\n"
    	        + "\n"
    	        + "        for (Interaction interaction : interactions) {\n"
    	        + "            if (interaction.getResult() == null || interaction.getResult().getAgent() == null)\n"
    	        + "                continue;\n"
    	        + "\n"
    	        + "            String agentName = interaction.getResult().getAgent().getName();\n"
    	        + "            agentStats.putIfAbsent(agentName, new int[2]);\n"
    	        + "            agentStats.get(agentName)[0]++; // total\n"
    	        + "            if (interaction.isPassedPreValidations()) {\n"
    	        + "                agentStats.get(agentName)[1]++; // passed\n"
    	        + "            }\n"
    	        + "        }\n"
    	        + "\n"
    	        + "        String bestAgent = null;\n"
    	        + "        double bestRate = -1.0;\n"
    	        + "\n"
    	        + "        for (Map.Entry<String, int[]> entry : agentStats.entrySet()) {\n"
    	        + "            String agent = entry.getKey();\n"
    	        + "            int total = entry.getValue()[0];\n"
    	        + "            int passed = entry.getValue()[1];\n"
    	        + "            double rate = (total > 0) ? (double) passed / total : 0.0;\n"
    	        + "\n"
    	        + "            System.out.printf(\"Agent: %s | Passed: %d | Total: %d | Rate: %.2f%%%n\",\n"
    	        + "                    agent, passed, total, rate * 100);\n"
    	        + "\n"
    	        + "            if (rate > bestRate) {\n"
    	        + "                bestRate = rate;\n"
    	        + "                bestAgent = agent;\n"
    	        + "            }\n"
    	        + "        }\n"
    	        + "\n"
    	        + "        return bestAgent;\n"
    	        + "	}\n"
    	        + "}";

    	try {
    	    Object result = InMemoryJavaCompiler.compileAndRun(className, code, "getData", projectPath);
    	    System.out.println("### Resultado: " + result.toString());
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    }
    
    
    public void runCompiler(String code) {
    	String projectPath = getCurrentProject().getLocation().toString();
    	Log.d(projectPath);
    	String className = "ExtractorStatistics";
    	try {
    	    Object result = InMemoryJavaCompiler.compileAndRun(className, code, "getData", projectPath);
    	    System.out.println("### Resultado: " + result.toString());
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
    }
    
    public void checkAgents() {
    	Display.getDefault().asyncExec(new Runnable() {
    	    public void run() {
    	    	if(!testAgents()) {
    				addMessage(BOT,"The agents are not available. Please check their configuration or your internet connection.", null, NOT_INDEX);
    	    	}
    	    }
    	});
    }
    
    public boolean testAgents() {
    	List<AgentInterface> agents = getAgents(true);
    	System.out.println("- Agents: "+agents.size());
    	for (AgentInterface agent : agents) {
    		Response response = agent.processMessage("Hi", TEMPERATURE_LOW);
    		System.out.println("- test agent ("+agent.getName()+"): "+response.getText());
			if(response != null){
				if(!response.isError()) {
					return true;
				}
			}
		}
    	return false;
    }
    
    public String getGreettingMessage() {
    	String grettingMessage = "Hi! How can I assist you today?";
    	try {
    		List<Task> tasks = TasksManager.getPreferenceTasks(); 
    		String listTasks = ""; 
    		for (Task itemTask : tasks) {
    			listTasks += "- "+itemTask.getName()+"\n";
    		}
    		if(!listTasks.equals("")) {
    			grettingMessage = "Hi! I am CARET, an assistant for Java development. I support the following tasks:\n"+listTasks
    					+"How can I assist you today?";
    		}
		} catch (Exception e) {
			Log.d(e.getMessage());
		}
    	return grettingMessage;
    }
    
    public void createPackage(String projectName, String packageName) {
        try {
            IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

            IProject project = workspaceRoot.getProject(projectName);
            
            if (project.exists() && project.isOpen()) {
                IJavaProject javaProject = JavaCore.create(project);

                IPackageFragmentRoot packageRoot = javaProject.getPackageFragmentRoot(project.getFolder("src"));
                IPackageFragment newPackage = packageRoot.createPackageFragment(packageName, false, null);
                
                System.out.println("Package " + packageName + " created " + projectName);
            } else {
                System.out.println("Project " + projectName + " doesn't exist or doesn't open.");
            }
        } catch (Exception e) {
            //e.printStackTrace();
        	Log.e(e.getMessage());
        }
    }
    
    public void analizeCode() {
    	Display.getDefault().asyncExec(new Runnable() {
    	    public void run() {
    	    	IResource resource = Resource.getSelectedResource();
    	    	CodeAnalizer codeAnalizer = new CodeAnalizer(resource);
    	    	Thread thread =new Thread(new Runnable() {
	  	    	    public void run() {
		    	        codeAnalizer.analize();
	  	    	    }
	  	    	});  
    	    	thread.start();
    	    }
    	});
    }
    
    public void updateTextHeight(){
    	if(textInputLines != textInput.getLineCount()) {
			GridData gridData = new GridData();
	        gridData.horizontalAlignment = GridData.FILL;
	        gridData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
	        gridData.grabExcessHorizontalSpace = true;
	        int lines = textInput.getLineCount() > textInputMaxLines ? textInputMaxLines : textInput.getLineCount();
	        gridData.heightHint = lines * textInput.getLineHeight()+12;
	        textInput.setLayoutData(gridData);
	        textInput.getParent().layout(true, true);
	        textInputLines = textInput.getLineCount();
		}
    }

    public static ChatView getInstance() {
    	return chatView;
    }

	public void addMessage(String role, String message, String code, int index) {
    	Button buttonCopy = null;
    	Button buttonGoTo = null;
    	Log.d("### ADD MESSAGE:"+role+"->"+message+"|#|code:"+code);
	    StyledText styledTextChat = new StyledText(compositeChat, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        styledTextChat.setEditable(false);
        
    	StyleRange styleRange = new StyleRange();
    	styleRange.start = 0;
    	styleRange.length = role.length();
    	String roleShow="";
    	if(role==USER) {
    		styleRange.fontStyle = SWT.BOLD;
    		styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_GREEN);
    		roleShow=role;
    		styledTextChat.setText(role+":\n"+message);
    	}
    	if(role==BOT) {
    		styleRange.fontStyle = SWT.BOLD;
    		styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_BLUE);
    		if(currentAgent != null) {
    			roleShow = role+" ("+currentAgent.getTechnology()+")";
    		}else {
    			roleShow = role;
    		}
    		styledTextChat.setText(roleShow+":\n"+message);
    		
    		StyleRange styleCode2 = new StyleRange();
	    	styleCode2.start = role.length();
	    	styleCode2.length = roleShow.length() - role.length();
	    	styleCode2.foreground = display.getSystemColor(SWT.COLOR_GRAY);
	    	styledTextChat.setStyleRange(styleCode2);
    	}
    	if(role==SYSTEM) {
    		styleRange.length = message.length();
    		styleRange.foreground = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        	styledTextChat.setText(message);
        }
    	styledTextChat.setStyleRange(styleRange);
	    GridData gridData = new GridData( SWT.FILL, SWT.CENTER, true, false );
        gridData.horizontalSpan=2;
        styledTextChat.setLayoutData(gridData);
        if((role == BOT || role == USER) && code != null) {
        	if(role == BOT) {
		        buttonCopy = new Button(compositeChat, SWT.PUSH);
		        buttonCopy.setText("Copy code");
		        buttonCopy.addListener(SWT.Selection, new Listener() {
		            public void handleEvent(Event e) {
		              switch (e.type) {
		              case SWT.Selection:
		                copyClipboard(code);
		                break;
		              }
		            }
		          });
		        buttonGoTo = new Button(compositeChat, SWT.PUSH);
		        buttonGoTo.setText("Go to");
		        buttonGoTo.addListener(SWT.Selection, new Listener() {
		            public void handleEvent(Event e) {
		              switch (e.type) {
		              case SWT.Selection:
		                goToCode(index);
		                break;
		              }
		            }
		          });
        	}
        	if(message.indexOf(code) > -1) {
		        StyleRange styleCode = new StyleRange();
		    	styleCode.start = roleShow.length()+2+message.indexOf(code);
		    	styleCode.length = code.length();
		    	styleCode.foreground = display.getSystemColor(SWT.COLOR_DARK_GRAY);
		    	styledTextChat.setStyleRange(styleCode);
		    	Util.applySyntaxHighlighting(styledTextChat, styleCode.start, styleCode.length);
        	}
        }
        int width = scrolledComposite.getClientArea().width;
	    scrolledComposite.setMinSize( compositeChat.computeSize( width, SWT.DEFAULT ) );
        compositeChat.layout();
	    scrolledComposite.setShowFocusedControl(true);
	    if(buttonCopy!=null) {
	    	scrolledComposite.showControl(buttonCopy);
	    }else {
	    	scrolledComposite.showControl(styledTextChat);
	    	styledTextChat.setSelection(styledTextChat.getCharCount());
	    }    
	}
	
    public void goToCode(int index) {
        String editorId = PlatformUI.getWorkbench().getEditorRegistry().getEditors("Country.java")[0].getId();
        String path = pathWorkspace+"/"+chatData.getInteraction(index).getContext().getResource().getProjectName()+"/"+chatData.getInteraction(index).getContext().getResource().getProjectRelativePath();
        File file = new File(path);
        IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI());
        if(files.length>0) {
	        try {
	        	IFile iFile = files[0];
	        	if(chatData.getInteraction(index).getContext().getResource().getCodeFragment() == null) {
	        		org.eclipse.ui.ide.IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), iFile, editorId);
	        	}else{
	        		IMarker marker = iFile.createMarker(IMarker.TEXT);
	        		marker.setAttribute(IMarker.LINE_NUMBER, chatData.getInteraction(index).getContext().getResource().getCodeFragment().getStartline()+7);
	    			org.eclipse.ui.ide.IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), marker);
	    			marker.delete();
	        	}
	        } catch (Exception e) {
	        	//e.printStackTrace();
	        	Log.e(e.getMessage());
			}
        }else {
        	Log.e("File not found");
        }
    }
    
    public void copyClipboard(String text) {
    	Clipboard cb = new Clipboard(display);
    	TextTransfer t = TextTransfer.getInstance();
    	clipboard=text;
    	cb.setContents(new Object[] { text }, new Transfer[] { t });
    	cb.dispose();
    }

    @Focus
    public void setFocus() {
        textInput.setFocus();
    }
    
    public void addMessageChat(String message) {
    	StyleRange styleRange = new StyleRange();
    	styleRange.start = textChat.length();
    	styleRange.length = message.length();
    	styleRange.fontStyle = SWT.BOLD;
    	styleRange.foreground = display.getSystemColor(SWT.COLOR_BLUE);
    	
    	StyleRange styleRange2 = new StyleRange();
    	styleRange.start = 0;
    	styleRange.length = 10;
    	styleRange.fontStyle = SWT.BOLD;
    	styleRange.foreground = display.getSystemColor(SWT.COLOR_RED);
  
    	textChat = textChat+message;
    }
    
    public String getLineNewSession(String preferenceAgent) {
    	String newsession;
    	String time = Util.getDateFormat("MMM dd, yyyy HH:mm:ss");
        time = Util.toCapitalize(time);
        newsession = "New session "+time+" | Agent: "+preferenceAgent;
        return newsession;
    }
   
    public void sendpost(){
    	String prompt = getTasksPrompt();
    	String messageText = prompt+"\n "+messageInput;
    	System.out.println("FirstLLM: TRUE");
    	if(FirstLLM) {
    		Response response = processMessageTaskClassifier(Util.codeToLine(messageText,true), TEMPERATURE_LOW);
    		if(response != null && !response.isError()) {
    			if(!response.isError()) {
    				System.out.println("Util.getJSON1");
    				if(Util.getJSON(response.getText()) != null) {
    					Task task = null;
    					HashMap<String, String> jsonParameters = null;
    					Gson gson = new Gson();
    					System.out.println("Util.getJSON2");
    					if(Util.getJSON(response.getText()) != null) {
    						System.out.println("Util.getJSON3");
    						jsonParameters = gson.fromJson(Util.getJSON(response.getText()), new TypeToken<HashMap<String, String>>(){}.getType());
    						System.out.println("ClassificationCode: "+jsonParameters.get("classificationCode"));
    						if(jsonParameters.get("classificationCode").equals(GREETING_MESSAGE)){
    							addMessage(BOT,getGreettingMessage(), null, NOT_INDEX);
    							return;
    						}
    						if(jsonParameters.get("classificationCode").equals(GOODBYE_MESSAGE)){
    							addMessage(BOT,"Goodbye!", null, NOT_INDEX);
    							return;
    						}
    						if(jsonParameters.get("classificationCode").equals(JAVA_PROGRAMMING)){
    							Response responseProgramming = processMessage(true, Util.codeToLine(messageInput,true), TEMPERATURE_LOW);
    				    		if(response != null && !response.isError()) {
    				    			addMessage(BOT,responseProgramming.getText(), null, NOT_INDEX);
    				    		}else {
    				    			addMessage(BOT,"Your request could not be processed because the agent doesn't respond", null, NOT_INDEX);
    				    		}
    							return;
    						}
    						task = TasksManager.getTask(jsonParameters.get("classificationCode"));
    					}
    					if (task != null) {
    						updateTaskParameters(task,jsonParameters);
    						Log.d("###Save tasK="+task.getParameterByType(JavaConcept.METHOD.name()).getValue());
    						processTask(task);
    					}else {
    						List<Task> tasks = TasksManager.getPreferenceTasks(); 
    						String listTasks = ""; 
    						for (Task itemTask : tasks) {
    							listTasks += "- "+itemTask.getName()+"\n";
    						}
    						Response fallbackResponse = processMessageFallback(true, getStatisticsPrompt()+" - "+messageInput, TEMPERATURE_LOW);
		            		if(fallbackResponse != null) {
    			            	if(fallbackResponse.getText() != null) {
			            			if(fallbackResponse.getText().indexOf("ABOUT_STATISTICS")>-1) {
			            				Gson statsGson = new Gson();
			            		        StatisticsResponse statsResponse = statsGson.fromJson(Util.getJSON(fallbackResponse.getText()), StatisticsResponse.class);
			            		        System.out.println("Classification: " + statsResponse.getClassificationCode());
			            		        System.out.println("Class: " + statsResponse.getJavaClass());
			            		        runCompiler(statsResponse.getJavaClass());
			            		        StatisticsQuery statsQuery = new StatisticsQuery(
			            		        		messageInput,
			            		        		statsResponse.getJavaClass(),
			            		                gitUser.getUser()
			            		        );
			            		        String projectName = getCurrentProject().getName();
			            		        Gson stGson = new GsonBuilder().setPrettyPrinting().create();;
			            		        String statsJSON = stGson.toJson(statsQuery);
			            		        String timeSession = Util.getDateFormat("yyyyMMdd-HHmmss");
			            		        Util.saveLog(pathWorkspace+"/"+projectName+"/", "query-"+timeSession+".gson", "["+statsJSON+"]", true);
    			            			addMessage(BOT,fallbackResponse.getText(), null, NOT_INDEX);
    			            		}else {
    			            			addMessage(BOT,"Your request could not be processed by some of the supported tasks:\n"+listTasks, null, NOT_INDEX);
        								System.out.println("sendPost: Error getting ABOUT_STATISTICS");
    			            		}
    			            	}else {
    			            		addMessage(BOT,"Your request could not be processed because the agent doesn't respond.", null, NOT_INDEX);
    			            	}
    						}else {
    							addMessage(BOT,"Your request could not be processed by some of the supported tasks:\n"+listTasks, null, NOT_INDEX);
    							System.out.println("sendPost: Error getting supported task");
    						}
    					}
    				}else {
    					if(response.getCode() != null) {
    			            addMessage(BOT,response.getText(),response.getCode(), NOT_INDEX);
    			            if(Parser.getClassName(response.getCode(), Parser.TYPE_CONTENT) != null) {
    			            	createJavaClass(getCurrentProject(), getCurrentPackage(), Parser.getClassName(response.getCode(), Parser.TYPE_CONTENT), response.getCode());
    			            }
    			        }else {
    			            Gson gson = new Gson();
    			            HashMap<String, String> jsonParameters = gson.fromJson(response.getText(), new TypeToken<HashMap<String, String>>(){}.getType());
    			            System.out.println("ClassificationCode: "+jsonParameters.get("classificationCode"));
    			            Task task = TasksManager.getTask(jsonParameters.get("classificationCode"));
    			            if (task!=null) {
    			              updateTaskParameters(task,jsonParameters);
    			              processTask(task);
    			            }else {
    			              String message = "You are a code Assistant that helps a software developer (User) in programming tasks. "
    			              		+ "Answer the following request/question only if is about programming response, "
    			              		+ "otherwise response [NOT_ABOUT_PROGRAMMING]: "+messageText;
    			              Response fallbackResponse = processMessage(true, message, TEMPERATURE_LOW);
    			              if(fallbackResponse != null) {
    			            	  if(fallbackResponse.getText().indexOf("NOT_ABOUT_PROGRAMMING")>-1) {
    			            		  addMessage(BOT,fallbackResponse.getText(), null, NOT_INDEX);
    			            	  }else {
    			            		  addMessage(BOT,"Your request could not be processed by some of the supported tasks.", null, NOT_INDEX);
    					              System.out.println("sendPost: Error getting task");
    			            	  }
    			              }else {
    			            	  addMessage(BOT,"Your request could not be processed by some of the supported tasks.", null, NOT_INDEX);
    				              System.out.println("sendPost: Error getting task");
    			              }
    			            }
    			          }
    				}
    			}else {
    				addMessage(SYSTEM, response.getErrorMessage(), null, NOT_INDEX);
    			}
    		}else {
    			addMessage(SYSTEM, "The agent is not responding", null, NOT_INDEX);
    		}
    		return;
    	}
    	 
    	if(workingAgent != null) {
    		Response response = workingAgent.processMessage(messageInput, TEMPERATURE_HIGH);
			if(response != null){
				System.out.println("response text: "+response.getText());
				System.out.println("response intent: "+response.getIntent());
				if(workingAgent.hasIntent() && !response.getFallbackIntent()) {
					addMessage(BOT,response.getText(), null, NOT_INDEX);
					if(response.getAllRequiredParams() == true) {
						processIntent(response);
						workingAgent = null;
					}
				}else {
					workingAgent = null;
				}
			}else {
				workingAgent = null;
			}
    	}else {
    		ArrayList<AgentInterface> agents = getAgents(false);
    		int i=0;
        	for (AgentInterface agent: agents) {
        		currentAgent = agent;
        		Log.d((++i)+"# Selected agent: "+ agent.getId());
    			Response response = agent.processMessage(messageInput, TEMPERATURE_HIGH);
    			if(response != null){
    				Log.d("response text: "+response.getText());
    				Log.d("response intent: "+response.getIntent());
    				if(agent.hasIntent() && !response.getFallbackIntent()) {
    					addMessage(BOT,response.getText(), null, NOT_INDEX);
    					if(response.getAllRequiredParams() == true) {
    						processIntent(response);
    						workingAgent = null;
    					}else {
    						workingAgent = agent;
    					}
						break;
    				}
    				if(!agent.hasIntent()) {
    					if(response.getCode() != null) {
    						addMessage(BOT,response.getText(),response.getCode(), NOT_INDEX);
    						if(Parser.getClassName(response.getCode(), Parser.TYPE_CONTENT) != null) {
    							createJavaClass(getCurrentProject().getName(), getCurrentPackage().getElementName(), Parser.getClassName(response.getCode(), Parser.TYPE_CONTENT), response.getCode());
    						}
    					}else {
    						addMessage(BOT,response.getText(), null, NOT_INDEX);
    					}
    					break;
    				}
    			}
    		}
    	}
		
    }
    
    private void updateTaskParameters(Task task, HashMap<String, String> jsonParameters) {
		for (int i=0; i< task.getParameters().length; i++) {
			if(getJsonParameter(jsonParameters, task.getParameters()[i].getName())!=null) {
				String value = getJsonParameter(jsonParameters, task.getParameters()[i].getName());
				if(value.equals(task.getParameters()[i].getNoValue())) {
					task.getParameters()[i].setValue(null);
				}else {
					task.getParameters()[i].setValue(getJsonParameter(jsonParameters, task.getParameters()[i].getName()));
				}
			}
		}
	}
    
    private String getJsonParameter(HashMap<String, String> jsonParameters, String name) {
    	for (String key:jsonParameters.keySet()) {
			if(key.toLowerCase().equals(name.toLowerCase())) {
				return jsonParameters.get(key);
			}
		}
    	return null;
    }

	public AgentInterface getAgent() {
    	AgentInterface agent = null;
    	IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(EXTENSION_POINT_AGENT);
		Log.d("Total agent extensions: "+ extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			if(preferenceAgent.equals(element.getAttribute("id"))){
				try {
					agent = (AgentInterface) element.createExecutableExtension("class");			
				} catch (Exception e) {
		        	Log.e("Error agent:"+e.getMessage());
				}
			}
		}
		return agent;
    }
    
 
    
    public ArrayList<AgentInterface> getAgents(boolean LLM) {
    	ArrayList<AgentInterface> agents = new ArrayList<AgentInterface>();
		IConfigurationElement [] extensions = getAgentExtensions();
		Log.d("Total agent extensions: "+ extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			try {
				AgentInterface agent = (AgentInterface) element.createExecutableExtension("class");
				if(agent.isLLM()) {
					agents.add(agent);
				}					
			} catch (Exception e) {
				Log.e("Error agent:"+e.getMessage());
			}			
		}
		return agents;
    }
    
    public IConfigurationElement [] getAgentExtensions() {
    	IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(EXTENSION_POINT_AGENT);
		return extensions;
    }
    
    public IConfigurationElement [] getValidatorExtensions() {
    	IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(EXTENSION_POINT_VALIDATOR);
		return extensions;
    }
    
    public ArrayList<IConfigurationElement> getAgentExtensions(boolean isLLM) {
    	ArrayList<IConfigurationElement> agentExtensions = new ArrayList<IConfigurationElement>();
    	IConfigurationElement [] extensions = getAgentExtensions();
    	Log.d("Total agent extensions: "+ extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			try {
				AgentInterface agent = (AgentInterface) element.createExecutableExtension("class");
				if(agent.isLLM()) {
					agentExtensions.add(element);
				}				
			} catch (Exception e) {
				Log.e("Error agent:"+e.getMessage());
			}			
		}
		return agentExtensions;
    }
    
    public AgentInterface getAgent(String id) {
    	AgentInterface agent = null;
		IConfigurationElement [] extensions = getAgentExtensions();
		Log.d("Total agent extensions: "+ extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			if(element.getAttribute("id").equals(id)){
				try {
					agent = (AgentInterface) element.createExecutableExtension("class");				
				} catch (Exception e) {
					Log.e("Error agent:"+e.getMessage());
				}
			}
		}
		currentAgent = agent;
		return agent;
    }
    
    public AgentInterface getAgent(boolean isLLM) {
    	AgentInterface agent = null;
		IConfigurationElement [] extensions = getAgentExtensions();
		Log.d("Total agent extensions: "+ extensions.length);
		String listAgents [] = getTaskProcessingAgents();
		for (int i = 0; i < listAgents.length; i++) {
			for (int j = 0; j < extensions.length; j++) {
				IConfigurationElement element = extensions[j];
				try {
					System.out.println(i+"-"+j+":AGENT PRINT->"+ element.getAttribute("name")+" - "+listAgents[i]);
					if(((AgentInterface) element.createExecutableExtension("class")).isLLM() == isLLM 
							&& element.getAttribute("name").toLowerCase().equals(listAgents[i].toLowerCase())){
						agent = (AgentInterface) element.createExecutableExtension("class");
						currentAgent = agent;
						return agent;
					}
				} catch (Exception e) {
					Log.e("Error agent:"+e.getMessage());
				}
			}
		}
		currentAgent = agent;
		return agent;
    }
    
    public AgentInterface getTaskClassifierAgent() {
    	AgentInterface agent = null;
		IConfigurationElement [] extensions = getAgentExtensions();
		Log.d("Total agent extensions: "+ extensions.length);
		String listAgents [] = getTaskClassifierAgents();
		for (int i = 0; i < listAgents.length; i++) {
			for (int j = 0; j < extensions.length; j++) {
				IConfigurationElement element = extensions[j];
				try {
					System.out.println(i+"-"+j+":AGENT PRINT->"+ element.getAttribute("name")+" - "+listAgents[i]);
					if(((AgentInterface) element.createExecutableExtension("class")).isLLM() == true 
							&& element.getAttribute("name").toLowerCase().equals(listAgents[i].toLowerCase())){
						agent = (AgentInterface) element.createExecutableExtension("class");
						currentAgent = agent;
						return agent;
					}
				} catch (Exception e) {
					Log.e("Error agent:"+e.getMessage());
				}
			}
		}
		currentAgent = agent;
		return agent;
    }
    
    public String [] getTaskProcessingAgents() {
    	String referenceListAgents = store.getString(PreferenceConstants.P_LIST_TASK_PROCESSING_AGENTS);
    	System.out.println("Agents: "+referenceListAgents);
		return referenceListAgents.split(",");
    }
    
    public String [] getTaskClassifierAgents() {
    	String referenceListAgents = store.getString(PreferenceConstants.P_LIST_TASK_CLASSIFIER_AGENTS);
    	System.out.println("Agents: "+referenceListAgents);
		return referenceListAgents.split(",");
    }
    
    public String [] getContentAssistantAgents() {
    	String referenceListAgents = store.getString(PreferenceConstants.P_LIST_CONTENT_ASSISTANT_AGENTS);
    	System.out.println("Agents: "+referenceListAgents);
		return referenceListAgents.split(",");
    }
    
    public String [] getContentAssistantValidators() {
    	String referenceListValidators = store.getString(PreferenceConstants.P_LIST_VALIDATORS);
    	System.out.println("Validators: "+referenceListValidators);
		return referenceListValidators.split(",");
    }
    
	public String getChatSession() {
		Gson gson = new Gson();
		String jsonChatData = gson.toJson(chatData.getInteractions());
		return jsonChatData;
	}
	
	public void processIntent(Response response) {
		System.out.println("PR1");
	}
	
	public void processTask(Task task) {
		setCurrentTask(task);
		ITasksGroup iTasksGroup = TasksManager.findITasksGroup(task.getCode());
		currentEventType = EVENT_TYPE_MESSAGE;
		iTasksGroup.runTask(task, EVENT_TYPE_MESSAGE);
	}
	
	public String getMethodName(Task task) {
		for (Parameter parameter : task.getParameters()) {
			if(parameter.getName() == "methodName") {
				return parameter.getValue();
			}
		}
		return "NO_METHOD_NAME";
	}
	
	public void createClass(Task task) { // TaskGroup.runTask()
		setCurrentTask(task);
		Log.d("runTask taskName -> createClass: "+task.getName());
		if(task.hasAllParameterValue(true)) {
			if(task.getParameterByType(OtherParameter.DESCRIPTION)!= null || task.parameterWithSource()) {
				if(task.parameterWithSource()) {
					createJavaClass(task);
				}else {
					if(task.getParameterByType(OtherParameter.DESCRIPTION).getValue()!=null) {
						createJavaClass(task);
					}else {
						createCUWithContentDefault(task);
					}
				}
			}else {
				createJavaClass(task);
			}
			
		}else {
			InputParametersDialog dialog = new InputParametersDialog(Display.getCurrent().getActiveShell(), task.getName(), "Fill the text field(s):", task.getParameters());
    		int result = dialog.open();
            if (result == dialog.OK) {
            	if(task.getParameterByType(OtherParameter.DESCRIPTION)!=null || task.parameterWithSource()) {
            		if(task.parameterWithSource()) {
            			createJavaClass(task);
            		}else if(task.getParameterByType(OtherParameter.DESCRIPTION).getValue() != null ) {
            			createJavaClass(task);
            		}else {
            			createCUWithContentDefault(task);
            		}
				}else {
					createCUWithContentDefault(task);
				}
            }
		}
	}
	
	public void createJavaClass(Task task) {
		if(currentEventType == EVENT_TYPE_MENU) {
			chatView.addMessage(chatView.USER, task.getDescriptionWithParameter(), null, chatView.NOT_INDEX);
		}
		Display.getDefault().asyncExec(new Runnable() {
	    	    public void run() {
	    	    	generateJavaClass(task);
	    	    }
	    	});	
	}
	
	public void generateJavaClass(Task task) {
		Log.d("generate Java Class"+task.getName());
		javaProject = new JavaProject(ResourcesPlugin.getWorkspace());
		String parameters ="";
		String source = null;
		for (Parameter parameter : task.getParameters()) {
			if(parameter.getValue() != null) {
				parameters += parameter.getDescription()+" = "+parameter.getValue()+"\n";
				if(parameter.hasSource()) {
					if(parameter.getParameterType().getName() == JavaConcept.CLASS.name() || parameter.getParameterType().getName() == JavaConcept.INTERFACE.name()) {
						IResource resource = Resource.getSelectedResource();
						IProject project = null;
						String packageName = JavaProject.PACKAGE_DEFAULT;
						if(task.getParameterByType(JavaConcept.PACKAGE.name()) != null) {
							packageName = task.getParameterByType(JavaConcept.PACKAGE.name()).getValue();
							if(packageName != null) {
								lastPackageName = packageName;
							}else {
								if(lastPackageName != null) {
									packageName = lastPackageName;
								}else {
									packageName = JavaProject.PACKAGE_DEFAULT;
								}
							}
						}
						String className;
						if(resource == null) {
							project = getCurrentProject();
							if(parameter.getParameterType().getName() == JavaConcept.CLASS.name()) {
								className = task.getParameterByType(JavaConcept.CLASS.name()).getValue();
							}else {
								className = task.getParameterByType(JavaConcept.INTERFACE.name()).getValue();
							}
						}else {
							project = resource.getProject();
							className = resource.getName();
						}
						if(parameter.getSource()!=null) {
							source = Util.codeToLine(parameter.getSource(), true);
						}else {
							if(javaProject.getClassSource(project, className) !=null ) {
								source = Util.codeToLine(javaProject.getClassSource(project, className), false);
							}else {
								addMessage(chatView.BOT, "Error processing the request", null, chatView.NOT_INDEX);
							}
						}
					}
				}
			}
		}
		messageInput = "You are an assistant for java development and output java code between ```java [[code]]```, just generate one class or interface, no more. The required task is: \n";
	    messageInput += getTaskDescriptionInstructions(task)+" with the parameter(s):\n" + parameters;
	    System.out.println("--- MessageInput -----------");
	    System.out.println(messageInput);
	    System.out.println("----------- MessageInput ---");
	    messageInput = Util.codeToLine(messageInput, false);
	    if(source != null) {
	    	messageInput += "Based on the following code: "+source;
	    }
	    Response response = processMessage(true, messageInput, TEMPERATURE_HIGH);
    	if(response != null) {
			if(response.getCode() != null) {
				addLibJar();
				String packageName;
				if(getCurrentPackage() != null) {
					packageName = getCurrentPackage().getElementName();	
				}else {
					packageName = JavaProject.PACKAGE_DEFAULT;
				}
		 		String code = finalizeJavaClass(response.getCode(), packageName);
    			String projectName=getCurrentProject().getName();
		 		if(projectName == null) {
    				Log.d("ProjectName null:"+projectName);
    			}else {
    				Log.d("ProjectName:"+projectName);
    			}
		 		String className = Parser.getClassName(response.getCode(), Parser.TYPE_CONTENT);
		 		if(className != null) {
			 		createJavaClass(projectName, packageName, Parser.getClassName(response.getCode(), Parser.TYPE_CONTENT), code);
		 		}else {
		 			addMessage(chatView.BOT, "Error processing the request", null, chatView.NOT_INDEX);
		 		}
		    }else {
		    	addMessage(chatView.BOT, "Error processing the request", null, chatView.NOT_INDEX);
		    }
	    }else {
	    	addMessage(chatView.BOT, "Error processing the query", null, chatView.NOT_INDEX);
	    }
	}
	
	
	public void createCUWithContentDefault(Task task) {
		String cuName = null;
		String content = null;
		if(task.getParameterByType(JavaConcept.CLASS.name()) != null) {
			cuName = task.getParameterByType(JavaConcept.CLASS.name()).getValue();
			if(cuName != null) {
				content = "public class "+cuName+" {\n\n}";
			}
		}
		if(task.getParameterByType(JavaConcept.INTERFACE.name()) != null) {
			cuName = task.getParameterByType(JavaConcept.INTERFACE.name()).getValue();
			if(cuName != null) {
				content = "public interface "+cuName+" {\n\n}";
			}
		}
		if(cuName != null) {
			if(getCurrentPackage()!=null) {
				content = finalizeJavaClass(content, getCurrentPackage().getElementName());
			}else {
				content = finalizeJavaClass(content, JavaProject.PACKAGE_DEFAULT);
			}
			createJavaClass(getCurrentProject(), getCurrentPackage(), cuName, content);
		}else {
	    	chatView.addMessage(chatView.BOT, "The file couldn't be created. Missing type name.", null, chatView.NOT_INDEX);
		}
		
	}
	
	private void createJavaClass(IProject project, IPackageFragment pack, String className, String content) {
		String projectName;
		if(project != null) {
			projectName = project.getName();
		}else {
			projectName = null;
		}
		String packageName;
		if(pack != null) {
			packageName = pack.getElementName();
		}else {
			packageName = javaProject.PACKAGE_DEFAULT;
		}
		createJavaClass(projectName, packageName, className, content);
	}

	private void createJavaClass(String projectName, String packageName, String className, String content) {
		if(projectName == null) {
			createJavaProject("NewProject");
			JavaProject javaProj = new JavaProject();
    		javaProj.setProject("NewProject");
    		javaProj.createClass(className, content, JavaProject.PACKAGE_DEFAULT);
    		Parameter parameter = getCurrentTask().getParameterByValue(className);
    		if(parameter !=null) {
    			addMessage(chatView.BOT, "The "+parameter.getParameterType().getName().toLowerCase()+" "+className+" has been created", null, chatView.NOT_INDEX);
    		}else {
    			addMessage(chatView.BOT, className+" has been created", null, chatView.NOT_INDEX);
    		}
	    }else {
			JavaProject javaProj = new JavaProject();
    		javaProj.setProject(projectName);
    		String classFilePath = javaProj.createClass(className, content, packageName);
			System.out.println("#5.3 classfile ="+classFilePath);
	 		openClassInEditor(projectName, classFilePath);
    		Parameter parameter = getCurrentTask().getParameterByValue(className);
    		if(parameter !=null) {
    			addMessage(chatView.BOT, "The "+parameter.getParameterType().getName().toLowerCase()+" "+className+" has been created", null, chatView.NOT_INDEX);
    		}else {
    			addMessage(chatView.BOT, className+" has been created", null, chatView.NOT_INDEX);
    		}
    	}
	}
	
	public String finalizeJavaClass(String content, String packageName) {
		ASTParser parser = ASTParser.newParser(AST.JLS16); // Use the appropriate Java version
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(content.toCharArray());
        parser.setResolveBindings(false);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        AST ast = cu.getAST();
        cu.recordModifications();

        System.out.println("##packageName: " + packageName);
        if (packageName == null || packageName.isEmpty()) {
        	System.out.println("##packageName1: " + packageName);
        	cu.setPackage(null);
        }else{
        	System.out.println("##packageName2: " + packageName);
        	PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
            packageDeclaration.setName(ast.newName(packageName.split("\\.")));
            cu.setPackage(packageDeclaration);
        }

        addImport(ast, cu, FULL_QUALIFIED_NAME_GENERATED);

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                // Add Javadoc
                Javadoc javadoc = ast.newJavadoc();
                TagElement tagElement = ast.newTagElement();
                tagElement.setTagName(null); // Main Javadoc body

                TextElement textElement = ast.newTextElement();
                textElement.setText(JAVADOC_LINE_CARET);
                tagElement.fragments().add(textElement);

                javadoc.tags().add(tagElement);
                node.setJavadoc(javadoc);

                List<IExtendedModifier> modifiers = node.modifiers();

                for (Iterator<IExtendedModifier> iterator = modifiers.iterator(); iterator.hasNext(); ) {
                    IExtendedModifier modifier = iterator.next();
                    if (modifier.isAnnotation()) {
                        Annotation annotation = (Annotation) modifier;
                        if (annotation.getTypeName().getFullyQualifiedName().equals("Generated")) {
                            iterator.remove(); 
                        }
                    }
                }
                
                NormalAnnotation annotation = ast.newNormalAnnotation();
                annotation.setTypeName(ast.newSimpleName("Generated"));

                try {
                	long timestamp = new Date().getTime();
                	Date date = new Date();
                	date.setTime(timestamp);
					addAnnotationMember(ast, annotation, "agent", currentAgent.getTechnology());
	                addAnnotationMember(ast, annotation, "task", currentTask.getCode());
	                addAnnotationMember(ast, annotation, "id", ""+timestamp+"");
	                addAnnotationMember(ast, annotation, "timestamp", Util.getDateFormat("yyyy-MM-dd HH:mm:ss", date));
				} catch (Exception e) {
					Log.e(e.getMessage());
				}

                node.modifiers().add(0, annotation); 

                if (node.getSuperclassType() != null) {
                    String superclassName = node.getSuperclassType().toString();
                    String superClassPackage = javaProject.getClassPackage(getCurrentProject(), superclassName);
                    if( superClassPackage != null) {
                    	if(!superClassPackage.equals(packageName) && superClassPackage != JavaProject.PACKAGE_DEFAULT) {
                    		String fullQualifiedName = superClassPackage+"."+superclassName;
                    		addImport(ast, cu, fullQualifiedName);
                    	}else {
                    		Log.d("superClassPackage = packagename: " +  superClassPackage+" - "+packageName);
                    	}
                    }else {
                    	Log.d("#superClassPackage: " + superClassPackage);
                    }
                }

                List<Type> interfaces = node.superInterfaceTypes();
                if (!interfaces.isEmpty()) {
                    for (Type iface : interfaces) {
                        String interfaceName = iface.toString();
                        String interfacePackage = javaProject.getClassPackage(getCurrentProject(), interfaceName);
                        if( interfacePackage != null) {
                        	if(!interfacePackage.equals(packageName) && interfacePackage != JavaProject.PACKAGE_DEFAULT) {
                        		String fullQualifiedName = interfacePackage+"."+ interfaceName;
                        		addImport(ast, cu, fullQualifiedName);
                        	}else {
                        		Log.d("superClassPackage = packagename: " +  interfacePackage+" - "+packageName);
                        	}
                        }else {
                        	Log.d("interfacePackage: " + interfacePackage);
                        }
                        Log.d("The class extends: " + interfaceName);
                    }
                }
                
                return false; 
            }
        });

        Document document = new Document(cu.toString());
        
        CodeFormatter cf = ToolFactory.createCodeFormatter(null);
		TextEdit te = cf.format(CodeFormatter.K_COMPILATION_UNIT, document.get(), 0, document.get().length(), 0, null);
		try {
			te.apply(document);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return document.get();
    }
	
	public boolean addImport(AST ast, CompilationUnit cu, String fullQualifiedName) {
        boolean hasImport = cu.imports().stream()
                .anyMatch(importDecl -> ((ImportDeclaration) importDecl).getName().getFullyQualifiedName().equals(fullQualifiedName));
        if (!hasImport) {
            ImportDeclaration importDeclaration = ast.newImportDeclaration();
            importDeclaration.setName(ast.newName(fullQualifiedName.split("\\.")));
            cu.imports().add(importDeclaration);
        }
		return false;
	}
	
	private static void addAnnotationMember(AST ast, NormalAnnotation annotation, String name, String value) {
        MemberValuePair pair = ast.newMemberValuePair();
        pair.setName(ast.newSimpleName(name));
        
        StringLiteral stringLiteral = ast.newStringLiteral();
        stringLiteral.setLiteralValue(value);

        pair.setValue(stringLiteral);
        annotation.values().add(pair);
    }

	
	public IPackageFragment getCurrentPackage() {
		String packageName = null;
		if(getCurrentTask() != null) {
			packageName = getCurrentTask().getParameterByType(JavaConcept.PACKAGE.name()).getValue();
		}
		if(packageName == null) {
			if(lastPackageName != null) {
				packageName = lastPackageName;
			}else {
				packageName = JavaProject.PACKAGE_DEFAULT;
			}
		}
		if(getCurrentProject() != null) {
			IPackageFragment pack = JavaProject.findPackage(getCurrentProject().getName(), packageName);
			lastPackageName = packageName;
			return pack;
		}else {
			return null;
		}
		
	}
	
	public void createProject(Task task) {
		Log.d("Create project:"+task.getCode());
		if(task.getParameter("projectName").getValue() != null) {
			String projectName = task.getParameter("projectName").getValue();
			if(JavaProject.getProject(projectName) != null){
				addMessage(BOT, "The project "+projectName+" already exists", null, NOT_INDEX);
			} else {
				if(createJavaProject(projectName)) {
					lastProjectName = projectName;
					addMessage(chatView.BOT, "The project "+projectName+" has been created", null, chatView.NOT_INDEX);
				}else {
					addMessage(BOT, "The project "+projectName+" couldn't be created", null, NOT_INDEX);
				}
			}
		}else {
			InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Create a new project", "Project name:", "", null);
    		int result = dlg.open();
            if (result == dlg.OK && dlg.getValue().length()>1) {
                String projectName = Util.toCapitalize(dlg.getValue().strip());
                if(projectName!="") {
                	if(JavaProject.getProject(projectName) != null){
        				addMessage(BOT, "The project \""+projectName+"\" already exists", null, NOT_INDEX);
        			} else {
        				if(createJavaProject(projectName)) {
        					lastProjectName = projectName;
        					addMessage(chatView.BOT, "The project "+projectName+" has been created", null, chatView.NOT_INDEX);
        				}else {
        					addMessage(BOT, "The project \""+projectName+"\" couldn't be created", null, NOT_INDEX);
        				}
        			}
                }else {
					addMessage(BOT, "The project "+projectName+" couldn't be created", null, NOT_INDEX);
                }
            }
		}
		
	}
	
	public void createPackage(Task task) {
		if(task.hasAllParameterValue(true)) {
			String packageName = null;
			if(task.getParameterByType(JavaConcept.PACKAGE.name())!= null) {
				packageName = task.getParameter("packageName").getValue();
				String projectName = null;
                if(task.getParameter("projectName").getValue() != null) {
                	projectName = task.getParameter("projectName").getValue();
                }else {
                	projectName = getCurrentProject().getName();
                }
                lastProjectName = projectName;
                if(JavaProject.findPackage(projectName, packageName) == null ) {
                	IPackageFragment newPackage = JavaProject.createPackage(projectName, packageName);
                	if(newPackage != null) {
                		addMessage(chatView.BOT, "The package "+packageName+" has been created", null, chatView.NOT_INDEX);
                		lastPackageName = packageName;
                	}else {
                		addMessage(chatView.BOT, "The package "+packageName+" couldn't be created", null, chatView.NOT_INDEX);
                	}
                }else {
                	addMessage(chatView.BOT, "The package "+packageName+" already exists", null, chatView.NOT_INDEX);
                }
			}else {
				addMessage(chatView.BOT, "The package "+packageName+" couldn't be created", null, chatView.NOT_INDEX);
			}
			
		}else {
			InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "Create a new package", "Package name:", "", null);
    		int result = dlg.open();
            if (result == dlg.OK && dlg.getValue().length()>1) {
                String packageName = Util.toCapitalize(dlg.getValue().strip());
                createJavaProject(packageName);
                String projectName = null;
                if(task.getParameter("projectName").getValue() != null) {
                	projectName = task.getParameter("projectName").getValue();
                }else {
                	projectName = getCurrentProject().getName();
                }
                lastProjectName = projectName;
                if(JavaProject.findPackage(projectName, packageName) == null ) {
                	IPackageFragment newPackage = JavaProject.createPackage(projectName, packageName);
                	if(newPackage != null) {
                		addMessage(chatView.BOT, "The package "+packageName+" has been created", null, chatView.NOT_INDEX);
                		lastPackageName = packageName;
                	}else {
                		addMessage(chatView.BOT, "The package "+packageName+" couldn't be created", null, chatView.NOT_INDEX);
                	}
                }else {
                	addMessage(chatView.BOT, "The package "+packageName+" already exists", null, chatView.NOT_INDEX);
                }
            }else {
            	if(dlg.getValue().length()>1) {
            		addMessage(chatView.BOT, "The package name is not correct", null, chatView.NOT_INDEX);
            	}
			}
		}
	}

	public boolean createJavaProject(String projectName) {
		javaProject = new JavaProject(ResourcesPlugin.getWorkspace());
		if(!javaProject.existsProject(projectName)) {
			javaProject.setProjectName(projectName);
		    if(javaProject.createProject()) {
		    	return true;
		    }else {
		    	return false;
		    }
		}else {
			return false;
		}
	}
	
	public void clearChatSession() {
		for(Control control : compositeChat.getChildren()) {
			  control.dispose();
		}
		chatData = new ChatData();
    }

	public void changeAgent(String agentId) {
		store.setValue(PreferenceConstants.P_AGENT, agentId);
		clearChatSession();
		addMessage(SYSTEM, getLineNewSession(agentId), null, NOT_INDEX);
	}
	
	public void createClass(Task task, IResource resource, String newClassName) {
		Log.d("Create class: "+resource.getProject().getName()+" - "+Util.getClassName(resource.getName()));
    	javaProject = new JavaProject(ResourcesPlugin.getWorkspace());
	    String text = javaProject.getClassSource(resource.getProject(), Util.getClassName(resource.getName()));
	    messageInput = "Create the java class "+newClassName+" that implements the following interface: "+Util.codeToLine(text, false);
	    Response response = processMessage(true, messageInput, TEMPERATURE_HIGH);
	    Interaction interaction = new Interaction();
		interaction.setRole(ChatView.BOT);
		interaction.setTaskCode(task.getCode());
		interaction.setTaskName(task.getName());
		interaction.setContext(new Context(resource, null));
		interaction.getContext().getResource().update(Parser.getClassName(response.getCode(), Parser.TYPE_CONTENT)+".java");
		interaction.setResult(new Result(new Agent(currentAgent.getName(), currentAgent.getTechnology(), currentAgent.isLLM()),false, false));
		if(response.getCode() != null) {
			String commentsAnnotation = getCommentsAnnotation(currentAgent.getTechnology(), task.getCode(), interaction.getTimestamp(), false);
	 		String code = commentsAnnotation+"\n"+response.getCode();
	    	createJavaClass(resource.getProject().getName(), getCurrentPackage().getElementName(), Parser.getClassName(response.getCode(), Parser.TYPE_CONTENT), code);
			interaction.setText(response.getText());
			interaction.setCode(response.getCode());
	    }else {
	    	interaction.setText("Error processing the query");
			interaction.setCode(null);
	    }
	    addInteraction(interaction);
    }
	
	public void createJavaTestClass(Task task, String text) {
		Log.d("Create test class: "+task.getCode());
		IResource resource = Resource.getSelectedResource();
		if(resource != null) {
			addInteraction(new Interaction(ChatView.USER, text, null, new Context(resource, null), task.getCode()));
	    	Display.getDefault().asyncExec(new Runnable() {
		    	    public void run() {
		    	    	createTestClass(task, resource);
		    	    }
		    	});
		}else {
			addInteraction(new Interaction(ChatView.USER, "The class could not be created", null, new Context(resource, null), task.getCode()));
		}
		
	}

    public void createTestClass(Task task, IResource resource) {
    	javaProject = new JavaProject(ResourcesPlugin.getWorkspace());
	    String source = javaProject.getClassSource(resource.getProject(), Util.getClassName(resource.getName()));
	    messageInput = "create a junit test class for the following class: "+Util.codeToLine(source, false);
	    Response response = processMessage(true, messageInput, TEMPERATURE_HIGH);
	    Interaction interaction = new Interaction();
		interaction.setRole(ChatView.BOT);
		interaction.setTaskCode(task.getCode());
		interaction.setTaskName(task.getName());
		interaction.setContext(new Context(resource, null));
		interaction.getContext().getResource().update(Parser.getClassName(response.getCode(), Parser.TYPE_CONTENT)+".java");
		interaction.setResult(new Result(new Agent(currentAgent.getName(), currentAgent.getTechnology(), currentAgent.isLLM()),false, false));
		if(response != null) {
			if(response.getCode() != null) {
				String commentsAnnotation = getCommentsAnnotation(currentAgent.getTechnology(), task.getCode(), interaction.getTimestamp(), true);
		 		String code = commentsAnnotation+"\n"+response.getCode();
		    	createJavaClass(resource.getProject().getName(), getCurrentPackage().getElementName(), Parser.getClassName(code, Parser.TYPE_CONTENT), code);
				interaction.setText(response.getText());
				interaction.setCode(response.getCode());
			}else {
				interaction.setText("Error processing the query");
				interaction.setCode(null);
			}
	    }else {
	    	interaction.setText("Error processing the query");
			interaction.setCode(null);
	    }
	    addInteraction(interaction);
    }
    
    public void createMethod(Task task, int eventType, boolean getResponseCode) { // from runTask (AgentInterface)
		setCurrentTask(task);
		Log.d("Create method:"+task.getCode()+":"+task.getParameterByType(JavaConcept.METHOD.name()).getValue());
    	ITextSelection iTextSelection = null;
    	String methodName = null;
    	if(eventType == EVENT_TYPE_MESSAGE) {
    		if(task.getParameterByType(JavaConcept.METHOD.name()) != null) {
    			if(task.getParameterByType(JavaConcept.METHOD.name()).getValue() != null) {
    				methodName = task.getParameterByType(JavaConcept.METHOD.name()).getValue();
    				contextConversation.setMethodName(methodName);
    			}else {
    				if(contextConversation.getMethodName() != null) {
    					methodName = contextConversation.getMethodName();
    				}else {
    					InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), task.getDescription(), "Method name:", "", null);
                		int result = dlg.open();
                        if (result == dlg.OK && dlg.getValue().length()>1) {
                            methodName = dlg.getValue().strip();
                        }
    				}
    				
    			}
    		}else {
    			InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), task.getDescription(), "Method name:", "", null);
        		int result = dlg.open();
                if (result == dlg.OK && dlg.getValue().length()>1) {
                    methodName = dlg.getValue().strip();
                }
    		}
    		
    	}else {
			InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), task.getDescription(), "Method name:", "", null);
    		int result = dlg.open();
            if (result == dlg.OK && dlg.getValue().length()>1) {
                methodName = dlg.getValue().strip();
            }
		}
    	if(methodName != null) {
    		String newMethod = methodName;
			Display.getDefault().asyncExec(new Runnable() {
  	    	    public void run() {
  	    	    	createJavaMethod(newMethod);
  	    	    }
  	    	});
    	}else{
			addMessage(BOT, "The method couldn't be created", null, NOT_INDEX);
    	}
	}
    
    public void createJavaMethod(String methodName) {
    	IResource resource = Resource.getSelectedResource();
    	if(resource == null) {
    		resource = getCurrentResource();
    	}
    	if(resource != null) {
    		String projectName = resource.getProject().getName();
    		String packageName = getCurrentPackage().getElementName();
    		String className = Util.getClassName(resource.getName());
    		String srcClass = null;
			String srcSuperClass = null;
			String srcInterface = null; 
    		try {
    			srcClass = JavaProject.getClass(resource.getProject(), Util.getClassName(resource.getName())).getSource();
    		} catch (JavaModelException e) {
    			Log.e(e.getMessage());
    		}
    		if(srcClass != null) {
    			ICompilationUnit cu = JavaProject.getClass(resource);
    			String superClassName = ClassInfoExtractor.getSuperclassName(cu);
    			if(superClassName != null) {
    				try {
						srcSuperClass = JavaProject.getClass(resource.getProject(), superClassName).getSource();
    				} catch (JavaModelException e) {
    	    			Log.e(e.getMessage());
					}
    			}else {
    				Log.d("No superclass");
    			}
    			String [] interfaceNames = ClassInfoExtractor.getImplementedInterfaces(cu);
    			
    			if(interfaceNames != null) {
    				try {
						srcInterface = JavaProject.getClass(resource.getProject(), interfaceNames[0]).getSource();
						//System.out.println("\n-----"+srcInterface+"\n-----interface");
    				} catch (JavaModelException e) {
    					//e.printStackTrace();
    	    			Log.e(e.getMessage());
					}
    			}else {
    				Log.d("No interfaces");
    			}
    		}
    		String messageInput =Util.codeToLine("Create the java method "+methodName+" for following code: "+srcClass, true);
    		if(srcSuperClass != null ) {
    			messageInput += "--- Extended Class: "+Util.codeToLine(srcSuperClass, true);
    		}
    		if(srcInterface != null ) {
    			messageInput += "--- Implemented Interface: "+Util.codeToLine(srcInterface, true);;
    		}
    	    Response response = processMessage(true, messageInput, TEMPERATURE_HIGH);
    	    if(response != null) {
    	    	if(!response.isError()) {
	    	    	String methodSource = ASTMethodExtractor.extractMethod(response.getCode(), methodName);
	    			String commentsAnnotation = getCommentsAnnotation(currentAgent.getTechnology(), getCurrentTask().getCode(), new Date().getTime(), false);
	    	    	String methodContent = commentsAnnotation+"\n"+methodSource;
	    			if(JavaProject.getMethod(getCurrentProject(), className, methodName) == null) {
	    	    		if(JavaProject.createMethod(resource.getProject(), className, methodContent)) {
	    	    			addMessage(BOT, "The method "+methodName+" has been created", null, NOT_INDEX);
	    	    		}else{
	    	    			addMessage(BOT, "The method "+methodName+" couldn't be created", null, NOT_INDEX);
	
	    	    		}
	    	    	}else {
	    	    		addMessage(BOT, "The method "+methodName+" exists", null, NOT_INDEX);
	    	    	}
    	    	}else {
    	    		addMessage(BOT, "Error: "+response.getErrorMessage(), null, NOT_INDEX);
    	    	}
    	    }
    	}else {
    		addMessage(BOT, "The method couldn't be created", null, NOT_INDEX);
    	}
    }
    
	public void processCode(Task task, int eventType, boolean getResponseCode) { // from runTask (AgentInterface)
		setCurrentTask(task);
		System.out.println("#runTask:"+task.getCode()+":"+task.getParameterByType(JavaConcept.METHOD.name()).getValue());
		Log.d("#descriptionTask: "+task.getDescription());
		Log.d("#instructionsTask: "+task.getInstructions());
    	ITextSelection iTextSelection = null;
    	if(eventType == EVENT_TYPE_MESSAGE) {
    		if(task.getParameterByType(JavaConcept.METHOD.name()) != null) {
    			if(task.getParameterByType(JavaConcept.METHOD.name()).getValue() != null) {
    				iTextSelection = getITextSelection(task.getParameterByType(JavaConcept.METHOD.name()).getValue());
    				contextConversation.setMethodName(task.getParameterByType(JavaConcept.METHOD.name()).getValue());
    			}else {
    				if(contextConversation.getMethodName() != null) {
    					iTextSelection = getITextSelection(contextConversation.getMethodName());
    				}else {
    					InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), task.getDescription(), "Method name:", "", null);
                		int result = dlg.open();
                        if (result == dlg.OK && dlg.getValue().length()>1) {
                            String methodName = dlg.getValue().strip();
                            iTextSelection = getITextSelection(methodName);
                        }
    				}
    				
    			}
    		}else {
    			InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), task.getDescription(), "Method name:", "", null);
        		int result = dlg.open();
                if (result == dlg.OK && dlg.getValue().length()>1) {
                    String methodName = dlg.getValue().strip();
                    iTextSelection = getITextSelection(task.getParameter(methodName).getValue());
                }
    		}
    		
    	}else {
    		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        	ISelection selection = (ISelection)page.getSelection();
    		if (selection instanceof ITextSelection) {
    			iTextSelection = (ITextSelection) selection;
            }
    	}
    	if(iTextSelection != null) {
    		processSelectedCode(task, iTextSelection, getResponseCode);
    	}else{
			addMessage(BOT, "Selected code not detected", null, NOT_INDEX);
    		Log.e("selected code not detected");
    	}
	}
	
	public void processSelectedCode(Task task, ITextSelection iTextSelection, boolean getResponseCode) {
		System.out.println("#runTask:"+task.getCode());
    	IResource resource = null;
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
    	ISelection selection = (ISelection)page.getSelection();
    	IEditorPart editorPart = page.getActiveEditor();
    	ITextSelection iTextSelectionTemp = iTextSelection;
    	if(iTextSelectionTemp == null) {
    		if (selection instanceof ITextSelection) {
    			iTextSelectionTemp = (ITextSelection) selection;
            }
    	}
    	ITextSelection textSelection = iTextSelectionTemp;
    	if(editorPart  != null && textSelection != null){
		    resource = (IResource)editorPart.getEditorInput().getAdapter(IResource.class);
        	if(resource.getProject() != null) {
        		this.resource = resource;
        		System.out.println("#classname:"+Util.getClassName(resource.getName()));
        		lastProjectName = resource.getProject().getName();
        		Display.getDefault().asyncExec(new Runnable() {
      	    	    public void run() {
      	    	    	if(getResponseCode) {
      	    	    		replaceJavaCodeAST(task, editorPart, textSelection, true);
      	    	    	}else {
      	    	    		examineJavaCode(task, editorPart, textSelection);
      	    	    	}
      	    	    	
      	    	    }
      	    	});
        	}   
		}
	}
	
	public void replaceJavaCodeAST(Task task, IEditorPart editorPart, ITextSelection textSelection, boolean firstAttempt) {
		MethodDeclaration methodDeclaration = JavaProject.findSelectedMethod(editorPart, textSelection);
		final String methodSource;
		List<Tuple<String, String>> listContextTypes = new ArrayList<>();
		String methodName;
		if(methodDeclaration!=null) {
			methodName = methodDeclaration.getName().getIdentifier();
			Log.d("Selected method: "+methodName);
			currentTask.getParameter("methodName").setValue(methodName);
			IResource resource = Resource.getSelectedResource();
			String srcClass = JavaProject.getClassSource(resource);
			if(resource != null) {
				methodSource = getMethodSource(srcClass, methodName);
				if(methodSource == null) {
					Log.e("methodSource: NULL");
					return;
				}else {
					listContextTypes.add(new Tuple<>(Util.getClassName(resource.getName()), srcClass));
					List<String> listTypeName = getContextTypes(JavaProject.getClassSource(resource), methodName);
					String source = null;
					for (String typeName : listTypeName) {
						source = JavaProject.getClassSource(resource.getProject(), typeName);
						if(source != null) {
							listContextTypes.add(new Tuple<>(typeName, source));
							
						}else {
							Log.e("ContextTypes NULL:"+typeName);
						}
					}
				}
			}else {
				Log.e("Selected Resource (NULL)");
				return;
			}
		}else {
			Log.e("Selected Method (NULL)");
			return;
		}
		String interactionMessage = "";
		String chatMessage = "";
		String interactionCode = null;
		if(firstAttempt) {
			messageInput = "You are a code Assistant that helps a software developer (User) in programming tasks. "
					+ "If the task asks to generate code, only provide a brief explanation at the beginning and a "
					+ "single code solution (no additional alternatives). Send a single method in the response (DO NOT DIVIDE INTO MULTIPLE METHODS) if the task "
					+ "applies only to the method; do not send an entire class, unless the task specifically requests "
					+ "to modify or create a whole class. Perform the next task:\n"
						+getTaskDescriptionInstructions(task)+" for following code: \n```"+methodSource.strip()+"```.";
			if(listContextTypes.size()>0) {
				messageInput += "\nConsider the following class/interface used: ";
				for (Tuple<String, String> entry : listContextTypes) {
		            messageInput += "\n```" + entry.getValue()+"```";
		        }
			}
			Log.d("Message input: \n"+messageInput);
			chatMessage = task.getName()+": method "+methodName;
		}else {
			messageInput = "You are a code Assistant that helps a software developer (User) in programming tasks. Our previous conversation is the following:\n"
					+getInteractions(task.getCode())+".\n"
					+"Now, try again another response. "+task.getDescription()+" (You answer only java code)";
			chatMessage = "Try again. Give a different answer";
			
		}
		interactionMessage = messageInput;
		messageInput = Util.codeToLine(messageInput,true);
		Interaction interactionU = new Interaction();
		
		interactionU.setRole(ChatView.USER);
		//interactionU.setGitUser(gitUser.getUser());
	    //interactionU.setGitEmail(gitUser.getMail());
		interactionU.setText(interactionMessage);
		interactionU.setChatMessage(chatMessage);
		interactionU.setCode(interactionCode);
		interactionU.setTaskCode(task.getCode());
		interactionU.setTaskName(task.getName());
		interactionU.setContext(new Context((IResource)editorPart.getEditorInput().getAdapter(IResource.class), textSelection));
		chatView.addInteraction(interactionU);
		
		Display.getDefault().asyncExec(new Runnable() {
	    	    public void run() {
	    	    	Response response = processMessage(true, messageInput, TEMPERATURE_HIGH);
	    	    	currentResponse = response;
	    		    Interaction interaction = new Interaction();
	    		    currentInteraction = interaction;
	    		    
	    			interaction.setRole(ChatView.BOT);
	    			interaction.setTaskCode(task.getCode());
	    			interaction.setTaskName(task.getName());
	    			Context context = new Context((IResource)editorPart.getEditorInput().getAdapter(IResource.class), textSelection);
	    			if(context.getResource() != null && context.getResource().getCodeFragment() != null) {
	    				context.getResource().getCodeFragment().setMethodName(methodDeclaration.getName().getIdentifier());
	    			}
	    			interaction.setContext(context);
	    			interaction.setResult(new Result(new Agent(currentAgent.getName(), currentAgent.getTechnology(), currentAgent.isLLM()),false, false));
	    			if(response != null) {
	    		    	interaction.setText(response.getText());
	    				interaction.setCode(response.getCode());
	    				interaction.setTargetParameterType(JavaConcept.METHOD.name());
	    				interaction.setTargetParameterName(methodDeclaration.getName().getIdentifier());
	    				if(response.getCode() != null) {
	    	    	    	//---- added comparator
	    	    	    	/*if(Util.similarCode(methodSource, response.getCode())) {
	    	    	    		return;
	    	    	    	}*/
	    	    	    	//----- end comparator
	    					interaction.getContext().getResource().getCodeFragment().setLength(response.getCode().length());
	    					ITextEditor editor = (ITextEditor)editorPart;
	    				    IDocumentProvider dp = editor.getDocumentProvider();
	    				    IDocument doc = dp.getDocument(editor.getEditorInput());
	    				    String source = doc.get();
	    				    String methodSrc = ASTMethodExtractor.extractMethod(source, methodDeclaration.getName().getIdentifier());
	    				    String injectedSource = MethodReplacer.modifyMethod(source, methodDeclaration.getName().getIdentifier(), response.getCode());
	    				    MethodDeclaration currentMethod = MethodReplacer.getCurrentMethod();
	    				    String body = currentMethod.getBody().toString();
	    				    String codeHash = Hash.md5(body);
	    				    System.out.println("body init:"+body);
	    				    interaction.setHash(codeHash);
	    				    //interactionU.setGitUser(gitUser.getUser());
	    				    //interactionU.setGitEmail(gitUser.getMail());
	    				    if(injectedSource!=null) {
	    				    	Log.d("Injected source:\n"+injectedSource+"\n--");
	    				    }else {
	    				    	Log.e("Injected source: NULL\n");
	    				    }
	    				    
	    				    if(task.hasPreviousValidation()) {
	    						if(!checkPreviousValidations(Resource.getSelectedResource() , injectedSource)) {
	    							interaction.setPassedPreValidations(false);
	    							interaction.getResult().setUsed(false);
	    							chatView.addInteraction(interaction);
	    							return;
	    						}else {
	    							interaction.setPassedPreValidations(true);
	    						}
	    					}
	    			    	
	    				    String message = Util.codeToDialog(response.getCode());
	    				    response.setAgentId(getCurrentAgent().getId());
	    			    	ResponseDialog responseDialog = new ResponseDialog(Display.getCurrent().getActiveShell(), task.getName(), methodSource, response, task, context);
	    			    	int option = responseDialog.open();
	    					switch (option) {
	    						case Dialog.OK:
	    							Response finalResponse = responseDialog.getResponse();
	    							if( finalResponse != null) {
	    								interaction.getResult().setUsed(true);
	    							}else {
	    								finalResponse = response;
	    								interaction.getResult().setUsed(false);
	    							}
	    							if(task.getCode().equals("GENERATE_JAVADOC_METHOD")) {
	    								MethodReplacer.replaceMethodJavadoc(source, methodDeclaration.getName().getIdentifier(), finalResponse.getCode(), editorPart);
	    							}else {
	    								MethodReplacer.replaceMethodBody(source, methodDeclaration.getName().getIdentifier(), finalResponse.getCode(), editorPart);
	    								MethodReplacer.replaceMethod(doc.get(), methodDeclaration.getName().getIdentifier(), finalResponse.getCode(), editorPart);
	    								
	    							}
	    							if(task.hasPostValidation()) {
	    				    			saveActiveEditor();
	    					    		boolean valid = checkPostValidations(editorPart, finalResponse);
	    								if(valid) {
	    									Log.d("VALIDATION: Ok");
	    								}else {
	    									Log.d("VALIDATION: ERROR");
	    								}
	    				    		}
	    							break;
	    						case Dialog.CANCEL:
	    							break;
	    						default:
	    							break;
	    					}
	    			    }
	    		    }else {
	    		    	interaction.setText("Error processing the query");
	    				interaction.setCode(null);
	    		    }   
	    			chatView.addInteraction(interaction);
	    	    }
	    	});
	    
	}
	
	
	public void replaceJavaCode(Task task, IEditorPart editorPart, ITextSelection textSelection, boolean firstAttempt) {
		MethodDeclaration methodDeclaration = JavaProject.findSelectedMethod(editorPart, textSelection);
		if(methodDeclaration!=null) {
			Log.d("Selected method: "+methodDeclaration.getName().getIdentifier());
			String srcMethod = null;
			if(Resource.getSelectedResource() != null) {
				String source = JavaProject.getClassSource(Resource.getSelectedResource());
				if(source != null) {
					srcMethod = ASTMethodExtractor.extractMethod(source, methodDeclaration.getName().getIdentifier());
				}else {
					Log.d("SOURCECLASS NULL");
				}
			}else {
				Log.d("PROJECT NULL");
			}
		}else {
			Log.d("Method (NULL)");
		}
		if(firstAttempt) {
			messageInput = getTaskDescriptionInstructions(task)+" for following code: "+Util.codeToLine(textSelection.getText().strip(), true);
		}else {
			messageInput = "You are a code Assistant that helps a software developer (User) in programming tasks. Our previous conversation is the following:\n"
					+getInteractions(task.getCode())+".\n"
					+"Now, try again another response. "+task.getDescription()+" (You answer only java code)";
			messageInput = Util.codeToLine(messageInput, true);
			Interaction interaction = new Interaction();
    		interaction.setRole(ChatView.USER);
    		interaction.setText("Try again. Give a different answer");
    		interaction.setCode(null);
    		interaction.setTaskCode(task.getCode());
    		interaction.setTaskName(task.getName());
    		interaction.setContext(new Context((IResource)editorPart.getEditorInput().getAdapter(IResource.class), textSelection));
    		chatView.addInteraction(interaction);
		}
		
		try {
			String classSource = JavaProject.getClass(Resource.getSelectedResource().getProject(), Util.getClassName(Resource.getSelectedResource().getName())).getSource();
			String methodContent = getMethodSource(classSource, methodDeclaration.getName().getIdentifier());
		} catch (Exception e) {
			//e.printStackTrace();
			Log.e(e.getMessage());
		}
		
	    Response response = processMessage(true, messageInput, TEMPERATURE_HIGH);
	    Interaction interaction = new Interaction();
		interaction.setRole(ChatView.BOT);
		interaction.setTaskCode(task.getCode());
		interaction.setTaskName(task.getName());
		Context context = new Context((IResource)editorPart.getEditorInput().getAdapter(IResource.class), textSelection);
		interaction.setContext(context);
		interaction.setResult(new Result(new Agent(currentAgent.getName(), currentAgent.getTechnology(), currentAgent.hasIntent()),false, false));
		if(response != null) {
	    	interaction.setText(response.getText());
			interaction.setCode(response.getCode());
			chatView.addInteraction(interaction);
			int lastIndex = chatData.getInteractions().size()-1;
			if(response.getCode() != null) {
				interaction.getContext().getResource().getCodeFragment().setLength(response.getCode().length());
				ITextEditor editor = (ITextEditor)editorPart;
			    IDocumentProvider dp = editor.getDocumentProvider();
			    IDocument doc = dp.getDocument(editor.getEditorInput());
			    String source = doc.get();
			    String injectedSource = replaceText(source, response.getCode(), textSelection.getOffset(), textSelection.getLength());
				if(task.hasPreviousValidation()) {
					if(!checkPreviousValidations(Resource.getSelectedResource().getProject() , injectedSource)) {
						return;
					}
				}
		    	String message = Util.codeToDialog(response.getCode());
		    	ResponseDialog responseDialog = new ResponseDialog(Display.getCurrent().getActiveShell(), task.getName(), textSelection.getText(), response, task, context);
		    	int option = responseDialog.open();
				switch (option) {
					case Dialog.OK:
						Response finalResponse = responseDialog.getResponse();
						Log.d("Validation: Ok");
						if( finalResponse != null) {
							interaction.getResult().setUsed(false);
						}else {
							finalResponse = response;
							interaction.getResult().setUsed(true);
						}
						replaceDocument(editorPart, textSelection.getOffset(), textSelection.getLength(), finalResponse.getCode(), task.getCode(), interaction.getTimestamp());
			    		if(task.hasPostValidation()) {
			    			saveActiveEditor();
				    		boolean valid = checkPostValidations(editorPart, finalResponse);
							if(valid) {
								Log.d("Validation: Ok");
							}else {
								Log.d("Validation: Error");
								return;
							}
			    		}
						break;
					case Dialog.CANCEL:
						break;
					default:
						break;
				}
		    }
	    }else {
	    	interaction.setText("Error processing the query");
			interaction.setCode(null);
			chatView.addInteraction(interaction);
	    }    
	}
	
	public void replaceJavaCode(String agentId, Task task, IEditorPart editorPart, ITextSelection textSelection, boolean firstAttempt) {
		if(firstAttempt) {
			messageInput = getTaskDescriptionInstructions(task)+", for following code: "+Util.codeToLine(textSelection.getText().strip(), true);
		}else {
			messageInput = "You are a code Assistant that helps a software developer (User) in programming tasks. Our previous conversation is the following:\n"
					+getInteractions(task.getCode())+".\n"
					+"Now, try again another response. "+getTaskDescriptionInstructions(task)+" (You answer only java code)";
			messageInput = Util.codeToLine(messageInput, true);
			Interaction interaction = new Interaction();
    		interaction.setRole(ChatView.USER);
    		interaction.setText("Try again. Give a different answer");
    		interaction.setCode(null);
    		interaction.setTaskCode(task.getCode());
    		interaction.setTaskName(task.getName());
    		interaction.setContext(new Context((IResource)editorPart.getEditorInput().getAdapter(IResource.class), textSelection));
    		chatView.addInteraction(interaction);
			
		}
	    Response response = processMessage(agentId, messageInput, TEMPERATURE_HIGH);
	    Interaction interaction = new Interaction();
		interaction.setRole(ChatView.BOT);
		interaction.setTaskCode(task.getCode());
		interaction.setTaskName(task.getName());
		interaction.setContext(new Context((IResource)editorPart.getEditorInput().getAdapter(IResource.class), textSelection));
		interaction.setResult(new Result(new Agent(currentAgent.getName(), currentAgent.getTechnology(), currentAgent.isLLM()),false, false));
		if(response != null) {
	    	interaction.setText(response.getText());
			interaction.setCode(response.getCode());
			chatView.addInteraction(interaction);
			int lastIndex = chatData.getInteractions().size()-1;
			if(response.getCode() != null) {
		    	String message = "Do you want to replace the code?:\n\n"+Util.codeToDialog(response.getCode());
				int option = MessageDialog.open(MessageDialog.CONFIRM, Display.getCurrent().getActiveShell(), task.getName()+" ("+agentId+")", message, SWT.NONE, new String[]{"OK", "Other>", "Cancel"});
		    	switch (option) {
					case 0:
						interaction.getContext().getResource().getCodeFragment().setLength(response.getCode().length());
			    		replaceDocument(editorPart, textSelection.getOffset(), textSelection.getLength(), response.getCode(), task.getCode(), interaction.getTimestamp());
			    		interaction.getResult().setUsed(true);
			    		buttonsGoTo.get(lastIndex).setVisible(true);
						break;
					case 1:
						String [] agents = getTaskProcessingAgents();
						List<String> items = Arrays.asList(agents);
				        PopupDialog dialog = new PopupDialog(Display.getCurrent().getActiveShell(), items);
				        if (dialog.open() == Dialog.OK ) {
				        	int selectedIndex = dialog.getSelectedIndex();
				        	if (selectedIndex != -1) {
					            String agent = items.get(selectedIndex);
					            Log.d("Selected item: " + selectedIndex+" : "+agent);
					            Display.getDefault().asyncExec(new Runnable() {
				      	    	    public void run() {
				      	    	    	replaceJavaCode(agent, task, editorPart, textSelection, false);
				      	    	    }
				      	    	});
					        } else {
					        	Log.d("Not selected");
					        }
				        }
						break;
					case 2:
						break;
					default:
						break;
				}
		    }
	    }else {
	    	interaction.setText("Error processing the query");
			interaction.setCode(null);
			chatView.addInteraction(interaction);
	    }    
	}
	
	
	public void examineJavaCode(Task task, IEditorPart editorPart, ITextSelection textSelection) {
		MethodDeclaration methodDeclaration = JavaProject.findSelectedMethod(editorPart, textSelection);
		String methodName;
		String methodSource = textSelection.getText().strip();
		if(methodDeclaration!=null) {
			methodName = methodDeclaration.getName().getIdentifier();
			Log.d("Selected method: "+methodName);
			currentTask.getParameter("methodName").setValue(methodName);
			IResource resource = Resource.getSelectedResource();
			String srcClass = JavaProject.getClassSource(resource);
			if(resource != null) {
				methodSource = getMethodSource(srcClass, methodName);
				if(methodSource == null) {
					Log.d("Method source: NULL");
				}
			}
		}
			
		Interaction interactionU = new Interaction();
		interactionU.setRole(ChatView.USER);
		interactionU.setText(task.getName()+": \n"+methodSource);
		interactionU.setCode(methodSource);
		interactionU.setTaskCode(task.getCode());
		interactionU.setTaskName(task.getName());
		interactionU.setContext(new Context(resource, textSelection));
		addInteraction(interactionU);
	    messageInput = getTaskDescriptionInstructions(task)+" for the following java code: "+Util.codeToLine(methodSource, true);
	    Response response = processMessage(true, messageInput, TEMPERATURE_HIGH);
	    Interaction interaction = new Interaction();
		interaction.setRole(ChatView.BOT);
		interaction.setTaskCode(task.getCode());
		interaction.setTaskName(task.getName());
		interaction.setContext(new Context((IResource)editorPart.getEditorInput().getAdapter(IResource.class), textSelection));
		interaction.setResult(new Result(new Agent(currentAgent.getName(), currentAgent.getTechnology(), currentAgent.isLLM()),false, false));
		if(response != null) {
	    	interaction.setText(response.getText());
			interaction.setCode(null);
	    }else {
	    	interaction.setText("Error processing the query");
			interaction.setCode(null);
	    }
	    chatView.addInteraction(interaction);    
	}
	
	public void replaceDocument(IEditorPart editorPart, int offset, int length, String text, String task, long timestamp) {
	   if (editorPart instanceof AbstractTextEditor) {
		   ITextEditor editor = (ITextEditor)editorPart;
		   IDocumentProvider dp = editor.getDocumentProvider();
		   IDocument doc = dp.getDocument(editor.getEditorInput());
		   String commentsAnnotation = getCommentsAnnotation(currentAgent.getTechnology(), task, timestamp, true);
		   String textTotal = commentsAnnotation+" "+text;
		   Log.d("###replaceDocument");
		   Log.d("---\n"+textTotal+"\n---");
		   try {
			   doc.replace(offset, length, textTotal);
			   CodeFormatter cf = ToolFactory.createCodeFormatter(null);
			   TextEdit te = cf.format(CodeFormatter.K_COMPILATION_UNIT, doc.get(), offset+(textTotal.length()-text.length()-1), text.length()+1, 0, null);
			   te.apply(doc);
			   addLibJar();
			   addImport(doc);
		   } catch (Exception e) {
			   //e.printStackTrace();
   			   Log.e(e.getMessage());
		   }
	   }
	}
	
	private void addLibJar() {
		ClasspathLoader.addRelativeJarToClasspath(getCurrentProject(), "lib"+File.separator+"caret.annotation.jar");
	}
	
	public void addImport(IDocument document) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(document.get().toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        boolean importExists = false;
        List<ImportDeclaration> imports = cu.imports();
        for (ImportDeclaration importDeclaration : imports) {
            if (importDeclaration.getName().getFullyQualifiedName().equals(FULL_QUALIFIED_NAME_GENERATED)) {
                importExists = true;
                break;
            }
        }
        if(!importExists) {
        	AST ast = cu.getAST();
            ImportDeclaration id = ast.newImportDeclaration();
            id.setName(ast.newName(FULL_QUALIFIED_NAME_GENERATED.split("\\.")));
            ASTRewrite rewriter = ASTRewrite.create(ast);
            ListRewrite lrw = rewriter.getListRewrite(cu, CompilationUnit.IMPORTS_PROPERTY);
            lrw.insertLast(id, null);
            TextEdit edits = rewriter.rewriteAST(document, null);
            try {
    			edits.apply(document);
    		} catch (Exception e) {
    			//e.printStackTrace();
    			Log.e(e.getMessage());
    		}
        }
        
	}
	
	public IProject getProject(String projectName) {
		IProject project = null;
		if(projectName != null) {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		}
		return project;
	}
	
	public IProject getCurrentProject() {
		IProject project = null;
		if(lastProjectName != null) {
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(lastProjectName);
		}
		if(project != null) {
			return project;
		}else {
			IResource  resource= Resource.getSelectedResource();
			if(resource != null){
			    project = resource.getProject();
			    if(project != null) {
			    	return project;
			    }
			}else {
		        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		        IProject lastProject = null;
		        long modificationStamp = 0;
		        for (IProject proj : projects) {
		        	if(lastProject == null) {
		        		lastProject = proj;
		        	}
		        	if(proj.getModificationStamp()> modificationStamp) {
		        		lastProject = proj;
		        		modificationStamp = proj.getModificationStamp();
		        	}
		        }
		        if(lastProject != null) {
		        	return lastProject;
		        }
			}
		}
		return project;
	}
	
	public void addInteractionToMongoDB(Interaction interaction) {
		
		if (mongoUser != null && !mongoUser.isEmpty() &&
			    mongoPassword != null && !mongoPassword.isEmpty() &&
			    mongoHost != null && !mongoHost.isEmpty() &&
			    mongoDatabase != null && !mongoDatabase.isEmpty() &&
			    mongoAppName != null && !mongoAppName.isEmpty()) {
			MongoDB mongoDB = new MongoDB();
	    	mongoDB.setUser(mongoUser);
	    	mongoDB.setPassword(mongoPassword);
	    	mongoDB.setHost(mongoHost);
	    	mongoDB.setDatabase(mongoDatabase);
	    	mongoDB.setAppName(mongoAppName);
	    	if(mongoDB.connect()) {
	    		mongoDB.setupCollection();
	 	        mongoDB.addDocument( interaction);
	 	        mongoDB.getDocument(interaction.getTimestamp());
	 	        mongoDB.close();
	    	}
	       
		}
		
	}

	public int addInteraction(Interaction interaction) {
		
		addInteractionToMongoDB(interaction);
		chatData.addInteraction(interaction);
		int index = chatData.getInteractions().size()-1;
		if(interaction.getChatMessage() != null) {
			addMessage(interaction.getRole(), interaction.getChatMessage(), interaction.getCode(), index);
		}else {
			if(interaction.getText() != null) {
				addMessage(interaction.getRole(), interaction.getText(), interaction.getCode(), index);
			}else {
				addMessage(SYSTEM, "Connection error", null, index);
			}
		}
		String projectName = interaction.getContext().getResource().getProjectName();
		List<Interaction> interactions = chatData.getInteractions();
		List<Interaction> currentProjectInteractions = new ArrayList<Interaction>();
		for (Interaction inter : interactions) {
		    if(inter.getContext().getResource().getProjectName().equals(projectName)) {
		    	 currentProjectInteractions.add(inter);
		    }
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String jsonInteraction = gson.toJson(currentProjectInteractions);
		Util.saveLog(pathWorkspace+"/"+projectName+"/", "log-"+timesession+".json", jsonInteraction, false);
		if(statisticsView != null) {
			try {
				statisticsView.updateStatistics();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		return (index);
	}
	
	public String getInteractions() {
		String interactionsString = "";
		ArrayList<Interaction> interactions = chatData.getInteractions();
		for (Interaction interaction : interactions) {
			interactionsString += "-"+interaction.getRole()+": "+interaction.getText()+"\n";
		}
		return interactionsString;
	}
	
	public String getInteractions(String task) {
		String interactionsString = "";
		ArrayList<Interaction> interactions = chatData.getInteractions();
		for (Interaction interaction : interactions) {
			if(task == interaction.getTaskCode()) {
				interactionsString += "-"+interaction.getRole()+": "+interaction.getText()+"\n";
			}
		}
		return interactionsString;
	}
	
	private static CompilationUnit parse(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(null); // parse
    }
	
	public void addTask(Task task) {
		tasks.add(task);
	}
	
	public String getTasksPrompt() {
		String tasksPrompt = null;
		String tasksString = "";
		String parametersDescription = "";
		String parametersName = "classificationCode";
		List<Task> listTasks = TasksManager.getPreferenceTasks();
		for (Task task : listTasks) {
			tasksString+="- "+task.getDescription()+" ("+task.getCode()+")\n";
		}
		IExtensionRegistry reg	= Platform.getExtensionRegistry();
		IConfigurationElement [] extensions = reg.getConfigurationElementsFor(EXTENSION_POINT_TASKS);
		Log.d("Total tasks extensions: "+ extensions.length);
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			try {
				ITasksGroup iTasksGroup = (ITasksGroup) element.createExecutableExtension("class");
				if(iTasksGroup != null) {
					iTasksGroup.setId(element.getAttribute("id"));
					iTasksGroup.setName(element.getAttribute("name"));
					iTasksGroup.setDescription(element.getAttribute("description"));
					Log.d("lenggth parameters -> "+iTasksGroup.getParameters().length);	
					for (Parameter parameter : iTasksGroup.getParameters()) {
						parametersDescription += parameter.getDescription()+" (otherwise "+parameter.getNoValue()+"), ";
						parametersName += ", "+parameter.getName();
					}
				}
			} catch (Exception e) {
    			Log.e(e.getMessage());
			}			
		}
		
        tasksPrompt = "You are a code assistant that helps software developers in programming tasks to output JSON with the detected parameters: "+parametersName+".\n"
    			+ "If the task is about a class, the requested task must explicitly mention the word 'class'; but if the task is about a method, the requested task must explicitly mention the word 'method'. "
    			+ "Please classify into one of the next categories (classification code in parentheses and in capital letter):\n"
    			+ "\n"
    			+ tasksString
    			+ "- General request for only java programming, if it could not be identified as a previous supported task(JAVA_PROGRAMMING)"
    			+ "* Greeting message, it isn't a task (GREETING_MESSAGE)"
    			+ "* Goodbye message, it isn't a task (GOODBYE_MESSAGE)"
    			+ "- None of the above ("+NO_CLASSIFICATION+")\n"
    			+ "\n"
    			+ "Output JSON for the following parameters mentioned: "
    			+ "classification code (otherwise "+NO_CLASSIFICATION+"), "
    			+ parametersDescription
    			+ "in the following request: ";
		
		return tasksPrompt;
	}
	
	public IEditorPart getIEditorPart() {
		IEditorPart iEditorPart = null;
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		iEditorPart = page.getActiveEditor();
		return iEditorPart;
	}
	
	public ITextSelection getITextSelection(String methodName) {
		JavaProject javaProje = new JavaProject();
		ITextSelection iTextSelection = null;
		ITextEditor iTextEditor = null;
		String editorId = PlatformUI.getWorkbench().getEditorRegistry().getEditors("Country.java")[0].getId();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart  iEditorPart = page.getActiveEditor();  
		IResource iresource= (IResource)iEditorPart.getEditorInput().getAdapter(IResource.class);
		File file = new File(iresource.getLocationURI());
        IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI());
        IFile iFile = null;
        if(files.length>0) {
	        try {
	        	iFile = files[0];
	        	iTextEditor = (ITextEditor) org.eclipse.ui.ide.IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), iFile, editorId);
	        } catch (Exception e1) {
				//e1.printStackTrace();
				Log.e(e1.getMessage());
			}
        }else {
        	Log.e("File not found");
        }
        String srcClass="";
        String srcMethod="";
		try {
			srcClass = javaProje.getClass(iresource.getProject(), Util.getClassName(iresource.getName())).getSource();
			srcMethod = javaProje.getMethod(iresource.getProject(), Util.getClassName(iresource.getName()), methodName).getSource();
		} catch (Exception e) {
			Log.e(e.getMessage());
			return null;
		}
		iTextEditor.selectAndReveal(srcClass.indexOf(srcMethod), srcMethod.length());
		iTextSelection = (ITextSelection)((ISelection)page.getSelection());
		return iTextSelection;
		
	}
	
	public IResource getCurrentResource() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart  iEditorPart = page.getActiveEditor();  
		IResource iresource= (IResource)iEditorPart.getEditorInput().getAdapter(IResource.class);
		File file = new File(iresource.getLocationURI());
        IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(file.toURI());
        if(files.length>0) {
        	return iresource;
        }
        return null;
	}
	
	
	
    public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}
	
	public String getCommentsAnnotation(String agent, String task, long timestamp, boolean space) {
		String comments ="";
		String annotation ="";
		Date date = new Date();
 		date.setTime(timestamp);
		if(space) {
			comments = "/**\n\t* "+JAVADOC_LINE_CARET+"\n\t*/";
			annotation = "\t@Generated(agent = \""+agent+"\", task = \""+task+"\", id = \""+timestamp+"\", timestamp = \""+Util.getDateFormat("yyyy-MM-dd HH:mm:ss", date)+"\")";		
		}else {
			comments = "/**\n* "+JAVADOC_LINE_CARET+"\n*/";
			annotation = "@Generated(agent = \""+agent+"\", task = \""+task+"\", id = \""+timestamp+"\", timestamp = \""+Util.getDateFormat("yyyy-MM-dd HH:mm:ss", date)+"\")";	
		}
		return comments+"\n"+annotation;   
	}
	
	public String getCodeSuggestion(String prompt, String idAgent) {
    	Log.d("Get code suggestion");
    	Response response = null;
    	if(idAgent != null) {
    		response = getAgent(idAgent).processMessage(prompt, TEMPERATURE_HIGH);
    	}else {
    		response = getAgent(true).processMessage(prompt, TEMPERATURE_HIGH);
    	}
    	if(response != null) {
    		System.out.println("Util.getJSON4");
    		String json = Util.getJSON(response.getText());
    		ResponseJSON responseJSON = null;
    		if(json!=null) {
    			Gson gson = new Gson();
    			try {
    				responseJSON = gson.fromJson(json, ResponseJSON.class);
    				return responseJSON.getCode();
				} catch (Exception e) {
					Log.e("Error processing JSON:"+ e.getMessage());
					return "Error processing JSON";
				}
    		}
    	}
	    return "Error processing the query";
    }
	
	public String getSuggestion(String idAgent, ContentAssistInvocationContext invocationContext) {
		String suggestion = Suggestions.getInstance().getProposal(idAgent, Util.getMD5(invocationContext.getDocument().get()), invocationContext.getInvocationOffset());
		if(suggestion != null) {
			return suggestion; 
		}else {
			String proposal = null;
			StringBuilder content = new StringBuilder(invocationContext.getDocument().get());
			String originalContent = content.toString();
	        String mark = "[[[REPLACEMENT_MARK]]]";
	        content.insert(invocationContext.getInvocationOffset(), mark);
	        String source = content.toString();
	        int MarkOffset = source.indexOf(mark);
	        
	        String prompt = "You are an assistant for Java developers that helps in java code autocompletion and you just output RESPONSE in format JSON with three parameters: "
	        +"explanation (java code explanation, otherwise NO_EXPLANATION), "
	        +"code (requested java code otherwise NO_CODE) "
	        +"and javaCodeType (code type: class, interface, method or snippet, otherwise NO_TYPE) for a REQUEST."
	        +"If the REQUEST doesn't need code put NO_CODE and NO_TYPE. "
	        +"REQUEST -> Generate java code to autocomplete and replace \""+mark
    		+"\" (replacement offset is "+MarkOffset+") in the following code (don't provide details or additional code explanation). If the code it's correct and doesn't need added anything, put NO_SUGGESTION in the paremeter explanation: "
    		+"\n```java\n"
    		+source
    		+"\n```";
        	String response = chatView.getCodeSuggestion(Util.codeToLine(prompt, true), idAgent);
	        if(response != null) {
	        	String lineContent = Util.getLineContent(invocationContext.getDocument(), invocationContext.getInvocationOffset());
	        	if(response.equals(lineContent)) {
	        		proposal = "[No suggestion *]";
	        	}
	        	if(response.length() > source.length()) {
	        		proposal = Util.getDiff(source, response);	
	        	}else {
	        		proposal = response;
	        	}
	        	String injectedSource = source.replace(mark, proposal);
	        	ArrayList<ValidatorInterface> validators = getContentAssistanValidators();
	        	for (ValidatorInterface validator : validators) {
        			if(validator.isPreviousValidation()) {
        				if(validator.isReady()) {
        					if(Resource.getActiveResourceNonUIThread() != null) {
		        				if(!validator.isValid(Resource.getActiveResourceNonUIThread() , injectedSource)) {
				        			Log.d("Validation error:"+idAgent);
				        			proposal =  "[No suggestion **]";
				        		}
		        			}else {
		        				Log.d("Validation: IResource null");
		        			}
        				}else {
    	        			Log.d("Validator - "+validator.getName()+" isn't configured.");
    	        			addMessage(SYSTEM, validator.getName()+" isn't configured", null, NOT_INDEX);
    	        		}
	        		}
				}
	        }else {
	        	proposal = "[No suggestion ***]";
	        }
	        Suggestions.getInstance().put(idAgent, proposal,
        			Util.getMD5(invocationContext.getDocument().get()), 
        			invocationContext.getInvocationOffset());
	        return proposal;
		}
	}
	
	public boolean syntaxError(String source) {
		IResource resource = Resource.getSelectedResource();
        IProject project = resource.getProject();
        IFolder tempFolder = project.getFolder("temp");
        boolean syntaxError = true;
        try {
	        if (!tempFolder.exists()) {
				tempFolder.create(true, true, null);
	        }
        	IFile file = tempFolder.getFile(resource.getName());
        	if (file.exists()) {
        		file.delete(true, null);
        	}
            file.create(new ByteArrayInputStream(source.getBytes()), true, null);
            syntaxError = SyntaxValidator.hasSyntaxError(file);
            return syntaxError;
	        
		} catch (CoreException e) {
			Log.e(e.getMessage());
		}
        return syntaxError;
	}
	
	public IResource getResource() {
		return resource;
	}

	public void setResource(IResource resource) {
		this.resource = resource;
	}
	
    public ArrayList<ValidatorInterface> getValidators() {
    	ArrayList<ValidatorInterface> validators = new ArrayList<ValidatorInterface>();
		IConfigurationElement [] extensions = getValidatorExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			try {
				ValidatorInterface Validator = (ValidatorInterface) element.createExecutableExtension("class");
				validators.add(Validator);					
			} catch (Exception e) {
				Log.e("Error getting validators: "+e.getMessage());
			}			
		}
		return validators;
    }
    
    public ArrayList<ValidatorInterface> getContentAssistanValidators() {
    	ArrayList<ValidatorInterface> validators = new ArrayList<ValidatorInterface>();
		IConfigurationElement [] extensions = getValidatorExtensions();
		String listValidators [] = getContentAssistantValidators();
		for (int j = 0; j < listValidators.length; j++) {
			System.out.println("TOTAL VALIDATORS EXTENSIONS: "+ extensions.length);
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement element = extensions[i];
				System.out.println("### VALIDATOR:"+element.getAttribute("id").toString());
				try {
					ValidatorInterface Validator = (ValidatorInterface) element.createExecutableExtension("class");
					if(element.getAttribute("name").toLowerCase().equals(listValidators[j].toLowerCase())){
						validators.add(Validator);
					}else {
						//Log.d("### DIFFERENTS:"+element.getAttribute("name").toLowerCase()+":"+listValidators[j].toLowerCase());
					}
				} catch (Exception e) {
					Log.e("Error getting validators: "+e.getMessage());
				}			
			}
		}
		
		return validators;
    }
    
    public ValidatorInterface getValidator(String id) {
    	ValidatorInterface validator = null;
		IConfigurationElement [] extensions = getValidatorExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement element = extensions[i];
			if(element.getAttribute("id").equals(id)){;
				try {
					validator = (ValidatorInterface) element.createExecutableExtension("class");
				} catch (Exception e) {
					Log.e("Validator  "+element.getAttribute("id")+": "+e.getMessage());
				}
			}
		}
		return validator;
    }
    
    public String replaceText(String sourceText, String fragment, int offset, int length) {
        if (sourceText == null || fragment == null) {
            throw new IllegalArgumentException("sourceText y fragment no pueden ser nulos");
        }
        if (offset < 0 || offset > sourceText.length()) {
            throw new IllegalArgumentException("offset fuera de los límites del texto fuente");
        }
        if (length < 0 || (offset + length) > sourceText.length()) {
            throw new IllegalArgumentException("length fuera de los límites del texto fuente");
        }
        StringBuilder result = new StringBuilder();
        result.append(sourceText, 0, offset);
        result.append(fragment);
        result.append(sourceText, offset + length, sourceText.length());
        return result.toString();
    }
    
    public boolean checkPostValidations(IEditorPart editorPart, Response response) {
    	ITextEditor editor = (ITextEditor)editorPart;
	    IDocumentProvider dp = editor.getDocumentProvider();
	    IDocument doc = dp.getDocument(editor.getEditorInput());
	    String source = doc.get();
	    ArrayList<ValidatorInterface> validators = getContentAssistanValidators();
    	for (ValidatorInterface validator : validators) {
			if(validator.isPostValidation()) {
				if(validator.isReady()) {
					if(!validator.isValid(null, null)) {
    	    			Log.d("Validator - "+validator.getName()+": ERROR");
    	    			addMessage(SYSTEM, validator.getName()+": ERROR", null, NOT_INDEX);
    	    			return false;
    	    		}else {
    	    			Log.d("Validator - "+validator.getName()+": Ok");
    	    			addMessage(SYSTEM, validator.getName()+": OK", null, NOT_INDEX);
    	    		}
				}else {
					Log.d("Validator - "+validator.getName()+" isn't configured.");
	    			addMessage(SYSTEM, validator.getName()+" isn't configured", null, NOT_INDEX);
	    		}
    		}
		}
    	return true;
    }
    
    /*public boolean checkPostValidations() {
	    ArrayList<ValidatorInterface> validators = getContentAssistanValidators();
    	for (ValidatorInterface validator : validators) {
			if(validator.isPostValidation()) {
				if(validator.isReady()) {
					if(!validator.isValid(null, null)) {
    	    			Log.d("Validator - "+validator.getName()+": ERROR");
    	    			addMessage(SYSTEM, validator.getName()+": ERROR", null, NOT_INDEX);
    	    			return false;
    	    		}else {
    	    			Log.d("Validator - "+validator.getName()+": Ok");
    	    			addMessage(SYSTEM, validator.getName()+": OK", null, NOT_INDEX);
    	    		}
				}else {
	    			Log.d("Validator - "+validator.getName()+" isn't configured.");
	    			addMessage(SYSTEM, validator.getName()+" isn't configured", null, NOT_INDEX);
	    			return false;
	    		}
    		}
		}
    	return true;
    }*/
    
    public Validation checkPostValidations() {
    	Validation validation = new Validation();
	    ArrayList<ValidatorInterface> validators = getContentAssistanValidators();
    	for (ValidatorInterface validator : validators) {
			if(validator.isPostValidation()) {
				if(validator.isReady()) {
					boolean valid = validator.isValid(null, null);
					validation.setInfo(validator.getInfo());
					if(!valid) {
						Log.d("Validator - "+validator.getName()+": ERROR");
    	    			addMessage(SYSTEM, validator.getName()+": ERROR", null, NOT_INDEX);
    	    			validation.setValid(false);
    	    			validation.setErrorMessage(validator.getError());
    	    			return validation;
    	    		}else {
    	    			Log.d("Validator - "+validator.getName()+": Ok");
    	    			addMessage(SYSTEM, validator.getName()+": OK", null, NOT_INDEX);
    	    			validation.setValid(true);
    	    			validation.setErrorMessage("");
    	    			return validation;
    	    		}
				}else {
					Log.d("Validator - "+validator.getName()+" isn't configured.");
	    			addMessage(SYSTEM, validator.getName()+" isn't configured", null, NOT_INDEX);
	    			validation.setValid(false);
	    			validation.setErrorMessage(validator.getName()+" isn't configured");
	    			return validation;
	    		}
    		}
		}
    	return validation;
    }
    
    public boolean checkPreviousValidations(IResource iResource, String injectedSource) {
    	Log.d("Realizing checkPreviousValidations");
    	Log.d("INJECTED: \n"+injectedSource);
	    if(injectedSource == null) {
	    	Log.e("INJECTED NULL");
	    	return false;
	    }
	    ArrayList<ValidatorInterface> validators = getContentAssistanValidators();
    	for (ValidatorInterface validator : validators) {
			if(validator.isPreviousValidation()) {
				if(validator.isReady()) {
					if(!validator.isValid(iResource, injectedSource)) {
						Boolean errorInMethod = false;
						MethodPosition methodPosition = Util.findMethodPosition(currentTask.getParameter("methodName").getValue(),injectedSource);
						Log.d("Method lines:"+methodPosition.getLineInitial()+" - "+methodPosition.getLineEnd());
						for (Diagnostic<? extends JavaFileObject> diagnostic :validator.getDiagnostics()) {
							if(diagnostic.getLineNumber()>=methodPosition.getLineInitial() && diagnostic.getLineNumber()<= methodPosition.getLineEnd()) {
								errorInMethod = true;
							}
							Log.d("checkPreviousValidations error:"+diagnostic.getLineNumber());
				        }
						
						if(!errorInMethod) {
							addMessage(SYSTEM, validator.getName()+": The proposed code is fine, but there is a previous error in other line(s) of code.", null, NOT_INDEX);
							return true;
						}
						Log.d("Validator - "+validator.getName()+": ERROR->"+validator.getError());
    	    			String messageInput = "You are a code Assistant that helps a software developer (User) in programming tasks. Our previous conversation is the following:\n"
    	    					+getInteractions()
    	    					+"* I get the following error, fix it (Return a brief explanation and only the correct java class code, no more code snippets): \n"
    	    					+validator.getError();
    	    			messageInput = Util.codeToLine(messageInput, true);
    	    			addMessage(SYSTEM, validator.getName()+": ERROR", null, NOT_INDEX);
    	    			Response response = processMessage(true, messageInput, TEMPERATURE_HIGH);
    	    			if(response != null) {
    	    				addMessage(BOT, response.getText(), response.getCode(), NOT_INDEX);
    	    			}
    	    			return false;
    	    		}else {
    	    			addMessage(SYSTEM, validator.getName()+": OK", null, NOT_INDEX);
    	    		}
				}else {
	    			addMessage(SYSTEM, validator.getName()+" isn't configured", null, NOT_INDEX);
	    		}
    		}
		}
    	return true;
    }
    
    public String getLastMessages(int num) {
    	ArrayList<Interaction> interactions = chatData.getInteractions();
    	String message = interactions.get(interactions.size()-2).getText()+"\n";
    	message += interactions.get(interactions.size()-1).getText()+"\n";
    	return message;
    }
    
    public void saveActiveEditor() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                IEditorPart editor = page.getActiveEditor();
                if (editor != null && editor.isDirty()) {
                    IHandlerService handlerService = (IHandlerService) window.getService(IHandlerService.class);
                    try {
                        handlerService.executeCommand("org.eclipse.ui.file.save", null);
                    } catch (Exception ex) {
                        //ex.printStackTrace();
                    	Log.e(ex.getMessage());
                    }
                } else {
                	Log.d("There are not changes to save.");
                }
            }
        }
    }
    
    public Response processMessage(Boolean isLLM, String messageInput, float temperature) {
    	String message;
    	if(contextConversation.getMessages().size() > 0) {
    	message= "You are a code Assistant that helps a software developer (User) in programming tasks. Our previous conversation is the following:\n"
    					+getMessages()
    					+"And now the last request message is: \n"
    					+messageInput;
    	}else {
    		message= "You are a code Assistant that helps a software developer (User) in programming tasks."
					+messageInput;
    	}
    	contextConversation.addMessage("- User:"+messageInput);
    	Response response;
    	if(store.getBoolean(PreferenceConstants.P_AGENTS_DINAMIC)) {
    		String projectPath = getCurrentProject().getLocation().toString();
    		AgentsStatistics agentsStatistics = new AgentsStatistics();
    		String bestAgent = agentsStatistics.getData(projectPath);
    		if(bestAgent !=null ) {
    			currentAgent = getAgent(bestAgent);
    		}else {
    			currentAgent = getAgent(true);
    		}
    		
        	response = currentAgent.processMessage(messageInput, temperature);
    	}else{
    		response = getAgent(true).processMessage(messageInput, temperature);
    	}
    	contextConversation.addMessage("- Assistant:"+response.getText());
    	return response;
    }
    
    public Response processMessage(String agentId, String messageInput, float temperature) {
    	String message;
    	if(contextConversation.getMessages().size() > 0) {
    	message= "You are a code Assistant that helps a software developer (User) in programming tasks. Our previous conversation is the following:\n"
    					+getMessages()
    					+"And now the last request message is: \n"
    					+messageInput;
    	}else {
    		message= "You are a code Assistant that helps a software developer (User) in programming tasks."
					+messageInput;
    	}
    	contextConversation.addMessage("- User:"+messageInput);
    	currentAgent = getAgent(agentId);
    	Response response = getAgent(agentId).processMessage(messageInput, temperature);
    	response.setAgentId(agentId);
    	contextConversation.addMessage("Assistant:"+response.getText());
    	return response;
    }
	
	public Response processMessageFallback(Boolean isLLM, String messageInput, float temperature) {
    	String message;
    	if(contextConversation.getMessages().size() > 0) {
    	message= "You are a code Assistant that helps a software developer (User) in programming tasks. Our previous conversation is the following:\n"
    					+getMessages()
    					+ ".\n\nAnswer the following request/question only if is about programming, "
        				+ "otherwise response [NOT_ABOUT_PROGRAMMING]: "+messageInput;
    	}else {
    		message= "You are a code Assistant that helps a java software developer (User) in programming tasks."
    				+ "\n\nAnswer the following request/question only if is about programming, "
    				+ "otherwise response [NOT_ABOUT_PROGRAMMING]: "+messageInput;
    	}
    	contextConversation.addMessage("- User:"+messageInput);
    	Response response = getAgent(true).processMessage(Util.codeToLine(message, true), temperature);
    	contextConversation.addMessage("- Assistant:"+response.getText());
    	//System.out.println("## INIT CONVERSATION #################");
    	//System.out.println(getMessages());
    	//System.out.println("################# END CONVERSATION ###");
    	return response;
    }
    
    public Response processMessageTaskClassifier(String messageInput, float temperature) {
    	Response response = null;
    	if(getTaskClassifierAgent() != null) {
    		response = getTaskClassifierAgent().processMessage(Util.codeToLine(messageInput,true), TEMPERATURE_LOW);		
    	}
    	System.out.println(getMessages());
    	return response;
    }
    
    
    public String getMessages() {
    	String result = null;
    	StringBuilder builder = new StringBuilder();
    	int max = contextConversation.getMessages().size();
    	if(maxMessages<contextConversation.getMessages().size()) {
    		max = maxMessages;
    	}
    	
        for (int i = 0; i < max; i++) {
        	builder.append(contextConversation.getMessages().get(i)).append("\n");
        }
        result= builder.toString().trim();
        return result;
    }
    
    public AgentInterface getCurrentAgent() {
    	if(currentAgent == null) {
    		currentAgent = getAgent(true);
    	}
    	return this.currentAgent;
    }
    
    public static String getMethodSource(String sourceClass, String methodName) {

        ASTParser parser = ASTParser.newParser(AST.JLS16);
        parser.setSource(sourceClass.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        StringBuilder methodSource = new StringBuilder();
        
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                if (node.getName().getIdentifier().equals(methodName)) {
                    methodSource.append(sourceClass, node.getStartPosition(), 
                                        node.getStartPosition() + node.getLength());
                }
                return super.visit(node);
            }
        });

        return methodSource.length() > 0 ? methodSource.toString() : null;
    }
    
    public List<String> getContextTypes(String sourceClass, String methodName) {

        ASTParser parser = ASTParser.newParser(AST.JLS16);
        parser.setSource(sourceClass.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
         
        Set<String> resultSet = new HashSet<>();
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodDeclaration node) {
                if (node.getName().getIdentifier().equals(methodName)) {
                	
                	if(getBooleanContext(PreferenceInitializer.CONTEXT_METHOD_PARAMETERS)) {
                        List<SingleVariableDeclaration> parameters = node.parameters();
                        for (SingleVariableDeclaration param : parameters) {
                            resultSet.add(param.getType().toString()); 
                        }
                	}
                    
                	if(getBooleanContext(PreferenceInitializer.CONTEXT_METHOD_VARIABLES)) {
                        Block body = node.getBody();
                        if (body != null) {
                            body.accept(new ASTVisitor() {
                                @Override
                                public boolean visit(VariableDeclarationStatement varDecl) {
                                    String type = varDecl.getType().toString();
                                    resultSet.add(type);
                                    return super.visit(varDecl);
                                }
                            });
                        }
                	}
                }
                return super.visit(node);
            }

            @Override
            public boolean visit(TypeDeclaration node) {
            	
            	if(getBooleanContext(PreferenceInitializer.CONTEXT_EXTENDED_CLASS)) {
            		if (node.getSuperclassType() != null) {
                        resultSet.add(node.getSuperclassType().toString()); // Add superclass
                    }
            	}
                if(getBooleanContext(PreferenceInitializer.CONTEXT_IMPLEMENTED_INTERFACE)) {
                	for (Object iface : node.superInterfaceTypes()) {
                        resultSet.add(iface.toString()); // Add implemented interface
                    }
                }
               	if(getBooleanContext(PreferenceInitializer.CONTEXT_ATTRIBUTE)) {
            		FieldDeclaration[] fields = node.getFields();
                    for (FieldDeclaration field : fields) {
                        resultSet.add(field.getType().toString()); // Add field type
                    }
            	}
                

                return super.visit(node);
            }
        });

        return new ArrayList<>(resultSet);
    }

    public boolean getBooleanContext(String contextInformation) {
    	String json = store.getString(PreferenceConstants.P_TABLE_TASKS);
        PTask [] originalTasks;
        if (json != null && !json.isEmpty()) {
            Gson gson = new Gson();
            originalTasks = gson.fromJson(json, PTask[].class);
            for (PTask task : originalTasks) {
            	if(task.getTaskName().equals(chatView.getCurrentTask().getName())){
            		HashMap<String, Boolean> contextInfo = task.getContext();
            		if( contextInfo.get(contextInformation) != null) {
            			return contextInfo.get(contextInformation);
            		}else {
            			return false;
            		}
            	}
            }
        }
    	return false;
    }
    
    
    public void openClassInEditor(String projectName, String classFilePath) {
        try {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (project != null && project.isAccessible()) {
                IFile file = project.getFile(new Path(classFilePath));
                if (file.exists()) {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    IDE.openEditor(page, file);
                } else {
                	Log.d("File not found: " + classFilePath);
                }
            } else {
            	Log.d("Project not accessible: " + projectName);
            }
        } catch (Exception e) {
            Log.e("Error opening file in editor: " + e.getMessage());
        }
    }
    
    
    public boolean checkPreviousValidations(String signature, String injectedSource) {
    	Log.d("Realizing checkPreviousValidations");
	    ArrayList<ValidatorInterface> validators = getContentAssistanValidators();
    	for (ValidatorInterface validator : validators) {
			if(validator.isPreviousValidation()) {
				if(validator.isReady()) {
					if(!validator.isValid(null, injectedSource)) {
						Boolean errorInMethod = false;
						MethodPosition methodPosition = Util.findMethodPositionBySignature(signature,injectedSource);
						Log.d("Method lines:"+methodPosition.getLineInitial()+" - "+methodPosition.getLineEnd());
						for (Diagnostic<? extends JavaFileObject> diagnostic :validator.getDiagnostics()) {
							if(diagnostic.getLineNumber()>=methodPosition.getLineInitial() && diagnostic.getLineNumber()<= methodPosition.getLineEnd()) {
								errorInMethod = true;
							}
				        	Log.d("checkPreviousValidations error:"+diagnostic.getLineNumber());
				        }
						
						if(!errorInMethod) {
							addMessage(SYSTEM, validator.getName()+": The proposed code is fine, but there is a previous error in other line(s) of code.", null, NOT_INDEX);
							return true;
						}
						Log.d("Validator - "+validator.getName()+": ERROR->"+validator.getError());
    	    			
    	    			return false;
    	    		}else {
    	    			Log.d("Validator - "+validator.getName()+": OK");
    	    			addMessage(SYSTEM, validator.getName()+": OK", null, NOT_INDEX);
    	    		}
				}else {
					Log.d("Validator - "+validator.getName()+" isn't configured.");
	    			addMessage(SYSTEM, validator.getName()+" isn't configured", null, NOT_INDEX);
	    		}
    		}
		}
    	return true;
    }
    
    public boolean checkPreviousValidations(MethodPosition methodPosition, String injectedSource) {
    	Log.d("Realizing checkPreviousValidations");
	    ArrayList<ValidatorInterface> validators = getContentAssistanValidators();
    	for (ValidatorInterface validator : validators) {
			if(validator.isPreviousValidation()) {
				if(validator.isReady()) {
					if(!validator.isValid(null, injectedSource)) {
						Boolean errorInMethod = false;
						Log.d("Method lines:"+methodPosition.getLineInitial()+" - "+methodPosition.getLineEnd());// firsrt check it's null
						for (Diagnostic<? extends JavaFileObject> diagnostic :validator.getDiagnostics()) {
							if(diagnostic.getLineNumber()>=methodPosition.getLineInitial() && diagnostic.getLineNumber()<= methodPosition.getLineEnd()) {
								errorInMethod = true;
							}
				        	Log.d("checkPreviousValidations error:"+diagnostic.getLineNumber());
				        }
						
						if(!errorInMethod) {
							Log.d("Validator RESULT- "+validator.getName()+": OK");
							addMessage(SYSTEM, validator.getName()+": The proposed code is fine, but there is a previous error in other line(s) of code.", null, NOT_INDEX);
							return true;
						}
						Log.d("Validator RESULT- "+validator.getName()+": ERROR->"+validator.getError());
    	    			
    	    			return false;
    	    		}else {
    	    			Log.d("Validator - "+validator.getName()+": OK");
    	    			addMessage(SYSTEM, validator.getName()+": OK", null, NOT_INDEX);
    	    		}
				}else {
					Log.d("Validator - "+validator.getName()+" isn't configured.");
	    			addMessage(SYSTEM, validator.getName()+" isn't configured", null, NOT_INDEX);
	    		}
    		}
		}
    	return true;
    }
    
    public Validation checkPreviousValidation(MethodPosition methodPosition, String injectedSource) {
    	Log.d("Realizing checkPreviousValidations");
	    ArrayList<ValidatorInterface> validators = getContentAssistanValidators();
	    Validation validation = new Validation();
    	if(methodPosition == null) {
    		validation.setErrorMessage("Method position could not be determined in the class");
    		validation.setValid(false);
			return validation;
    	}
    	for (ValidatorInterface validator : validators) {
			if(validator.isPreviousValidation()) {
				if(validator.isReady()) {
					if(!validator.isValid(null, injectedSource)) {
						StringBuilder errorsInMethod = new StringBuilder();
						Boolean errorInMethod = false;
						Log.d("Method lines:"+methodPosition.getLineInitial()+" - "+methodPosition.getLineEnd());// firsrt check it's null
						for (Diagnostic<? extends JavaFileObject> diagnostic :validator.getDiagnostics()) {
							long lineNumber = diagnostic.getLineNumber();
						    String message = diagnostic.getMessage(Locale.getDefault());
							if(diagnostic.getLineNumber()>=methodPosition.getLineInitial() && diagnostic.getLineNumber()<= methodPosition.getLineEnd()) {
								errorInMethod = true;
								JavaFileObject tempSource = diagnostic.getSource();
					    	    String sourceInfo = (tempSource != null) ? tempSource.toUri().toString() : "<no source>";
					    	    String errorMessage = String.format("Error in line %d in %s%n",
					    	            diagnostic.getLineNumber(),
					    	            sourceInfo)
					    	            + diagnostic.getMessage(null);
								errorsInMethod.append(errorMessage+"\n");
							}
				        	Log.d("checkPreviousValidations error:"+diagnostic.getLineNumber());
				        }
						if(errorInMethod) {
							validation.setErrorMessage(errorsInMethod.toString());
							Log.d("Validator RESULT- "+validator.getName()+": ERROR->"+validator.getError());
	    	    			validation.setValid(false);
							return validation;
						}else {
							Log.d("Validator RESULT- "+validator.getName()+": OK");
							addMessage(SYSTEM, validator.getName()+": The proposed code is fine, but there is a previous error in other line(s) of code.", null, NOT_INDEX);
							validation.setValid(true);
							return validation;
						}
						
    	    		}else {
    	    			Log.d("Validator - "+validator.getName()+": OK");
    	    			addMessage(SYSTEM, validator.getName()+": OK", null, NOT_INDEX);
    	    		}
				}else {
					Log.d("Validator - "+validator.getName()+" isn't configured.");
	    			addMessage(SYSTEM, validator.getName()+" isn't configured", null, NOT_INDEX);
	    		}
    		}
		}
    	validation.setValid(true);
    	return validation;
    }
    
    public Validation checkPreviousValidation(String injectedSource) {
    	Log.d("Realizing checkPreviousValidations");
	    ArrayList<ValidatorInterface> validators = getContentAssistanValidators();
	    Validation validation = new Validation();
    	for (ValidatorInterface validator : validators) {
			if(validator.isPreviousValidation()) {
				if(validator.isReady()) {
					if(!validator.isValid(null, injectedSource)) {
						validation.setValid(false);
						StringBuilder errorsInMethod = new StringBuilder();
						Boolean errorInMethod = false;
						if(validator.getDiagnostics() != null) {
							for (Diagnostic<? extends JavaFileObject> diagnostic :validator.getDiagnostics()) {
								long lineNumber = diagnostic.getLineNumber();
							    String message = diagnostic.getMessage(Locale.getDefault());
								errorInMethod = true;
								JavaFileObject tempSource = diagnostic.getSource();
					    	    String sourceInfo = (tempSource != null) ? tempSource.toUri().toString() : "<no source>";
					    	    String errorMessage = String.format("Error in line %d in %s%n",
					    	            diagnostic.getLineNumber(),
					    	            sourceInfo)
					    	            + diagnostic.getMessage(null);
								errorsInMethod.append(errorMessage+"\n");
					        	Log.d("checkPreviousValidations error:"+diagnostic.getLineNumber());
					        }
							if(errorInMethod) {
								validation.setErrorMessage(errorsInMethod.toString());
								Log.d("Validator RESULT- "+validator.getName()+": ERROR->"+validator.getError());
							}else {
								validation.setErrorMessage("The code is not valid, but the diagnostic errors could not be retrieved.");
							}
						}else {
							Log.d("checkPreviousValidations error: Diagnostic NULL");
							validation.setErrorMessage(validator.getError());
						}
						validation.setErrorStackTrace(validator.getErrorStackTrace());
						return validation;
    	    		}else {
    	    			Log.d("Validator - "+validator.getName()+": OK");
    	    			addMessage(SYSTEM, validator.getName()+": OK", null, NOT_INDEX);
    	    		}
				}else {
					Log.d("Validator - "+validator.getName()+" isn't configured.");
	    			addMessage(SYSTEM, validator.getName()+" isn't configured", null, NOT_INDEX);
	    		}
    		}
		}
    	//return true;
    	validation.setValid(true);
    	return validation;
    }
    
    public Interaction getCurrentInteraction() {
    	return this.currentInteraction;
    }
    
    public String getTaskDescriptionInstructions(Task task) {
    	if(task.getInstructions()!=null) {
    		return task.getDescription()+", "+task.getInstructions()+", ";
    	}else {
    		return task.getDescription();
    	}
    }
    
    public String getStatisticsPrompt() {
    	String prompt =  "Generate a class ExtractorStatistics that implements the Extractor interface and its getData method. This method should return a String value of a requested data item based on searching the available project logs. Consider the following contextual information: Example for the query: get the best agent based on pre-validations. ´´´java import java.util.ArrayList; import java.util.HashMap; import java.util.List; import java.util.Map; import com.google.gson.Gson; import com.google.gson.reflect.TypeToken; import caret.data.Extractor; import caret.data.Interaction; import caret.tool.Util; public class ExtractorStatistics implements Extractor{ public ExtractorStatistics (){ } /* * Query: get best agent based on acceptance */ @Override public String getData(String projectPath) { if (projectPath == null) { System.err.println(\"No project found.\"); return null; } List<Interaction> interactions = getIteractionsJSON(projectPath); if (interactions.isEmpty()) { System.err.println(\"No interactions found in \" + projectPath); return null; } Map<String, int[]> agentStats = new HashMap<>(); // [0] = total interactions, [1] = passed pre-validations for (Interaction interaction : interactions) { if (interaction.getResult() == null || interaction.getResult().getAgent() == null) continue; String agentName = interaction.getResult().getAgent().getName(); agentStats.putIfAbsent(agentName, new int[2]); agentStats.get(agentName)[0]++; // total if (interaction.isPassedPreValidations()) { agentStats.get(agentName)[1]++; // passed } } String bestAgent = null; double bestRate = -1.0; for (Map.Entry<String, int[]> entry : agentStats.entrySet()) { String agent = entry.getKey(); int total = entry.getValue()[0]; int passed = entry.getValue()[1]; double rate = (total > 0) ? (double) passed / total : 0.0; System.out.printf(\"Agent: %s | Passed: %d | Total: %d | Rate: %.2f%%%n\", agent, passed, total, rate * 100); if (rate > bestRate) { bestRate = rate; bestAgent = agent; } } return bestAgent; } public void print(String projectPath) { String bestAgent = getData(projectPath); System.out.println(\"# Best agent [Dynamic Mode]: \" + bestAgent); } } ´´´ Consider the following contextual information: ´´´java package caret.data; import java.util.ArrayList; import java.util.List; import com.google.gson.Gson; import com.google.gson.reflect.TypeToken; import caret.tool.Util; public interface Extractor { public default List<Interaction> getIteractionsJSON(String projectPath) { List<Interaction> totalInteractions = new ArrayList<Interaction> (); List <String> listInteractionsJSON = Util.readFilesFromDirectory(projectPath+\"/.log\", \".json\"); Gson gson = new Gson(); for (String interactionsJSON : listInteractionsJSON) { List<Interaction> interactions = gson.fromJson(interactionsJSON, new TypeToken<List<Interaction>>() {}.getType()); totalInteractions.addAll(interactions); } return totalInteractions; } /** * Returns the result of a query as a String. * @return query result in String format */ public String getData(String projectPath); } ´´´ ´´´java import java.util.Date; import caret.tasks.Parameter; import caret.vcs.GitUser; public class Interaction { private long timestamp; private String gitUser; private String gitEmail; private String role; private String text; private String code; private String hash; private Context context; private Result result; private String taskCode; private String taskName; private String targetParameterType; private String targetParameterName; private String chatMessage; private boolean passedPreValidations; public Interaction() { loadGitUser(); this.timestamp = new Date().getTime(); } public Interaction(String role, String text, String code, Context context, String taskCode) { loadGitUser(); this.timestamp = new Date().getTime(); this.role = role; this.text = text; this.code = code; this.context = context; this.taskCode = taskCode; } public Interaction(String role, String text, String code, Context context, String taskCode, long timestamp) { loadGitUser(); this.timestamp = timestamp; this.role = role; this.text = text; this.code = code; this.context = context; this.taskCode = taskCode; } private void loadGitUser() { GitUser _gitUser = new GitUser(); _gitUser.loadGitUser(); _gitUser.printGitUser(); if(_gitUser !=null && _gitUser.getUser() != null && _gitUser.getMail() != null) { this.gitUser = _gitUser.getUser(); this.gitEmail = _gitUser.getMail(); } } public long getTimestamp() { return timestamp; } public void setTimestamp(long timestamp) { this.timestamp = timestamp; } public String getRole() { return role; } public void setRole(String role) { this.role = role; } public String getText() { return text; } public void setText(String text) { this.text = text; } public String getCode() { return code; } public void setCode(String code) { this.code = code; } public Context getContext() { return context; } public void setContext(Context context) { this.context = context; } public Result getResult() { return result; } public void setResult(Result result) { this.result = result; } public String getTaskCode() { return taskCode; } public void setTaskCode(String taskCode) { this.taskCode = taskCode; } public String getTargetParameterType() { return targetParameterType; } public void setTargetParameterType(String targetParameterType) { this.targetParameterType = targetParameterType; } public String getTargetParameterName() { return targetParameterName; } public void setTargetParameterName(String targetParameterName) { this.targetParameterName = targetParameterName; } public String getTaskName() { return taskName; } public void setTaskName(String taskName) { this.taskName = taskName; } public String getChatMessage() { return chatMessage; } public void setChatMessage(String chatMessage) { this.chatMessage = chatMessage; } public String getHash() { return hash; } public void setHash(String hash) { this.hash = hash; } public String getGitUser() { return gitUser; } public void setGitUser(String gitUser) { this.gitUser = gitUser; } public String getGitEmail() { return gitEmail; } public void setGitEmail(String gitEmail) { this.gitEmail = gitEmail; } public boolean isPassedPreValidations() { return passedPreValidations; } public void setPassedPreValidations(boolean passedPreValidations) { this.passedPreValidations = passedPreValidations; } } ´´´ ´´´ java package caret.data; public class Result { boolean used; boolean createdResource; Agent agent; public Result(Agent agent, boolean used, boolean createdResource){ this.agent = agent; this.used = used; this.createdResource = createdResource; } public boolean isUsed() { return used; } public void setUsed(boolean used) { this.used = used; } public boolean isCreatedResource() { return createdResource; } public void setCreatedResource(boolean createdResource) { this.createdResource = createdResource; } public Agent getAgent() { return agent; } public void setAgent(Agent agent) { this.agent = agent; } } ´´´ And an example of the log is: ´´´JSON [ { ...}, { \"timestamp\": 1762932974747, \"gitUser\": \"devaptest\", \"gitEmail\": \"mail@mail.com\", \"role\": \"BOT\", \"text\": \"The method can be optimized by simplifying the Fibonacci calculation loop and improving variable updates for clarity and efficiency.\\n\\n\n"
    			+ "java\\npublic int generateFibonacci(int n) {\\n    if (n \\u003c\\u003d 0) {\\n        throw new IllegalArgumentException(\\\"Invalid input. Fibonacci series starts with index 1.\\\");\\n    }\\n    if (n \\u003d\\u003d 1) \\n        return 0;\\n    if (n \\u003d\\u003d 2) \\n        return 1;\\n    int a \\u003d 0, b \\u003d 1;\\n    for (int i \\u003d 3; i \\u003c\\u003d n; i++) {\\n        int next \\u003d a + b;\\n        a \\u003d b;\\n        b \\u003d next;\\n    }\\n    return b;\\n}\\n\n"
    			+ "\", \"code\": \"\\npublic int generateFibonacci(int n) {\\n if (n \\u003c\\u003d 0) {\\n throw new IllegalArgumentException(\\\"Invalid input. Fibonacci series starts with index 1.\\\");\\n }\\n if (n \\u003d\\u003d 1) \\n return 0;\\n if (n \\u003d\\u003d 2) \\n return 1;\\n int a \\u003d 0, b \\u003d 1;\\n for (int i \\u003d 3; i \\u003c\\u003d n; i++) {\\n int next \\u003d a + b;\\n a \\u003d b;\\n b \\u003d next;\\n }\\n return b;\\n}\", \"hash\": \"e3934d9de796f2d623547d1754ca1efd\", \"context\": { \"resource\": { \"projectName\": \"HiperPro\", \"fileName\": \"MathOperations.java\", \"fullPath\": \"/HiperPro/src/operation/MathOperations.java\", \"projectRelativePath\": \"src/operation/MathOperations.java\", \"codeFragment\": { \"startline\": 64, \"endline\": 64, \"offset\": 1684, \"length\": 370, \"methodName\": \"generateFibonacci\" } } }, \"result\": { \"used\": true, \"createdResource\": false, \"agent\": { \"name\": \"GPT\", \"technology\": \"GPT-4.1-MINI\", \"isLLM\": true } }, \"taskCode\": \"OPTIMISE_CODE\", \"taskName\": \"Optimise code\", \"targetParameterType\": \"METHOD\", \"targetParameterName\": \"generateFibonacci\", \"passedPreValidations\": true } ] ´´´ From this log, it is possible to obtain statistical information — for example, about the agent with the highest used rate (i.e., whose response was accepted). There is support for many LLM agents. The goal is, based on these data, to determine whether an open statistical query from a user is related to — or can be answered using — the data in the log. If it is possible give the code ABOUT_STATISTICS; otherwise, NO_STATISTICS. Then, you must response only a Java class that implements the interface. The response must be in JSON format as follows: ´´´JSON { \"classificationCode\": \"NO_STATISTICS\", \"javaClass\": \"\" } ´´ Respond (JSON) according to the instructions above for a following query:";
    	return prompt;
    }
}