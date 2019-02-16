import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.FileReader;



class Controller {
 
public static int bank_total;
public static ArrayList<String> list_of_branches;


public static void main(String[] args) throws Exception {

	
	bank_total=Integer.parseInt(args[0]);
	//System.out.println("total bank amt= "+bank_total);
	list_of_branches=new ArrayList<String>();

	FileReader fr=null;
	BufferedReader br=null;
	try
	{

		fr=new FileReader(args[1]);
		br=new BufferedReader(fr);
		String line;
		while((line=br.readLine())!= null)
		{
			
			list_of_branches.add(line); 
			//System.out.println(line);
		}
	}
	catch(FileNotFoundException e)
	{
		System.out.println("file is not found\n");
	}
	catch(IOException e)
	{
		System.out.println("exe 2\n");
	}
	finally
	{
		try{
		if(br!=null)
			br.close();
		}
		catch(IOException e)
		{
		System.out.println("exe 2\n");
		}

	}



	

	//socket opening

	OutputStream outToServer=null;
	InputStream inFromServer=null;
	

	try {

		for(int i=0;i<list_of_branches.size();i++)
		{
         	
			String[] str = list_of_branches.get(i).split(" ");
			//System.out.println("Connecting to " + str[1] + " on port " + str[2]);
         		Socket client = new Socket(str[1], Integer.parseInt(str[2]));
         		//System.out.println("Just connected to " + client.getRemoteSocketAddress());
			outToServer = client.getOutputStream();
         
			
			//SETTING INIT BARNCH VALUES

			Bank.InitBranch.Builder init = Bank.InitBranch.newBuilder();
			int bal = Integer.parseInt(args[0])/ list_of_branches.size();
			init.setBalance(bal);
			Bank.BranchMessage.Builder builder = Bank.BranchMessage.newBuilder();
		
			for(int j=0;j<list_of_branches.size();j++)
			{
				Bank.InitBranch.Branch.Builder branch=Bank.InitBranch.Branch.newBuilder();
				String str2[] = list_of_branches.get(j).split(" ");
				branch.setPort(Integer.parseInt(str2[2]));
				branch.setIp(str2[1]);
				branch.setName(str2[0]);
				init.addAllBranches(branch);
			
			}

    					

			builder.setInitBranch(init);
			builder.build().writeTo(outToServer);
        		client.close();
		
		}//end of for loop
		


		//START OF SNAPSHOT MESSAGE
		int k=1;
		try
		{
			Thread.sleep(2000);
			while(k==1)
			{
		
				Random r = new Random();
				int rand = r.nextInt(list_of_branches.size());
				String[] str = list_of_branches.get(rand).split(" ");
			        Socket client = new Socket(str[1], Integer.parseInt(str[2]));
         			outToServer = client.getOutputStream();
         
			
				//setting snapshot values

				Bank.InitSnapshot.Builder initS = Bank.InitSnapshot.newBuilder();
				initS.setSnapshotId(k);
				Bank.BranchMessage.Builder builder = Bank.BranchMessage.newBuilder();
				builder.setInitSnapshot(initS);
				builder.build().writeTo(outToServer);
        			client.close();
				k++;
			}//end of while
		}//end of try
		catch(InterruptedException e)
		{
			System.out.println(e);
		}


		//START OF RETRIVE SNAPSHOT MESSAGE
		try {
			Thread.sleep(2000);
			for(int i=0;i<list_of_branches.size();i++)
			{
         	
				String[] str = list_of_branches.get(i).split(" ");
				Socket client = new Socket(str[1], Integer.parseInt(str[2]));
         			outToServer = client.getOutputStream();
		
				//setting retrive snapshot value values

				Bank.RetrieveSnapshot.Builder retrieveS = Bank.RetrieveSnapshot.newBuilder();
				retrieveS.setSnapshotId(1);
				Bank.BranchMessage.Builder builder = Bank.BranchMessage.newBuilder();
				builder.setRetrieveSnapshot(retrieveS);
				builder.build().writeTo(outToServer);
				
		
			}//end of for loop
		/*	ServerSocket serverSocket = new ServerSocket(9000);
			Socket server = serverSocket.accept();
			Bank.BranchMessage builder3 = Bank.BranchMessage.parseFrom(inFromServer);
			
				if(builder3.hasReturnSnapshot())
				{
				System.out.println("has snapshot2 !");

				//Bank.ReturnSnapshot.Builder returnS = builder3.getReturnSnapshot.toBuilder();
				Bank.ReturnSnapshot.LocalSnapshot localS = builder3.getReturnSnapshot().getLocalSnapshot();
				System.out.println("return snapshot balnce is "+localS.getBalance());
				System.out.println("before closing retrive snapshot2!");
				}
				else
				{
					System.out.println("snapshot2 not present!");

				}
        			server.close();
		*/

		}  //end of socket try
		catch(InterruptedException e)
		{
			System.out.println(e);
		}
		catch(EOFException e)
		{
			System.out.println("end of files!");
		}
		catch (IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
        	
		}
	

	}
	catch(IOException e)
	{
		System.out.println(e);
		e.printStackTrace();
	}





	}//end of main
}//end of class

