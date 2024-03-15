import java.io.Serializable;

public class Posting implements Serializable {
    int docId;
    int count;

    public Posting(int docId, int count) {
        this.docId = docId;
        this.count = count;
    }

}
