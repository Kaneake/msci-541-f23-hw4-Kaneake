# msci-541-f23-hw4-Kaneake
msci-541-f23-hw4-Kaneake created by GitHub Classroom
#### Author: Kanykei Nurmambetova
#### Student ID#: 20872575

# How to build and run the code

IndexEngine program consumes documents as its input. - Metadata storage file, - (latimes.dat.gz) document to process file.

#### 1. Open command prompt/shell/terminal. Navigate to SearchEngine cloned repository on your device.

<i>
  The program accepts two line command arguments: a path to the latimes.gz file and a path to a directory
  where the documents and metadata will be stored. 
</i>

#### 2.A Run document storage program called IndexEngine.java as follows:
```
javac IndexEngine.java
java IndexEngine <path to the latimes.gz file> <path to directory where the docs and metadata will be stored>
```
<p>
  <i>
    When the program runs, you should see a folder for storage created. In there, folders - named after dates. Each of the dated folders will contain .txt files containing raw documents of the latimes articles that were released on the same date as the name of their dated directory.
    You will also get a .txt file called addressBook that stores metadata of each article.

    You will also get a .txt file called doc-lengths that store count of tokens of each article.

    You will also get a .txt file called output that is used to serialize and deserialize lexicon and inverted index for saving and loading.
  </i>
  <br><br>
  Take note that if you are writing <b>relative paths</b> for inputs, the pathing pointer starts inside the repository root folder.
  So if I my latimes.dat.gz file is in the same folder as <u>msci-541-f23-hw4-Kaneake</u>, the path to it will be:

  ```
  java IndexEngine ../latimes.dat.gz <path to directory where the docs and metadata will be stored>
  ```

  The usage of '/' may vary if you are using Windows or Mac. The above usage is for terminals on Mac.
</p>

#### 2.B Run BM25.java to create hw4-bm25-baseline-knurmamb.txt TREC file:
```
javac BM25.java
java BM25 output.txt queries.txt
```
This will take a long time to run. After all, we are retrieving and BM25 judging all documents that have any terms from the query. We sort them as we go and at the end, we store the top 1000 results per topic into the text file as per HW4 instructions. Feel free to go for a walk or grab a cup of tea. The results will be posted in the newly created file called hw4-bm25-baseline-knurmamb.txt.

#### 3.A Run IndexEngineStemmed.java to create inverted index and lexicon through Porter Stemmer as follows:
```
javac IndexEngineStemmed.java
java IndexEngineStemmed <path to the latimes.gz file>
```
<p>
  <i>
    You will see a .txt file called outputStemme that is used to serialize and deserialize stemmed lexicon and inverted index for saving and loading.
  </i>
</p>

#### 3.B Run BM25.java to create hw4-bm25-stem-knurmamb.txt TREC file:
```
javac BM25ForStem.java
java BM25ForStem stemOutput.txt queries.txt
```
The results will be posted in the newly created file called hw4-bm25-stem-knurmamb.txt.
