package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AuthenticationService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.hibernate.sql.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        LogoutResponse logoutResponse = new LogoutResponse().id(customerEntity.getUuid()).message("LOGGED OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(logoutResponse,HttpStatus.OK);

    }

    //Update Customer Password
    @RequestMapping(method=RequestMethod.PUT,path="/customer/password",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)

    public ResponseEntity<UpdatePasswordResponse> updateCustomerPassword(@RequestHeader("authorization") final String authorization,
                                                                         @RequestParam String oldPassword,
                                                                         @RequestParam String newPassword) throws AuthorizationFailedException {

        final CustomerEntity customerEntity = customerService.updatePassword(authorization,oldPassword,newPassword);

        // Message for successful password update
        UpdatePasswordResponse passwordUpdateResponse = new UpdatePasswordResponse().id(customerEntity.getUuid()).status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");

        return new ResponseEntity<UpdatePasswordResponse>(passwordUpdateResponse,HttpStatus.OK);
    }

    //Update Customer Password
    @RequestMapping(method=RequestMethod.PUT,path="/customer",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)

    public ResponseEntity<UpdateCustomerResponse> updateCustomerResponse(@RequestHeader("authorization") final String authorization,
                                                                         @RequestParam String firstName,
                                                                         @RequestParam String lastName) throws AuthorizationFailedException {

        final CustomerEntity customerEntity = customerService.updateCustomer(authorization,firstName,lastName);

        // Message for successful password update
        UpdateCustomerResponse customerUpdateResponse = new UpdateCustomerResponse().id(customerEntity.getUuid()).
                                                            firstName(customerEntity.getFirstName()).
                                                            lastName(customerEntity.getLastName()).
                                                            status("CUSTOMER DETAILS UPDATED SUCCESSFULLY");

        return new ResponseEntity<UpdateCustomerResponse>(customerUpdateResponse,HttpStatus.OK);
    }





//    //Update Customer Method
//    @RequestMapping(method=RequestMethod.PUT,path="/customer",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    public ResponseEntity<UpdateCustomerResponse> updateCustomer(@RequestHeader String accessToken, @RequestParam(value = "First Name") String firstname, @RequestParam(value = "Last Name", required = false) String lastname){
//        final CustomerEntity customerEntity = customerService.logout(accessToken);
//    }



}