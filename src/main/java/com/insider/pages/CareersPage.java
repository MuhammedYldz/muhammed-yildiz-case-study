package com.insider.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CareersPage extends BasePage {

    private final By seeAllQAJobsBtn = By.xpath("//a[normalize-space()='See all QA jobs']");

    public CareersPage(WebDriver driver) {
        super(driver);
    }

    public void open() {
        String url = "https://useinsider.com/careers/quality-assurance/";
        logger.info("Navigating to Careers QA Page: {}", url);
        driver.get(url);
    }

    public void clickSeeAllQAJobs() {
        logger.info("Clicking 'See all QA jobs' button");
        // Handling potential cookie banners or overlays might be needed here in a real scenario
        // click(cookieAcceptBtn); 
        click(seeAllQAJobsBtn);
    }
}
