package de.uni.trier.infsec.eVotingSystem.apps;

import de.uni.trier.infsec.eVotingSystem.core.Params;

/**
 * Registers the final server (using its id from the Params).
 */
public class RegisterFinalServer 
{
	public static void main(String[] args) {		
		int id = Params.SERVER2ID;
		String filename = AppParams.PATH_STORAGE + "server" + id + ".info";
		System.out.printf("Registering server with ID '%d' and saving the keys in %s\n", id, filename);		
		try {
			AppUtils.registerAndSave(id, filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
