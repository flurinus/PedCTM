
/**
 * Functional way of invoking PedCTM
 * 
 * @author Thomas Muehlematter
 * 
 */
public class FunctionalPedCTMMain {
	
	static boolean pictures;

	/**
	 * First parameter : name of the layout Second parameter : name of the
	 * demand file Third parameter : boolean to decide whether to output
	 * pictures or not
	 * 
	 * @param args
	 */
	
	
	public static void main(String[] args) {

		//check Java heap space
		//System.out.println(java.lang.Runtime.getRuntime().maxMemory()); 
		
		if (args.length != 3) {

			System.out
					.println(" wrong number of arguments "
							+ args.length
							+ ". correct number is 3 (layout file, demand file and pictures boolean)");
		} else {

			@SuppressWarnings("unused")
			FunctionalPedCTM pedCTM = new FunctionalPedCTM(args);

		}

	}

}
