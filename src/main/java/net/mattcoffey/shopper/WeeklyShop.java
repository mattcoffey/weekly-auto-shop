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
    private final WebDriver driver = loadDriver();

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
        
        checkout();
    }
    
    private WebDriver loadDriver() {
    	System.setProperty("webdriver.gecko.driver", config.getDriverPath());
		return new FirefoxDriver();
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
        driver.get("https://www.sainsburys.co.uk/shop/LogonView?catalogId=10123&logonCallerId=LogonButton&langId=44&storeId=10151&URL=https%3A%2F%2Fwww.sainsburys.co.uk%2Fshop%2FMyAccount%3FlangId%3D44%26storeId%3D10151");
        
        WebElement username = waitForElement(By.id("logonId"));
        username.sendKeys(config.getUsername());
        
        WebElement password = driver.findElement(By.name("logonPassword"));
        password.sendKeys(config.getPassword());
        
        WebElement login = driver.findElement(By.cssSelector("input.button.process"));
        login.click();
    }

    /**
     * @param by the web element specifier
     * @return a page element
     * 
     * Wait for an element on the page to be loaded
     */
    private WebElement waitForElement(By by) {
        for(int retries = 0; retries < config.getMaxRetries(); retries++) {
            try {
                return driver.findElement(by);
            }
            catch(Exception e) {
                sleep(config.getRetryInterval());
            }
        }
        throw new RuntimeException("Unable to find element " + by + " after " + config.getMaxRetries() + " attempts.");
    }
    
    

    /**
     * @param by the web element specifier
     * @return some page elements
     * 
     * Wait for a some page elements to be loaded
     */
    private List<WebElement> waitForElements(By by) {
        for(int retries = 0; retries < config.getMaxRetries(); retries++) {
            try {
                return driver.findElements(by);
            }
            catch(Exception e) {
                sleep(config.getRetryInterval());
            }
        }
        throw new RuntimeException("Unable to find elements " + by + " after " + config.getMaxRetries() + " attempts.");
    }

    /**
     * Reset the contents of the basket (if any)
     */
    private void emptyBasket() {
        logger.info("Empty the previous shopping basket");
        
        waitForElement(By.id("miniTrolley"));
        
        if(driver.getPageSource().contains("Your trolley is empty.")) {
            //Trolly is empty
            return;
        }

        WebElement emptyTrolleyLink = waitForElement(By.id("emptyTrolleyLink"));
        
        emptyTrolleyLink.click();
        
        //Confirm empty
        for(WebElement button : waitForElements(By.className("button"))) {
            if(button.getText() != null && "Empty trolley".equals(button.getText().trim())) {
                button.click();
                return;
            }
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
