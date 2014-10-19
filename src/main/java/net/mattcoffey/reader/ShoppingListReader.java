package net.mattcoffey.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.mattcoffey.shopper.ShoppingListItem;

import net.mattcoffey.config.Config;

/**
 * Reads shopping list from file
 * @author mcoffey
 */
public class ShoppingListReader {
    
    /**
     * The file configuration
     */
    private Config config;

    /**
     * Constructor.
     * @param config
     */
    public ShoppingListReader(Config config) {
        this.config = config;
    }

    /**
     * @return the shopping list
     * @throws IOException 
     */
    public List<ShoppingListItem> readShoppingList() throws IOException {
        List<ShoppingListItem> shoppingList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(config.getShoppingListPath()));
        String line;
        try {
            while((line = reader.readLine()) != null) {
                ShoppingListItem item = parseLine(line);
                if (item != null) {
                    shoppingList.add(item);
                }
            }
        }
        finally {
            close(reader);
        }
        
        return shoppingList;
    }
    
    /**
     * close the reader
     * @param reader
     */
    private void close(BufferedReader reader) {
        try {
        reader.close();
        }
        catch (IOException e) {
           //Ignore failed close attempt 
        }
    }
    
    /**
     * @param line
     * @return the ShoppingListItem represented by the line
     */
    private ShoppingListItem parseLine(String line) {
        if(isEmpty(line)) {
            return null;
        }
        else if(isComment(line)) {
            return null;
        }
            
        String[] parts = line.split("\\|");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid line in input file: " + line);
        }
        
        Integer amount;
        try {
            amount = Integer.parseInt(parts[0]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid amount: " + parts[0] + "for line " + line);
        }
        
        return new ShoppingListItem(parts[1], amount);
    }

    /**
     * @param line
     * @return true if the line is null or empty
     */
    private boolean isEmpty(String line) {
        return line == null
            || line.isEmpty();
    }

    /**
     * @param line
     * @return true if the line is a comment
     */
    private boolean isComment(String line) {
        return line.trim().startsWith("#");
    }
}