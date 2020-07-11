package test.my;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class TestFrame extends JFrame {
	private TestFrame myself = this;
	private JTextArea txtSql;
	private JComboBox cmbDatasource;
	private JButton btnTry;
	private JButton btnCommit;
	private JButton btnClear;
	private JButton btnExit;



//	private static Hashtable<String, String> ht=new Hashtable<String, String>();
	
	private boolean isTry = false;
	
//	static {
//		ht.put(Context.INITIAL_CONTEXT_FACTORY,INITIAL_CONTEXT_FACTORY); 
//		ht.put(Context.PROVIDER_URL, PROVIDER_URL); 
//	}

	public TestFrame() {
		init();
	}

	private void init() {


		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Hehe");
		JPanel p = new JPanel();
		this.add(p);
		
		BorderLayout bl = new BorderLayout();
		p.setLayout(bl);
		JPanel pInput = new JPanel(); 
		JPanel pBottom = new JPanel(); 
		p.add(pInput,BorderLayout.CENTER);
		p.add(pBottom,BorderLayout.SOUTH);
		
		txtSql = new JTextArea(30,80);
		JScrollPane sc = new JScrollPane(txtSql);
		pInput.add(sc);
		
		cmbDatasource = new JComboBox();
		btnTry = new JButton("Try");
		btnCommit = new JButton("Do");
		btnClear = new JButton("Clear");
		btnExit = new JButton("Exit");
		
		initCombox();
		
		pBottom.add(cmbDatasource);
		pBottom.add(btnTry);
		pBottom.add(btnCommit);
		pBottom.add(btnClear);
		pBottom.add(btnExit);

		addListners();
		
		pack();
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		this.setSize((int)screenSize.getWidth()/2,(int)screenSize.getHeight()/2);
		this.setLocation((int)(screenSize.getWidth()-this.getWidth())/2, (int)(screenSize.getHeight()-this.getHeight())/2);
		this.setResizable(false);

		try {
			String systemLookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
//			System.out.println("LookAndFeel: "+systemLookAndFeelClassName);
			UIManager.setLookAndFeel(systemLookAndFeelClassName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void initCombox() {
		cmbDatasource.addItem("Please Select");
		cmbDatasource.addItem(new DatasourceConfig("sales test",    "t3://10.100.135.185:8001/sales/", "salesDataSource"));
		cmbDatasource.addItem(new DatasourceConfig("sales uat",    	"t3://10.100.137.185:8001/sales/", "salesDataSource"));
		cmbDatasource.addItem(new DatasourceConfig("sales prod",    "t3://10.80.33.38:8001/sales/",    "salesDataSource"));
//		cmbDatasource.addItem(new DatasourceConfig("banksales uat", "t3://10.100.137.192:8001/sales/", "salesDataSource"));
		cmbDatasource.addItem(new DatasourceConfig("banksales prod","t3://10.80.35.18:8001/sales/",    "salesDataSource"));
//		cmbDatasource.addItem(new DatasourceConfig("newbiz test",    "t3://10.100.135.168:7001", "sinosoftDataSource"));
//		cmbDatasource.addItem(new DatasourceConfig("newbiz uat",    "t3://10.100.137.168:8001", "sinosoftDataSource"));
//		cmbDatasource.addItem(new DatasourceConfig("newbiz prod",   "t3://10.100.6.28:8001", "sinosoftDataSource"));
//		cmbDatasource.addItem(new DatasourceConfig("fin test",    "t3://10.100.135.170:8001", "sinosoftDataSource"));
//		cmbDatasource.addItem(new DatasourceConfig("fin uat",    	"t3://10.100.137.170:8001", "sinosoftDataSource"));
//		cmbDatasource.addItem(new DatasourceConfig("fin prod",    "t3://10.100.6.32:8001", "sinosoftDataSource"));

		cmbDatasource.addItem(new DatasourceConfig("newsales test",    "t3://10.100.135.243:8001", "FMS"));
		cmbDatasource.addItem(new DatasourceConfig("newsales uat",    "t3://10.100.137.220:8001", "FMS"));
		cmbDatasource.addItem(new DatasourceConfig("newsales prod",    "t3://10.80.32.105:8001", "FMS"));
	}
	
	private void addListners() {
		btnTry.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onClickRun();
			}
		});
		
		btnCommit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClickCommit();
			}
		});
		
		
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onClickClear();
			}
		});
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		txtSql.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				isTry = false;
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				isTry = false;
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				isTry = false;
			}
		});
		
		cmbDatasource.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				isTry = false;
			}
			
		});
	}

	private Map excuteSql( boolean needCommit) throws Exception {
		String txtSqls = txtSql.getText();
		Map retMap = new HashMap(3);
		if (txtSqls.trim().length()==0) {
			retMap.put("resutlStr","no sql");
			return retMap;
		}
		String splitStr = ";";
		if (txtSqls.indexOf(";;")>0) {
			splitStr = ";;";
		}

		String[] sqls = txtSqls.split(splitStr);
		if (sqls==null || sqls.length==0) {
			retMap.put("resutlStr","no sql");
			return retMap;
		}
		Connection connection = getConnection();
		boolean autoCommit = connection.getAutoCommit();
		connection.setAutoCommit(false);
		Statement stmt = null;
		StringBuffer buf = new StringBuffer();
		int[] result = null;

		try {
			for (int i=0; i<sqls.length; i++) {
				if ( sqls[i]==null) {
					continue;
				}
				String sql = sqls[i].trim();
				if (sql.length()==0) {
					continue;
				}

				StringBuilder builderLine = new StringBuilder();
				String[] lines = sql.split("\n");
				for (String aLine : lines) {
					aLine = aLine.trim();
					if (aLine.length()==0) {
						continue;
					}
					int commentStrIndex = aLine.indexOf("--");
					if (commentStrIndex>=0) {
						builderLine.append(aLine, 0, commentStrIndex);
					}
					else {
						builderLine.append(aLine);
					}
					builderLine.append(" ");
				}

				String tmpSql = builderLine.toString().trim();
				if (tmpSql.length()==0) {
					continue;
				}
//				JOptionPane.showMessageDialog(myself,tmpSql);
				String lowcaseTmpSql = tmpSql.toLowerCase();
				if ( (lowcaseTmpSql.indexOf("update ")>=0 && lowcaseTmpSql.indexOf(" where ")<0)
						|| (lowcaseTmpSql.indexOf("delete ")>=0 && lowcaseTmpSql.indexOf(" where ")<0) ) {
					throw new Exception("no where clause");
				}

				if (stmt==null) {
					stmt = connection.createStatement();
				}
				stmt.addBatch(tmpSql);
				sqls[i]=tmpSql;
			}
			result = stmt.executeBatch();
			if (needCommit) {
				connection.commit();
			}
			else {
				connection.rollback();
			}
		}
		catch(Exception ex) {
			connection.rollback();
			throw ex;
		}
		finally {
			try {if (connection!=null) connection.setAutoCommit(autoCommit); } catch(Exception ex) {}
			closeDB(connection,stmt);
		}
		
		if (result!=null) {
			int k=0;
			for (int i : result) {
				buf.append(i).append(", ").append(++k%20==0?"\n":"");
			}
		}

		retMap.put("resutlStr",buf.toString());
		retMap.put("sqls",sqls);
		retMap.put("sqlresults",result);
		return retMap;

	}
	
	private void closeDB(Connection connection, Statement stmt) {
		try {if (stmt!=null)stmt.close();} catch(Exception ex) {}
		try {if (connection!=null)connection.close();} catch(Exception ex) {}
	}
	private Connection getConnection() throws Exception{
		DatasourceConfig config = (DatasourceConfig)cmbDatasource.getSelectedItem();
		Context context = new InitialContext(config.getJndiContext());
		javax.sql.DataSource ds = (javax.sql.DataSource) context.lookup(config.getDatasourceName());
		Connection connection = ds.getConnection();
		return connection;
	}

	private void onClickRun() {
		try {
			if (txtSql.getText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(myself,"please input a sql with where clause!","Try",JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object selectedDataSource = cmbDatasource.getSelectedItem();
			if (!(selectedDataSource instanceof DatasourceConfig)) {
				isTry = false;
				JOptionPane.showMessageDialog(myself,"please select a datasource!","Try",JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if (selectedDataSource.toString().toLowerCase().indexOf("prod")>=0){
				int isSure = JOptionPane.showConfirmDialog(myself, "Are you sure try something prod?","Try Confirm",JOptionPane.YES_NO_CANCEL_OPTION);
				if (isSure!=JOptionPane.YES_OPTION) {
					return ;
				}
			}
			Map resultMap = excuteSql(false);
			JOptionPane.showMessageDialog(myself, "try done!,result: " + resultMap.get("resutlStr"));
			isTry = true;
		} catch (Exception ex) {
			isTry = false;
//					ex.printStackTrace();
			JOptionPane.showMessageDialog(myself, ex.getMessage()!=null && !"".equals(ex.getMessage()) ? ex.getMessage() : (ex.getCause()!= null ? ex.getCause().getMessage() : "error"));
		}
	}
	
	private void onClickCommit() {
		try {
			if (txtSql.getText().trim().isEmpty()) {
				JOptionPane.showMessageDialog(myself,"please input a sql with where clause!","Try",JOptionPane.ERROR_MESSAGE);
				return;
			}
			Object selectedDataSource = cmbDatasource.getSelectedItem();
			if (!(selectedDataSource instanceof DatasourceConfig)) {
				isTry = false;
				JOptionPane.showMessageDialog(myself,"please select a datasource!","Do",JOptionPane.ERROR_MESSAGE);
				return;
			}
			else if (selectedDataSource.toString().toLowerCase().indexOf("prod")>=0){
				int isSure = JOptionPane.showConfirmDialog(myself, "Are you sure do something prod?","Do Confirm",JOptionPane.YES_NO_CANCEL_OPTION);
				if (isSure!=JOptionPane.YES_OPTION) {
					return ;
				}
			}
			if (!isTry) {
				JOptionPane.showMessageDialog(myself, "Please try first, pay attention to involved rows!","Do",JOptionPane.ERROR_MESSAGE);
				return;
			}
			Map resultMap = excuteSql(true);
			JOptionPane.showMessageDialog(myself, "try done!,result: " + resultMap.get("resutlStr"));
			isTry = false;
//			logSql( (String[]) resultMap.get("sqls"), (int [])resultMap.get("sqlresults"));
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(myself, ex.getMessage());
			isTry = false;
		}
	}

	private void logSql(String[] sqls, int[] sqlresults) {
		if (sqls==null || sqlresults==null || sqlresults.length!=sqls.length) {
			return ;
		}
		Connection connection = null;
		PreparedStatement pstmt = null;
		try {
			connection = getLogConnection();
			pstmt = connection.prepareStatement("insert into hehe (SQLSTR,SQLRES,RUNDATE) values(?,?,?)");
			for (int i = 0; i < sqls.length; i++) {
				pstmt.setString(1,sqls[i]);
				pstmt.setString(2,"" + sqlresults[i]);
				pstmt.setTimestamp(3,new Timestamp(System.currentTimeMillis()));
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			connection.commit();
		} catch (Exception e) {
			try {connection.rollback();} catch (Exception ex) {}
			e.printStackTrace();
		} finally {
			closeDB(connection,pstmt);
		}

	}

	private Connection getLogConnection() throws Exception{
		DatasourceConfig config = new DatasourceConfig("newsales dev",    "t3://10.100.134.13:8001/", "FMS");
		Context context = new InitialContext(config.getJndiContext());
		javax.sql.DataSource ds = (javax.sql.DataSource) context.lookup(config.getDatasourceName());
		Connection connection = ds.getConnection();
		return connection;
	}
	private void onClickClear() {
		if ("".equals(txtSql.getText())) {
			cmbDatasource.setSelectedIndex(0);
		}
		txtSql.setText("");
		txtSql.grabFocus();
		isTry = false ;
	}
}


class DatasourceConfig {
	private static String INITIAL_CONTEXT_FACTORY="weblogic.jndi.WLInitialContextFactory";
	private Hashtable<String, String> ht=new Hashtable<String, String>();
	private String name;
	private String providerUrl;
	private String dataSourceName;
	
	public DatasourceConfig(String name,String providerUrl,String dataSourceName) {
		this.name = name;
		this.providerUrl = providerUrl;
		this.dataSourceName = dataSourceName;
		ht.put(Context.INITIAL_CONTEXT_FACTORY,INITIAL_CONTEXT_FACTORY);
		ht.put(Context.PROVIDER_URL, providerUrl); 
	}
	
	public Hashtable<String, String> getJndiContext() {
		return ht;
	}
	
	public String getDatasourceName() {
		return dataSourceName;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String toString() {
		return this.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DatasourceConfig other = (DatasourceConfig) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
