package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.LoginResponse;
import com.upgrad.FoodOrderingApp.api.model.LogoutResponse;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.service.businness.AuthenticationService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
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
    private CustomerService customerService;

    // Signup Method
    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {
        final CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setFirstName(signupCustomerRequest.getFirstName());
        customerEntity.setLastName(signupCustomerRequest.getLastName());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setPassword(signupCustomerRequest.getPassword());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());

        customerEntity.setSalt("1234abc");

        final CustomerEntity createdUserEntity = customerService.signup(customerEntity);

        //Status for successful user creation
        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse().id(createdUserEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);
    }


    @Autowired
    private AuthenticationService authenticationService;

    // Login Method
    @RequestMapping(method = RequestMethod.POST,path = "/customer/login",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {

        if(!(authorization.split("\\s+"))[0].equals("Basic")){
            throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
        }

        byte[] decode= Base64.getDecoder().decode(authorization.split("Basic ")[1]);

        String decodedText = new String(decode);

        if(decodedText.split(":").length !=2 ){
            throw new AuthenticationFailedException("ATH-003","Incorrect format of decoded customer name and password");
        }

        String[] decodedArray = decodedText.split(":");

        CustomerAuthTokenEntity customerAuthToken = authenticationService.authenticate(decodedArray[0],decodedArray[1]);
        CustomerEntity customer = customerAuthToken.getCustomer();

        // Message for successful Login
        LoginResponse loginResponse = new LoginResponse().id(customer.getUuid()).firstName(customer.getFirstName()).
                                          lastName(customer.getLastName()).
                                          emailAddress(customer.getEmail()).
                                          contactNumber(customer.getContactNumber()).message("LOGGED IN SUCCESSFULLY");

        HttpHeaders headers = new HttpHeaders();
        headers.add("access-token",customerAuthToken.getAccessToken());


        return new ResponseEntity<LoginResponse>(loginResponse,headers, HttpStatus.OK);

    }


    // Logout method
    @RequestMapping(method=RequestMethod.POST,path="/user/logout",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logout(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {
        final CustomerEntity customerEntity = customerService.logout(authorization);

        //Message for successful Logout
        LogoutResponse logoutResponse = new LogoutResponse().id(customerEntity.getUuid()).message("SIGNED OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(logoutResponse,HttpStatus.OK);

    }




}