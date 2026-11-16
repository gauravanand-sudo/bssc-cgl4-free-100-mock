package com.bssc.cgl4.free100mock

import org.junit.Assert.*
import org.junit.Test

class RegistrationFlowPolicyTest {
    @Test fun googleOnlyCannotEnterHome() {
        assertFalse(RegistrationFlowPolicy.profileComplete(false,null,"a@gmail.com",null,""))
    }
    @Test fun missingMobileOrNameMustComplete() {
        assertFalse(RegistrationFlowPolicy.profileComplete(true,"a@gmail.com","a@gmail.com","Rahul",""))
        assertFalse(RegistrationFlowPolicy.profileComplete(true,"a@gmail.com","a@gmail.com",null,"+919876543210"))
    }
    @Test fun invalidPhoneOrWrongAccountNotReady() {
        assertFalse(RegistrationFlowPolicy.profileComplete(true,"a@gmail.com","a@gmail.com","Rahul","+911234567890"))
        assertFalse(RegistrationFlowPolicy.profileComplete(true,"other@gmail.com","a@gmail.com","Rahul","+919876543210"))
    }
    @Test fun completedProfileCanResumeOnRestart() {
        assertTrue(RegistrationFlowPolicy.profileComplete(true,"a@gmail.com","a@gmail.com","राहुल","+919876543210"))
    }
}