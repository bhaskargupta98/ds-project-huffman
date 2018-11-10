import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import com.sun.xml.internal.rngom.parse.host.Base;
import org.apache.commons.codec.binary.Base64;

public class HuffmanEncoder {
    private static final int ALPHABET_SIZE = 256;
    public HuffmanEncodedResult compress(final String data){
        final int[] freq = buildFrequencyTable(data);
        final Node root = buildHuffmanTree(freq);
        final Map<Character,String> lookupTable = lookupTable(root);

        return new HuffmanEncodedResult(generatedEncodedData(data, lookupTable), root);
    }

    private static String generatedEncodedData(String data, Map<Character, String> lookupTable) {
        final StringBuilder builder = new StringBuilder();
        for(char character : data.toCharArray()){
            builder.append(lookupTable.get(character));
        }
        return builder.toString();
    }

    private static Map<Character, String> lookupTable(final Node root){
        final Map<Character,String> lookupTable = new HashMap<>();
        lookupTableHelper(root, "", lookupTable);
        return lookupTable;
    }

    private static void lookupTableHelper(Node root, String s, Map<Character, String> lookupTable) {
        if(!root.isLeaf()){
            lookupTableHelper(root.leftChild,s+'0',lookupTable);
            lookupTableHelper(root.rightChild,s+'1',lookupTable);
        }
        else{
            lookupTable.put(root.character,s);
        }
    }

    private static Node buildHuffmanTree(int[] freq){
        final PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
        for(char i=0;i<ALPHABET_SIZE;i++){
            if(freq[i]>0){
                priorityQueue.add(new Node( i,freq[i],null,null));

            }
        }
        if(priorityQueue.size() == 1){
            priorityQueue.add(new Node('\0',1,null,null));
        }
        while(priorityQueue.size()>1){
            final Node left = priorityQueue.poll();
            final Node right = priorityQueue.poll();
            final Node parent = new Node('\0', left.frequency + right.frequency,left,right);
            priorityQueue.add(parent);
        }
        return priorityQueue.poll();
    }
    private static int[] buildFrequencyTable (final String data){
        final int[] freq = new int[ALPHABET_SIZE];
        for(final char character : data.toCharArray()){
            freq[character]++;

        }
        return freq;
    }
    public static String encodeImage(byte[] imageByteArray){
        return Base64.encodeBase64URLSafeString(imageByteArray);
    }
    public String decompress(final HuffmanEncodedResult result){
        final StringBuilder resultBuilder = new StringBuilder();
        Node current = result.getRoot();
        int i=0;
        while(i<result.getEncodedData().length()){
            while(!current.isLeaf()){
                char bit = result.getEncodedData().charAt(i);
                if(bit == '1'){
                    current = current.rightChild;
                }
                else if(bit == '0'){
                    current = current.leftChild;
                }
                else{
                    throw new IllegalArgumentException("Bit in message is invalid "+ bit);
                }
                i++;
            }
            resultBuilder.append(current.character);
            current = result.getRoot();
        }

        return resultBuilder.toString();
    }
    static class Node implements Comparable<Node>{
        private final char character;
        private final int frequency;
        private Node leftChild;
        private Node rightChild;
        private Node(final char character, final int frequency, final Node leftChild, final Node rightChild){
            this.character = character;
            this.frequency = frequency;
            this.leftChild = leftChild;
            this.rightChild = rightChild;

        }
        boolean isLeaf(){
            return this.leftChild==null && this.rightChild==null;
        }

        @Override
        public int compareTo(Node that) {
            final int frequencyComparison = Integer.compare(this.frequency, that.frequency);
            if(frequencyComparison!=0){
                return frequencyComparison;
            }
            return Integer.compare(this.character, that.character); //just so we get any result
        }
    }
    static class HuffmanEncodedResult{
        final Node root;
        final String encodedData;
        HuffmanEncodedResult(final String encodedData, final Node root){
            this.encodedData = encodedData;
            this.root = root;
        }
        public Node getRoot(){
            return this.root;
        }
        public String getEncodedData(){
            return this.encodedData;
        }
    }

    public static void main(String[] args) {
        File file = new File("/Users/bhaskargupta/Desktop/29899818927_69b5977cfe_k.jpg");
        try {
            FileInputStream imageInFile = new FileInputStream(file);
            byte imageData[] = new byte[(int) file.length()];
            imageInFile.read(imageData);
            String imageDataString = encodeImage(imageData);
            final HuffmanEncoder encoder = new HuffmanEncoder();
            final HuffmanEncodedResult result = encoder.compress(imageDataString);
            int a = result.encodedData.length();
            int b = encoder.decompress(result).length()*64;
            System.out.println("Encoded message: " + result.encodedData.length()+ " bits");
            System.out.println("Decoded message: " + encoder.decompress(result).length()*64+ " bits");
            System.out.println("Percentage compression: "+ (100.00-(float)a/b*100));
        } catch (FileNotFoundException e) {
            System.out.println("Image not found" + e);
        } catch (IOException ioe){
            System.out.println("Exception while reading the image" + ioe);
        }



    }
}
