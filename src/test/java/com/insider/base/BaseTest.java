package com.insider.base;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.time.Duration;

/**
 * BaseTest class to handle Driver initialization and teardown.
 * I use this base class to avoid code duplication across test classes and ensure a clean state for each test.
 */
public class BaseTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    
    // I use ThreadLocal to ensure thread safety when running tests in parallel.
    // This allows each thread to have its own WebDriver instance.
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(@Optional("chrome") String browser) {
        logger.info("Initializing driver for browser: {}", browser);
        
        if (browser.equalsIgnoreCase("chrome")) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--start-maximized");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);
            
            // Check for headless property, default to false if not set, but can be passed via -Dheadless=true
            String headless = System.getProperty("headless", "false");
            if ("true".equalsIgnoreCase(headless)) {
                options.addArguments("--headless=new");
            }
            
            // I use the default Selenium Manager (Selenium 4.6+) which automatically manages driver binaries.
            driver.set(new ChromeDriver(options));
            
        } else if (browser.equalsIgnoreCase("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            driver.set(new FirefoxDriver(options));
        } else {
            throw new IllegalArgumentException("Browser not supported: " + browser);
        }

        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        getDriver().manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            logger.info("Quitting driver");
            getDriver().quit();
            driver.remove();
        }
    }

    public WebDriver getDriver() {
        return driver.get();
    }
}
