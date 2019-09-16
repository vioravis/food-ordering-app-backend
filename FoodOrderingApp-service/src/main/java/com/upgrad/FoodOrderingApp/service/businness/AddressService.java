package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.StateDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

public class AddressService {

    @Autowired
    private CustomerAddressDao customerAddressDao;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private StateDao stateDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public StateEntity getStateByUUID(final String StateUuid) throws AddressNotFoundException, SaveAddressException {
        StateEntity stateEntity = stateDao.getStateByStateUuid(StateUuid);
        if(StateUuid.isEmpty()){
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }
        if(stateEntity == null){
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        } else {
            return stateEntity;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public StateEntity getStateById(final long id)  {
        return  stateDao.getStateById(id);

    }


    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(final AddressEntity addressEntity) throws SaveAddressException {

        String pinCodeRegex = "^[0-9]{6}$";

        if (addressEntity.getFlatBuilNumber().isEmpty() || addressEntity.getLocality().isEmpty() || addressEntity.getCity().isEmpty() || addressEntity.getPinCode().isEmpty() || addressEntity.getUuid().isEmpty()) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        } else if (!addressEntity.getPinCode().matches(pinCodeRegex)) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        } else {
            return addressDao.createAddress(addressEntity);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddressEntity createCustomerAddress(final CustomerAddressEntity customerAddressEntity) {
        return addressDao.createCustomerAddress(customerAddressEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<AddressEntity> getAllAddressByCustomer(final CustomerEntity customerEntity) { return addressDao.getAllSavedAddresses(); }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<StateEntity> getAllStates() { return stateDao.getAllStates(); }

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity getAddressByAddressUuid(final String addressUuid) throws AddressNotFoundException {
        AddressEntity addressEntity=addressDao.getAddressByAddressUuid(addressUuid);
        if(addressUuid.isEmpty()) {
            throw new AddressNotFoundException("ANF-005","Address id can not be empty");
        }
        if(addressEntity == null ) {
            throw new AddressNotFoundException("ANF-003","No address by this id");
        } else {
            return addressEntity;
        }

    }

    @Transactional
    public AddressEntity getAddressById(final Long addressId) {
        return addressDao.getAddressById(addressId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAddressEntity getCustomerIdByAddressId(final long addressId) {
        return customerAddressDao.getCustomerAddressByAddressId(addressId);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteAddress(AddressEntity addressEntity,CustomerEntity signedcustomerEntity, CustomerEntity ownerofAddressEntity) throws AuthorizationFailedException {
        if(!(signedcustomerEntity.getContactNumber().equals(ownerofAddressEntity.getContactNumber()))) {
            throw new AuthorizationFailedException("ATHR-004","You are not authorized to view/update/delete any one else's address");
        } else {
            return addressDao.deleteAddress(addressEntity);
        }
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public List<CustomerAddressEntity> getAllCustomerAddressByCustomerId(final CustomerEntity customerEntity) {
        return customerAddressDao.getCustomerAddressesListByCustomerId(customerEntity);
    }
}