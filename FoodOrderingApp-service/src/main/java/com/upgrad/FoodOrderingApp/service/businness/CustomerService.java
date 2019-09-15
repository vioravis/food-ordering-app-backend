package com.upgrad.FoodOrderingApp.service.businness;



import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.regex.Pattern;

@Service
public class CustomerService {

    @Autowired
    private AdminBusinessService adminBusinessService;

    @Autowired
    private CustomerDao customerDao;

    // Service method for signup
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity saveCustomer(CustomerEntity customerEntity) throws SignUpRestrictedException {
        return adminBusinessService.createCustomer(customerEntity);
    }

    @Autowired
    private AuthenticationService authenticationService;

    // Service method for login
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(final String contactNumber, final String password) throws AuthenticationFailedException {
        return authenticationService.authenticate(contactNumber,password);
    }

    // Service method for logout
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity logout(final String authorizationToken) throws AuthorizationFailedException {
        return authenticationService.userLogout(authorizationToken);
    }

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    // Service method for Password Update
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updateCustomerPassword(final String oldPassword, final String newPassword, final String authorization) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(authorization);

        if(oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            throw new AuthorizationFailedException("UCR-003", "No field should be empty");
        }

        // Validate if user is signed in or not
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }

        final ZonedDateTime lastLoginTime = customerAuthEntity.getLoginAt();
        final ZonedDateTime lastLogoutTime = customerAuthEntity.getLogoutAt();
        final ZonedDateTime expiryTime = customerAuthEntity.getExpiresAt();

        // For previously logged out users, check their logged out times
        // This avoids exceptions during repeated logout calls
        if (lastLogoutTime != null && lastLogoutTime.isAfter(lastLoginTime)) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");

        }

        final ZonedDateTime now = ZonedDateTime.now();

        if (expiryTime != null && now.isAfter(expiryTime)) {
            throw new AuthorizationFailedException("ATH-003", "Your session is expired. Log in again to access this endpoint.");

        }

        // Check if password is valid

        String passwordPattern = "((?=.*[a-z])(?=.*\\d)(?=.*[A-Z])(?=.*[#@$%&*!^]).{8,40})";

        Pattern pattern = Pattern.compile(passwordPattern);
        boolean matched = pattern.matcher(newPassword).matches();

        if(!matched) {
            throw new AuthorizationFailedException("UCR-001","Weak password!");
        }

        CustomerEntity customer = customerAuthEntity.getCustomer();
        String encryptedOldPassword = cryptographyProvider.encrypt(oldPassword,customer.getSalt());
        String oldPasswordStored = customer.getPassword();

        //Check if the old password provided is correct
        if (!oldPasswordStored.equals(encryptedOldPassword)) {
            throw new AuthorizationFailedException("UCR-004", "Incorrect old password!");
        }

//        String newPasswordHex = Hashing.sha256().hashString(newPassword, Charsets.US_ASCII).toString();
        String encryptedNewPassword = cryptographyProvider.encrypt(newPassword,customer.getSalt());
        customer.setPassword(encryptedNewPassword);
        customerDao.updateCustomer(customer);

        return customerAuthEntity.getCustomer();

    }

    // Service method for Password Update
    @Transactional(propagation = Propagation.REQUIRED)
    //
    public CustomerEntity updateCustomer(final String authorization, final String firstName, final String lastName) throws AuthorizationFailedException {

        String authorization1 = authorization.split("Bearer ")[1];
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(authorization1);

        // Validate if user is signed in or not
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }

        final ZonedDateTime lastLoginTime = customerAuthEntity.getLoginAt();
        final ZonedDateTime lastLogoutTime = customerAuthEntity.getLogoutAt();
        final ZonedDateTime expiryTime = customerAuthEntity.getExpiresAt();

        // For previously logged out users, check their logged out times
        // This avoids exceptions during repeated logout calls
        if (lastLogoutTime != null && lastLogoutTime.isAfter(lastLoginTime)) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");

        }

        final ZonedDateTime now = ZonedDateTime.now();

        if (expiryTime != null && now.isAfter(expiryTime)) {
            throw new AuthorizationFailedException("ATH-003", "Your session is expired. Log in again to access this endpoint.");

        }

        CustomerEntity customer = customerAuthEntity.getCustomer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);

        customerDao.updateCustomer(customer);

        return customerAuthEntity.getCustomer();

    }

    @Transactional
    public CustomerEntity getCustomer(final String customerUuid) {
        return customerDao.getCustomer(customerUuid);
    }


}