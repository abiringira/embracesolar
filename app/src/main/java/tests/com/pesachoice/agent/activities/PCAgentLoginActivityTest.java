package tests.com.pesachoice.agent.activities;

import android.test.ActivityInstrumentationTestCase2;

import com.pesachoice.agent.activities.PCAgentLoginActivity;
import com.pesachoice.billpay.business.PCPesachoiceConstant;
import com.pesachoice.billpay.model.PCGenericError;


import org.junit.Before;
import org.junit.Test;

/**
 * Created by emmy on 10/01/2018.
 */

public class PCAgentLoginActivityTest extends ActivityInstrumentationTestCase2<PCAgentLoginActivity> {

      private static final String CLAZZ = PCAgentLoginActivityTest.class.getName();
      private PCAgentLoginActivity activity;

      public PCAgentLoginActivityTest() {

          super(CLAZZ,PCAgentLoginActivity.class);
      }


    @Before
    protected void setUp () throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }


    @Test
    public void testMissingPasswordField() {
        /**
         * Test missing password field  with negative case.
         */
        try {

            activity.validCustomerInfo("some_user23@pesachoice.com", "");
        } catch (PCGenericError error) {
            error = new PCGenericError();
            assertEquals(error.getMessage(), "Password is required");
        }
    }


    @Test
    public void testMissingUsernameField() throws PCGenericError {

        /**
         * Test missing username field negative case.
         */
        try {

            activity.validCustomerInfo("", "m9$JdcU4g!ospSF<?1uf3");
            System.out.println("Error message wait");
        } catch (PCGenericError error) {
            error = new PCGenericError();
            System.out.println("Error message" +"  " + error.getMessage());
            assertEquals(error.getMessage(), "Email is required");

        }

    }


    @Test
    public void testLoginWithInvalidNumber() throws PCGenericError {

        /**
         * Test login with invalid number negative case by putting invalid phoneNumber below 6 characters
         * and containing letters not digits.
         */
        try {
            activity.loginUser("5dsan", "m9$JdcU4g!ospSF<?1uf3", "323232");
        } catch (Exception error) {
            assertEquals(error.getMessage(), PCPesachoiceConstant.FAILED_IN_AES_ENCRIPTION);
        }

        /**
         * Test login with invalid number negative case by putting invalid phoneNumber above 15 characters
         * and contain digits only.
         */
        try {
            activity.loginUser("2507826479856265738934","m9$JdcU4g!ospSF<?1uf3","323232");

        } catch (Exception error) {

            assertEquals(error.getMessage(), PCPesachoiceConstant.FAILED_IN_AES_ENCRIPTION);

        }

    }

}
