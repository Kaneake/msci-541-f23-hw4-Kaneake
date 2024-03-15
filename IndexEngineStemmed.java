/**
 * Author: kanykei nurmambetova
 * Date: Feb 26, 2024
 * Course: MSCI 541
 */

import java.io.BufferedReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;

public class IndexEngineStemmed {

    //creates lexicon where index is integer id, and the value is the term
    public static ArrayList<String> lexicon = new ArrayList<String>();

    //inverted index: arraylist of arraylists of objects that contain docId and Count params
    public static ArrayList<ArrayList<Posting>> invIndex = new ArrayList<ArrayList<Posting>>();

    public static void main(String[] args) throws IOException {
        
        String gzFilePathString = "";

        try{
            gzFilePathString = args[0]; //gz file path must include the name of the .gz file
        } catch(ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
            System.exit(0);
        }

        //* Perform in-memory inversion of the LATimes collection (from TEXT, HEADLINE, GRAPHIC)
        readLinesFromGZ(gzFilePathString);

        //Serialization of the inverted index
        invIndexSerialization();

        System.out.println("KANYKEI, program ended.");
    }

    //Serialization for after Problem 1 of Assignment 2
    public static void invIndexSerialization(){
        try{

            FileOutputStream fout = new FileOutputStream("stemOutput.txt");
            ObjectOutputStream out = new ObjectOutputStream(fout);

            out.writeObject(invIndex);
            out.writeObject(lexicon);

            out.close();
            System.out.println("Serialization has been completed");

        } catch(Exception e) {System.out.println(e);}
    }

    /* @param1 path to find latimes.gz file
     * @param2 path to the storage
     * @output reads laimes.gz to find and save txt files in the right date folder */
    public static String readLinesFromGZ(String filePath) {
        String lines = ""; //to be saved as a text file
        String textToToken =""; //to read in text from TEXT, HEADLINE, and GRAPHIC tags
        File file = new File(filePath); //the place to read from
        int idOfDoc = 0;

        try (GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
                BufferedReader br = new BufferedReader(new InputStreamReader(gzip));) {
            String line = null;

            int h = 0;
            int t = 0;
            int g = 0;
            //int k =0;
            while ((line = br.readLine()) != null ) { //&& k<1000
                //k++;
                lines = lines + "\n" + line;

                //find headline
                if(line.contains("<HEADLINE>")){
                    h++;
                } else if(h == 1){
                    h++;
                } else if(h == 2){
                    textToToken = textToToken + line; //add HEADLINE content to 'text to tokenize'
                    h=0;
                }

                //read text from TEXT tags without including TEXT tags and save to 'text to tokenize'
                if(line.contains("<TEXT>")){
                    t=1;
                } else if (line.contains("</TEXT>")){
                    t=0;
                } else if(line.contains("<P>") || line.contains("</P>")){
                    //do nothing, dont add that line to 'text to token'
                } else if(t==1){
                    textToToken = textToToken + line;
                }

                //read text from GRAPHIC tags without including GRAPHIC tags and save to 'text to tokenize'
                if(line.contains("<GRAPHIC>")){
                    g=1;
                } else if (line.contains("</GRAPHIC>")){
                    g=0;
                } else if(line.contains("<P>") || line.contains("</P>")){
                    //do nothing, dont add that line to 'text to token'
                } else if(g==1){
                    textToToken = textToToken + line;
                }

                //document completely read, so we save it into a file in the right date-named directory
                if(line.contains("</DOC>")){
                    //Tokenization call. All text from TEXT, HEADLINE, and GRAPHIC is saved, now to tokenize it for the file
                    String tokens = getTokens(textToToken); ///////
                    String[] tokensArray = tokenize(tokens); ///////

                    //tokenIDs is a list of termIds that were found in the tokens. e.g. 3 5 6 3 5 3 6 8 0 1 3 6 0 0
                    ArrayList<Integer> tokenIDs = convertTokensToIDs(tokensArray, lexicon);
                    //get the count of termIds in the current doc 
                    HashMap<Integer, Integer> wordCounts = countWords(tokenIDs);

                    AddToPostings(wordCounts, idOfDoc, invIndex);

                    idOfDoc++;
                    lines="";
                    textToToken="";
                }
                
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace(System.err);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return lines;
    }

    /* @param1 text we need to get the length of 
     * @output returns the tokens as an array of contiguous alphanumerics*/
    public static String[] tokenize(String tokens){
        // Use a regular expression to split the string on non-alphanumeric characters
        tokens = tokens.trim();
        String[] tokensArray = tokens.split("\\s+");
        return tokensArray;
    }

    /* @param1 text we need to tokenize
     * @output returns the tokens*/
    public static String getTokens(String textToTokenize){
        //lowercase all
        textToTokenize = textToTokenize.toLowerCase();
        //tokenize continuous alphanumeric terms
        textToTokenize = textToTokenize.replaceAll("[^a-zA-Z0-9]", " ");

        return textToTokenize;
    }

    //creates both TokenIDs and non-repeating lexicon
    public static ArrayList<Integer> convertTokensToIDs(String[] tokens, ArrayList<String> lexicon){
        ArrayList<Integer> tokenIDs = new ArrayList<Integer>();

        for (String token : tokens){
            String stem = PorterStemmer.stem(token);
            if (lexicon.contains(stem)){
                tokenIDs.add(lexicon.indexOf(stem));
            } else {
                lexicon.add(stem);
                tokenIDs.add(lexicon.indexOf(stem));
            }
        }
        return tokenIDs;
    }

    public static void AddToPostings(HashMap<Integer, Integer> wordCounts, int docId, ArrayList<ArrayList<Posting>> invIndex){
        for (Map.Entry<Integer,Integer> termId : wordCounts.entrySet()){
            int count = termId.getValue();
            int id = termId.getKey();
            Posting post = new Posting(docId, count);
            if(invIndex.size()>id){
                invIndex.get(id).add(post);
            }else{
                ArrayList<Posting> pos = new ArrayList<Posting>();
                pos.add(post);
                invIndex.add(invIndex.size(), pos);
            }
        }
    }

    public static HashMap<Integer, Integer> countWords(ArrayList<Integer> tokenIDs){
        HashMap<Integer, Integer> wordCounts = new HashMap<Integer, Integer>(); // let wordCounts be a dictionary  of termId to count
        for(int id : tokenIDs){
            if(wordCounts.containsKey(id)){
                wordCounts.put(id, wordCounts.get(id)+1);
            } else {
                wordCounts.put(id, 1);
            }
        }
        return wordCounts;
    }
}
