package tests.com.pesachoice.agent.activities;

import android.test.ActivityInstrumentationTestCase2;
import android.view.MenuItem;
import com.pesachoice.agent.activities.PCAgentAccountDetailsActivity;


import org.junit.Before;
import org.junit.Test;

/**
 * Created by emmy on 10/01/2018.
 */

public class PCAgentAccountDetailsActivityTest extends ActivityInstrumentationTestCase2<PCAgentAccountDetailsActivity> {

    private static final String CLAZZ = PCAgentAccountDetailsActivityTest.class.getName();
    private PCAgentAccountDetailsActivity accountDetailsActivity;
     public PCAgentAccountDetailsActivityTest() {
         super(CLAZZ,PCAgentAccountDetailsActivity.class);

    }

    @Before
    protected void setUp () throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        accountDetailsActivity = getActivity();
    }

    @Test
    public void testOnOptionsItemSelected () {
        MenuItem item = (MenuItem) getActivity();
        Boolean itemSelectedResult = accountDetailsActivity.onOptionsItemSelected(item);
        assertTrue(itemSelectedResult);

    }


}
