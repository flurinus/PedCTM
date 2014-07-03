package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import processing.core.*;

import model.Board;
import model.BoardThread;
import model.Heatmap;

/**
 * Handles the interface and communicates with the board and the grid
 * 
 * @author Thomas Muehlematter
 * 
 */

public class View extends JFrame implements ActionListener, Observer {

	private static final long serialVersionUID = 1L;

	private static final int windowWidth = 1370;

	private static final int windowHeight = 900;

	private JMenuBar menuBar = new JMenuBar();
	private JMenu file = new JMenu("File");
	private JMenu help = new JMenu("Help");

	private JMenuItem open;
	private JMenuItem about;

	private JMenuItem loadCSV;

	private JFileChooser fc;
	private FileFilter filter;

	JPanel buttons;
	private JButton start;
	private JButton stop;
	private JButton output;

	private JLabel timeLabel;

	JPanel middle = new JPanel();
	JPanel progress = new JPanel();

	private PApplet grid;

	private Board board;

	private BoardThread bthread;

	private Heatmap heatmap;

	private int timestep;

	/**
	 * constructor of the view
	 * 
	 * @param b
	 *            - board
	 * @param hm
	 *            - heatmap
	 */

	public View(Board b, Heatmap hm) {

		this.board = b;

		board.addObserver(this);

		this.heatmap = hm;

		this.timestep = 0;

		this.setSize(windowWidth, windowHeight);
		this.setTitle("Pedestrian Simulator");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);

		open = new JMenuItem("Open");
		loadCSV = new JMenuItem("Heatmap");
		about = new JMenuItem("About");

		file = new JMenu("File");
		help = new JMenu("Help");

		this.file.add(open);
		this.file.add(loadCSV);
		this.help.add(about);

		this.menuBar.add(file);
		this.menuBar.add(help);

		this.setJMenuBar(menuBar);

		this.fc = new JFileChooser();
		this.filter = new FileNameExtensionFilter(
				"Property file (.txt or .csv)", "txt", "csv");
		this.fc.setFileFilter(filter);

		start = new JButton("Start Simulation");
		stop = new JButton("Stop");
		output = new JButton("Output");

		start.setEnabled(false);
		stop.setEnabled(false);
		output.setEnabled(false);

		buttons = new JPanel();
		buttons.setLayout(new FlowLayout(0));

		buttons.add(start);
		buttons.add(stop);
		buttons.add(output);

		grid = new Grid();

		middle = new JPanel();
		middle.add(grid, BorderLayout.CENTER);

		timeLabel = new JLabel("Timestep : 0");

		progress = new JPanel();
		progress.setLayout(new FlowLayout(0));

		progress.add(timeLabel);

		this.add(buttons, BorderLayout.PAGE_START);
		this.add(middle);
		this.add(progress, BorderLayout.PAGE_END);

		this.open.addActionListener(this);
		this.loadCSV.addActionListener(this);
		this.about.addActionListener(this);
		this.start.addActionListener(this);
		this.stop.addActionListener(this);
		this.output.addActionListener(this);

		grid.init();

		this.setVisible(true);
	}

	/**
	 * All action listeners are regrouped in this function
	 */

	public synchronized void actionPerformed(ActionEvent e) {

		if (e.getSource() == open) {

			int ret = fc.showOpenDialog(this);

			File f = fc.getSelectedFile();
			boolean valid = false;

			if (ret != JFileChooser.CANCEL_OPTION) {

				valid = board.loadLayout(f);

				if (valid && ret != JFileChooser.CANCEL_OPTION) {

					ret = fc.showOpenDialog(this);

					f = fc.getSelectedFile();

					if (ret != JFileChooser.CANCEL_OPTION) {

						valid = board.loadDemand(f);

						if (valid && ret != JFileChooser.CANCEL_OPTION) {

							board.addMissing();

							board.computePotentialFields();

							start.setEnabled(true);

							this.bthread = new BoardThread(board);

							grid.clear();

							((Grid) grid).loadGrid(board.updateGrid(),
									board.getTimestep(), board.getBounds(),
									false);

							((Grid) grid).setInitialized(true);

							grid.validate();
						}

						else if (!valid && ret != JFileChooser.CANCEL_OPTION) {

							JOptionPane.showMessageDialog(null,
									"At least one file is not valid", "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}

				} else if (!valid && ret != JFileChooser.CANCEL_OPTION) {

					JOptionPane.showMessageDialog(null,
							"At least one file is not valid", "Error",
							JOptionPane.ERROR_MESSAGE);

				}

			}

		} else if (e.getSource() == loadCSV) {

			int ret = fc.showOpenDialog(this);

			File f = fc.getSelectedFile();
			boolean valid = false;

			if (ret != JFileChooser.CANCEL_OPTION) {

				valid = heatmap.loadZones(f);

				if (valid && ret != JFileChooser.CANCEL_OPTION) {

					ret = fc.showOpenDialog(this);

					f = fc.getSelectedFile();

					if (ret != JFileChooser.CANCEL_OPTION) {

						valid = heatmap.loadOccupations(f);

						if (valid && ret != JFileChooser.CANCEL_OPTION) {

							((Grid) grid).writeHeatmaps(
									heatmap.getFirstInterval(),
									heatmap.getLastInterval(),
									heatmap.getZones(), heatmap.getBounds());

							JOptionPane.showMessageDialog(null,
									"The Heatmaps were successfully generated",
									"Success", JOptionPane.PLAIN_MESSAGE);

						}

						else if (!valid && ret != JFileChooser.CANCEL_OPTION) {

							JOptionPane.showMessageDialog(null,
									"At least one file is not valid", "Error",
									JOptionPane.ERROR_MESSAGE);

						}
					}

				} else if (!valid && ret != JFileChooser.CANCEL_OPTION) {

					JOptionPane.showMessageDialog(null,
							"At least one file is not valid", "Error",
							JOptionPane.ERROR_MESSAGE);

				}

			}

		}

		else if (e.getSource() == about) {

			JOptionPane.showMessageDialog(null,
					"Thomas Muehlematter \nTransp-OR \n 2013", "About",
					JOptionPane.PLAIN_MESSAGE);

		}

		else if (e.getSource() == start) {

			start.setEnabled(false);
			stop.setEnabled(true);

			this.validate();

			this.bthread = new BoardThread(board);
			bthread.start();

		} else if (e.getSource() == stop) {

			output.setEnabled(true);

			board.setGo(false);

			bthread.interrupt();

			while (!bthread.isInterrupted()) {

				try {

					wait();

				} catch (InterruptedException e1) {

				}
			}

			start.setEnabled(true);
			stop.setEnabled(false);

		} else if (e.getSource() == output) {

			output.setEnabled(false);
			start.setEnabled(false);
			//board.writeSpaceTime();
			//board.writeSimulation();
			((Grid) grid).writeSimulationmaps();

		}
	}

	/**
	 * updates the grid
	 */

	@SuppressWarnings("unchecked")
	public void update(Observable o, Object arg) {

		((Grid) grid).loadGrid((ArrayList<float[]>) arg, board.getTimestep(),
				board.getBounds(), false);

		if (board.getEnd() == true) {

			this.stop.setEnabled(false);
			this.output.setEnabled(true);
			((Grid) grid).setInitialized(false);
			board.setEnd(false);

		}

		this.timestep = board.getTimestep();
		this.timeLabel.setText("Timestep : " + timestep);

		grid.validate();

	}

}
