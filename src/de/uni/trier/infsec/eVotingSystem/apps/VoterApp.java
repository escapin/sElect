package de.uni.trier.infsec.eVotingSystem.apps;


import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseListener;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;

import javax.swing.SwingConstants;
import java.awt.Font;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.coreSystem.Params;
import de.uni.trier.infsec.eVotingSystem.coreSystem.Voter;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkienc.*;
import de.uni.trier.infsec.functionalities.pkisig.*;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.lib.network.NetworkError;
import javax.swing.JTextArea;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Toolkit;




public class VoterApp extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private JTextField textField;
	private JLabel lblUserNotRegister;
	private JPanel center;
	private final static String STORE = "Store";
    private final static String RETRIEVE = "Retrieve";
    private JTextField labelField;
	private JTextArea msgToStore;
	private JTextArea textMsgRetrieved;
	private JLabel lblStoreStatus;
	private JLabel lblRetrieveStatus;
	private JComboBox comboBox;
	private JLabel lblWait;
	/*
	 * CORE FIELD
	 */
	private int voterID;
	private Decryptor user_decr;
	private Signer user_sign;
	private Voter client;
	//private static final int STORE_ATTEMPTS = 3; 
	// attempts to store a msg under a label in such a way that server and client counters are aligned
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VoterApp frame = new VoterApp();
					frame.setVisible(true);
					frame.setResizable(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	/**
	 * Create the frame.
	 */
	public VoterApp() {
		//setIconImage(Toolkit.getDefaultToolkit().getImage(UserGUI.class.getResource("/de/uni/trier/infsec/cloudStorage/cloud.png")));
		setTitle("Voter - eVotingSystem RS3");
		//TODO: find a cooler name
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 489, 334);
		
		
		CardLayout cl = new CardLayout();
		getContentPane().setLayout(cl);
		
		
		// login Panel
		JPanel login = new JPanel();
		JButton btnLogIn = new JButton("Log In");
		
		JLabel lblUserId = new JLabel("User ID:");
		textField = new JTextField();
		textField.setColumns(10);
		lblUserNotRegister = new JLabel("");
		lblUserNotRegister.setFont(new Font("Dialog", Font.BOLD, 14));
		lblUserNotRegister.setHorizontalAlignment(SwingConstants.LEFT);
		lblUserNotRegister.setForeground(Color.RED);
		
		lblWait = new JLabel();
		lblWait.setHorizontalAlignment(SwingConstants.CENTER);
		GroupLayout gl_login = new GroupLayout(login);
		gl_login.setHorizontalGroup(
			gl_login.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_login.createSequentialGroup()
					.addGap(57)
					.addGroup(gl_login.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_login.createSequentialGroup()
							.addComponent(lblUserId, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblUserNotRegister, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(64, Short.MAX_VALUE))
				.addGroup(gl_login.createSequentialGroup()
					.addContainerGap(247, Short.MAX_VALUE)
					.addComponent(lblWait, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
					.addGap(32)
					.addComponent(btnLogIn, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
					.addGap(48))
		);
		gl_login.setVerticalGroup(
			gl_login.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_login.createSequentialGroup()
					.addGap(58)
					.addGroup(gl_login.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblUserId, GroupLayout.DEFAULT_SIZE, 17, Short.MAX_VALUE)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(37)
					.addComponent(lblUserNotRegister, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_login.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnLogIn, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblWait))
					.addGap(85))
		);
		login.setLayout(gl_login);
		
		btnLogIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				if(textField.getText().length()==0){
					lblUserNotRegister.setText("<html>User ID field empty!<br>Please insert a valid ID number of a previously registered user.</html>");
					return;
				}
				
				try{
					voterID = Integer.parseInt(textField.getText());
					if(voterID<0)
						throw new NumberFormatException();
				} catch (NumberFormatException e){
					System.out.println("'" + textField.getText() + "' is not a proper userID!\nPlease insert the ID number of a previously registered user.");
					lblUserNotRegister.setText("<html>'" + textField.getText() + "' is not a proper userID!<br>Please insert the ID number of a registered user.</html>");
					return;
				}
				
				
				lblUserNotRegister.setText("");
				lblWait.setText("Wait..."); //FIXME: it doesn't work!
				
				JPanel loginPanel = (JPanel) ((JButton)ev.getSource()).getParent();
				//lblWait.paintImmediately(loginPanel.getVisibleRect());
				loginPanel.paintImmediately(loginPanel.getVisibleRect());
				
				boolean userRegistered=false;
				try {
					setupClient(voterID);
					userRegistered=true;
				} catch (FileNotFoundException e){
					System.out.println("User " + voterID + " not registered!\nType \'UserRegisterApp <user_id [int]>\' in a terminal to register him/her.");
					lblUserNotRegister.setText("<html>User " + voterID + " not registered!<br>Please register yourself before log in.</html>");
				} catch (IOException e){
					System.out.println("IOException occurred while reading the credentials of the user!");
					lblUserNotRegister.setText("IOException occurred while reading the credentials of the user!");
				} catch (RegisterSig.PKIError | RegisterEnc.PKIError e){
					System.out.println("PKI Error occurred: perhaps the PKI server is not running!");
					lblUserNotRegister.setText("<html>PKI Error:<br> perhaps the PKI server is not running!</html>");
				} catch (NetworkError e){
					//FIXME: java.net.ConnectException when the PKIServer is not running!
					System.out.println("Network Error occurred while connecting with the PKI server: perhaps the PKI server is not running!");
					lblUserNotRegister.setText("<html>Network Error occurred:<br> perhaps the PKI server is not running!</html>");
				} finally{
					lblWait.setText("");
				}
				if(userRegistered){
					textField.setText("");
					setTitle("User " + voterID + " - Cloud Storage 2013");
					CardLayout cl = (CardLayout) getContentPane().getLayout();
					cl.show(getContentPane(), "2");
				}
			}
		});
		
		
		// main windows panel
		JPanel main = new JPanel();
		
		// add the two layout to the main
		getContentPane().add(login, "1");
		getContentPane().add(main, "2");
		main.setLayout(new BorderLayout(0, 0));
		// set the default option
		cl.show(getContentPane(), "1");
		
		// North panel
		JPanel north = new JPanel();
		main.add(north, BorderLayout.NORTH);
		
		comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {STORE, RETRIEVE}));
		JLabel lblLabel = new JLabel("Label:");
		
		labelField = new JTextField();
		labelField.setColumns(10);
		GroupLayout gl_north = new GroupLayout(north);
		gl_north.setHorizontalGroup(
			gl_north.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_north.createSequentialGroup()
					.addGap(23)
					.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
					.addGap(68)
					.addComponent(lblLabel)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(labelField, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(56, Short.MAX_VALUE))
		);
		gl_north.setVerticalGroup(
			gl_north.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_north.createSequentialGroup()
					.addContainerGap(29, Short.MAX_VALUE)
					.addGroup(gl_north.createParallelGroup(Alignment.BASELINE)
						.addComponent(labelField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblLabel))
					.addGap(20))
		);
		north.setLayout(gl_north);
		
		
		// Center panel
		center = new JPanel();
		main.add(center, BorderLayout.CENTER);
		CardLayout clCenter = new CardLayout(0, 0);
		center.setLayout(clCenter);
		
		JPanel storePanel = new JPanel();
		
		
		JLabel lblInsertTheMessage = new JLabel("Insert the message to store:");
		lblStoreStatus = new JLabel();
		lblStoreStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		lblStoreStatus.setFont(new Font("Dialog", Font.PLAIN, 14));
		
		msgToStore = new JTextArea();
		JScrollPane ScrollTextToStore = new JScrollPane(msgToStore);
		
		JButton btnStore = new JButton("Store");
		btnStore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				
				lblStoreStatus.setForeground(Color.BLACK);
				if(labelField.getText().length()==0){
					lblStoreStatus.setText("Label empty!");
					return;
				}
				if(msgToStore.getText().length()==0){
					lblStoreStatus.setText("Message to store is empty!");
					return;
				}
				
				lblStoreStatus.setForeground(Color.BLACK);
				lblStoreStatus.setText("Wait...");
				JPanel storePanel = (JPanel) ((JButton) ev.getSource()).getParent();
				storePanel.paintImmediately(storePanel.getVisibleRect()); // it would be better to use a separate Thread 
				
/*				boolean correctlyStored=false;
				boolean outOfDate=true;
				int i=0;
				for(;i<STORE_ATTEMPTS && outOfDate && !correctlyStored; i++){
					outOfDate=false;
					try{
						client.store(msgToStore.getText().getBytes(), labelField.getText().getBytes());
						correctlyStored=true;
					} catch(CounterOutOfDate e){
						outOfDate=true;
					} catch (NetworkError e) {
						lblStoreStatus.setForeground(Color.RED);
						lblStoreStatus.setText("<html>Network Error: perhaps the server is not running!</html>");
					} catch (StorageError e) {
						lblStoreStatus.setForeground(Color.RED);
						lblStoreStatus.setText("<html>The message has not been stored due to a Storage Error!</html>");
					}
				}
				if(i>=STORE_ATTEMPTS && !correctlyStored){
					lblStoreStatus.setForeground(Color.RED);
					lblStoreStatus.setText("<html>The message has not been stored because the counter was always out of date!</html>");
					System.out.println("The message has not been stored: during " + STORE_ATTEMPTS + " attempts, the Client's counter has always been out of date!");
				}
				if(correctlyStored){
					msgToStore.setText("");
					lblStoreStatus.setForeground(Color.BLACK);
					lblStoreStatus.setText("<html>Message stored!</html>");
				}*/
				
			}
		});
		
		
		GroupLayout gl_storePanel = new GroupLayout(storePanel);
		gl_storePanel.setHorizontalGroup(
			gl_storePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_storePanel.createSequentialGroup()
					.addGap(24)
					.addGroup(gl_storePanel.createParallelGroup(Alignment.TRAILING, false)
						.addGroup(gl_storePanel.createSequentialGroup()
							.addComponent(lblStoreStatus, GroupLayout.PREFERRED_SIZE, 313, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnStore))
						.addComponent(lblInsertTheMessage, Alignment.LEADING)
						.addComponent(ScrollTextToStore, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 412, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(53, Short.MAX_VALUE))
		);
		gl_storePanel.setVerticalGroup(
			gl_storePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_storePanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblInsertTheMessage)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(ScrollTextToStore, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 30, Short.MAX_VALUE)
					.addGroup(gl_storePanel.createParallelGroup(Alignment.LEADING)
						.addComponent(btnStore)
						.addComponent(lblStoreStatus, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
					.addGap(22))
		);
		storePanel.setLayout(gl_storePanel);
		
		JPanel retrievePanel = new JPanel();
		
		lblRetrieveStatus = new JLabel();
		lblRetrieveStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRetrieveStatus.setFont(new Font("Dialog", Font.PLAIN, 14));
		
		JLabel lblMessageRetrieved = new JLabel("Message Retrieved:");
		
		JButton btnRetrieve = new JButton("Retrieve");
		// FIXME: the button is moving when the 'lblRetrieveStatus' label changes!
		btnRetrieve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				lblRetrieveStatus.setForeground(Color.BLACK);
				if(labelField.getText().length()==0){
					lblRetrieveStatus.setText("Label empty!");
					return;
				}
				
				textMsgRetrieved.setText("");
				lblRetrieveStatus.setForeground(Color.BLACK);
				lblRetrieveStatus.setText("Wait...");	
				
				JPanel retrievePanel = (JPanel) ((JButton) ev.getSource()).getParent();
				retrievePanel.paintImmediately(retrievePanel.getVisibleRect()); // it would be better to use a separate Thread 
				
				/*byte[] msg=null;
				boolean msgRetrieved=false;
				try {
					msg = client.retrieve(labelField.getText().getBytes());
					msgRetrieved=true;
				} catch (NetworkError e) {
					lblRetrieveStatus.setForeground(Color.RED);
					lblRetrieveStatus.setText("<html>Network Error: perhaps the server is not running!</html>");
				} catch (StorageError e) {
					lblRetrieveStatus.setForeground(Color.RED);
					lblRetrieveStatus.setText("<html>The message has not been stored due to a Storage Error!</html>");
				}
				if(msgRetrieved)
					if(msg!=null){
						textMsgRetrieved.setText(new String(msg));
						lblRetrieveStatus.setText("");
					}
					else{
						lblRetrieveStatus.setForeground(Color.BLACK);
						lblRetrieveStatus.setText("<html>No message stored under this label!</html>");
					}*/
			}
		});
		
		
		textMsgRetrieved = new JTextArea();
		textMsgRetrieved.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				setCursor(new Cursor(Cursor.TEXT_CURSOR));
			}
			public void mouseExited(MouseEvent e) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		textMsgRetrieved.setEditable(false);
		
		
		JScrollPane scrollMsgRetrieved = new JScrollPane(textMsgRetrieved);
		
		
		
		GroupLayout gl_retrievePanel = new GroupLayout(retrievePanel);
		gl_retrievePanel.setHorizontalGroup(
			gl_retrievePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_retrievePanel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_retrievePanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_retrievePanel.createSequentialGroup()
							.addGroup(gl_retrievePanel.createParallelGroup(Alignment.LEADING)
								.addComponent(lblRetrieveStatus, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblMessageRetrieved))
							.addGap(86)
							.addComponent(btnRetrieve))
						.addComponent(scrollMsgRetrieved, GroupLayout.PREFERRED_SIZE, 421, GroupLayout.PREFERRED_SIZE))
					.addContainerGap(56, Short.MAX_VALUE))
		);
		gl_retrievePanel.setVerticalGroup(
			gl_retrievePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_retrievePanel.createSequentialGroup()
					.addGroup(gl_retrievePanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnRetrieve)
						.addGroup(gl_retrievePanel.createSequentialGroup()
							.addComponent(lblRetrieveStatus, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblMessageRetrieved)))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollMsgRetrieved, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
					.addGap(55))
		);
		
		retrievePanel.setLayout(gl_retrievePanel);
		
		center.add(storePanel, STORE);
		center.add(retrievePanel, RETRIEVE);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 JComboBox jcb = (JComboBox) e.getSource();
				 CardLayout cl = (CardLayout)(center.getLayout());
				 
				 lblWait.setText("");
				 lblStoreStatus.setText("");
				 lblRetrieveStatus.setText("");
				 
				 cl.show(center, jcb.getSelectedItem().toString());
			}
		});
		
		// South panel
		JPanel south = new JPanel();
		main.add(south, BorderLayout.SOUTH);
		
		JButton btnLogOut = new JButton("Log Out");
		btnLogOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				destroyClient();
				
				lblUserNotRegister.setText("");
				labelField.setText("");
				lblStoreStatus.setText("");
				lblRetrieveStatus.setText("");
				textMsgRetrieved.setText("");
				
				comboBox.setSelectedIndex(0); // set the combo box to Store!
				
				CardLayout centerCl = (CardLayout) center.getLayout();
				centerCl.show(center, STORE);
				
				CardLayout cl = (CardLayout) getContentPane().getLayout();
				cl.show(getContentPane(), "1");
				
				setTitle("Voter - eVoting System RS3");
			}
		});
		GroupLayout gl_south = new GroupLayout(south);
		gl_south.setHorizontalGroup(
			gl_south.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_south.createSequentialGroup()
					.addContainerGap()
					.addComponent(btnLogOut)
					.addContainerGap(348, Short.MAX_VALUE))
		);
		gl_south.setVerticalGroup(
			gl_south.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_south.createSequentialGroup()
					.addComponent(btnLogOut)
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		south.setLayout(gl_south);
	}
	
	
	
	/*
	 * CORE CODE
	 */
	private void setupClient(int voterID) throws IOException, RegisterEnc.PKIError, RegisterSig.PKIError, NetworkError {
		
		// De-serialize keys and create cryptographic functionalities:
		byte[] serialized=null;
		
		String filename = AppParams.PATH_STORAGE + "voter" + voterID + ".info";
		out("private keys filename = " + filename);
		serialized = AppUtils.readFromFile(filename);
		
		byte[] idMsg =  MessageTools.first(serialized);
		int idFromMsg = MessageTools.byteArrayToInt(idMsg);
		if ( idFromMsg != voterID ) {
			out("Something wrong with identifiers");
			System.exit(-1);
		}
		byte[] decr_sig = MessageTools.second(serialized);
		byte[] decryptorMsg = MessageTools.first(decr_sig);
		byte[] signerMsg = MessageTools.second(decr_sig);		
		user_decr = Decryptor.fromBytes(decryptorMsg);
		user_sign = Signer.fromBytes(signerMsg);
		
		// Initialize the interface to the public key infrastructure:
		System.setProperty("remotemode", Boolean.toString(true));
		PKI.useRemoteMode();
		
		// Verify that the verifier stored in the file is the same as the one in the PKI:
		Verifier myVerif = RegisterSig.getVerifier(voterID, Params.SIG_DOMAIN);
		if ( !MessageTools.equal(myVerif.getVerifKey(), user_sign.getVerifier().getVerifKey()) ) {
			out("Something wrong with the keys");
			System.exit(-1);
		}
		
		// Create the voter:
		out("Creating a voter object.");
		Voter voter = new Voter(voterID, AppParams.electionID, user_decr, user_sign);
	}
	
	private void destroyClient(){
		user_decr=null;
		user_sign=null;
		client=null;
	}
	
	private static byte[] readFromFile(String path) throws IOException {
		FileInputStream f = new FileInputStream(path);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while (f.available() > 0){			
			bos.write(f.read());
		}
		f.close();
		byte[] data = bos.toByteArray();
		return data;
	}
	
	private static void out(String s) {
		System.out.println(s);
	}
}

