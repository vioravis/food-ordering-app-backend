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

/**
 * This class contains Admin related business operations
 */
@Service
public class AdminBusinessService {

    @Autowired
    private UserDao userDao;

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
    public UserEntity createUser(final UserEntity userEntity) throws SignUpRestrictedException {

        UserEntity user = userDao.getUserByUserName(userEntity.getUserName());

        // Check if username already exists
        if(user != null) {
            throw new SignUpRestrictedException("SGR-001","Try any other Username, this Username has already been taken");
        }

        // Check if email already exists
        UserEntity userEmail = userDao.getUserByEmail(userEntity.getEmail());

        if(userEmail != null) {
            throw new SignUpRestrictedException("SGR-002","This user has already been registered, try with any other emailId");
        }

        //Encrypt password and add salt
        String[] encryptedText = cryptographyProvider.encrypt(userEntity.getPassword());
        userEntity.setSalt(encryptedText[0]);
        userEntity.setPassword(encryptedText[1]);

        return userDao.createUser(userEntity);

    }
}