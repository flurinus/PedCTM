import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import model.Board;
import view.Grid;

/**
 * 
 * Represents all the logic needed to run PedCTM in a functional way.
 * 
 * @author Thomas Muehlematter
 * 
 */
class FunctionalPedCTM implements Observer {

	boolean pictures;

	Grid grid;

	Board board;

	/**
	 * First parameter : name of the layout Second parameter : name of the
	 * demand file Third parameter : boolean to decide whether to output
	 * pictures or not.
	 * 
	 * @param args
	 */
	public FunctionalPedCTM(String[] args) {

		pictures = Boolean.parseBoolean(args[2]);

		board = new Board(pictures);

		board.addObserver(this);

		board.loadLayout(new File(args[0]));
		board.loadDemand(new File(args[1]));

		board.addMissing();

		board.computePotentialFields();

		if (pictures) {

			grid = new Grid();

			grid.init();

			grid.setup();

			grid.setInitialized(true);

			grid.validate();

		}

		board.simulate();

		board.writeSpaceTime();
		board.writeSimulation();

	}

	/**
	 * updates the grid
	 */

	@SuppressWarnings("unchecked")
	public void update(Observable o, Object arg) {

		if (pictures) {
			grid.loadGrid((ArrayList<float[]>) arg, board.getTimestep(),
					board.getBounds(), true);
		}
		if (board.getEnd() == true) {

			if (pictures) {

				grid.setInitialized(false);

				grid.writeSimulationmaps();

			}

			board.writeSpaceTime();
			board.writeSimulation();

			System.exit(0);
		}
	}
}
