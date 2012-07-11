package com.alenribic.atodo.views;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.alenribic.atodo.model.TodoEntry;
import com.alenribic.atodo.model.TodoEntryError;
import com.alenribic.atodo.parse.TodoXMLParser;
import com.alenribic.atodo.util.ATodoLog;
import com.alenribic.atodo.util.StringUtils;

public class ATodoViewDataProvider {
	private final String finderPath;
	private final String cruncherPath;
	private String effectedFilePath;
	private String finderArgsStr;
	
	public ATodoViewDataProvider(
			final String finderPath, 
			final String cruncherPath, 
			final String[] srcFileExts) {
		this.finderPath = finderPath;
		this.cruncherPath = cruncherPath;
		this.effectedFilePath = ResourcesPlugin
				.getWorkspace()
				.getRoot()
				.getLocation()
				.toOSString(); // entire workspace location
		finderArgsStr = "-R -f " + 
		                StringUtils.join(srcFileExts, ",") + 
		                " " + 
		                effectedFilePath;
	}
	
	public ATodoViewDataProvider(
			final String finderPath, 
			final String cruncherPath, 
			final String[] srcFileExts,
			final String effectedFilePath) {
		this(finderPath, cruncherPath, srcFileExts);
		this.effectedFilePath = effectedFilePath; // specific, effected file
		this.finderArgsStr = effectedFilePath;
	}
	
	public Object[] getData() throws Exception {
		Object[] results;
		
		ATodoLog.logInfo("finder->args: " + finderArgsStr);
		Process todoFindProc = Runtime.getRuntime().exec(finderPath + " " + finderArgsStr);
		BufferedReader input = new BufferedReader(new InputStreamReader(todoFindProc.getInputStream()));
		String rawTodo;
		StringBuffer rawTodos = new StringBuffer();
	    while ((rawTodo = input.readLine()) != null) {
	    	rawTodos.append(rawTodo + "\n");
	    }
	    input.close();
	    BufferedReader brCleanUp = 
	    	new BufferedReader (new InputStreamReader (todoFindProc.getErrorStream()));
	    String errLine;
        while ((errLine = brCleanUp.readLine()) != null) {
        	ATodoLog.logInfo("[Stderr] " + errLine);
        }
        brCleanUp.close();
        //todoFindProc.waitFor();
        //ATodoLog.logInfo("todoFindProc->exitVal: "+ todoFindProc.exitValue());
        //ATodoLog.logInfo(rawTodos.toString());
	    
	    Process todoCrunchProc = Runtime.getRuntime().exec(cruncherPath + " -o xml");
	    OutputStream in = todoCrunchProc.getOutputStream();
	    in.write(rawTodos.toString().getBytes());
	    in.close();
	    
	    BufferedReader output = new BufferedReader(new InputStreamReader(todoCrunchProc.getInputStream()));
		String todo;
		StringBuffer todos = new StringBuffer();
	    while ((todo = output.readLine()) != null) {
	    	todos.append(todo + "\n");
	    }
	    input.close();
	    brCleanUp = 
	    	new BufferedReader (new InputStreamReader (todoCrunchProc.getErrorStream()));
        while ((errLine = brCleanUp.readLine()) != null) {
        	ATodoLog.logInfo("[Stderr] " + errLine);
        }
        brCleanUp.close();
	    //todoCrunchProc.waitFor();
        //ATodoLog.logInfo("todoCrunchProc->exitVal: "+ todoCrunchProc.exitValue());
        //ATodoLog.logInfo(todos.toString());
        results = new TodoXMLParser().parse(todos.toString());
        return results;
	}
	
	public static int[] getNumItems(Object[] items) {
		int[] result = new int[2];
		int numTasks  = 0;
		int numErrors = 0;
		for (int i = 0; i < items.length; i++) {
			if (items[i] instanceof TodoEntry) numTasks++;
			else if (items[i] instanceof TodoEntryError) numErrors++;
		}
		result[0] = numTasks; result[1] = numErrors;
		return result;
	}
	
	public static int[] getNumItems(Table table) {
		int[] result = new int[2];
		int numTasks  = 0;
		int numErrors = 0;
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData() instanceof TodoEntry) numTasks++;
			else if (items[i].getData() instanceof TodoEntryError) numErrors++;
		}
		result[0] = numTasks; result[1] = numErrors;
		return result;
	}
}
