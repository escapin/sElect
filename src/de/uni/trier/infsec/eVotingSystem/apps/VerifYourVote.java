package de.uni.trier.infsec.eVotingSystem.apps;


import static de.uni.trier.infsec.utils.MessageTools.concatenate;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.UIManager;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;
import java.awt.BorderLayout;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;
import java.awt.Color;



import javax.swing.SwingConstants;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.coreSystem.Params;
import de.uni.trier.infsec.eVotingSystem.coreSystem.Utils;
import de.uni.trier.infsec.eVotingSystem.coreSystem.Voter;
import de.uni.trier.infsec.eVotingSystem.coreSystem.Utils.MessageSplitIter;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkisig.*;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.utils.Utilities;
import de.uni.trier.infsec.lib.network.NetworkError;
import javax.swing.JPasswordField;





public class VerifYourVote extends JFrame {
	
	private static final long serialVersionUID = 1L;
	
	private JTextField fldVoterID;
	private JPasswordField fldPassword;
	private JPanel center;
	private final static String LOGIN = "LOGIN";
	private final static String MAIN = "MAIN";
	private final static String VERIF_ok="ACCEPTED";
    private final static String VERIF_fail = "REJECTED";
  
	
	private JLabel lblRejectedReason = new JLabel("Rejected Reason");
	private JLabel lblUserNotRegister = new JLabel("New label");
	private JLabel lblWait = new JLabel("Wait...");
	private JLabel lblVoterID = new JLabel("VoterID");
	private JLabel lblElectionID = new JLabel("ElectionID");
	
	private JLabel lblTheVote=new JLabel("The Vote");;
	private JLabel lblTheReceipt =new JLabel("The Receipt");;
	
	

	/*
	 * CORE FIELD
	 */
	private int voterID;
	private static Verifier server1ver = null;
	private static Verifier server2ver = null;
	private Voter.Receipt receipt;
	private byte[] signedPartialResult=null, 
					signedFinalResult=null;
	
	
	// UTILS FIELDS
		// login
	private final String lblCREDENTIALS = "Please enter";
	private final String lblVOTERID = "Your Voter ID: ";
	private final String lblPASSWORD = "Your Password: ";
	private final String lblLOGIN = "Verify your Vote";
		// main
	private final String lblACCEPTED = "<font color=\"green\"> Your vote has been counted correctly!</font>";
	private final String lblREJECTED = "<font color=\"red\"> Your Vote has not been counted correctly!</font>";
	private final String lblRECEIPTID = "Your Receipt ID: ";
	private final String lblYOURVOTE = "Your Vote: ";
	
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
					VerifYourVote frame = new VerifYourVote();
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
	public VerifYourVote() {
		//setIconImage(Toolkit.getDefaultToolkit().getImage(UserGUI.class.getResource("/de/uni/trier/infsec/cloudStorage/cloud.png")));
		setTitle(AppParams.VERIFYAPPNAME);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 530,334);
		
		CardLayout cl = new CardLayout();
		getContentPane().setLayout(cl);
		
		
		// login Panel
		JPanel login = new JPanel();
		JButton btnVerify = new JButton(lblLOGIN);
		
		JLabel lblVoterId = new JLabel(lblVOTERID);
		fldVoterID = new JTextField();
		fldVoterID.setColumns(10);
		lblUserNotRegister.setVerticalAlignment(SwingConstants.TOP);
		lblUserNotRegister.setHorizontalAlignment(SwingConstants.LEFT);
		
		lblUserNotRegister.setText("");
		
		JLabel lblCredentials = new JLabel(lblCREDENTIALS);
		
		JLabel lblPassword = new JLabel(lblPASSWORD);
		
		fldPassword = new JPasswordField();
		GroupLayout gl_login = new GroupLayout(login);
		gl_login.setHorizontalGroup(
			gl_login.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_login.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblCredentials)
					.addContainerGap(427, Short.MAX_VALUE))
				.addGroup(gl_login.createSequentialGroup()
					.addGap(30)
					.addGroup(gl_login.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_login.createParallelGroup(Alignment.LEADING)
							.addComponent(lblPassword)
							.addComponent(lblVoterId, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE))
						.addComponent(lblUserNotRegister, GroupLayout.PREFERRED_SIZE, 203, GroupLayout.PREFERRED_SIZE))
					.addGroup(gl_login.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_login.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
							.addComponent(btnVerify)
							.addGap(77))
						.addGroup(gl_login.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_login.createParallelGroup(Alignment.TRAILING, false)
								.addComponent(fldPassword, Alignment.LEADING)
								.addComponent(fldVoterID, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
							.addGap(171))))
		);
		gl_login.setVerticalGroup(
			gl_login.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_login.createSequentialGroup()
					.addGap(58)
					.addGroup(gl_login.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_login.createSequentialGroup()
							.addComponent(lblCredentials)
							.addGap(18)
							.addComponent(lblVoterId, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))
						.addComponent(fldVoterID, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(30)
					.addGroup(gl_login.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblPassword)
						.addComponent(fldPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(60)
					.addGroup(gl_login.createParallelGroup(Alignment.LEADING)
						.addComponent(btnVerify, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblUserNotRegister, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE))
					.addGap(238))
		);
		login.setLayout(gl_login);
		
		/*
		 * LOGIN!
		 */
		btnVerify.addActionListener(new Verify());
		
		
		// main windows panel
		JPanel main = new JPanel();
		
		// add the two layout to the main
		getContentPane().add(login, LOGIN);
		getContentPane().add(main, MAIN);
		main.setLayout(new BorderLayout(0, 0));
		// set the default option
		cl.show(getContentPane(), LOGIN);
		
		// North panel
		JPanel north = new JPanel();
		main.add(north, BorderLayout.NORTH);
		lblVoterID.setText(lblVOTERID);
		
		lblElectionID.setText("");
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
		
		
		JPanel verifOkPanel = new JPanel();
		
		JLabel lblVoteCorrect = new JLabel(html(lblACCEPTED));
		lblVoteCorrect.setHorizontalAlignment(SwingConstants.CENTER);
		lblVoteCorrect.setFont(new Font("Dialog", Font.BOLD, 18));
		
		JLabel lblYourVote = new JLabel(lblYOURVOTE);
		lblYourVote.setHorizontalAlignment(SwingConstants.LEFT);
		lblYourVote.setFont(new Font("Dialog", Font.PLAIN, 14));
		
		JLabel lblYourReceiptID = new JLabel(lblRECEIPTID);
		lblYourReceiptID.setHorizontalAlignment(SwingConstants.LEFT);
		lblYourReceiptID.setFont(new Font("Dialog", Font.PLAIN, 14));
		
		lblTheVote.setText("");
		lblTheVote.setVerticalAlignment(SwingConstants.TOP);
		lblTheVote.setHorizontalAlignment(SwingConstants.LEFT);
		
		lblTheReceipt.setText(""); 
		lblTheReceipt.setVerticalAlignment(SwingConstants.TOP);
		lblTheReceipt.setHorizontalAlignment(SwingConstants.LEFT);
		
		GroupLayout gl_verifOkPanel = new GroupLayout(verifOkPanel);
		gl_verifOkPanel.setHorizontalGroup(
			gl_verifOkPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_verifOkPanel.createSequentialGroup()
					.addGroup(gl_verifOkPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_verifOkPanel.createSequentialGroup()
							.addGap(72)
							.addGroup(gl_verifOkPanel.createParallelGroup(Alignment.LEADING, false)
								.addGroup(gl_verifOkPanel.createSequentialGroup()
									.addComponent(lblYourReceiptID, GroupLayout.PREFERRED_SIZE, 131, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(lblTheReceipt, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addGroup(gl_verifOkPanel.createSequentialGroup()
									.addComponent(lblYourVote, GroupLayout.PREFERRED_SIZE, 131, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.UNRELATED)
									.addComponent(lblTheVote, GroupLayout.PREFERRED_SIZE, 256, GroupLayout.PREFERRED_SIZE))))
						.addGroup(gl_verifOkPanel.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblVoteCorrect, GroupLayout.PREFERRED_SIZE, 518, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		gl_verifOkPanel.setVerticalGroup(
			gl_verifOkPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_verifOkPanel.createSequentialGroup()
					.addGap(40)
					.addGroup(gl_verifOkPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblYourVote)
						.addComponent(lblTheVote, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(gl_verifOkPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblYourReceiptID, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblTheReceipt, GroupLayout.PREFERRED_SIZE, 69, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblVoteCorrect, GroupLayout.PREFERRED_SIZE, 49, GroupLayout.PREFERRED_SIZE)
					.addGap(103))
		);
		
		verifOkPanel.setLayout(gl_verifOkPanel);
		
		JPanel verifFailPanel = new JPanel();
		
		JLabel lblRejected=new JLabel(html(lblREJECTED));
		lblRejected.setFont(new Font("Dialog", Font.PLAIN, 14));
		
		
		
		//center.add(votePanel, VOTE);
		center.add(verifOkPanel, VERIF_ok);
		center.add(verifFailPanel, VERIF_fail);
		GroupLayout gl_verifFailPanel = new GroupLayout(verifFailPanel);
		gl_verifFailPanel.setHorizontalGroup(
			gl_verifFailPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_verifFailPanel.createSequentialGroup()
					.addGap(86)
					.addComponent(lblRejected, GroupLayout.PREFERRED_SIZE, 356, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(45, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, gl_verifFailPanel.createSequentialGroup()
					.addContainerGap(49, Short.MAX_VALUE)
					.addComponent(lblRejectedReason, GroupLayout.PREFERRED_SIZE, 436, GroupLayout.PREFERRED_SIZE)
					.addGap(45))
		);
		gl_verifFailPanel.setVerticalGroup(
			gl_verifFailPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_verifFailPanel.createSequentialGroup()
					.addGap(33)
					.addComponent(lblRejected)
					.addPreferredGap(ComponentPlacement.RELATED, 100, Short.MAX_VALUE)
					.addComponent(lblRejectedReason, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
					.addGap(43))
		);
		verifFailPanel.setLayout(gl_verifFailPanel);
		
		
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
	private class Verify implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			lblUserNotRegister.setForeground(Color.RED);
			// fetch the verifiers of the servers
			if(fldVoterID.getText().length()==0){
				lblUserNotRegister.setText("<html>User ID field empty!<br>Please insert a valid ID number of a previously registered user.</html>");
				return;
			}
			try{
				voterID = Integer.parseInt(fldVoterID.getText());
				if(voterID<0)
					throw new NumberFormatException();
			} catch (NumberFormatException e){
				System.out.println("'" + fldVoterID.getText() + "' is not a proper userID!\nPlease insert the ID number of a previously registered user.");
				lblUserNotRegister.setText("<html>'" + fldVoterID.getText() + "' is not a proper userID!<br>Please insert the ID number of a registered user.</html>");
				return;
			}
			PKI.useRemoteMode();
			try {
				server1ver = RegisterSig.getVerifier(Params.SERVER1ID, Params.SIG_DOMAIN);
				server2ver = RegisterSig.getVerifier(Params.SERVER2ID, Params.SIG_DOMAIN);
			} catch (RegisterSig.PKIError e){
				System.out.println("PKI Error occurred: perhaps the PKI server is not running!");
				lblUserNotRegister.setText(html("PKI Error:<br> perhaps the PKI server is not running!"));
				return;
			} catch (NetworkError e){
				//FIXME: java.net.ConnectException when the PKIServer is not running!
				System.out.println("Network Error occurred while connecting with the PKI server: perhaps the PKI server is not running!");
				lblUserNotRegister.setText(html("Network Error occurred:<br> perhaps the PKI server is not running!"));
				return;
			}
			
			
			lblWait.setText("Wait..."); //FIXME: it doesn't work!
			
			JPanel loginPanel = (JPanel) ((JButton)ev.getSource()).getParent();
			//lblWait.paintImmediately(loginPanel.getVisibleRect());
			loginPanel.paintImmediately(loginPanel.getVisibleRect());
			
			boolean fileLoaded=false;
			byte[] receiptMsg=null;
			try {
				try{
					receiptMsg=AppUtils.readFromFile(AppParams.RECEIPT_file +  voterID + ".msg");
				} catch (FileNotFoundException e){
					out("Voter " + voterID + " have not voted. No receipt found: \n\t" + e.getMessage());
					// out("User " + voterID + " not registered!\nType \'UserRegisterApp <user_id [int]>\' in a terminal to register him/her.");
					lblUserNotRegister.setText(html("You have not voted for this election."));
					return;
				}
				try{
					signedPartialResult=AppUtils.readFromFile(AppParams.COLL_SERVER_RESULT_file);
					signedFinalResult=AppUtils.readFromFile(AppParams.FIN_SERVER_RESULT_file);
				} catch (FileNotFoundException e){
					out("Can not read one of the files:\n\t" + e.getMessage());
					// out("User " + voterID + " not registered!\nType \'UserRegisterApp <user_id [int]>\' in a terminal to register him/her.");
					lblUserNotRegister.setText(html("You can not verify your vote until the election phase is over."));
					return;
				}
				fileLoaded=true;
			} catch (IOException e){
				out("IOException occurred while reading the credentials of the user!");
				lblUserNotRegister.setText("IOException occurred while reading the credentials of the user!");
				return;
			} finally{
				lblWait.setText("");
			}
			
			lblUserNotRegister.setText("");
			if(fileLoaded){
				fldVoterID.setText("");
				
				receipt = Voter.Receipt.fromMessage(receiptMsg);
				
				// TODO: verify the signature in the receipt
				if (!signatureInReceiptOK(receipt)) {
						out("\nPROBLEM: Server's signature in your receipt is not correct!");
						out("You may not be able to prove that you are cheated on, if you are.");
				}
				
				// Print out (part of) the receipt:
				out("\nRECEIPT:");
				out("    election ID  = " + new String(receipt.electionID) );
				out("    candidate number   = " + receipt.candidateNumber );
				out("    nonce        = " + Utilities.byteArrayToHexString(receipt.nonce));
				
				boolean ok = true;
				// Check whether the partial results contains the inner ballot from the receipt:
				if (!partialResultOK(receipt, signedPartialResult))
					ok = false;
				
				// Check whether the final result contains your nonce and print out the vote:
				if (ok && !finalResultOK(receipt, signedFinalResult))
					ok = false;
				
				if (ok){ 
					lblTheVote.setText(html(AppParams.CANDIDATESARRAY[receipt.candidateNumber]));
					lblTheReceipt.setText(html(Utilities.byteArrayToHexString(receipt.nonce)));
					
					CardLayout centerCl = (CardLayout) center.getLayout();
					centerCl.show(center, VERIF_ok);
					out("\nEverything seems ok.");
				}
				else{
					CardLayout centerCl = (CardLayout) center.getLayout();
					centerCl.show(center, VERIF_fail);
				}
				CardLayout cl = (CardLayout) getContentPane().getLayout();
				cl.show(getContentPane(), MAIN);
						
			}
			lblVoterID.setText("<html>" +  lblVOTERID + "<strong>" + voterID + "</strong></html>");
			lblElectionID.setText(new String(receipt.electionID));
		}
	}
	
	
	private boolean signatureInReceiptOK(Voter.Receipt receipt){
		byte[] expected_signed_msg = concatenate(Params.ACCEPTED, concatenate(receipt.electionID, receipt.innerBallot));
		return server1ver.verify(receipt.serverSignature, expected_signed_msg);
	}
	
	private boolean partialResultOK(Voter.Receipt receipt, byte[] signedPartialResult) {
		byte[] result = MessageTools.first(signedPartialResult);
		byte[] signature = MessageTools.second(signedPartialResult);
		
		String err="";
		out("");
		// check the signature
		if (!server1ver.verify(signature, result)) {
			err="PROBLEM: Invalid signature on the " + bold("partial result") + ".";
			out(err);
			lblRejectedReason.setText(html(err));
			return false;
		}
		
		// check the election id
		byte[] elid = MessageTools.first(result);
		if (!MessageTools.equal(elid, receipt.electionID)) {
			err="PROBLEM: The election ID in the receipt does not match the one in the " + bold("partial result") + ".";
			out(err);
			lblRejectedReason.setText(html(err));
			return false;
		}
		
		// check if the result contain the inner ballot from the receipt
		byte[] ballotsAsMessage = MessageTools.first(MessageTools.second(result));
		if (!Utils.contains(ballotsAsMessage, receipt.innerBallot)) {
			err="PROBLEM: The " + bold("partial result") + " does not containt your inner ballot!";
			out(err);
			out(Utilities.byteArrayToHexString(receipt.innerBallot));
			lblRejectedReason.setText(html(err));
			return false;
		}
		
		return true;
	}
	
	private boolean finalResultOK(Voter.Receipt receipt, byte[] signedFinalResult) {
		byte[] result = MessageTools.first(signedFinalResult);
		byte[] signature = MessageTools.second(signedFinalResult);
		
		String err="";
		out("");
		// check the signature
		if (!server2ver.verify(signature, result)) {
			err="PROBLEM: Invalid signature on the " + bold("final result") + ".";
			out(err);
			lblRejectedReason.setText(html(err));
			return false;
		}
		
		// check the election id
		byte[] elid = MessageTools.first(result);
		if (!MessageTools.equal(elid, receipt.electionID)) {
			err="PROBLEM: The election ID in the receipt does not match the one in the " + bold("final result") + ".";
			out(err);
			lblRejectedReason.setText(html(err));
			return false;
		}
		
		byte[] entriesAsMessage = MessageTools.second(result);
		
		// look up for our nonce:
		int candidateNumber=0;
		byte[] vote = null;
		for( MessageSplitIter iter = new MessageSplitIter(entriesAsMessage); vote==null && iter.notEmpty(); iter.next() ) { 
			if (MessageTools.equal(MessageTools.first(iter.current()), receipt.nonce)) // nonce found
				vote = MessageTools.second(iter.current());
		}
		candidateNumber=MessageTools.byteArrayToInt(vote);
		if (vote == null) {
			err="PROBLEM: The " + bold("final result") + " does not containt your nonce!";
			out(err);
			out(Utilities.byteArrayToHexString(receipt.nonce));
			lblRejectedReason.setText(html(err));
			return false;
		}
		else if (candidateNumber!=receipt.candidateNumber) {
			err="PROBLEM: In the " + bold("final result") + ", the vote next to your nonce is not your vote!";
			out(err);
			out("Found candidate number: " + candidateNumber);
			lblRejectedReason.setText(html(err));
			return false;
		}
		else {
			out("Your nonce is in the result along with next the number of the candidate you voted:");
			out("" + AppParams.CANDIDATESARRAY[candidateNumber]);
		}
		
		return true;
	}
	
	
	private class Logout implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			
			lblUserNotRegister.setText("");
			lblRejectedReason.setText("");
			lblTheVote.setText("");
			lblTheReceipt.setText("");
			
			CardLayout centerCl = (CardLayout) center.getLayout();
			centerCl.show(center, VERIF_ok);
			
			CardLayout cl = (CardLayout) getContentPane().getLayout();
			cl.show(getContentPane(), LOGIN);
			
			setTitle(AppParams.VOTERAPPNAME);
		}
	}
	
	
		
	
	// UTILS methods
	
	private static void outl(String s){
		System.out.print(s);
	}
	private static void out(String s) {
		System.out.println(s);
	}
	private static String html(String s){
		return "<html>" + s + "</html>";
	}
	private static String bold(String s){
		return "<b>" + s + "</b>";
	}
}

