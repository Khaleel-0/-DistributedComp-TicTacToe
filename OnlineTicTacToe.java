import com.jcraft.jsch.*;
import java.util.Scanner;
import java.io.Console;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.Random;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author MOHAMMED KHALEELUR REHMAN
 */
public class OnlineTicTacToe implements ActionListener {

    private final int INTERVAL = 1000;         // 1 second
    private final int NBUTTONS = 9;            // #bottons
    private ObjectInputStream input = null;    // input from my counterpart
    private ObjectOutputStream output = null;  // output from my counterpart
    private JFrame window = null;              // the tic-tac-toe window
    private JButton[] button = new JButton[NBUTTONS]; // button[0] - button[9]
    private boolean[] myTurn = new boolean[1]; // T: my turn, F: your turn
    private String myMark = null;              // "O" or "X"
    private String yourMark = null;            // "X" or "O"
    private ArrayList<Integer> numbersList = new ArrayList<Integer>(Arrays.asList(0,1,2,3,4,5,6,7,8)); //list to keep track of available positions on board
    Random random = new Random();	       //to select the numbers randomly
    boolean autoMode = false;		       //to indicate the auto mode
    int mode = random.nextInt(2);	       //handle the modes for complexity in the game

    /**
     * Prints out the usage.
     */
    private static void usage( ) {
        System.err.
	    println( "Usage: java OnlineTicTacToe ipAddr ipPort(>=5000) [auto]" );
        System.exit( -1 );
    }

    /**
     * Prints out the track trace upon a given error and quits the application.
     * @param an exception 
     */
    private static void error( Exception e ) {
        e.printStackTrace();
        System.exit(-1);
    }

    /**
     * Starts the online tic-tac-toe game.
     * @param args[0]: my counterpart's ip address, args[1]: his/her port, (arg[2]: "auto")
     *        if args.length == 0, this Java program is remotely launched by JSCH.
     */
    public static void main( String[] args ) {

	if ( args.length == 0 ) {
	    // if no arguments, this process was launched through JSCH
	    try {
		OnlineTicTacToe game = new OnlineTicTacToe( );
	    } catch( IOException e ) {
		error( e );
	    }
	}
	else {
	    // this process wa launched from the user console.

	    // verify the number of arguments
	    if ( args.length != 2 && args.length != 3 ) {
		System.err.println( "args.length = " + args.length );
		usage( );
	    }

	    // verify the correctness of my counterpart address
	    InetAddress addr = null;
	    try {
		addr = InetAddress.getByName( args[0] );
	    } catch ( UnknownHostException e ) {
		error( e );
	    }
	    
	    // verify the correctness of my counterpart port
	    int port = 0;
	    try {
		port = Integer.parseInt( args[1] );
	    } catch (NumberFormatException e) {
		error( e );
	    }
	    if ( port < 5000 ) {
		usage( );
	    }
	    
	    // check args[2] == "auto"
	    if ( args.length == 3 && args[2].equals( "auto" ) ) {
		// auto play
		OnlineTicTacToe game = new OnlineTicTacToe( args[0] );
		//autoMode = true;
	    }
	    else { 
		// interactive play
		OnlineTicTacToe game = new OnlineTicTacToe( addr, port );
	    }
	}
    }

    /**
     * I`s the constructor that is remote invoked by JSCH. It behaves as a server.
     * The constructor uses a Connection object for communication with the client.
     * It always assumes that the client plays first. 
     */
    public OnlineTicTacToe( ) throws IOException {
	// receive an ssh2 connection from a user-local master server.
	System.err.println("Syatem mic test 123");
	Connection connection = new Connection( );
	input = connection.in;
	output = connection.out;
	autoMode = true;
	System.err.println("Syatem mic test 123");

	// for debugging, always good to write debugging messages to the local file
	// don't use System.out that is a connection back to the client.
	PrintWriter logs = new PrintWriter( new FileOutputStream( "logs.txt" ) );
	logs.println( "Autoplay: got started. mic test`" );
	logs.flush( );
	System.err.println("Syatem mic test 123");

	myMark = "X";   // auto player is always the 2nd.
	yourMark = "O"; 
	
	// the main body of auto play.  
	// IMPLEMENT BY YOURSELF

	int position = 0;
	// read the clicked button and remove from the list of available positions
	try { position = input.readInt();} catch(Exception e) {}
	System.err.println("position "+position);	
	logs.println("position "+position);	
	numbersList.remove(position);
	System.err.println("numbersList "+numbersList);	
	logs.println("numbersList "+numbersList);	
	
	int generatedInt = 0; 
	
	// mode1: randomly selects the number
	if(mode == 0) generatedInt = numbersList.get(random.nextInt(numbersList.size()));
	// mode2: selects the numbers in ascending order
	else if(mode == 1) generatedInt = numbersList.get(0);
	// mode3: selects the numbers in descending order
	else if(mode == 2) generatedInt = numbersList.get(numbersList.size()-1);
	
	System.err.println("generated Number "+generatedInt);	
	//logs.println("generated Number "+generatedInt);	
	
	numbersList.remove(Integer.valueOf(generatedInt));	
	
	System.err.println("numbersList "+numbersList);	
	logs.println("numbersList "+numbersList);	
	try { 
		output.writeInt(generatedInt);
	System.err.println("generated Number "+generatedInt);	
	logs.println("generated Number "+generatedInt);	
	} catch (Exception e) {}
	System.err.println("Inside the write catch");	
	}

    /**
     * Is the constructor that, upon receiving the "auto" option,
     * launches a remote OnlineTicTacToe through JSCH. This
     * constructor always assumes that the local user should play
     * first. The constructor uses a Connection object for
     * communicating with the remote process.
     *
     * @param my auto counter part's ip address
     */
    public OnlineTicTacToe( String hostname ) {
        final int JschPort = 22;      // Jsch IP port

	// Read username, password, and a remote host from keyboard
        Scanner keyboard = new Scanner( System.in );
        String username = null;
        String password = null;
	//autoMode = true;
	
	try {
            // read the user name from the console                  
            System.out.print( "User: " );
            username = keyboard.nextLine( );

            // read the password from the console                  
            Console console = System.console( );
            password = new String( console.readPassword( "Password: " ) );

        } catch( Exception e ) {
            e.printStackTrace( );
            System.exit( -1 );
        }

	// A command to launch remotely:
	//          java -cp ./jsch-0.1.54.jar:. JSpace.Server
	String cur_dir = System.getProperty( "user.dir" );
	String command 
	    = "java -cp " + cur_dir + "/jsch-0.1.54.jar:" + cur_dir + 
	      " OnlineTicTacToe";

        // establish an ssh2 connection to ip and run
        // Server there.
        Connection connection = new Connection( username, password,
						hostname, command );

        // the main body of the master server
	input = connection.in;
	output = connection.out;

	// set up a window
	makeWindow( true ); // I'm a former

        // start my counterpart thread
        Counterpart counterpart = new Counterpart( );
        counterpart.start();
    }

    /**
     * Is the constructor that sets up a TCP connection with my counterpart,
     * brings up a game window, and starts a slave thread for listenning to
     * my counterpart.
     * @param my counterpart's ip address
     * @param my counterpart's port
     */
    public OnlineTicTacToe( InetAddress addr, int port ) {
        // set up a TCP connection with my counterpart
	// IMPLEMENT BY YOURSELF

        // set up a window
       // makeWindow( true ); // or makeWIndow( false );
	ServerSocket server = null;
	Socket client = null;
	try{
		//setup a server connection
		server = new ServerSocket(port);
		//timeout if the connection fails after 1000 milliseconds
		server.setSoTimeout(1000);
		//if server connected is established then open the game window
		if(server!=null) {
			makeWindow(true);
		}
	}
	catch(Exception e){
		//if server already runs on a port number & same port is requested by client then create a new socket connection (scenario 2)
		if(server!=null) {
			try {
				client = new Socket(addr, port);
			}
			catch(IOException err) { }
		}
	}
	//Open window for the second player
	if(client!=null) makeWindow(false);
	
	//To create the clients
	while(true) {
	try {
	  	//establish a connection with the server 
	    if(server!=null) client = server.accept();
	} catch( SocketTimeoutException e) {
	
	} catch( IOException e){error(e);}
	if(client!=null) break;
	
	try {
		// create a new client connection
		client = new Socket(addr, port);
		if(client!=null) makeWindow(false);
	}
	catch(IOException e) { }
	if(client!=null) break;
	}
	
	//Communicate through the connection using the respective Input and Output Streams
	try {
		output = new ObjectOutputStream(client.getOutputStream());
		input = new ObjectInputStream(client.getInputStream());
	} catch (Exception e) {}
        
	// start my counterpart thread
        Counterpart counterpart = new Counterpart( );
        counterpart.start();
    }

    /**
     * Creates a 3x3 window for the tic-tac-toe game
     * @param true if this window is created by the former, (i.e., the
     *        person who starts first. Otherwise false.
     */
    private void makeWindow( boolean amFormer ) {
        myTurn[0] = amFormer;
        myMark = ( amFormer ) ? "O" : "X";    // 1st person uses "O"
        yourMark = ( amFormer ) ? "X" : "O";  // 2nd person uses "X"

        // create a window
        window = new JFrame("OnlineTicTacToe(" +
                ((amFormer) ? "former)" : "latter)" ) + myMark );
        window.setSize(300, 300);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new GridLayout(3, 3));

	// initialize all nine cells.
        for (int i = 0; i < NBUTTONS; i++) {
            button[i] = new JButton();
            window.add(button[i]);
            button[i].addActionListener(this);
        }

	// make it visible
        window.setVisible(true);
    }

    /**
     * Marks the i-th button with mark ("O" or "X")
     * @param the i-th button
     * @param a mark ( "O" or "X" )
     * @param true if it has been marked in success
     */
    private boolean markButton( int i, String mark ) {
	if ( button[i].getText( ).equals( "" ) ) {
	    button[i].setText( mark );
	    button[i].setEnabled( false );
	    return true;
	}
	return false;
    }

    /**
     * Checks which button has been clicked
     * @param an event passed from AWT 
     * @return an integer (0 through to 8) that shows which button has been 
     *         clicked. -1 upon an error. 
     */
    private int whichButtonClicked( ActionEvent event ) {
	for ( int i = 0; i < NBUTTONS; i++ ) {
	    if ( event.getSource( ) == button[i] )
		return i;
	}
	return -1;
    }

    /**
     * Checks if the i-th button has been marked with mark( "O" or "X" ).
     * @param the i-th button
     * @param a mark ( "O" or "X" )
     * @return true if the i-th button has been marked with mark.
     */
    private boolean buttonMarkedWith( int i, String mark ) {
	return button[i].getText( ).equals( mark );
    }

    /**
     * Pops out another small window indicating that mark("O" or "X") won!
     * @param a mark ( "O" or "X" )
     */
    private void showWon( String mark ) {
	JOptionPane.showMessageDialog( null, mark + " won!" );	
    }

    /**
     * Is called by AWT whenever any button has been clicked. You have to:
     * <ol>
     * <li> check if it is my turn,
     * <li> check which button was clicked with whichButtonClicked( event ),
     * <li> mark the corresponding button with markButton( buttonId, mark ),
     * <li> send this informatioin to my counterpart,
     * <li> checks if the game was completed with 
     *      buttonMarkedWith( buttonId, mark ) 
     * <li> shows a winning message with showWon( )
     */
    public void actionPerformed( ActionEvent event ) {
	// IMPLEMENT BY YOURSELF
	synchronized(myTurn){
	// The player waits for the opponent's turn
	while(!myTurn[0]) {
		//wait if it is opponent's turn
		try{ myTurn.wait();} catch(Exception e) {}
	}
	
	//Mark the button from first player
	markButton(whichButtonClicked(event),myMark);
	
	if(autoMode) {	
		numbersList.remove(Integer.valueOf(whichButtonClicked(event)));
	}
	
	//logic that checks if player O has won the game
	if( 
	(buttonMarkedWith(0,"O") && buttonMarkedWith(1,"O") &&  buttonMarkedWith(2,"O")) || 
	(buttonMarkedWith(3,"O") && buttonMarkedWith(4,"O") &&  buttonMarkedWith(5,"O")) || 
	(buttonMarkedWith(6,"O") && buttonMarkedWith(7,"O") &&  buttonMarkedWith(8,"O")) || 
	(buttonMarkedWith(0,"O") && buttonMarkedWith(3,"O") &&  buttonMarkedWith(6,"O")) || 
	(buttonMarkedWith(1,"O") && buttonMarkedWith(4,"O") &&  buttonMarkedWith(7,"O")) || 
	(buttonMarkedWith(2,"O") && buttonMarkedWith(5,"O") &&  buttonMarkedWith(8,"O")) || 
	(buttonMarkedWith(0,"O") && buttonMarkedWith(4,"O") &&  buttonMarkedWith(8,"O")) || 
	(buttonMarkedWith(2,"O") && buttonMarkedWith(4,"O") &&  buttonMarkedWith(6,"O")) 
	) { 
		showWon("O"); 
	}
	
	//Send the position of the button clicked through output stream to the Counterpart
	try { 
		output.writeInt(whichButtonClicked(event));
		output.flush();
	} catch (Exception e) {}

	//Change the value for the opponent to get the turn	
	myTurn[0] = !myTurn[0];
	//Notify the object reference
	myTurn.notify();
	}
	
    }

    /**
     * This is a reader thread that keeps reading fomr and behaving as my
     * counterpart.
     */
    private class Counterpart extends Thread {

	/**
	 * Is the body of the Counterpart thread.
	 */
        @Override
        public void run( ) {
	// IMPLEMENT BY YOURSELF
	//To keep the thread running place in the while(true) loop
	while(true) {
	//thread synchronization with the reference object(myturn)
	synchronized(myTurn){
	// The opponent waits until the first player finished his turn
	while(myTurn[0]) {
		//wait if it is first player's turn
		try {myTurn.wait();} catch(Exception e) {}
	}
	
	//position keeps track of first player selection
	int position=0;
	
	if(autoMode) {
	// mode1: randomly selects the number
	if(mode == 0) position = numbersList.get(random.nextInt(numbersList.size()));
	// mode2: selects the numbers in ascending order
	else if(mode == 1) position = numbersList.get(0);
	// mode3: selects the numbers in descending order
	else if(mode == 2) position = numbersList.get(numbersList.size()-1);
	System.err.println(" mode "+mode+" position "+position+" numbersList ");
	numbersList.remove(Integer.valueOf(position));
	}
	
	else {
		//read the position from the first player
		try {
			position = input.readInt();
		System.err.println("input  "+input+ " input.readInt() "+input.readInt());
		}
		catch (Exception e) {
			System.err.println("In catch block position "+position);
		}
	}
	
	//mark the button for the opponent that first player has clicked
	markButton(position, yourMark);
	
	// logic that checks if player X has won the game
	if( 
	(buttonMarkedWith(0,"X") && buttonMarkedWith(1,"X") &&  buttonMarkedWith(2,"X")) || 
	(buttonMarkedWith(3,"X") && buttonMarkedWith(4,"X") &&  buttonMarkedWith(5,"X")) || 
	(buttonMarkedWith(6,"X") && buttonMarkedWith(7,"X") &&  buttonMarkedWith(8,"X")) || 
	(buttonMarkedWith(0,"X") && buttonMarkedWith(3,"X") &&  buttonMarkedWith(6,"X")) || 
	(buttonMarkedWith(1,"X") && buttonMarkedWith(4,"X") &&  buttonMarkedWith(7,"X")) || 
	(buttonMarkedWith(2,"X") && buttonMarkedWith(5,"X") &&  buttonMarkedWith(8,"X")) || 
	(buttonMarkedWith(0,"X") && buttonMarkedWith(4,"X") &&  buttonMarkedWith(8,"X")) || 
	(buttonMarkedWith(2,"X") && buttonMarkedWith(4,"X") &&  buttonMarkedWith(6,"X"))
	) {
		//pop up the window that shows the winner 
		showWon("X");
	}

	//Negate the value for first player to get his turn
	myTurn[0] = !myTurn[0];
	//Notify the object reference
	myTurn.notify();
	}
    	}
	}
	
	}
}
