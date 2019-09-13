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

        CustomerEntity customer = customerDao.getCustomerByCustomerName(customerEntity.getCustomerName());

        // Check if username already exists
        if(customer != null) {
            throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");
        }

        // Check if email already exists
        CustomerEntity customerEmail = customerDao.getCustomerByEmail(customerEntity.getEmail());

        if(customerEmail != null) {
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }

        //Encrypt password and add salt
        String[] encryptedText = cryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);

        return customerDao.createCustomer(customerEntity);

    }
}