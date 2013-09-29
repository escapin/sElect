package de.uni.trier.infsec.eVotingSystem.apps;


import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.BorderFactory; 

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
	private JPanel ballot;
	private JTextArea textMsgRetrieved;
	private JLabel lblStoreStatus;
	private JLabel lblRetrieveStatus;
	
	private JLabel lblWait;
	private JLabel lblVoterID;
	private JLabel lblElectionID;
	private JLabel lblElectionMsg;
	/*
	 * CORE FIELD
	 */
	private int voterID;
	private Decryptor user_decr;
	private Signer user_sign;
	private Voter client;
	private ElectionMetadata electionData;
	//private static final int STORE_ATTEMPTS = 3; 
	// attempts to store a msg under a label in such a way that server and client counters are aligned
	
	// UTILS FIELDS
	private final String msgBefVoterID = "Your Identifier Number: ";
	
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
		setTitle(AppParams.APPNAME);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 530, 580);
		
		
		CardLayout cl = new CardLayout();
		getContentPane().setLayout(cl);
		
		
		// login Panel
		JPanel login = new JPanel();
		JButton btnLogIn = new JButton("Log In");
		
		JLabel lblUserId = new JLabel("Insert your Identifier Number: ");
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
			gl_login.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_login.createSequentialGroup()
					.addContainerGap(336, Short.MAX_VALUE)
					.addComponent(lblUserId, GroupLayout.PREFERRED_SIZE, 261, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(textField, GroupLayout.PREFERRED_SIZE, 114, GroupLayout.PREFERRED_SIZE)
					.addGap(172))
				.addGroup(gl_login.createSequentialGroup()
					.addGap(47)
					.addComponent(lblUserNotRegister, GroupLayout.PREFERRED_SIZE, 368, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(492, Short.MAX_VALUE))
				.addGroup(gl_login.createSequentialGroup()
					.addGap(312)
					.addComponent(lblWait, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnLogIn, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(421, Short.MAX_VALUE))
		);
		gl_login.setVerticalGroup(
			gl_login.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_login.createSequentialGroup()
					.addContainerGap(93, Short.MAX_VALUE)
					.addGroup(gl_login.createParallelGroup(Alignment.TRAILING)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblUserId, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
					.addGap(18)
					.addComponent(lblUserNotRegister, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
					.addGroup(gl_login.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_login.createSequentialGroup()
							.addGap(42)
							.addComponent(btnLogIn, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_login.createSequentialGroup()
							.addGap(53)
							.addComponent(lblWait)))
					.addGap(279))
		);
		login.setLayout(gl_login);
		
		/*
		 * LOGIN!
		 */
		btnLogIn.addActionListener(new Login());
		
		
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
		lblVoterID = new JLabel(msgBefVoterID);
		
		lblElectionID = new JLabel("");
		//labelField.setColumns(10);
		GroupLayout gl_north = new GroupLayout(north);
		gl_north.setHorizontalGroup(
			gl_north.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_north.createSequentialGroup()
					.addGap(21)
					.addComponent(lblVoterID)
					.addContainerGap(314, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, gl_north.createSequentialGroup()
					.addContainerGap(348, Short.MAX_VALUE)
					.addComponent(lblElectionID, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE)
					.addGap(20))
		);
		gl_north.setVerticalGroup(
			gl_north.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_north.createSequentialGroup()
					.addContainerGap(29, Short.MAX_VALUE)
					.addComponent(lblVoterID)
					.addGap(20))
				.addGroup(Alignment.LEADING, gl_north.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblElectionID)
					.addContainerGap(37, Short.MAX_VALUE))
		);
		north.setLayout(gl_north);
		
		
		// Center panel
		center = new JPanel();
		main.add(center, BorderLayout.CENTER);
		CardLayout clCenter = new CardLayout(0, 0);
		center.setLayout(clCenter);
		
		JPanel votePannel = new JPanel();
		
		
		lblElectionMsg = new JLabel("Election Message");
		lblElectionMsg.setFont(new Font("Dialog", Font.BOLD, 16));
		lblStoreStatus = new JLabel();
		lblStoreStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		lblStoreStatus.setFont(new Font("Dialog", Font.PLAIN, 14));
		
		ballot = new JPanel();
		BoxLayout boxCandidate = new BoxLayout(ballot, BoxLayout.Y_AXIS);
		ballot.setLayout(boxCandidate);
		JScrollPane ScrollBallot = new JScrollPane(ballot);
		
        
		//Border paneEdge = BorderFactory.createEmptyBorder(0,10,10,10);
		//Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border blackline = BorderFactory.createLineBorder(Color.black);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(blackline, "ballot");
        titledBorder.setTitleJustification(TitledBorder.RIGHT);
        titledBorder.setTitlePosition(TitledBorder.DEFAULT_POSITION);
        ScrollBallot.setBorder(titledBorder);
        
        
        
        JButton btnCandidate = new JButton("Candidate 01");
        ballot.add(btnCandidate);
        
        JButton btnVote = new JButton("Vote");
		btnVote.setFont(new Font("Dialog", Font.BOLD, 18));
		
		/*
		 * VOTE!
		 */
		btnVote.addActionListener(new Vote());
		
		
		GroupLayout gl_votePannel = new GroupLayout(votePannel);
		gl_votePannel.setHorizontalGroup(
			gl_votePannel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_votePannel.createSequentialGroup()
					.addGap(24)
					.addGroup(gl_votePannel.createParallelGroup(Alignment.LEADING, false)
						.addComponent(lblElectionMsg, GroupLayout.PREFERRED_SIZE, 453, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_votePannel.createSequentialGroup()
							.addComponent(lblStoreStatus, GroupLayout.PREFERRED_SIZE, 313, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnVote, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE))
						.addComponent(ScrollBallot, GroupLayout.PREFERRED_SIZE, 475, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		gl_votePannel.setVerticalGroup(
			gl_votePannel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_votePannel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblElectionMsg, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(ScrollBallot, GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_votePannel.createParallelGroup(Alignment.LEADING, false)
						.addComponent(btnVote, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblStoreStatus, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
					.addGap(22))
		);
		votePannel.setLayout(gl_votePannel);
		
		JPanel retrievePanel = new JPanel();
		
		lblRetrieveStatus = new JLabel();
		lblRetrieveStatus.setHorizontalAlignment(SwingConstants.RIGHT);
		lblRetrieveStatus.setFont(new Font("Dialog", Font.PLAIN, 14));
		
		JLabel lblMessageRetrieved = new JLabel("Message Retrieved:");
		
		JButton btnRetrieve = new JButton("Retrieve");
		// FIXME: the button is moving when the 'lblRetrieveStatus' label changes!
		btnRetrieve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
/*				lblRetrieveStatus.setForeground(Color.BLACK);
				if(labelField.getText().length()==0){
					lblRetrieveStatus.setText("Label empty!");
					return;
				}
				
				textMsgRetrieved.setText("");
				lblRetrieveStatus.setForeground(Color.BLACK);
				lblRetrieveStatus.setText("Wait...");	
				
				JPanel retrievePanel = (JPanel) ((JButton) ev.getSource()).getParent();
				retrievePanel.paintImmediately(retrievePanel.getVisibleRect()); // it would be better to use a separate Thread 
				
				byte[] msg=null;
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
		
		center.add(votePannel, STORE);
		center.add(retrievePanel, RETRIEVE);
		
		// South panel
		JPanel south = new JPanel();
		main.add(south, BorderLayout.SOUTH);
		
		JButton btnLogOut = new JButton("Log Out");
		
		btnLogOut.addActionListener(new Logout());
		
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
	 * LISTENERS
	 */

	private class Login implements ActionListener {
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
				outl("Getting election metadata...");
				electionData=getElectionData(AppParams.electionID);
				out("OK");
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
				//setTitle("Voter " + voterID + " - " + AppParams.APPNAME);
				CardLayout cl = (CardLayout) getContentPane().getLayout();
				cl.show(getContentPane(), "2");
			}
			lblVoterID.setText("<html>" +  msgBefVoterID + "<strong>" + voterID + "</strong></html>");
			lblElectionID.setText(new String(electionData.id));
			lblElectionMsg.setText("<html>" +  electionData.introMsg + "</html>");
		}
	}
	
	private class Logout implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			destroyClient();
			
			lblUserNotRegister.setText("");
			//labelField.setText("");
			//lblStoreStatus.setText("");
			//lblRetrieveStatus.setText("");
			//textMsgRetrieved.setText("");
			
			//comboBox.setSelectedIndex(0); // set the combo box to Store!
			
			CardLayout centerCl = (CardLayout) center.getLayout();
			centerCl.show(center, STORE);
			
			CardLayout cl = (CardLayout) getContentPane().getLayout();
			cl.show(getContentPane(), "1");
			
			setTitle(AppParams.APPNAME);
		}
	}
	
	private class Vote implements ActionListener {
		public void actionPerformed(ActionEvent ev){
			/*lblStoreStatus.setForeground(Color.BLACK);
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
			
			boolean correctlyStored=false;
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
	}

	private class ElectionMetadata{
		//TODO: add all the other election metadata
		//TODO: refactor the code according to another entity such as the electionAdministrator
		public byte[] id;
		public String introMsg;
		public String[] candidatesArray;
		
		public ElectionMetadata (byte[] id, String introMsg, String[] candidatesArray){
			this.id=id;
			this.introMsg=introMsg;
			this.candidatesArray=candidatesArray;
		}
	}
	
	
	
	private ElectionMetadata getElectionData(byte[] electionID){
		return new ElectionMetadata(electionID, AppParams.electionMsg, AppParams.CANDIDATESARRAY);
	}
	
	
	
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
		outl("Setting up the voter...");
		Voter voter = new Voter(voterID, AppParams.electionID, user_decr, user_sign);
		out("OK");
	}
	
	private void destroyClient(){
		user_decr=null;
		user_sign=null;
		client=null;
	}

	
	// UTILS methods
	
	private static void outl(String s){
		System.out.print(s);
	}
	private static void out(String s) {
		System.out.println(s);
	}
}

