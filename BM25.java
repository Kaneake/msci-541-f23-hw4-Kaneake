import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class BM25 {
    //creates lexicon where index is integer id, and the value is the term
    public static ArrayList<String> lexicon = new ArrayList<String>();
    //inverted index: arraylist of arraylists of objects that contain docId and Count params
    public static ArrayList<ArrayList<Posting>> invIndex = new ArrayList<ArrayList<Posting>>();
    public static double k1 = 1.2;
    public static double b=0.75;
    public static double avgDocLen = 523.55;
    public static BufferedReader br;

    public static void main(String[] args) throws IOException {
        String indexPathString = "";
        String queriesPathString = "";

        //create baseline txt file
        //topicID 0 docID BM25
        File baseline = new File("hw4-bm25-baseline-knurmamb.txt");
        FileWriter fw = new FileWriter(baseline.getAbsoluteFile()); //this line here throws the problem
        BufferedWriter bw = new BufferedWriter(fw);

        try{ //accepting commands
            indexPathString = args[0];
            queriesPathString = args[1];

            FileReader fr = new FileReader(queriesPathString); //place to read from
            br = new BufferedReader(fr);
        } catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            System.exit(0);
        } catch (FileNotFoundException e) {
            System.out.println("Queries file, second argument, is not found");
            e.printStackTrace();
        }

        GetLexiconAndIndex(indexPathString);
        
        for(int topicCount = 0; topicCount < 1; topicCount++){ //for every topic
            String topicID = GetID();
            int[] queryAsIds = GetQueryAsIds();
            TreeMap<Double, String> allResults = new TreeMap<>(Comparator.reverseOrder());
            
            for(int queryID = 0; queryID < queryAsIds.length; queryID++){ //for every term in query
                //Get ArrayList of posting objects of docs and term frequencies for given query term ID
                ArrayList<Posting> posts = invIndex.get(queryID);
                double docLength = GetDocLength(queryID);
                int N = 78731;
                int ni = posts.size();
                double IDF = Math.log((N - ni + 0.5)/(ni + 0.5));

                //BM25(D,Q, frequency of term i in doc D)
                for(int docCount = 0; docCount < posts.size(); docCount++){
                    double result = calculateBM25(posts.get(docCount).docId, queryID, posts.get(docCount).count, IDF, docLength);
                    String docno = GetDocNo(posts.get(docCount).docId);
                    allResults.put(result, (topicID + " 0 " + docno + " " + result));
                }
            }

            int k = 0;
            for(Map.Entry<Double, String> entry : allResults.entrySet()){
                if(k==1000) {break;}
                bw.write(entry.getValue()+"\n"); // Write in file
                k++;
            }
        }
        // Close connections
        br.close();
        bw.close();
    }

    //Deserialize lexicon and index
    @SuppressWarnings (value="unchecked")
    public static void GetLexiconAndIndex(String indexPathString){
        try{
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(indexPathString));         
            invIndex = (ArrayList<ArrayList<Posting>>)in.readObject();
            lexicon = (ArrayList<String>)in.readObject();
            in.close();
        }catch(Exception e){System.out.println(e);}
    }

    public static String GetID(){
        try{
            String query = br.readLine();
            return query; 
        } catch (IOException e) {
            System.out.println("Error with GetID function");
            e.printStackTrace();
        }
        System.out.println("GetQuery function did not find query. Exit program.");
        System.exit(0);
        return "";
    }

    public static int[] GetQueryAsIds(){
        try{
            String query = br.readLine();
            //tokenize
            query = query.toLowerCase();
            String[] tokensArray = query.split("\\s+");
            int[] queryAsIds = new int[tokensArray.length];

            //after the for loop, the queryAsIds is int[], a query as termIds
            for(int i=0; i<tokensArray.length; i++){
                if(lexicon.contains(tokensArray[i])){
                    queryAsIds[i] = lexicon.indexOf(tokensArray[i]);
                } else {
                    //we dismiss the term we dont have in the lexicon
                }
            }

            return queryAsIds; 
            
        } catch (IOException e) {
            System.out.println("Error with GetQueryAsIds function");
            e.printStackTrace();
            
        }
        System.out.println("GetQuery function did not find query. Exit program.");
        System.exit(0);
        return new int[0];
    }

    public static double GetDocLength(int queryID) throws IOException{
        FileReader fr2 = new FileReader("doc-lengths.txt");
        BufferedReader br2 = new BufferedReader(fr2);
        String line= br2.readLine();
        String[] numList = line.split("\\s+");

        while(Integer.parseInt(numList[0]) != queryID){
            line= br2.readLine();
            numList = line.split("\\s+");
        }
        br2.close();

        return Double.parseDouble(numList[1]);
    }

    public static double calculateBM25(int docID, int queryID, int termFrequencyInDoc, double IDF, double docLength){
        double result = 0;
        result = IDF * (termFrequencyInDoc * (k1 + 1)/(termFrequencyInDoc + k1 * (1-b + b* docLength/avgDocLen)));
        return result;
    }

    public static String GetDocNo(int docID) throws IOException{
        //find docno given id
        FileReader fr5 = new FileReader("addressBook.txt");
        BufferedReader br5 = new BufferedReader(fr5);
        String line;
        String docIdString = Integer.toString(docID);

        while((line = br5.readLine())!=null){
            String[] arr = line.split("@");
            String idContent = (String) Array.get(arr,0);
            if(idContent.equals(docIdString)){
                break;
            }
        }
        br5.close();

        String[] arr = line.split("@");
        String docno = (String)Array.get(arr, 3);
        return docno;
    }
}
