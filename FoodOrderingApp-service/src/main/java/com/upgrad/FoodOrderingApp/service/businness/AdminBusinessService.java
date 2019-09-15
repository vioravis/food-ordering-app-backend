package com.upgrad.FoodOrderingApp.service.businness;


import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.EmailValidator;

/**
 * This class contains Admin related business operations
 */
@Service
public class AdminBusinessService {

    @Autowired
    private CustomerDao customerDao;

    /**
     * delete user
     * @param userUuid
     * @param authorizationToken
     * @return
     * @throws AuthorizationFailedException
     */

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    //Method for creating a user...used during signup
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity createCustomer(final CustomerEntity customerEntity) throws SignUpRestrictedException {

        CustomerEntity customer = customerDao.getCustomerByContactNumber(customerEntity.getContactNumber());



        // Check if username already exists
        if(customer != null) {
            throw new SignUpRestrictedException("SGR-001","his contact number is already registered! Try other contact number.");
        }

        // Check if email is valid
        String customerEmail = customerEntity.getEmail();
        String customerNumber = customerEntity.getContactNumber();
        String customerPassword = customerEntity.getPassword();
        String customerFirstName = customerEntity.getFirstName();

        // Check for empty fields...all fields except last name is mandatory

        if(customerFirstName == null || customerEmail == null || customerPassword == null || customerNumber == null) {
            throw new SignUpRestrictedException("SGR-005","Except last name all fields should be filled");
        }

        boolean emailValid = EmailValidator.getInstance().isValid(String.valueOf(customerEmail));

        if(!emailValid) {
            throw new SignUpRestrictedException("SGR-002","Invalid email-id format!");
        }

        // Check if contact number is valid


        int len = customerNumber.length();

        if(!customerNumber.matches("[0-9]+") || len != 10) {
            throw new SignUpRestrictedException("SGR-003","Invalid contact number!");
        }

        // Check if password is valid

        String passwordPattern = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[#@$%&*!^]).{8,40})";

        Pattern pattern = Pattern.compile(passwordPattern);
        boolean matched = pattern.matcher(customerPassword).matches();

        if(!matched) {
            throw new SignUpRestrictedException("SGR-004","Weak password!");
        }



        //Encrypt password and add salt
        String[] encryptedText = cryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);

        return customerDao.createCustomer(customerEntity);

    }
}