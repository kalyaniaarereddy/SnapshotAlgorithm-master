import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.io.*;
import java.util.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.* ;


class Branch extends Thread {
 
public static ArrayList<String> bname = new ArrayList<>();
public static ArrayList<String> ips = new ArrayList<>();
public static ArrayList<Integer> ports = new ArrayList<>();
private ServerSocket serverSocket;
public boolean firstMarker;
public static int m;
public int port;
public String ip;
public String branchName;
public int branchBalance;
public int initialBalance;
public int processState;
public ArrayList<String> channelState = new ArrayList<>();
Socket trasferingSocket; //transfering socket 
public static ArrayList<String> prevChannelState = new ArrayList<>();
String transIp;
String transName;
String clientIP;
int clientPort; 
public boolean startReceivingSet;


public Branch(int portIn,String bname) throws IOException {

	port = portIn;
	branchName = bname;
	firstMarker = false;
	startReceivingSet = false;
  	serverSocket = new ServerSocket(port);
  	  
}




public void run() 
{
	while(true)
  	{
		OutputStream outToServer=null;
		InputStream inFromServer=null;
	
   	try {
   		
    		Socket server = serverSocket.accept();
                clientIP = server.getRemoteSocketAddress().toString();
		clientIP = clientIP.substring(clientIP.indexOf('/') + 1, clientIP.indexOf(':'));
		clientPort = server.getPort();
		
		
    		inFromServer= server.getInputStream();
       		outToServer = server.getOutputStream();


                Bank.BranchMessage builder = Bank.BranchMessage.parseFrom(inFromServer);
		if(builder.hasInitBranch()) 
		{
			Bank.InitBranch.Builder init = builder.getInitBranch().toBuilder();
	     		setBranchBalance(init.getBalance());
			setInitialBalance(init.getBalance());
			for (Bank.InitBranch.Branch branch : init.getAllBranchesList()) {
				
				ips.add(branch.getIp());
				ports.add(branch.getPort());
				bname.add(branch.getName());
				
                    	}
			
			startTransferingSocket();

						
		} //end of if hasInitBranch()

		else if(builder.hasTransfer())
		{
			transIp = server.getRemoteSocketAddress().toString();
			transIp = transIp.substring(transIp.indexOf('/') + 1, transIp.indexOf(':'));
			int h=0;
			for(h=0;h<ips.size();h++)
			{
				if(ips.get(h).equals(transIp))
				{
					break;
				}
			}
			transName = bname.get(h);
			
			transferMoney(builder);
			startTransferingSocket();
			
		} //end of else if hasTransfer()


		else if(builder.hasInitSnapshot())
		{
						
			startMarkers(builder);
			
		
		} //end of else if hasInitSnapshot()

		else if(builder.hasMarker())
		{
			startMarkers(builder);

		} //end of hasMarker()
		
		else if(builder.hasRetrieveSnapshot())
		{
			
			startRetrieveSnapshot(builder);

		} //end of hasRetrieveSnapshot()

		inFromServer.close();

    		            
    	} //end of socket try 
    	catch(SocketException e)
    	{
		System.out.println("Socket closed");
    	}
    	catch (SocketTimeoutException s) {
      		System.out.println("Socket timed out!");
    	} 
    	catch (IOException e) {
      		e.printStackTrace();
    	}
      

   	}
}


public void startRetrieveSnapshot(Bank.BranchMessage builder)
{
	//System.out.println("Retrieving Snapshot");
	Bank.RetrieveSnapshot.Builder retrieveS = builder.getRetrieveSnapshot().toBuilder();
	if(retrieveS.getSnapshotId() == 1)
	{
		if(!channelState.isEmpty() && !bname.isEmpty() && channelState !=null && bname!=null)
		{
			System.out.println("process state is "+processState);
			System.out.println("channel state is "+channelState);
		}
	}	

/*	if(retrieveS.getSnapshotId() == 1)
	{
		if(!channelState.isEmpty() && !bname.isEmpty() && channelState !=null && bname!=null)
		{
			System.out.println("Channel state : ");
			for(int i=0;i<channelState.size();i++)
			{
				String[] st = channelState.get(i).split(" ");
				System.out.println(st[0] +" -> "+st[1]+" : "+st[2]);
			}		
		}

	}

*/	
/*
	Bank.ReturnSnapshot.Builder returnS = Bank.ReturnSnapshot.newBuilder();
	Bank.BranchMessage.Builder builder2 = Bank.BranchMessage.newBuilder();
	Bank.ReturnSnapshot.LocalSnapshot.Builder localS = Bank.ReturnSnapshot.LocalSnapshot.newBuilder();
	OutputStream outToServer=null;
	try{
		Bank.RetrieveSnapshot.Builder retrieveS = builder.getRetrieveSnapshot().toBuilder();	
		if(retrieveS.getSnapshotId() == 1)
		{
			
			//Bank.ReturnSnapshot.Builder returnS = Bank.ReturnSnapshot.newBuilder();
			
			localS.setSnapshotId(retrieveS.getSnapshotId());
			localS.setBalance(processState);
			int i=0;
			int j=0;
			if(!channelState.isEmpty() && !bname.isEmpty() && channelState !=null && bname!=null)
			{
			for(j=0;j<bname.size();j++)
			{
				int branchSum=0;
				for(i=0;i<channelState.size();i++)
				{
					String[] st = channelState.get(i).split(" ");
					if(bname.get(j).equals(st[0]))
					{					
						branchSum = branchSum + Integer.parseInt(st[2]);
						
					}
				}
				localS.setChannelState(1,branchSum);
				//localS.addAllChannelState(branchSum);
			}
			}
			returnS.setLocalSnapshot(localS);
			System.out.println("snapshot is "+ localS.getBalance());
			builder2.setReturnSnapshot(returnS);
		}
	}
	catch (Exception e) 
	{
		System.out.println(e);
        	e.printStackTrace();
	}

*/
}

public void startMarkers(Bank.BranchMessage builder)
{

	OutputStream outToServer=null;
	try{
		if(firstMarker == false)
		{	 
			firstMarker = true;
			Bank.InitSnapshot.Builder initS = builder.getInitSnapshot().toBuilder();
			Bank.Marker.Builder marker = Bank.Marker.newBuilder();
			marker.setSnapshotId(initS.getSnapshotId());
			marker.setBranchName(branchName);
			Bank.BranchMessage.Builder builder2 = Bank.BranchMessage.newBuilder();
			builder2.setMarker(marker);
			//setting process state
			processState = branchBalance;

			for (int j=0;j<bname.size();j++) 
			{
				if(bname.get(j).equals(branchName))
				{
					continue;		
				}		
				else
				{
					Socket client = new Socket(ips.get(j), ports.get(j));
         				outToServer = client.getOutputStream();
					builder2.build().writeTo(outToServer);
					outToServer.close();
				}
			}//end of for loop

		}//end of if 
		else
		{
			//System.out.println("prevChannelState.size() = "+prevChannelState.size());
			/*	for(int i=0;i<prevChannelState.size();i++)
				{
					System.out.println("prevChannelState.get("+i+") = "+prevChannelState.get(i));
					String str[] = 	prevChannelState.get(i).split(" ");
					if(str[1].equals(branchName))
					{
						channelState.add(i,prevChannelState.get(i));
						System.out.println("ChannelState.get("+i+") = "+channelState.get(i));
					}
	
				}
			*/
		}//end of else

	}//end of try

	catch (IOException e) 
	{
		System.out.println(e);
        	e.printStackTrace();
	}
		
	System.out.println("channel state is : " + channelState);
	System.out.println("processState  is :"+processState);
	

}



public void startTransferingSocket()
{
	Random r = new Random();
int i=2;
//while(getBranchBalance()>0)
while(i>0)
{
	synchronized(this){
	try{
		int rand1 = r.nextInt(5-1+1)+1;
		Thread.sleep(1000*rand1);		
		OutputStream outToServer=null;
		InputStream inFromServer=null;

		int rand = r.nextInt(5-1+1)+1;
		int randBranch;
		do{
			randBranch = r.nextInt(bname.size()-1-0+1);
		}while((bname.get(randBranch)).equals(branchName));

	
		try{
			
			trasferingSocket = new Socket(ips.get(randBranch),ports.get(randBranch));
			outToServer = trasferingSocket.getOutputStream();
			Bank.Transfer.Builder transfer = Bank.Transfer.newBuilder();
			int moneyT = getInitialBalance()*rand/100;
			DeductingBalance(moneyT);
			System.out.println("Transfered amount : "+moneyT +" to "+bname.get(randBranch));
			//System.out.println("branch bal="+branchBalance);
			transfer.setMoney(moneyT);
			//if(firstMarker == false)
			//{
			//	prevChannelState.add(branchName+ " " +bname.get(randBranch)+ " " +String.valueOf(moneyT)); //string with sender branch + receiver branch + amount sent
			//	System.out.println("PrevChannelState all : "+prevChannelState);
			//	System.out.println("PrevChannelState.get("+m+") = "+prevChannelState.get(m));
			//	m++;
			//}
			Bank.BranchMessage.Builder builder2 = Bank.BranchMessage.newBuilder();
			builder2.setTransfer(transfer);
			builder2.build().writeTo(outToServer);

			
			trasferingSocket.close();
		}
		catch(IOException e)
		{
			System.out.println(e);
		}

	}
	catch(InterruptedException e)
	{
		System.out.println(e);
	}	
}//end of synchronized

i--;
}//end of while
}

public void transferMoney(Bank.BranchMessage builder)
{
	Bank.Transfer.Builder transfer = builder.getTransfer().toBuilder();
	AddingBalance(transfer.getMoney());
	if(firstMarker == true)
	{
		channelState.add(branchName+ " " +transName+ " " +String.valueOf(transfer.getMoney())); //string with sender branch + receiver branch + amount sent
		//System.out.println("PrevChannelState all : "+channelState);
	}
				

	System.out.println("Received Balance :"+transfer.getMoney());
	//System.out.println("branch bal="+branchBalance);
	
}

public int getPort() 
{
	return this.port;
}

	
public void setPort(int port) 
{
	this.port = port;	
}

public String getIp() 
{
	return this.ip;
}

	
public void setIp(String ip) 
{
	this.ip = ip;	
}

public String getBranchName() 
{
	return this.branchName;
}

	
public void setBranchName(String bn) 
{
	this.branchName= bn;	
}

public int getBranchBalance() 
{
	return this.branchBalance;
}

	
public void setBranchBalance(int branchBalance) 
{
	this.branchBalance = branchBalance;	
}

public int getInitialBalance() 
{
	return this.initialBalance;
}

	
public void setInitialBalance(int branchBalance) 
{
	this.initialBalance= branchBalance;	
}


public void AddingBalance(int bal)
{
	this.branchBalance = this.branchBalance + bal;
}

public void DeductingBalance(int bal)
{
	this.branchBalance = this.branchBalance - bal;

}

 
public static void main(String[] args) throws Exception {

	int port = Integer.parseInt(args[1]);
	String bname = args[0];

  	try {
    		Branch branch = new Branch(port,bname);
    		branch.start();
  	} 
  	catch (IOException e) {
    		e.printStackTrace();
  	}


	}//end of main

}//end of class
