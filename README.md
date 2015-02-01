weekly-auto-shop
================

Browser Automation for Online Shopping

Setup

1. Install Java https://java.com/en/download/
2. Install Firefox https://www.mozilla.org/en-GB/firefox/new/
3. Download the Weekly Auto Shop zip distribution from (TODO host releases somewhere)
4. Unzip release into directory


Configuration

1. Add your username and password to config.properties
2. Add the items you are interested in to weeklyShop.txt

Invocation

1. Open a shell (cmd or bash), cd into directory
2. Run either run.sh or run.bat depending on Operating System

Build Software (Optional)

1. Install Git + Java + Maven and add them to executable path
2. git clone https://github.com/mattcoffey/weekly-auto-shop.git
3. cd weekly-auto-shop
4. mvn package
5. Distribution available in target/weekly-auto-shop-dist.zip
