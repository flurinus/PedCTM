import model.Board;
import model.Heatmap;
import view.View;

/**
 * main class of the PedCTM
 * 
 * @author Thomas Muehlematter
 * 
 */
public class GUIPedCTMMain {

	public static void main(String[] args) {

		Board board = new Board(true);
		Heatmap heatmap = new Heatmap();

		@SuppressWarnings("unused")
		View view = new View(board, heatmap);

	}
}
