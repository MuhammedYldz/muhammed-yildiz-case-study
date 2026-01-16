package com.insider.tests;

import com.insider.base.BaseTest;
import com.insider.pages.CareersPage;
import com.insider.pages.HomePage;
import com.insider.pages.QAJobsPage;
import org.testng.annotations.Test;

public class InsiderTests extends BaseTest {

    @Test(description = "Insider Career Page QA Job Verification")
    public void testInsiderQAJobs() {
        // Verify Home Page accessibility and main component visibility
        HomePage homePage = new HomePage(getDriver());
        homePage.open();
        homePage.verifyHomePageOpened();
        homePage.verifyMainBlocksLoaded();

        // Navigate to QA Careers page and proceed to job listings
        CareersPage careersPage = new CareersPage(getDriver());
        careersPage.open();
        careersPage.clickSeeAllQAJobs();

        QAJobsPage qaJobsPage = new QAJobsPage(getDriver());
        
        // Wait for the initial API load before filtering
        qaJobsPage.waitForJobListApiLoad();
        
        // Apply filters for Location (Istanbul) and Department (QA)
        qaJobsPage.filterJobs("Istanbul, Turkiye", "Quality Assurance");
        
        // Verify that all listed jobs match the filter criteria (Location, Department, Title)
        qaJobsPage.verifyJobDetails();
        
        // Verify that the 'View Role' button for each job correctly redirects to the Lever application form
        qaJobsPage.clickAllViewRoleButtonsAndVerify();
    }
}
