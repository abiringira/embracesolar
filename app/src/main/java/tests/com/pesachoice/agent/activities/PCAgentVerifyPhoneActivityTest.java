package tests.com.pesachoice.agent.activities;

import android.app.Dialog;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import com.pesachoice.agent.activities.PCAgentVerifyPhoneActivity;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

/**
 * Created by emmy on 16/04/2018.
 */

public class PCAgentVerifyPhoneActivityTest extends ActivityInstrumentationTestCase2<PCAgentVerifyPhoneActivity>{

    private static final String CLAZZ = PCAgentVerifyPhoneActivityTest.class.getName();
    private PCAgentVerifyPhoneActivity verifyPhoneActivity;
    public PCAgentVerifyPhoneActivityTest() {
        super(CLAZZ,PCAgentVerifyPhoneActivity.class);
    }

    @Before
    public void setup() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        verifyPhoneActivity = getActivity();
    }

    @Test
    public void  testValidateInputsAndSetValueMissingInput() {
        Dialog dialog = new Dialog(getActivity());
        EditText accecodeEdit = (EditText) dialog.findViewById(com.pesachoice.billpay.activities.R.id.accessCode);
        boolean validateResult = true;
        String accecodeEditText = accecodeEdit.getText().toString();
        accecodeEditText = "";
        if(accecodeEdit != null && StringUtils.isEmpty(accecodeEditText))
            validateResult = verifyPhoneActivity.validateInputsAndSetValue(null);
           assertEquals(validateResult,false);
         }

}
