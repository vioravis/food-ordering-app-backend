package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/")
public class AddressController {


    @Autowired
    private AddressService addressService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private AuthenticationService authenticationService;


    @RequestMapping(method = RequestMethod.POST, path = "/address", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(final SaveAddressRequest saveAddressRequest,
                                                           @RequestHeader("accessToken") final String accessToken) throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        String [] bearerToken = accessToken.split("Bearer ");

        String authorization1 = accessToken.split("Bearer ")[1];
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


        final CustomerEntity customerEntity = customerAuthEntity.getCustomer();
        final StateEntity stateEntity = addressService.getStateByUUID(saveAddressRequest.getStateUuid());
        final AddressEntity addressEntity= new AddressEntity();

        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setFlatBuilNumber(saveAddressRequest.getFlatBuildingName());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setPinCode(saveAddressRequest.getPincode());
        addressEntity.setState(stateEntity);
        addressEntity.setActive(1);

        final AddressEntity savedAddressEntity = addressService.saveAddress(addressEntity);

        final CustomerAddressEntity customerAddressEntity=new CustomerAddressEntity();
        customerAddressEntity.setAddress(savedAddressEntity);
        customerAddressEntity.setCustomer(customerEntity);
        addressService.createCustomerAddress(customerAddressEntity);

        SaveAddressResponse saveAddressResponse = new SaveAddressResponse()
                .id(savedAddressEntity.getUuid())
                .status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.CREATED);

    }

    /*WORK IN PROGRESS*/
    //getallsavedaddresses endpoint retrieves all the addresses of a valid customer present in the database
    @RequestMapping(method = RequestMethod.GET, path = "/address/customer",  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<AddressListResponse> getAllAddress(@RequestHeader("accessToken") final String accessToken) throws AuthorizationFailedException  {

        String [] bearerToken = accessToken.split("Bearer ");
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(bearerToken[1]);

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

        // Validate if user session expired
        if (expiryTime != null && now.isAfter(expiryTime)) {
            throw new AuthorizationFailedException("ATH-003", "Your session is expired. Log in again to access this endpoint.");

        }

        final CustomerEntity customerEntity = customerAuthEntity.getCustomer();
        final List<CustomerAddressEntity> customerAddressesListByCustomerId = addressService.getAllCustomerAddressByCustomerId(customerEntity);

        AddressList addressList =new AddressList();
        AddressListResponse addressListResponse=new AddressListResponse();

        for( CustomerAddressEntity customerAddressEntity : customerAddressesListByCustomerId){
            AddressEntity addressEntity = addressService.getAddressById(customerAddressEntity.getAddress().getId());
            addressList.id(UUID.fromString(addressEntity.getUuid()));
            addressList.flatBuildingName(addressEntity.getFlatBuilNumber());
            addressList.locality(addressEntity.getLocality());
            addressList.pincode(addressEntity.getPinCode());
            addressList.city(addressEntity.getCity());

            final StateEntity stateEntity =addressService.getStateById(addressEntity.getState().getId());
            AddressListState addressListState=new AddressListState();
            addressListState.id(UUID.fromString(stateEntity.getUuid()));
            addressListState.stateName(stateEntity.getStateName());

            addressList.state(addressListState);

            addressListResponse.addAddressesItem(addressList);
        }

        return new ResponseEntity<AddressListResponse>(addressListResponse,HttpStatus.OK);
    }

    @RequestMapping(method= RequestMethod.DELETE,path="/address/{address_id}",produces= MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteAddress(@PathVariable("address_id") final String addressUuid,
                                                               @RequestHeader("accessToken") final String accessToken) throws AuthorizationFailedException, AddressNotFoundException {

        String [] bearerToken = accessToken.split("Bearer ");
        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(bearerToken[1]);

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

        // Validate if user session expired
        if (expiryTime != null && now.isAfter(expiryTime)) {
            throw new AuthorizationFailedException("ATH-003", "Your session is expired. Log in again to access this endpoint.");

        }

        //CustomerAuthEntity customerAuthEntity = customerDao.getCustomerAuthToken(bearerToken[1]);
        final CustomerEntity customerEntity = customerAuthEntity.getCustomer();

//        final CustomerEntity signedinCustomerEntity = customerService.getCustomer(bearerToken[1]);
        final AddressEntity addressEntityToDelete=addressService.getAddressByAddressUuid(addressUuid);
        final CustomerAddressEntity customerAddressEntity=addressService.getCustAddressByCustIdAddressId(customerAuthEntity.getCustomer(),addressUuid);
        final CustomerEntity ownerofAddressEntity=customerAddressEntity.getCustomer();
        final String Uuid = addressService.deleteAddress(addressEntityToDelete,customerEntity,ownerofAddressEntity);

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse()
                .id(UUID.fromString(Uuid))
                .status("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }


    //getallstates endpoint retrieves all the states present in the database
    @RequestMapping(method = RequestMethod.GET, path = "/states",  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getAllStates(){
        return new ResponseEntity<>(addressService.getAllStates(), HttpStatus.OK);
    }

}