package net.mattcoffey.shopper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.mattcoffey.config.Config;
import net.mattcoffey.reader.ShoppingListReader;

/**
 * Automates the browser to perform the shop
 * 
 * @author mcoffey
 */
public class WeeklyShop {
    
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(WeeklyShop.class);
    
    /**
     * The project configuration
     */
    private final Config config = new Config();
    
    /**
     * The browser driver
     */
    private final WebDriver driver = new FirefoxDriver();

    /**
     * Do the shop
     * @throws IOException 
     */
    public void doShop() throws IOException {
        
        List<ShoppingListItem> items = readShoppingList();
        
        login();
        
        emptyBasket();
        
        addItemsToBasket(items);
        
        List<ShoppingListItem> missing = findItemsMissingFromBasket(items);
        
        printMissingItems(missing);
        
        logger.info("Choose delivery slot, input payment details and confirm");
    }
    
    /**
     * @param missing
     */
    private void printMissingItems(List<ShoppingListItem> missing) {
        for(ShoppingListItem item : missing) {
            logger.error("Basket missing: {}", item);
        }
    }

    /**
     * @param itemsInShoppingList
     * @return items missing from the basket
     */
    private List<ShoppingListItem> findItemsMissingFromBasket(List<ShoppingListItem> itemsInShoppingList) {
        //TODO handle errors e.g. number format exception
        logger.info("Checking for items missing from the basket");
        
        WebElement fullTrollyView = driver.findElement(By.className("callToAction"));
        fullTrollyView.click();
        
        List<ShoppingListItem> itemsInBasket = getItemsInBasket();
        List<ShoppingListItem> itemsMissingFromBasket = new ArrayList<>();
        
        for(ShoppingListItem itemInShoppingList : itemsInShoppingList) {
            if(!itemsInBasket.contains(itemInShoppingList)) {
                itemsMissingFromBasket.add(itemInShoppingList);
            }
        }
        
        return itemsMissingFromBasket;
    }

    /**
     * @return the items in the basket
     */
    private List<ShoppingListItem> getItemsInBasket() {
        List<ShoppingListItem> itemsInBasket = new ArrayList<>();
        List<WebElement> itemNamesInBasket = driver.findElements(By.className("productContainer"));
        Iterator<WebElement> itemAmountsInBasket = driver.findElements(By.className("inTrolley")).iterator();
        
        for(WebElement element : itemNamesInBasket) {
            WebElement productElement = element.findElement(By.tagName("a"));
            WebElement amountElement = itemAmountsInBasket.next();
            ShoppingListItem item = new ShoppingListItem(stripCommas(productElement.getText()), extractAmount(amountElement));
            itemsInBasket.add(item);
        }
        
        return itemsInBasket;
    }
    
    /**
     * @param amountElement
     * @return the amount
     */
    private int extractAmount(WebElement amountElement) {
        return Integer.parseInt(amountElement.getText());
    }

    /**
     * @return the list of items on the shopping list
     * @throws IOException
     */
    private List<ShoppingListItem> readShoppingList() throws IOException {
        logger.info("Read Shopping List");
        return new ShoppingListReader(config).readShoppingList();
    }
    
    /**
     * Login to the shopping site
     */
    private void login() {
        logger.info("Logging in");
        driver.get("http://www.sainsburys.co.uk/shop/gb/groceries");
        
        WebElement username = driver.findElement(By.id("logonId"));
        username.sendKeys(config.getUsername());
        
        WebElement password = driver.findElement(By.id("logonPassword"));
        password.sendKeys(config.getPassword());
        
        WebElement login = driver.findElement(By.className("process"));
        login.click();
    }

    /**
     * Reset the contents of the basket (if any)
     */
    private void emptyBasket() {
        logger.info("Empty the previous shopping basket");
        
        WebElement emptyTrolleyLink = getEmptyTrollyLink();
        if(emptyTrolleyLink == null) {
            //Trolly is empty
            return;
        }
        
        emptyTrolleyLink.click();
        
        //Confirm empty
        List<WebElement> buttons = driver.findElements(By.className("button"));
        for (WebElement button : buttons) {
            if(button.getText().contains("Empty")) {
                button.click();
                return;
            }
        }
    }

    /**
     * @return the link to empty the trolley
     */
    private WebElement getEmptyTrollyLink() {
        try {
            return driver.findElement(By.id("emptyTrolleyLink"));
        }
        catch(Exception e) {
            // trolley is empty
            return null;
        }
    }

    /**
     * Add the items in the shopping list to the basket
     * @param items
     */
    private void addItemsToBasket(List<ShoppingListItem> items) {
        logger.info("Add Items to basket");
        for(ShoppingListItem item : items) {
            
            WebElement search = driver.findElement(By.id("search"));
            search.clear();
            sleep(1000L); //laggy javascript here
            search.sendKeys(item.getItemName());
            
            WebElement go = driver.findElement(By.name("searchSubmit"));
            go.click();
            
            addItemToBasket(item, driver);
        }
    }

    /**
     * Add an item to the shopping basket
     * 
     * @param item
     * @param driver
     */
    private void addItemToBasket(ShoppingListItem item, WebDriver driver) {
        
        try {
            WebElement product = findProductFromResults(item, driver);
            if (product != null) {
                addItemToBasket(item, product);
                return;
            }
            
        } catch(Exception e) {
            handleAddItemError(item, e);
            return;
        }
        handleAddItemError(item, null);
    }

    /**
     * @param item
     * @param driver
     * @return the product in the list of results or null if not present
     */
    private WebElement findProductFromResults(ShoppingListItem item, WebDriver driver) {
        WebElement productLister = driver.findElement(By.id("productLister"));
        for(WebElement product : productLister.findElements(By.className("product"))) {
            if(containsItem(product, item.getItemName())) {
                return product;
            }
        }
        
        return null;
    }

    /**
     * add the item to the basket
     * @param item
     * @param product
     */
    private void addItemToBasket(ShoppingListItem item, WebElement product) {
        WebElement quantity = product.findElement(By.name("quantity"));
        quantity.clear();
        quantity.sendKeys(item.getAmount().toString());
         
        WebElement add = product.findElement(By.name("Add"));
        add.click();
        logger.info("Added {}", item);
    }

    /**
     * @param item
     * @param e the error or null
     */
    private void handleAddItemError(ShoppingListItem item, Exception e) {
        //stack traces not that interesting to the end user
        logger.debug("Error adding item: {}", item, e);
    }
    
    /**
     * @param product
     * @param itemName
     * @return true if the supplied product WebElement matches the itemName
     */
    private boolean containsItem(WebElement product, String itemName) {
        String productName = product.findElement(By.className("productNameAndPromotions")).findElement(By.tagName("h3")).getText();
        productName = stripCommas(productName);
        return productName.contains(stripCommas(itemName));
    }

    /**
     * @param string 
     * @return the string with commas removed
     */
    private String stripCommas(String string) {
        return string.trim().replace(",", "");
    }

    /**
     * @param milliseconds
     */
    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e) {
        }
    }  
}