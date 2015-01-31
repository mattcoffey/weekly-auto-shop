package net.mattcoffey.shopper;

/**
 * Immutable datatype containing a desired basket item and quantity
 * 
 * @author mcoffey
 */
public class ShoppingListItem {
    
    /**
     * the name of the item to purchase
     */
    private final String itemName;
    
    /**
     * the number of items to purchase
     */
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((amount == null) ? 0 : amount.hashCode());
        result = prime * result + ((itemName == null) ? 0 : itemName.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ShoppingListItem other = (ShoppingListItem) obj;
        if (amount == null) {
            if (other.amount != null) {
                return false;
            }
        }
        else if (!amount.equals(other.amount)) {
            return false;
        }
        if (itemName == null) {
            if (other.itemName != null) {
                return false;
            }
        }
        else if (!itemName.equals(other.itemName)) {
            return false;
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "Item: " + itemName + " Amount: " + amount;
    }
    
    /**
     * @return the name of the item to purchase
     */
    public String getItemName() {
        return itemName;
    }
    
    /**
     * @return the amount of items to purchase
     */
    public Integer getAmount() {
        return amount;
    }
}