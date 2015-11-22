package net.mattcoffey.shopper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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
        
        deleteAllItems();
        
        addItemsToBasket(items);
        
        List<ShoppingListItem> missing = findItemsMissingFromBasket(items);
        
        printMissingItems(missing);
        
        checkout();
    }
    
    /**
     * Delete all items in the basket one by one.
     * (Workaround for the empty trolley button not functioning)
     */
    private void deleteAllItems() {
        // The whole table is reloaded by Ajax call whenever an item changes so it is necessary to get the list of items every time one is removed
        while(true) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector("a[class='repressive delete']"));
                if(elements.isEmpty()) {
                    //Trolley empty
                    return;
                }
                elements.iterator().next().click();
                driver.get(driver.getCurrentUrl());
            }
            catch (Exception e) {
                logger.error("Error removing item from trolley:", e);
            }
        }
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
        driver.get("https://www.sainsburys.co.uk/webapp/wcs/stores/servlet/LogonView?catalogId=10122&langId=44&storeId=10151&krypto=gPjk3%2BzluQwvKX86bngreDpsD2PZbgo0k%2BK1QRr9ktf2LQnIbem8yOn9zO92uvtU47wuMM1Yd97g%0AwRxQ05ZbtOW1HNtDiSA68zyCVSw5uV9R9PLBIUgNcWdzfwJu76J6ROykfvF081ZfenNS8LhYE%2F01%0AbRVPm0jH87tBNFudtyc2ZyUb3GK0ST2YBKBCQsriu8fI7tQ9bMYDcTJGlUF2WVjvzFxupS6dU3wQ%0ANPDHhcCB%2FjyRsL3Tn9yV%2BlatV%2FmTIzOM8GyFfWO6NMxEEAs1KrGJ7kEXTA75%2BC5wAdALexl1DHvR%0AV6mpqjJL%2FP3upaHGkGuVtqtgGHdkntALwkqrb5ij2MBik0EBD%2FerGKB29gzlBNUnnRqAkoJ1%2Fhhj%0A53D4&ddkey=http:LogonView");
        sleep(500L);
        WebElement username = driver.findElement(By.name("logonId"));
        username.sendKeys(config.getUsername());
        
        WebElement password = driver.findElement(By.name("logonPassword"));
        password.sendKeys(config.getPassword());
        
        WebElement login = driver.findElement(By.cssSelector("input.button.process"));
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
        for(WebElement button : driver.findElements(By.className("button"))) {
            if(button.getText() != null && "Empty trolley".equals(button.getText().trim())) {
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
     * Checkout basket
     */
    private void checkout() {
        driver.findElement(By.id("deliveryInfoPanel")).findElement(By.tagName("a")).click();
        logger.info("Choose delivery slot, input payment details and confirm");
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
        String productName = stripCommas(product.findElement(By.className("productNameAndPromotions")).findElement(By.tagName("h3")).getText());
        return productName.contains(itemName);
    }

    /**
     * TODO refactor into helper class
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