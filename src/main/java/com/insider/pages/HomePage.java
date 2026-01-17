package com.insider.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class HomePage extends BasePage {

    
    
    private final By body = By.tagName("body");
    private final By header = By.id("navigation");
    private final By main = By.cssSelector("main.flexible-layout");
    private final By footer = By.id("footer");

    // Sections under main
    private final By heroSection = By.className("homepage-hero");
    private final By socialProofSection = By.className("homepage-social-proof");
    private final By coreDifferentiatorsSection = By.className("homepage-core-differentiators");
    private final By capabilitiesSection = By.className("homepage-capabilities");
    private final By insiderOneAiSection = By.className("homepage-insider-one-ai");
    private final By channelsSection = By.className("homepage-channels");
    private final By caseStudySection = By.className("homepage-case-study");
    private final By analystSection = By.className("homepage-analyst");
    private final By integrationsSection = By.className("homepage-integrations");
    private final By resourcesSection = By.className("homepage-resources");
    private final By callToActionSection = By.className("homepage-call-to-action");


    public HomePage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        String url = "https://useinsider.com/";
        logger.info("Navigating to Home Page: {}", url);
        driver.get(url);
    }

    public void verifyHomePageOpened() {
        Assert.assertTrue(driver.getTitle().contains("Insider"), "Home page title does not contain 'Insider'");
        logger.info("Home page opened successfully.");
    }

    public void verifyMainBlocksLoaded() {
        // Checking visibility of critical elements 
        Assert.assertTrue(isDisplayed(body), "Body is not visible");
        Assert.assertTrue(isDisplayed(header), "Header (navigation) is not visible");
        Assert.assertTrue(isDisplayed(main), "Main content area is not visible");

        // Verifying sections under main - Scrolling to each to ensure visibility/loading
        verifySection(heroSection, "Hero");
        verifySection(socialProofSection, "Social Proof");
        verifySection(coreDifferentiatorsSection, "Core Differentiators");
        verifySection(capabilitiesSection, "Capabilities");
        verifySection(insiderOneAiSection, "Insider One AI");
        verifySection(channelsSection, "Channels");
        verifySection(caseStudySection, "Case Study");
        verifySection(analystSection, "Analyst");
        verifySection(integrationsSection, "Integrations");
        verifySection(resourcesSection, "Resources");
        verifySection(callToActionSection, "Call To Action");

        verifySection(footer, "Footer");
        
        logger.info("Main blocks and all sections are loaded successfully.");
    }

    private void verifySection(By locator, String sectionName) {
        scrollToElement(locator);
        Assert.assertTrue(isDisplayed(locator), sectionName + " section is not visible");
    }
}
