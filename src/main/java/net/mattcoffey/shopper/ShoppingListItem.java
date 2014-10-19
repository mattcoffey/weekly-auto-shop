package net.mattcoffey.shopper;

/**
 * Immutable datatype containing a desired basket item and quantity
 * 
 * @author mcoffey
 */
public class ShoppingListItem {
    
    private final String itemName;
    private final Integer amount;
    
    /**
     * Constructor.
     * @param itemName
     * @param amount
     */
    public ShoppingListItem(String itemName, Integer amount) {
        this.itemName = itemName;
        this.amount = amount;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public Integer getAmount() {
        return amount;
    }
    
    public String toString() {
        return "Item: " + itemName + " Amount: " + amount;
    }
}