package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;
import java.util.Vector;


/**
 * This is the main class for your Kalaha AI bot. 
 * 
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;
    
    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;
    private final int DEPTH_CH=15;
    private final int xyz=0;
    	
    /**
     * Creates a new client.
     */
    public AIClient()
    {
	player = -1;
        connected = false;
        
        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();
	
        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }
    
    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }
    
    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());
        
        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));
        
        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setVisible(true);
    }
    
    /**
     * Adds a text string to the GUI textarea.
     * 
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }
    
    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;
        
        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);
                    
                    addText("I am player " + player);
                }
                
                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);
                            
                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;
                            
                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }
                
                //Wait
                Thread.sleep(100);
            }
	}
        catch (Exception ex)
        {
            running = false;
        }
        
        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }
    
    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     * 
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
    
    
	/**
     * Returns a random ambo number (1-6) used when making
     * a random move.
     * 
     * @return Random ambo number
     */

    public int getRandom()
    {
        return 1 + (int)(Math.random() * 6);
    }

/*==============================START HERE====================================================*/
    
    public int getMove(GameState currentBoard)
     {
        long initial_time = System.currentTimeMillis();
        int myMove=0;
        int id=1;
        GameState gm = (GameState) currentBoard.clone();
        while(id<=DEPTH_CH)
        {
                long final_time=System.currentTimeMillis();
		int alpha=Integer.MIN_VALUE; 
		int beta=Integer.MAX_VALUE;
                if((final_time-initial_time)<5000)
                {
                myMove = minimax(currentBoard,alpha,beta,id,initial_time);
                ++id;
                }
                else 
                {
                    ++id;
                    break;  
                }
                
        }
        if((myMove<1)||(myMove>6))
        {
            int nextplayer = gm.getNextPlayer();
            int playerc,j;
            if(nextplayer==1)playerc=2;else playerc=1;
            for(int cm=1;cm<=6;cm++)
            {
            if(gm.getSeeds(cm, playerc)==13 || gm.getSeeds(cm, playerc)>=14)
            {
                j=6-cm;
                if(gm.getSeeds(j,nextplayer)==0)
                {
                    return cm;
                }
            }
            else 
            {
                myMove=getRandom();
                return myMove;
            }
          }
        }
        return myMove;
      }
     
    //Generate moves
     public Vector<Integer> generate_moves(GameState mvl) 
    {
        int player2=mvl.getNextPlayer();
        Vector<Integer> move_list = new Vector<>();
        int it;
        
	if (player2 == 1)
        {
            for(it=1;it<=6;it++)
            {
                if(mvl.getSeeds(it, player2)!=0)
                    move_list.add(it);
            }
        }
        else
        {
            for(it=1;it<=6;it++)
            {
                if(mvl.getSeeds(it, player2)!=0)
                    move_list.add(it);
            }
        }
	return move_list;
    }
    
    //minimax
     
    public int minimax(GameState mini_brd,int alpha,int beta,int inl_depth,long initial_time)
    {
        int f_val;
        int step=0; 
        int cp;
        Vector<Integer> mini_lt = new Vector<>();
     	mini_lt=generate_moves(mini_brd);
        if(mini_lt.isEmpty()) return 0;
        
        for(int it:mini_lt)
	   {
            long final_time=System.currentTimeMillis();
            if((final_time-initial_time)<5000)
            {
                GameState mini_cl= (GameState) mini_brd.clone();
                int hx=mini_cl.getNextPlayer();
                if(hx==1){cp=2;}else{cp=1;}
                mini_cl.makeMove(it);
                if(mini_cl.getSeeds(it,cp)==(7-it))
                {
                    f_val=max1_turn(mini_cl,alpha,beta,inl_depth,initial_time);
                }
                else
                {
		            f_val=min2_turn(mini_cl,alpha,beta,(inl_depth-1),initial_time);
                }
                
                if(alpha<f_val)
		{
		   alpha=f_val;
		   step=it;
		}
		 if(alpha>=beta)
		{
	           return step;
		}
            } else break;
	}
	return step;
      }
   
  //max 1 turn
   public int max1_turn(GameState max_brd,int alpha,int beta,int mt_depth,long initial_time)// change name of method
    {
     int max_val=0;
     int cp;
     
	if(mt_depth<=0)
        return calculate_move_value(max_brd);
	Vector<Integer> mt = new Vector<>();
	mt=generate_moves(max_brd);
        if(mt.isEmpty())  return max_val;
        int i=0,j;
      
	for(int cmove: mt)
	{
            long final_time=System.currentTimeMillis();
            if((final_time-initial_time)<5000)
            {
		GameState max_cl= (GameState) max_brd.clone();
                int hx=max_cl.getNextPlayer();
                if(hx==1){cp=2;}else{cp=1;}
                max_cl.makeMove(cmove);
                if(max_cl.getSeeds(cmove,cp)==(7-cmove))
                {
                    max_val=max1_turn(max_cl,alpha,beta,mt_depth,initial_time);
                }  
                else
                {
	            	max_val=min2_turn(max_cl,alpha,beta,(mt_depth-1),initial_time);
                }
               
		if(max_val>alpha)
		{
			alpha=max_val;
		}
		if(alpha>=beta)
		{
			return alpha;
		}
            }
            else break;
	}
	return alpha;
    } 

    //min2_turn
    
    public int min2_turn(GameState min_brd,int alpha,int beta,int min_d,long initial_time)
    {
        int min_value=0;
        int cp;
	if(min_d<=0) return calculate_move_value(min_brd);
	Vector<Integer> min_l = new Vector<Integer>();
	min_l=generate_moves(min_brd);
        if(min_l.isEmpty())return min_value;
        int i=0,j;
        
	for(int cmove:min_l)
	{
            long final_time=System.currentTimeMillis();
            if((final_time-initial_time)<5000)
            {
		GameState min_cl= (GameState) min_brd.clone();
                int hx=min_cl.getNextPlayer();
                if(hx==1){cp=2;}else{cp=1;}
                min_cl.makeMove(cmove);
                if(min_cl.getSeeds(cmove,cp)==(7-cmove))
                {
                    min_value=min2_turn(min_cl,alpha,beta,min_d,initial_time);
                }
                else
                {
		min_value=max1_turn(min_cl,alpha,beta,(min_d-1),initial_time);
                }
                if(min_value<beta)
		{
			beta=min_value;
		}
		if(alpha>=beta)
		{
			return beta;
		}
            }
            else break;
	}
	return beta;
    }
    
   //Assigning values to each move possible
    
    public int calculate_move_value(GameState hv) 
    { 
        int xs=0,xwm=0,xbm=0,xcm=0,plus=0,hb;
        String hss = hv.toString();
        String[] h2 = hss.split(";");
        String hn=h2[0];
        String hs=h2[7];
        int[] house = new int[2];
        house[0]= Integer.parseInt(hn);
        house[1]=Integer.parseInt(hs);
        int player2=hv.getNextPlayer();
        int cp;
        if(player2 == 1)
        {
            cp=2;
            hb=(house[1]-house[0])*12;
            int m=1;
            while(m<=6)
            { 
                if(hv.getSeeds(m,cp)==(7-m)){xs=xs*5;}
                if(hv.getSeeds(m, cp)>7 && hv.getSeeds(m, cp)!=13){xwm=xwm+7;}
                if(hv.getSeeds(m, cp)==13){xbm=xbm+13;}
                if(hv.getSeeds(m,cp)==0)
                {
                    int j=1;
                    for(;j<m;j++)
                    {
                        if(hv.getSeeds(j, cp)==(m-j))
                        {
                            plus=plus+10;
                        }
                    }                 
                     xcm=xcm+5;
                }
                m++;
            }
            int ls=xs+xwm+xbm+xcm+plus+hb;
            return ls;
            
            
        }
        else
        {
            cp=1;
            hb=(house[0]-house[1])*12;
            int m=1;
            while(m<=6)
            {
                if(hv.getSeeds(m,cp)==(7-m)){xs=xs*5;}
                if(hv.getSeeds(m, cp)>7 && hv.getSeeds(m, cp)!=13){xwm=xwm-7;}
                if(hv.getSeeds(m, cp)==13){xbm=xbm+13;}
                if(hv.getSeeds(m,cp)==0)
                {
                    int j=1;
                    for(;j<m;j++)
                    {
                        if(hv.getSeeds(j, cp)==(m-j))
                        {
                            plus=plus+10;
                        }
                    }                 
                     xcm=xcm+5;
                }
                m++;
            }
            int ls=xs+xwm+xbm+xcm+plus+hb;
            return ls;
        }
    }

}