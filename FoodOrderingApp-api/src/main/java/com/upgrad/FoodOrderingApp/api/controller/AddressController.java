package com.upgrad.FoodOrderingApp.api.controller;


import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

//RestController annotation specifies that this class represents a REST API(equivalent of @Controller + @ResponseBody)
@RestController
//"@CrossOrigin” annotation enables cross-origin requests for all methods in that specific controller class.
@CrossOrigin
@RequestMapping("/")
public class AddressController {

    //Required services are autowired to enable access to methods defined in respective Business services
    @Autowired
    private AddressService addressService;
    @Autowired
    private CustomerService customerService;



    //saveaddress  endpoint requests for all the attributes in “SaveAddressRequest” about the customer and saves the address of a customer successfully.
    //PLEASE NOTE @RequestBody(required = false) inside saveaddress function will disable parameters in request body in request model.
    @RequestMapping(method = RequestMethod.POST, path = "/address", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveaddress(@RequestBody(required = false) final SaveAddressRequest saveAddressRequest,
                                                           @RequestHeader("accessToken") final String accessToken) throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        String [] bearerToken = accessToken.split("Bearer ");
        final CustomerEntity customerEntity = customerService.getCustomer(bearerToken[1]);
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
    public ResponseEntity<AddressListResponse> getallsavedaddresses(@RequestHeader("accessToken") final String accessToken) throws AuthorizationFailedException  {

        String [] bearerToken = accessToken.split("Bearer ");
        final CustomerEntity customerEntity = customerService.getCustomer(bearerToken[1]);
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
        final CustomerEntity signedinCustomerEntity = customerService.getCustomer(bearerToken[1]);
        final AddressEntity addressEntityToDelete=addressService.getAddressByAddressUuid(addressUuid);
        final CustomerAddressEntity customerAddressEntity=addressService.getCustomerIdByAddressId(addressEntityToDelete.getId());
        final CustomerEntity ownerofAddressEntity=customerAddressEntity.getCustomer();
        final String Uuid = addressService.deleteAddress(addressEntityToDelete,signedinCustomerEntity,ownerofAddressEntity);

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse()
                .id(UUID.fromString(Uuid))
                .status("ADDRESS DELETED SUCCESSFULLY");
        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }


    //getallstates endpoint retrieves all the states present in the database
    @RequestMapping(method = RequestMethod.GET, path = "/states",  produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getallstates(){
        return new ResponseEntity<>(addressService.getAllStates(), HttpStatus.OK);
    }

}