package de.uni.trier.infsec.functionalities.pki;

import java.io.File;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.utils.Utilities;

public class PKIServerCore implements PKIServer {
	
	public static final String DEFAULT_DATABASE = System.getProperty("java.io.tmpdir") + File.separator + "PKI_server.db";
	private static final String DB_TABLE_NAME_PKE = "PKI";
	private static final String DB_COLUMN_NAME_KEY = "KEY";
	// Table PKI stores ID and corresponding Public Key in hex-representation
	private static final String DB_TABLE_CREATE_PKE = "CREATE TABLE " + DB_TABLE_NAME_PKE + " (ID TEXT NOT NULL PRIMARY KEY, " + DB_COLUMN_NAME_KEY + " TEXT NOT NULL)";
	private static boolean dbInitialized = false;
	
	
	
	@Override
	public void register(int id, byte[] domain, byte[] key) throws PKI.Error, NetworkError {
		pki_register(id, domain, key);
	}

	@Override
	public byte[] getKey(int id, byte[] domain) throws PKI.Error, NetworkError {
		return pki_getKey(id, domain);
	}

	/**
	 * Registers the key and stores it into a local filebased database.
	 * True, if key was successfully stored,
	 * False, in case key is already registered or an error occured. 
	 */
	protected static void pki_register(int id, byte[] domain, byte[] pubKey) throws PKI.Error, NetworkError {
		if (!dbInitialized) initDB();
		try {
			db.beginTransaction(SqlJetTransactionMode.WRITE);
			ISqlJetTable table = db.getTable(DB_TABLE_NAME_PKE);
			ISqlJetCursor cursor = table.lookup(null, Utilities.byteArrayToHexString(domain) + "." + id);
			if (cursor.first())	throw new PKI.Error(); // ID has been claimed
			
			table.insert(Utilities.byteArrayToHexString(domain) + "." + id, Utilities.byteArrayToHexString(pubKey));
			db.commit();
		} catch (SqlJetException e) {
			e.printStackTrace();
			throw new NetworkError(); // Something went wrong 
			// TODO: change this to some other exception
		}
	}

	/**
	 * Reads the public key from a local database and returns it.
	 * Returns null if no entry found. 
	 */
	protected static byte[] pki_getKey(int id, byte[] domain) throws PKI.Error, NetworkError {
		if (!dbInitialized) initDB();
		try {
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			ISqlJetTable table = db.getTable(DB_TABLE_NAME_PKE);
			ISqlJetCursor cursor = table.lookup(null, Utilities.byteArrayToHexString(domain) + "." + id);
			if (cursor.first()) {
				String sPubKey = cursor.getString(DB_COLUMN_NAME_KEY);
				byte[] pubKey  = Utilities.hexStringToByteArray(sPubKey);
				return pubKey;
			} else {
				throw new PKI.Error(); // ID not registered
			}
		} catch (SqlJetException e) {
			e.printStackTrace();
			throw new NetworkError(); // Something went wrong
			// TODO: change this to some other exception
		}
	}
	
	/// Implementation ///
	// We store the public keys in the SQLite DB (located in system temp directory)

	private static SqlJetDb db = null;
	private static void initDB() {
		try {
			File dbFile = new File(DEFAULT_DATABASE);
			if (!dbFile.exists()) {
				// We need to initialize a completely new Database
				db = SqlJetDb.open(dbFile, true);
				db.createTable(DB_TABLE_CREATE_PKE);
				db.commit();
			} else {
				db = SqlJetDb.open(dbFile, true);
			}
			dbInitialized = true;
		} catch (SqlJetException e) {
			e.printStackTrace();
		}
	}
}