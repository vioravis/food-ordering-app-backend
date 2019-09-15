package com.upgrad.FoodOrderingApp.service.businness;



import com.upgrad.FoodOrderingApp.service.dao.UserDao;
import com.upgrad.FoodOrderingApp.service.entity.UserAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.UserEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerBusinessService {

    @Autowired
    private AdminBusinessService adminBusinessService;

    @Autowired
    private UserDao userDao;

    // Service method for signup
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity) throws SignUpRestrictedException {
        return adminBusinessService.createUser(userEntity);
    }

    @Autowired
    private AuthenticationService authenticationService;

    // Service method for signout
    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity Logout(final String authorizationToken) throws AuthorizationFailedException {
        return authenticationService.userLogout(authorizationToken);
    }



}