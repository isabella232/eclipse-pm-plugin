//package com.akamai.ruletreevalidator.handlers;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
//import org.apache.commons.configuration2.ex.ConfigurationException;
//import org.eclipse.jface.dialogs.IMessageProvider;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.dialogs.TitleAreaDialog;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.layout.GridData;
//import org.eclipse.swt.layout.GridLayout;
//import org.eclipse.swt.widgets.Button;
//import org.eclipse.swt.widgets.Composite;
//import org.eclipse.swt.widgets.Control;
//import org.eclipse.swt.widgets.Label;
//import org.eclipse.swt.widgets.List;
//import org.eclipse.swt.widgets.Listener;
//import org.eclipse.swt.widgets.Shell;
//import org.eclipse.swt.widgets.Text;
//import org.eclipse.swt.widgets.Event;
//
//import com.akamai.ruletreevalidator.exceptions.RuleTreeDownloadError;
//import com.akamai.ruletreevalidator.models.Contracts;
//import com.akamai.ruletreevalidator.models.OpenCredentials;
//import com.akamai.ruletreevalidator.papiCalls.*;
//import com.akamai.ruletreevalidator.utils.FileUtils;
//
//public class GetContractsDialog extends TitleAreaDialog {
//
//    private Shell parentShell;
//    private OpenCredentials openCredentials;
//    private List contracts;
//    private List products;
//    private static final String[] ITEMS = { "A", "B", "C", "D" };
//
//
//    public GetContractsDialog(Shell parentShell, OpenCredentials openCredentials) {
//    	super(parentShell);
//    	this.parentShell=parentShell;
//    	this.openCredentials = openCredentials;
//    }
//
//    @Override
//    public void create() {
//        super.create();
//        setTitle("Select a product");
//        setMessage("Please select a product", IMessageProvider.INFORMATION);
//    }
//
//    @Override
//    protected Control createDialogArea(Composite parent) {
//        Composite area = (Composite) super.createDialogArea(parent);
//        Composite container = new Composite(area, SWT.NONE);
//        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//        GridLayout layout = new GridLayout(2, false);
//        container.setLayout(layout);
//
//        createContractstList(container);
//
//        return area;
//    }
//
//    private void createContractstList(Composite container) {
//    	contracts = new List(container, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
//    	try {
//			PapiOpenCalls papiOpenCalls = new PapiOpenCalls(openCredentials);
//			ArrayList<String> contractList = papiOpenCalls.getContracts();
//			for (int i = 0, n = contractList.size(); i < n; i++) {
//		  	      contracts.add(contractList.get(i));
//		  	    }
//		        contracts.addListener(SWT.Selection, new Listener() {
//		          public void handleEvent(Event e) {
//		        	  createProductList(container);
//		          }});
//			
//		} catch (ConfigurationException | IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (RuleTreeDownloadError e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//    	
//    }
//    
//    private void createProductList(Composite container) {
//    	products = new List(container, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
//    	for (int i = 0, n = ITEMS.length; i < n; i++) {
//  	      products.add(ITEMS[i]);
//  	    }
//    	
//    }
//    
//    @Override
//    protected boolean isResizable() {
//        return true;
//    }
//    
//
//    // save content of the Text fields because they get disposed
//    // as soon as the Dialog closes
//    private void processInput() {
//		try {
//			System.out.println("Selected Contract: "+contracts.getSelection()[0]);
//			GetProductsDialog getProductsDialog = new GetProductsDialog(parentShell.getShell(), filePath, sectionName, contracts.getSelection()[0]);
//	        getProductsDialog.create();
//	        getProductsDialog.open();
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//		}
//	}
//
//
//    @Override
//    protected void okPressed() {
//        processInput();
//        super.okPressed();
//    }
//}