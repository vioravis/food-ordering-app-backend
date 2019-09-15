package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;


@Service
public class AuthenticationService {

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    //Service method for authenticaion while signing in
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthTokenEntity authenticate(final String contactNumber, final String password) throws AuthenticationFailedException{
        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(contactNumber);

        // Check if user name does not exist
        if(customerEntity == null) {
            throw new AuthenticationFailedException("ATH-001","This contact number has not been registered!");
        }

        String encryptedPassword = cryptographyProvider.encrypt(password,customerEntity.getSalt());

        //Check if password matches the password-salt stored in the database
        if(encryptedPassword.equals(customerEntity.getPassword()))
        {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthTokenEntity customerAuthToken = new CustomerAuthTokenEntity();
            customerAuthToken.setCustomer(customerEntity);

            final ZonedDateTime now = ZonedDateTime.now();
            final ZonedDateTime expiresAt = now.plusHours(8);
            customerAuthToken.setUuid(customerEntity.getUuid());
            customerAuthToken.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(),now,expiresAt));

            customerAuthToken.setLoginAt(now);
            customerAuthToken.setExpiresAt(expiresAt);

            customerDao.createAuthToken(customerAuthToken);
            customerDao.updateCustomer(customerEntity);

            return customerAuthToken;
        } else {
            throw new AuthenticationFailedException("ATH-002","Invalid Credentials");
        }

    }

    //Method for user logout
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity userLogout(final String authorizationToken) throws AuthorizationFailedException {
        CustomerAuthTokenEntity customerAuthEntity = customerDao.getCustomerAuthToken(authorizationToken);

        // Validate if user is signed in or not
        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("SGR-001", "User is not Signed in");
        }


        final ZonedDateTime lastLoginTime = customerAuthEntity.getLoginAt();
        final ZonedDateTime lastLogoutTime = customerAuthEntity.getLogoutAt();

        // For previously logged out users, check their logged out times
        // This avoids exceptions during repeated logout calls
        if(lastLogoutTime!=null && lastLogoutTime.isAfter(lastLoginTime)) {
            throw new AuthorizationFailedException("SGR-001", "User is not Signed in");

        }

        final ZonedDateTime now = ZonedDateTime.now();

        //Set the new logout time
        customerAuthEntity.setLogoutAt(now);

        return customerAuthEntity.getCustomer();
    }


}