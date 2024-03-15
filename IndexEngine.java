/**
 * Author: kanykei nurmambetova
 * Date: Oct 24, 2023
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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class IndexEngine {

    //creates lexicon where index is integer id, and the value is the term
    public static ArrayList<String> lexicon = new ArrayList<String>();

    //inverted index: arraylist of arraylists of objects that contain docId and Count params
    public static ArrayList<ArrayList<Posting>> invIndex = new ArrayList<ArrayList<Posting>>();

    public static void main(String[] args) throws IOException {
        
        String gzFilePathString = "";
        String storagePathString = "";

        //PART A) PROGRAM ACCEPTS TWO COMMAND LINE ARGUMENTS and PART C) HANDLES IF NO ARGUMENTS SUPPLIED
        try{
            gzFilePathString = args[0]; //gz file path must include the name of the .gz file
            storagePathString = args[1];
        } catch(ArrayIndexOutOfBoundsException e){
            System.out.println("Only One or No arguments supplied to the program. " 
            + "Please enter two arguments, the path to the dircetory with gzipped file "
            + "(latimes.gz), and the other, a path to the directory where you would like to store the docs and metadata. \n" 
            + "Exiting program.");
            e.printStackTrace();
            System.exit(0);
        }

        //Path inputPath = Paths.get(gzFilePathInput);
        Path storagePath = Paths.get(storagePathString);

        //PART B) CREATE STORAGE DIRECTORY AND CHECK IF EXISTS
        checkIfFolderExists(storagePath);
        
        //PART D) READING GZIPPED FILE (latimes.gz)
        ///////////////ASSIGNMENT 2////////////////
        //Problem 1:
        //* Perform in-memory inversion of the LATimes collection (from TEXT, HEADLINE, GRAPHIC)
        readLinesFromGZ(gzFilePathString, storagePathString);

        //Serialization of the inverted index
        invIndexSerialization();

        System.out.println("KANYKEI, program ended.");
    }

    //Serialization for after Problem 1 of Assignment 2
    public static void invIndexSerialization(){
        try{

            FileOutputStream fout = new FileOutputStream("output.txt");
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
    public static String readLinesFromGZ(String filePath, String storagePath) {
        String lines = ""; //to be saved as a text file
        String textToToken =""; //to read in text from TEXT, HEADLINE, and GRAPHIC tags
        File file = new File(filePath); //the place to read from
        int idOfDoc = 0;
        String headlineOfDoc = "";
        String docnoOfDoc = "";

        try (GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
                BufferedReader br = new BufferedReader(new InputStreamReader(gzip));) {
            String line = null;
            String fileFolderNameString = ""; //path to folder named with date
            String date = ""; //date, i.e. January 1, 1989
            String delims = "[,]+";

            int d = 0;
            int h = 0;
            int t = 0;
            int g = 0;
            //int k =0;
            while ((line = br.readLine()) != null ) { //&& k<1000
                //k++;
                lines = lines + "\n" + line;

                //find docno
                if(line.contains("<DOCNO>")){
                    String[] docnoLineStrings = line.split(" ");
                    docnoOfDoc = (String) Array.get(docnoLineStrings,1);

                    date = docnoOfDoc.substring(2, 4) + 
                                    "-" + docnoOfDoc.substring(4, 6) + 
                                    "-19" + docnoOfDoc.substring(6, 8);
                    
                    fileFolderNameString = storagePath + "/" + date;
                }

                //find headline
                if(line.contains("<HEADLINE>")){
                    h++;
                } else if(h == 1){
                    h++;
                } else if(h == 2){
                    headlineOfDoc = line;
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
                Path fileFolderName = Paths.get(fileFolderNameString);
                String pathToFile = fileFolderNameString + "/" + docnoOfDoc + ".txt";
                if(line.contains("</DOC>")){
                    //Tokenization call. All text from TEXT, HEADLINE, and GRAPHIC is saved, now to tokenize it for the file
                    String tokens = getTokens(textToToken); ///////
                    String[] tokensArray = tokenize(tokens); ///////
                    int lengthOfDoc = tokensArray.length;
                    saveLenghtOfDoc(idOfDoc, lengthOfDoc);

                    //tokenIDs is a list of termIds that were found in the tokens. e.g. 3 5 6 3 5 3 6 8 0 1 3 6 0 0
                    ArrayList<Integer> tokenIDs = convertTokensToIDs(tokensArray, lexicon);
                    //get the count of termIds in the current doc 
                    HashMap<Integer, Integer> wordCounts = countWords(tokenIDs);

                    AddToPostings(wordCounts, idOfDoc, invIndex);

                    createFileWithContent(fileFolderNameString + "/" + docnoOfDoc + ".txt", lines, fileFolderName);
                    saveMetadataFromContent(line, date, idOfDoc, headlineOfDoc, docnoOfDoc, pathToFile);
                    idOfDoc++;
                    headlineOfDoc = "";
                    docnoOfDoc = "";
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

    /* @param file to a storage file
     * @output tries to create the file with the path given, if storage file exists, throws error */
    public static void checkIfFolderExists(Path filePath) throws IOException{
        try {

            Files.createDirectory(filePath);

        } catch (FileAlreadyExistsException e) {
            System.out.println("Doc and meta storage directory already exists");
            e.printStackTrace();
            System.exit(0);
        }
    }

    /* @param1 path of the txt file you are creating (path includes the folder named after the date)
     * @param2 content of the txt file you are creating
     * @param3 creates folder with the right date name, if already exists, nothing happens
     * @output creates the text file with the given content (param2) at the given path (param1)
     * creates the date directory if it doesn't already exists*/
    public static void createFileWithContent(String path, String content, Path directoryPath) {
        try{
            //Create the folder named after the date of the file
            //If it already exists, nothing happens
            Files.createDirectories(directoryPath);

            // Create new file
            File file = new File(path);
                        
            FileWriter fw = new FileWriter(file.getAbsoluteFile()); //this line here throws the problem
            BufferedWriter bw = new BufferedWriter(fw);

            // Write in file
            bw.write(content);

            // Close connection
            bw.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    /* Takes all params and saves it in the addressBook.txt as a new line */
    public static void saveMetadataFromContent(String content, String date, int id, String headline, String docno, String pathToFile){
        String metadata = "";
        metadata += id + "@" + date + "@" + headline + "@" + docno + "@" + pathToFile + "\n";
        
        try {
            File file = new File("addressBook.txt");
            BufferedWriter bufferWrite = new BufferedWriter(new FileWriter(file.getAbsolutePath(), true));

            //save to metadata to the address book file
            bufferWrite.write(metadata);
            if (bufferWrite != null )
                bufferWrite.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
   
    /* Save id of doc and its number of words (tokens) in the doc-lengths.txt as a new line */
    public static void saveLenghtOfDoc(int idOfDoc, int lengthOfDoc){
        String idToCount = "";
        idToCount += idOfDoc + " " + lengthOfDoc + "\n";
        
        try {
            File file = new File("doc-lengths.txt");
            BufferedWriter bufferWrite1 = new BufferedWriter(new FileWriter(file.getAbsolutePath(), true));

            //save to metadata to the doc-lengths.txt file
            bufferWrite1.write(idToCount);
            if (bufferWrite1 != null )
                bufferWrite1.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
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
            if (lexicon.contains(token)){
                tokenIDs.add(lexicon.indexOf(token));
            } else {
                lexicon.add(token);
                tokenIDs.add(lexicon.indexOf(token));
            }
        }
        return tokenIDs;
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
}
