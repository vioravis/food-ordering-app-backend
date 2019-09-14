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

@Service
public class CustomerService {

    @Autowired
    private AdminBusinessService adminBusinessService;

    @Autowired
    private CustomerDao customerDao;

    // Service method for signup
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity signup(CustomerEntity customerEntity) throws SignUpRestrictedException {
        return adminBusinessService.createCustomer(customerEntity);
    }

    @Autowired
    private AuthenticationService authenticationService;

    // Service method for signout
    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity logout(final String authorizationToken) throws AuthorizationFailedException {
        return authenticationService.userLogout(authorizationToken);
    }



}