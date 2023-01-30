package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;

import message.Message;
import message.MessageTypes;


public class Sender extends Thread implements MessageTypes
{

   //sender processes user input, translates to messages
   //and sends to chat server
   Socket serverConnection = null;
   Scanner userInput = new Scanner(System.in);
   String inputLine = null;
   boolean hasJoined;
      
      
   /*
      Constructor
   */
   public Sender()
   {
      userInput = new Scanner(System.in);
      hasJoined = false;
   }
      
   @Override
   public void run()
   {
      ObjectOutputStream writeToNet;
      ObjectInputStream readFromNet;
         
      //until SHUTDOWN/SHUTDOWN_ALL
      while(true)
      {
         //get user input
         inputLine = userInput.nextLine(); 
         
         if (inputLine.startsWith("JOIN"))
         {
            if(hasJoined == true)
            {
               System.err.println("You have already joined a chat ...");
               continue;
            }
            
            // read server information user provided with JOIN command
            String[] connectivityInfo = inputLine.split("[ ]+");
            
            // if there is information, that may override the connectivity information
            // that was provided through the properties
            try
            {
               ChatClient.serverNodeInfo = new NodeInfo(connectivityInfo[1], Integer.parseInt(connectivityInfo[2]));
            }
            catch(ArrayIndexOutOfBoundsException ex)
            {
               // don't do anything, we may have defaults
            }
            
            // check if we have valid server connectivity information
            if(ChatClient.serverNodeInfo == null)
            {
               System.err.println("[Sender].run No server connectivity information provided");
               continue;
            }

            // server information was provided, so send join request
            try
            {
               //open connection to server
               serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());
               
               //open object streams
               readFromNet = new ObjectInputStream(serverConnection.getInputStream());
               writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());
               
               //send join request
               writeToNet.writeObject(new Message(JOIN, ChatClient.myNodeInfo));
               
               //close connection
               serverConnection.close();
            }
            catch(IOException ex)
            {
               Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "[Sender].run Error connectiong to server, creating object streams, or closing connection", ex);
               continue;
            }

            // we are in!
            hasJoined = true;

            System.out.println("Joined chat...");
         }

         else if (inputLine.startsWith("LEAVE"))
         {
            if (hasJoined == false)
            {
               System.err.println("You have not joined a chat yet ...");
               continue;
            }

            //send leave request
            try
            {
               //open connection to server
               serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());
               
               //open object streams
               readFromNet = new ObjectInputStream(serverConnection.getInputStream());
               writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());
               
               //send leave request
               writeToNet.writeObject(new Message(LEAVE, ChatClient.myNodeInfo));
               
               //close connection
               serverConnection.close();
            }
            catch(IOException ex)
            {
               Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "[Sender].run Error connectiong to server, creating object streams, or closing connection", ex);
               continue;
            }

            // we are out!
            hasJoined = false;

            System.out.println("Left chat...");
         }
         
         else if (inputLine.startsWith("SHUTDOWN_ALL"))
         {
            if (hasJoined == false)
            {
               System.err.println("To shut down the whole chat, you need to first join a chat ...");
               continue;
            }

            // we are a participant, send out a SHUTDOWN_ALL message
            try
            {
               //open connection to server
               serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());
               
               //open object streams
               readFromNet = new ObjectInputStream(serverConnection.getInputStream());
               writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());
               
               //send shutdown_all request
               writeToNet.writeObject(new Message(SHUTDOWN_ALL, ChatClient.myNodeInfo));
               
               //close connection
               serverConnection.close();
            }
            catch (IOException ex)
            {
               Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error opening connection to server, creating object streams, or closing connection", ex);
            }

            System.out.println("Sent shutdown all request...");
         }
         
         else if (inputLine.startsWith("SHUTDOWN"))
         {
            // if we are a participant, leave chat first
            if (hasJoined == true)
            {
               //Send leave request
               try
               {
                  //open connection to server
                  serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());
                  
                  //open object streams
                  readFromNet = new ObjectInputStream(serverConnection.getInputStream());
                  writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());	
                  
                  //send shutdown request
                  writeToNet.writeObject(new Message(SHUTDOWN, ChatClient.myNodeInfo));
                  
                  //close connection
                  serverConnection.close();
                  
                  System.out.println("Left chat...");
               }
               catch (IOException ex)
               {
                  Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error opening connection to server, creating object streams, or closing connection", ex);
                  continue;
               }  
               
               System.out.println("Exiting...");
               
               System.exit(0);
            }   
         }
         
         else // sending a note
         {
            if (hasJoined == false)
            {
               System.err.println("You need to join a chat first ...");
               continue;
            }
            
            // send note
            try
            {
               //open connection to server
               serverConnection = new Socket(ChatClient.serverNodeInfo.getAddress(), ChatClient.serverNodeInfo.getPort());	
               
               //open object streams
               readFromNet = new ObjectInputStream(serverConnection.getInputStream());
               writeToNet = new ObjectOutputStream(serverConnection.getOutputStream());	
               
               //send note
               writeToNet.writeObject(new Message(NOTE, ChatClient.myNodeInfo));	
               
               //close connection
               serverConnection.close();
               
               System.out.println("Message Sent...");
            }
            catch(IOException ex)
            {
               Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, "Error opening connection to server, creating object streams, or closing connection", ex);
               continue;
            } 
         }
      }
   }   
}
