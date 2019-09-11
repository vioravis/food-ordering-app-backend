package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.LogoutResponse;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.service.businness.AuthenticationService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerBusinessService;
import com.upgrad.FoodOrderingApp.service.entity.UserAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.UserEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/")

public class CustomerController {

    @Autowired
    private CustomerBusinessService customerBusinessService;

    // Signup Method
    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {
        final UserEntity userEntity = new UserEntity();
        userEntity.setUuid(UUID.randomUUID().toString());
        userEntity.setFirstName(signupCustomerRequest.getFirstName());
        userEntity.setLastName(signupCustomerRequest.getLastName());
        userEntity.setEmail(signupCustomerRequest.getEmailAddress());
        userEntity.setPassword(signupCustomerRequest.getPassword());
        userEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        userEntity.setRole("nonadmin");
        userEntity.setSalt("1234abc");

        final UserEntity createdUserEntity = customerBusinessService.signup(userEntity);

        //Status for successful user creation
        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse().id(createdUserEntity.getUuid()).status("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);
    }


    @Autowired
    private AuthenticationService authenticationService;

    // Login Method
    @RequestMapping(method = RequestMethod.POST,path = "/customer/login",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {

        byte[] decode= Base64.getDecoder().decode(authorization.split("Basic ")[1]);

        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        UserAuthTokenEntity userAuthToken = authenticationService.authenticate(decodedArray[0],decodedArray[1]);
        UserEntity user = userAuthToken.getUser();

        // Message for successful Login
        LoginResponse loginResponse = new LoginResponse().id(user.getUuid()).message("SIGNED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token",userAuthToken.getAccessToken());

        return new ResponseEntity<LoginResponse>(loginResponse,headers, HttpStatus.OK);

    }


    // Logout method
    @RequestMapping(method=RequestMethod.POST,path="/user/logout",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        final UserEntity userEntity = customerBusinessService.Logout(authorization);

        //Message for successful Logout
        LogoutResponse logoutResponse = new LogoutResponse().id(userEntity.getUuid()).message("SIGNED OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(logoutResponse,HttpStatus.OK);

    }




}