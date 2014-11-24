package net.mattcoffey.shopper;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import net.mattcoffey.config.Config;
import net.mattcoffey.reader.ShoppingListReader;

/**
 * Automates the browser to perform the shop
 * 
 * @author mcoffey
 */
public class WeeklyShop {
    
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
        List<ShoppingListItem> items = new ShoppingListReader(config).readShoppingList();
        
        login();
        
        emptyBasket();
        
        addItemsToBasket(items);
        
        driver.close();        
    }
    
    /**
     * Login to the shopping site
     */
    private void login() {
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

    private WebElement getEmptyTrollyLink() {
        try {
            return driver.findElement(By.id("emptyTrolleyLink"));
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Add the items in the shopping list to the basket
     * @param items
     */
    private void addItemsToBasket(List<ShoppingListItem> items) {
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
        WebElement productLister = driver.findElement(By.id("productLister"));
        for(WebElement product : productLister.findElements(By.className("product"))) {
            if(containsItem(product, item.getItemName())) {
                WebElement quantity = product.findElement(By.name("quantity"));
                quantity.clear();
                quantity.sendKeys(item.getAmount().toString());
                
                WebElement add = product.findElement(By.name("Add"));
                add.click();
            }
        }
    }

    /**
     * @param product
     * @param itemName
     * @return true if the supplied product WebElement matches the itemName
     */
    private boolean containsItem(WebElement product, String itemName) {
        String productName = product.findElement(By.className("productNameAndPromotions")).findElement(By.tagName("h3")).getText();
        productName = stripCommas(productName);
        return productName.contains(itemName);
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