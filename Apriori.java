/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//package Apriori;
//
//  Author: Su Yibin, under the supervision of Howard Hamilton and
//          Mengchi Liu
//  Copyright: University of Regina and Su Yibin, April 2000
//  No reproduction in whole or part without maintaining this copyright
//  notice and imposing this condition on any subsequent users.
//

//---- apriori.java

//---- input file need:
//----   1. config.txt
//----      four lines, each line a integer
//----      item number, transaction number , minsup
//----   2. transa.txt

import java.io.*;
import java.util.*;

//-------------------------------------------------------------
//  Class Name : apriori
//  Purpose    : main program class
//-------------------------------------------------------------
class Apriori
{
    public static void main(String[] args) throws IOException
    {
        AprioriProcess process1=new AprioriProcess();
        System.exit(0);
    }
}

//-------------------------------------------------------------
//  Class Name : aprioriProcess
//  Purpose    : main processing class
//-------------------------------------------------------------
class AprioriProcess
{
    private final int HT=1; // state of tree node (hash table or
    private final int IL=2; // itemset list)
    int N; // total number of items per transaction
    int M; // total number of transactions
    Vector<Vector<String>> largeitemset=new Vector<Vector<String>>();
    Vector<candidateelement> candidate=new Vector<candidateelement>();
    int minsup; // minimum support to make frequent
    String fullitemset;
    String configfile="config.txt"; // default configuration file
    String transafile="table.txt"; // default transaction file

    String[] nameList;

    //-------------------------------------------------------------
    //  Class Name : candidateelement
    //  Purpose    : object that will be stored in Vector candidate
    //             : include 2 item
    //             : a hash tree and a candidate list
    //-------------------------------------------------------------
    class candidateelement
    {
        hashtreenode htroot;
        Vector candlist;
    }

    //-------------------------------------------------------------
    //  Class Name : hashtreenode
    //  Purpose    : node of hash tree
    //-------------------------------------------------------------
    class hashtreenode
    {
        int nodeattr; //  IL or HT
        int depth; // the current itemset depth (ie: 1-itemset, 2-itemsets, etc)
        Hashtable<String, hashtreenode> ht;
        Vector<itemsetnode> itemsetlist;

        public void hashtreenode()
        {
            nodeattr=HT;
            ht=new Hashtable<String, hashtreenode>();
            itemsetlist=new Vector<itemsetnode>();
            depth=0;
        }

        public void hashtreenode(int i)
        {
            nodeattr=i;
            ht=new Hashtable<String, hashtreenode>();
            itemsetlist=new Vector<itemsetnode>();
            depth=0;
        }
    }

    //-------------------------------------------------------------
    //  Class Name : itemsetnode
    //  Purpose    : node of itemset
    //-------------------------------------------------------------
    class itemsetnode
    {
        String itemset;
        int counter;

        public itemsetnode(String s1,int i1)
        {
            itemset=new String(s1);
            counter=i1;
        }

        public itemsetnode()
        {
            itemset=new String();
            counter=0;
        }

        public String toString()
        {
            String tmp=new String();
            tmp=tmp.concat("<\"");
            tmp=tmp.concat(itemset);
            tmp=tmp.concat("\",");
            tmp=tmp.concat(Integer.toString(counter));
            tmp=tmp.concat(">");
            return tmp;
        }
    }

    //-------------------------------------------------------------
    //  Method Name: printhashtree
    //  Purpose    : print the whole hash tree
    //  Parameter  : htn is a hashtreenode (when other method call this method,it is the root)
    //             : transa : special transaction with all items occurr in it.
    //             : a : recursive depth
    //  Return     :
    //-------------------------------------------------------------
    public void printhashtree(hashtreenode htn,String transa,int a)
    {
        if (htn.nodeattr == IL )
        {
            System.out.println("Node is an itemset list");
            System.out.println("  depth :<"+htn.depth+">");
            System.out.println("  iteset:<"+htn.itemsetlist+">");
        }
        else
        { // HT
            System.out.println("Node is a hashtable");
            if (htn.ht==null)
                return;
            for (int b=a+1;b<=N;b++)
                if (htn.ht.containsKey((getitemat(b,transa))))
                {
                    System.out.println("  key:<"+getitemat(b,transa));
                    printhashtree((hashtreenode)htn.ht.get((getitemat(b,transa))),transa,b);
                }
        }
    }

    //-------------------------------------------------------------
    //  Method Name: getconfig
    //  Purpose    : open file config.txt
    //             : get the total number of items of transaction file
    //             : and the total number of transactions
    //             : and minsup
    //-------------------------------------------------------------
    public void getconfig() throws IOException
    {
        FileInputStream file_in;
        BufferedReader data_in;
        String oneline=new String();
        int i=0;

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String response = "";

        // ask if the user wants a non default config or transaction files
        System.out.println("Press 'C' to change the default configuration and transaction files");
        System.out.print("or any other key to continue.  ");
        try
        {
            response = reader.readLine();
        } 
        catch (Exception e)
        {
            System.out.println(e);
        }

        // if they want to change config or transaction files, get their input
        if(response.compareTo("C") * response.compareTo("c") == 0)
        {
            System.out.print("\nEnter new transaction filename: ");
            try
            {
                transafile = reader.readLine();
            }
            catch (Exception e)
            {
                System.out.println(e);
            }

            System.out.print("Enter new configuration filename: ");
            try
            {
                configfile = reader.readLine();
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
            System.out.println("Filenames changed");
        }

        //open the config file and load the values
        try
        {
            file_in = new FileInputStream(configfile);
            data_in = new BufferedReader(new InputStreamReader(file_in));

            //number of transactions
            oneline=data_in.readLine();
            N=Integer.valueOf(oneline).intValue();

            //number of items
            oneline=data_in.readLine();
            M=Integer.valueOf(oneline).intValue();

            //minsup
            oneline=data_in.readLine();
            minsup=Integer.valueOf(oneline).intValue();

            //output config info to the user
            System.out.print("\nInput configuration: "+N+" items, "+M+" transactions, ");
            System.out.println("minsup = "+minsup+"%");
            System.out.println();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    //-------------------------------------------------------------
    //  Method Name: getitemat
    //  Purpose    : get an item from an itemset
    //             : get the total number of items of transaction file
    //  Parameter  : int i : i-th item ; itemset : string itemset
    //  Return     : String : the item at i-th in the itemset
    //-------------------------------------------------------------
    public String getitemat(int i,String itemset)
    {
        String str1=new String(itemset); //copy the itemset into a new string
        StringTokenizer st=new StringTokenizer(itemset); //create a tokenizer
        int j;

        //if trying to get an position outside of the set, notify user of error
        if (i > st.countTokens())
            System.out.println("eRRor! in getitemat, !!!!");

        //loop through until get the token in the ith position
        for (j=1;j<=i;j++)
            str1=st.nextToken();

        //return the integer value of the ith position
        return(str1);
    }

    //-------------------------------------------------------------
    //  Method Name: itesetsize
    //  Purpose    : get item number of an itemset
    //  Parameter  : itemset : string itemset
    //  Return     : int : the number of item of the itemset
    //-------------------------------------------------------------
    public int itemsetsize(String itemset)
    {
        StringTokenizer st=new StringTokenizer(itemset);//tokenize the itemset
        return st.countTokens();//number of tokens is the size of the set
    }

    //-------------------------------------------------------------
    //  Method Name: gensubset
    //  Purpose    : generate all subset given an itemset
    //  Parameter  : itemset
    //  Return     : a string contains all subset deliminated by ","
    //             : e.g. "1 2,1 3,2 3" is subset of "1 2 3"
    //-------------------------------------------------------------
    public String gensubset(String itemset)
    {
        int len=itemsetsize(itemset);
        int i,j;
        String str1;
        String str2=new String();
        String str3=new String();

        if (len==1)
            return null;
        for (i=1;i<=len;i++)
        {
            StringTokenizer st=new StringTokenizer(itemset); //tokenize the itemset
            str1=new String();
            //add each token to str1 and put the spaces in
            for (j=1;j<i;j++)
            {
                str1=str1.concat(st.nextToken());
                str1=str1.concat(" ");
            }
            //
            str2=st.nextToken();
            for (j=i+1;j<=len;j++)
            {
                str1=str1.concat(st.nextToken());
                str1=str1.concat(" ");
            }

            if (i!=1)
                str3=str3.concat(",");

            str3=str3.concat(str1.trim());
        }
        //  System.out.println(str3);
        return str3;

    } //end public String gensubset(String itemset)

    //-------------------------------------------------------------
    //  Method Name: createcandidate
    //  Purpose    : generate candidate n-itemset
    //  Parameter  : int n : n-itemset
    //  Return     : Vector : candidate is stored in a Vector
    //-------------------------------------------------------------
    public Vector createcandidate(int n)
    {
        Vector<String> tempcandlist=new Vector<String>();
        Vector ln_1=new Vector();
        int i,j,length1;
        String cand1=new String();
        String cand2=new String();
        String newcand=new String();

        //    System.out.println("Generating "+n+"-candidate item set ....");
        //if its the 1-itemset, just add all the items to the list
        if (n==1)
            for (i=1;i<=N;i++)
                tempcandlist.addElement(Integer.toString(i));
        //if its 2 or more itemset
        else
        {
            ln_1=(Vector)largeitemset.elementAt(n-2);
            length1=ln_1.size();
            //for each item in the set
            for (i=0;i<length1;i++)
            {
                cand1=(String)ln_1.elementAt(i);
                //check from the next one until the end and make new item sets
                for (j=i+1;j<length1;j++)
                {
                    cand2=(String)ln_1.elementAt(j);
                    newcand=new String();
                    if (n==2) //if depth = 2, then no formula to determine which ones can combine
                    {
                        newcand=cand1.concat(" ");
                        newcand=newcand.concat(cand2);
                        tempcandlist.addElement(newcand.trim());
                    }
                    else //first n-2 items in the itemset must be same for itemsets to be combined
                    {
                        int c;
                        String i1,i2;
                        boolean same=true;

                        for (c=1;c<=n-2;c++)
                        {
                            i1=getitemat(c,cand1);
                            i2=getitemat(c,cand2);
                            if ( i1.compareToIgnoreCase(i2)!=0 )
                            {
                                same=false;
                                break;
                            }
                            else
                            {
                                newcand=newcand.concat(" ");
                                newcand=newcand.concat(i1);
                            }
                        }
                        if (same) //if the first n-2 items are the same, combine the sets
                        {
                            i1=getitemat(n-1,cand1);
                            i2=getitemat(n-1,cand2);
                            newcand=newcand.concat(" ");
                            newcand=newcand.concat(i1);
                            newcand=newcand.concat(" ");
                            newcand=newcand.concat(i2);
                            tempcandlist.addElement(newcand.trim());
                        }
                    } //end if n==2 else
                } //end for j
            } //end for i
        } //end if n==1 else

        if (n<=2)
            return tempcandlist;

        Vector<String> newcandlist=new Vector<String>();
        //for each candidate, if already has the itemset (tokenizer splits at ",") then don't add it
        for (int c=0; c<tempcandlist.size(); c++)
        {
            String c1=(String)tempcandlist.elementAt(c);
            String subset=gensubset(c1);
            StringTokenizer stsubset=new StringTokenizer(subset,",");
            boolean fake=false;
            while (stsubset.hasMoreTokens())
                if (!ln_1.contains(stsubset.nextToken()))
                {
                    fake=true;
                    break;
                }
            if (!fake)
                newcandlist.addElement(c1);
        }

        return newcandlist;

    } //end public createcandidate(int n)

    //-------------------------------------------------------------
    //  Method Name: createcandidatehashtre
    //  Purpose    : generate candidate hash tree
    //  Parameter  : int n : n-itemset
    //  Return     : hashtreenode : root of the hashtree
    //-------------------------------------------------------------
    public hashtreenode createcandidatehashtree(int n)
    {
        int i,len1;
        hashtreenode htn=new hashtreenode();

        //    System.out.println("Generating candidate "+n+"-itemset hashtree ....");
        if (n==1)
            htn.nodeattr=IL;
        else
            htn.nodeattr=HT;

        len1=((candidateelement)candidate.elementAt(n-1)).candlist.size();
        for (i=1;i<=len1;i++)
        {
            String cand1=new String();
            cand1=(String)((candidateelement)candidate.elementAt(n-1)).candlist.elementAt(i-1);
            genhash(1,htn,cand1);
        }

        return htn;

    } //end public createcandidatehashtree(int n)


    //-------------------------------------------------------------
    //  Method Name: genhash
    //  Purpose    : called by createcandidatehashtree
    //             : recursively generate hash tree node
    //  Parameter  : htnf is a hashtreenode (when other method call this method,it is the root)
    //             : cand : candidate itemset string
    //             : int i : recursive depth,from i-th item, recursive
    //  Return     :
    //-------------------------------------------------------------
    public void genhash(int i, hashtreenode htnf, String cand) {

        int n=itemsetsize(cand);
        if (i==n) {
            htnf.nodeattr=IL;
            htnf.depth=n;
            itemsetnode isn=new itemsetnode(cand,0);
            if (htnf.itemsetlist==null)
                htnf.itemsetlist=new Vector<itemsetnode>();
            htnf.itemsetlist.addElement(isn);
        }
        else {
            if (htnf.ht==null)
                htnf.ht=new Hashtable<String, hashtreenode>(HT);
            if (htnf.ht.containsKey((getitemat(i,cand)))) {
                htnf=(hashtreenode)htnf.ht.get((getitemat(i,cand)));
                genhash(i+1,htnf,cand);
            }
            else {
                hashtreenode htn=new hashtreenode();
                htnf.ht.put((getitemat(i,cand)),htn);
                if (i==n-1) {
                    htn.nodeattr=IL;
                    //Vector isl=new Vector();
                    //htn.itemsetlist=isl;
                    genhash(i+1,htn,cand);
                }
                else {
                    htn.nodeattr=HT;
                    //Hashtable ht=new Hashtable();
                    //htn.ht=ht;
                    genhash(i+1,htn,cand);
                }
            }
        }
    } //end public void genhash(int i, hashtreenode htnf, String cand)

    //-------------------------------------------------------------
    //  Method Name: createlargeitemset
    //  Purpose    : find all itemset which have their counters>=minsup
    //  Parameter  : int n : n-itemset
    //  Return     :
    //-------------------------------------------------------------
    public void createlargeitemset(int n)
    {
        Vector candlist=new Vector();
        Vector<String> lis=new Vector<String>(); //large item set
        hashtreenode htn=new hashtreenode();
        int i;

        //    System.out.println("Generating "+n+"-large item set ....");
        candlist=((candidateelement)candidate.elementAt(n-1)).candlist;
        htn=((candidateelement)candidate.elementAt(n-1)).htroot;

        getlargehash(0,htn,fullitemset,lis);

        largeitemset.addElement(lis);

    } // end public void createlargeitemset(int n)


    //-------------------------------------------------------------
    //  Method Name: getlargehash
    //  Purpose    : recursively traverse candidate hash tree
    //             : to find all large itemset
    //  Parameter  : htnf is a hashtreenode (when other method call this method,it is the root)
    //             : cand : candidate itemset string
    //             : int i : recursive depth
    //             : Vector lis : Vector that stores large itemsets
    //  Return     :
    //-------------------------------------------------------------
    public void getlargehash(int i,hashtreenode htnf,String transa,Vector<String> lis)
    {
        Vector tempvec=new Vector();
        int j;

        if (htnf.nodeattr==IL)
        {
            tempvec=htnf.itemsetlist;
            for (j=1;j<=tempvec.size();j++)
                if (((itemsetnode)tempvec.elementAt(j-1)).counter >= ((minsup * M) / 100))
                    lis.addElement( ((itemsetnode)tempvec.elementAt(j-1)).itemset );
        }
        else
        {
            if (htnf.ht==null)
                return;
            for (int b=i+1;b<=N;b++)
                if (htnf.ht.containsKey((getitemat(b,transa))))
                    getlargehash(b,(hashtreenode)htnf.ht.get((getitemat(b,transa))),transa,lis);
        }
    }

    //-------------------------------------------------------------
    //  Method Name: transatraverse
    //  Purpose    : read each transaction, traverse hashtree,
    //               incrment approporiate itemset counter.
    //  Parameter  : int n : n-itemset
    //  Return     :
    //-------------------------------------------------------------
    public void transatraverse(int n)
    {
        FileInputStream file_in;
        BufferedReader data_in;
        String oneline=new String();
        int i=0,j=0;
        String transa;
        hashtreenode htn=new hashtreenode();
        StringTokenizer st;
        String str0;
        int numRead=0;

        //    System.out.println("Traverse "+n+"-candidate hashtree ... ");
        htn=((candidateelement)candidate.elementAt(n-1)).htroot;
        try
        {

            file_in = new FileInputStream(transafile);
            data_in = new BufferedReader(new InputStreamReader(file_in));


            // read the item names
            String nameString = data_in.readLine();
            nameList = nameString.split(",");


            while ( true )
            {
                transa=new String();
                oneline=data_in.readLine();
                numRead++;

                //if there are no more transactions, break the loop
                if ((oneline==null)||(numRead > M))
                    break;

                st=new StringTokenizer(oneline.trim());
                j=0;

                //check each item in the transaction and add to transaction string
                while ((st.hasMoreTokens()) && j < N)
                {
                    j++;
                    str0=st.nextToken();
                    i=Integer.valueOf(str0).intValue();
                    //add to string indicating what items are present in this transaction
                    if (i!=0)
                    {
                        transa=transa.concat(" ");
                        transa=transa.concat(Integer.toString(j));
                    }
                }
                transa=transa.trim();
                transatrahash(0,htn,transa);
            }
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    //-------------------------------------------------------------
    //  Method Name: transatrahash
    //  Purpose    : called by transatraverse
    //             : recursively traverse hash tree
    //  Parameter  : htnf is a hashtreenode (when other method call this method,it is the root)
    //             : cand : candidate itemset string
    //             : int i : recursive depth,from i-th item, recursive
    //  Return     :
    //-------------------------------------------------------------
    public void transatrahash(int i,hashtreenode htnf,String transa)
    {
        Vector itemsetlist=new Vector();
        int j,lastpos,len;
        String d;
        itemsetnode tmpnode=new itemsetnode();
        StringTokenizer st;

        if (htnf.nodeattr==IL)
        {
            itemsetlist=(Vector)htnf.itemsetlist;
            len=itemsetlist.size();
            for (j=0;j<len;j++)
            {
                st = new StringTokenizer(transa);
                tmpnode=(itemsetnode)itemsetlist.elementAt(j);
                d=getitemat(htnf.depth,tmpnode.itemset);

                while(st.hasMoreTokens())
                {
                    if(st.nextToken().compareToIgnoreCase(d)==0)
                        ((itemsetnode)(itemsetlist.elementAt(j))).counter++;
                }
                /*
                   System.out.println("transa: " + transa + "  d: " + d);
                   lastpos=transa.indexOf(Integer.toString(d));
                   System.out.println("Lastpos: " + lastpos);
                   if (lastpos!=-1)
                   {
                   System.out.println("here");
                   ((itemsetnode)(itemsetlist.elementAt(j))).counter++;
                   }
                   */
            }
            return;
        }
        else  //HT
            for (int b=i+1;b<=itemsetsize(transa);b++)
                if (htnf.ht.containsKey((getitemat(b,transa))))
                    transatrahash(i,(hashtreenode)htnf.ht.get((getitemat(b,transa))),transa);

    } // public transatrahash(int ii,hashtreenode htnf,String transa)

    //-------------------------------------------------------------
    //  Method Name: aprioriProcess()
    //  Purpose    : main processing method
    //  Parameters :
    //  Return     :
    //-------------------------------------------------------------
    public AprioriProcess()  throws IOException
    {
        candidateelement cande;
        int k=0;
        Vector large=new Vector();
        Date d=new Date();
        long s1,s2;

        System.out.println();
        System.out.println("Algorithm apriori starting now.....");
        System.out.println();

        getconfig();

        fullitemset=new String();
        fullitemset=fullitemset.concat("1");
        for (int i=2;i<=N;i++)
        {
            fullitemset=fullitemset.concat(" ");
            fullitemset=fullitemset.concat(Integer.toString(i));
        }

        //start time
        d=new Date();
        s1=d.getTime();

        while (true)
        {
            k++;
            cande=new candidateelement();
            cande.candlist=createcandidate(k);

            //      System.out.println("C"+k+"("+k+"-candidate-itemset): "+cande.candlist);

            if (cande.candlist.isEmpty())
                break;

            cande.htroot=null;
            candidate.addElement(cande);

            ((candidateelement)candidate.elementAt(k-1)).htroot=createcandidatehashtree(k);

            //      System.out.println("Now reading transactions, increment counters of itemset");
            transatraverse(k);

            createlargeitemset(k);
            System.out.println("Frequent "+k+"-itemsets:");
            System.out.println((Vector)(largeitemset.elementAt(k-1)));
            for (Object obj : largeitemset.elementAt(k-1)) {
                String listStr = (String)obj;
                String[] items = listStr.split(" ");
                for (String item : items) {
                    int index = Integer.parseInt(item);
                    System.out.print(nameList[index] + " ");
                }
                System.out.println("");
            }
        }

        hashtreenode htn=new hashtreenode();
        htn=((candidateelement)candidate.elementAt(k-2)).htroot;

        //end time
        d=new Date();
        s2=d.getTime();
        System.out.println();
        System.out.println("Execution time is: "+((s2-s1)/(double)1000) + " seconds.");

    }
}
