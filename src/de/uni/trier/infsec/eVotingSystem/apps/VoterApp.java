package de.uni.trier.infsec.eVotingSystem.apps;


import java.awt.EventQueue;

import javax.swing.AbstractAction;
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
import de.uni.trier.infsec.lib.network.NetworkClient;
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
	
	private JLabel lblCandidateSelected = new JLabel("");
	private JLabel lblRetrieveStatus = new JLabel("");
	private JLabel lblWait = new JLabel("");
	private JLabel lblVoterID = new JLabel("");
	private JLabel lblElectionID = new JLabel("");
	private JLabel lblElectionMsg = new JLabel("");
	
	private JButton[] btnCandidates = new JButton[1];
	private JButton btnVote = new JButton();
	private VoteButton voteButtonAction;
	/*
	 * CORE FIELD
	 */
	private int voterID;
	private Voter voter;
	private Decryptor user_decr;
	private Signer user_sign;
	private byte[] serverResponse=null;
	private static ElectionMetadata electionData;
	//private static final int STORE_ATTEMPTS = 3; 
	// attempts to store a msg under a label in such a way that server and client counters are aligned

	private int selectedCandidate = -100;
	
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
		setBounds(100, 100, 530, 600);
				// (530,334);
		
		outl("Getting election metadata...");
		electionData=getElectionData(AppParams.ELECTIONID);
		out("OK");
		
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
		
		JPanel votePanel = new JPanel();
		
		
		lblElectionMsg = new JLabel("Election Message");
		lblElectionMsg.setFont(new Font("Dialog", Font.BOLD, 16));
		lblCandidateSelected = new JLabel();
		lblCandidateSelected.setHorizontalAlignment(SwingConstants.RIGHT);
		lblCandidateSelected.setFont(new Font("Dialog", Font.PLAIN, 14));
		
		ballot = new JPanel();
		
		JScrollPane ScrollBallot = new JScrollPane(ballot);
		//Border paneEdge = BorderFactory.createEmptyBorder(0,10,10,10);
		//Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		Border blackline = BorderFactory.createLineBorder(Color.black);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(blackline, "ballot");
        titledBorder.setTitleJustification(TitledBorder.RIGHT);
        titledBorder.setTitlePosition(TitledBorder.DEFAULT_POSITION);
        ScrollBallot.setBorder(titledBorder);
        
        
        // CREATE N BUTTONS AS NUMBER OF CANDIDATES
        int nCandidates=electionData.candidatesArray.length;
        ballot.setLayout(new GridLayout(nCandidates,1));
        btnCandidates = new JButton[nCandidates];
        
        for(int i=0;i<nCandidates; i++){
        	btnCandidates[i] = new JButton(electionData.candidatesArray[i]);
        	btnCandidates[i].setFont(new Font("Dialog", Font.BOLD, 14));
        	btnCandidates[i].setForeground(Color.BLACK);
        	//btnCandidates[i].setContentAreaFilled(false);
        	//btnCandidates[i].setOpaque(true);
        	/*
        	 * SELECT A CANDIDATE
        	 */
        	btnCandidates[i].addActionListener(new CandidateSelected());
        	ballot.add(btnCandidates[i]);
        }
        
        voteButtonAction = new VoteButton("Vote");
        btnVote = new JButton(voteButtonAction);
		btnVote.setFont(new Font("Dialog", Font.BOLD, 18));
		btnVote.setEnabled(false);
		/*
		 * VOTE!
		 */
		//btnVote.addActionListener(new Vote(selectedCandidate));
		
		
		GroupLayout gl_votePanel = new GroupLayout(votePanel);
		gl_votePanel.setHorizontalGroup(
			gl_votePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_votePanel.createSequentialGroup()
					.addGap(24)
					.addGroup(gl_votePanel.createParallelGroup(Alignment.LEADING, false)
						.addComponent(lblElectionMsg, GroupLayout.PREFERRED_SIZE, 453, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_votePanel.createSequentialGroup()
							.addComponent(lblCandidateSelected, GroupLayout.PREFERRED_SIZE, 313, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(btnVote, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE))
						.addComponent(ScrollBallot, GroupLayout.PREFERRED_SIZE, 475, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		gl_votePanel.setVerticalGroup(
			gl_votePanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_votePanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblElectionMsg, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(ScrollBallot, GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_votePanel.createParallelGroup(Alignment.LEADING, false)
						.addComponent(btnVote, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblCandidateSelected, GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
					.addGap(22))
		);
		votePanel.setLayout(gl_votePanel);
		
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
		
		center.add(votePanel, STORE);
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
			
			lblUserNotRegister.setText("");
			lblCandidateSelected.setText("");
			//labelField.setText("");
//			assert(btnCandidates!=null);
			for(int i=0;i<btnCandidates.length;i++)
				btnCandidates[i].setForeground(Color.BLACK);
			//lblStoreStatus.setText("");
			//lblRetrieveStatus.setText("");
			//textMsgRetrieved.setText("");
			
			//comboBox.setSelectedIndex(0); // set the combo box to Store!
			
			CardLayout centerCl = (CardLayout) center.getLayout();
			centerCl.show(center, STORE);
			
			CardLayout cl = (CardLayout) getContentPane().getLayout();
			cl.show(getContentPane(), "1");
			
			destroyConfidentialInfo();
			setTitle(AppParams.APPNAME);
		}
	}
	private void destroyConfidentialInfo(){
		selectedCandidate=-100;
		
		user_decr=null;
		user_sign=null;
		voter=null;
	}
	
	private class CandidateSelectedButton implements ActionListener{
		public void actionPerformed(ActionEvent ev){
			JButton btnSelected= (JButton) ev.getSource();
			btnSelected.setForeground(Color.RED);
			//FIXME: it does not work
					// btnSelected.setFont(new Font("Dialog", Font.BOLD, 30));
			btnSelected.repaint();
			// btnCandidates[candidateNumber].setBackground(Color.GREEN);
			for(int i=0;i<btnCandidates.length;i++)
				if(btnCandidates[i].equals(btnSelected)){
					selectedCandidate=i;
				} else{
					btnCandidates[i].setForeground(Color.BLACK);
					//FIXME: it does not work 
							// btnSelected.setFont(new Font("Dialog", Font.BOLD, 14));
							//	btnCandidates[i].repaint();
				}
			voteButtonAction.setCandidateNumber(selectedCandidate);
			btnVote.setEnabled(true);
			lblCandidateSelected.setText("<html>Your Candidate:<br>&nbsp;&nbsp;&nbsp;&nbsp;<font face=\"Dialog\" size=10  color=\"red\"><b>" +
					 electionData.candidatesArray[selectedCandidate] + "</b></font></html>");
			
		}
	}
	
	private class VoteButton extends AbstractAction {
		int candidateNumber=-200;
		public VoteButton(String name){
			super(name);
		}
		// Remember to leave button.setEnable(false) before set the candidate for the first time
		public void setCandidateNumber(int candiateNumber){
			this.candidateNumber=candidateNumber;
			if(candidateNumber<0)
				throw new NumberFormatException();
		}
		public void actionPerformed(ActionEvent ev){
			
			// Create a ballot;
			//TODO: comment this line and uncomment the other after testing
			outl("Creating a ballot with candidate number " + candidateNumber + "...");
			//outl("Creating the ballot...");
			byte[] ballot = voter.createBallot(candidateNumber);
			
			// Send the ballot:
			out("Sending the ballot to the server");
			try {
				serverResponse = 
						NetworkClient.sendRequest(ballot, AppParams.SERVER1_NAME, AppParams.SERVER1_PORT);
			} catch (NetworkError e) {
				// TODO Auto-generated catch block
				System.out.println("Vote(candidate <int>): networkError");
				e.printStackTrace();
			}
			//CardLayout cl = (CardLayout)(center.getLayout());
			//cl.show(center, RETRIEVE);
			//JPanel votePanel = (JPanel) ((JButton) ev.getSource()).getParent();
			//storePanel.paintImmediately(storePanel.getVisibleRect()); //FIXME: it would be better to use a separate Thread 
		}					
	}
	
	
//	private class Vote implements ActionListener {
//		int candidateNumber=-200;
//		public Vote(int candidateNumber){
//			this.candidateNumber=candidateNumber;
//		}
//		public void actionPerformed(ActionEvent ev){
//			
//			// Create a ballot;
//			//TODO: comment this line and uncomment the other after testing
//			outl("Creating a ballot with candidate number " + candidateNumber + "...");
//			//outl("Creating the ballot...");
//			byte[] ballot = voter.createBallot(candidateNumber);
//			
//			// Send the ballot:
//			out("Sending the ballot to the server");
//			try {
//				serverResponse = 
//						NetworkClient.sendRequest(ballot, AppParams.SERVER1_NAME, AppParams.SERVER1_PORT);
//			} catch (NetworkError e) {
//				// TODO Auto-generated catch block
//				System.out.println("Vote(candidate <int>): networkError");
//				e.printStackTrace();
//			}
//			//CardLayout cl = (CardLayout)(center.getLayout());
//			//cl.show(center, RETRIEVE);
//			//JPanel votePanel = (JPanel) ((JButton) ev.getSource()).getParent();
//			//storePanel.paintImmediately(storePanel.getVisibleRect()); //FIXME: it would be better to use a separate Thread 
//		}					
//	}

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
		return new ElectionMetadata(electionID, AppParams.ELECTIONMSG, AppParams.CANDIDATESARRAY);
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
		Voter voter = new Voter(voterID, AppParams.ELECTIONID, user_decr, user_sign);
		out("OK");
	}

	
	// UTILS methods
	
	private static void outl(String s){
		System.out.print(s);
	}
	private static void out(String s) {
		System.out.println(s);
	}
}

