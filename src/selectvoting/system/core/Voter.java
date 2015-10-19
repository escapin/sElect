package selectvoting.system.core;

import de.unitrier.infsec.functionalities.digsig.Verifier;
import de.unitrier.infsec.functionalities.nonce.NonceGen;
import de.unitrier.infsec.functionalities.pkenc.Encryptor; 
import de.unitrier.infsec.utils.MessageTools;


/**
 * Core voter's client class. It provides cryptographic operations (formating a ballot) 
 * of a voter.
 */
public class Voter 
{
	public static class BallotInfo {
		public final byte[] ballot;
		public final byte[] nonce;
		public final byte[] innerBallot;
		public BallotInfo(byte[] ballot, byte[] nonce, byte[] innerBallot) {
			this.ballot = ballot;
			this.nonce = nonce;
			this.innerBallot = innerBallot;
		}
	}
	
	private static final NonceGen noncegen = new NonceGen(); // nonce generation functionality

	private final byte[] electionID;
	private final Encryptor colServEnc;
	private final Verifier colServVerif;
	private final Encryptor finServEnc;

	public Voter(byte[] electionID, Encryptor colServEnc, Verifier colServVerif, Encryptor finServEnc) {
		this.electionID = electionID;
		this.colServEnc = colServEnc;
		this.colServVerif = colServVerif;
		this.finServEnc = finServEnc;
	}

	/**
	 * Creates and returns a ballot containing the given vote. 
	 * The ballot is of the form:
	 * 
	 *     Enc_CS(electionID, innerBallot )
	 * 
	 * with innerBallot = Enc_FS(electionID, choice, receiptID)
	 * 
	 * where recID is a freshly generated nonce, and Enc_CS(msg) and Enc_FS denote
	 * the message msg encrypted with the public key of, respectively the collecting
	 * or the final server.
	 */
	public BallotInfo createBallot(int votersChoice) {
		byte[] nonce = noncegen.nextNonce();
		byte[] vote = MessageTools.intToByteArray(votersChoice);
		byte[] innerBallot = finServEnc.encrypt(MessageTools.concatenate(electionID, MessageTools.concatenate(nonce, vote)));
		byte[] ballot = colServEnc.encrypt(MessageTools.concatenate(electionID, innerBallot));
		return new BallotInfo(ballot, nonce, innerBallot);
	}

	/**
	 * Checks if 'receiptSignature' is a signature on the receipt. If yes, the method saves the signature and return true.  
	 */ 
	public boolean validateReceipt(byte[] receipt, byte[] innerBallot) {
		byte[] expectedMessage = MessageTools.concatenate(Tag.ACCEPTED, MessageTools.concatenate(electionID, innerBallot));
		return colServVerif.verify(receipt, expectedMessage);
	}
}
