package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class AddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    public AddressEntity getAddressById(Long addressId) {
        try {
            return entityManager.createNamedQuery("addressById", AddressEntity.class).setParameter("id", addressId)
                    .getSingleResult();
        } catch(NoResultException nre) {
            return null;
        }
    }

    //saves the customer address information of the created address record  in the database
    public AddressEntity createAddress(AddressEntity addressEntity) {
        this.entityManager.persist(addressEntity);
        return addressEntity;
    }
    //creates a customerAddress record
    public CustomerAddressEntity createCustomerAddress(CustomerAddressEntity customerAddressEntity) {
        this.entityManager.persist(customerAddressEntity);
        return customerAddressEntity;
    }

    public AddressEntity getAddressByAddressUuid(String addressUuid) {
        try {
            return this.entityManager.createNamedQuery("addressByUuid", AddressEntity.class).setParameter("uuid", addressUuid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    //retrieves all the saved addresses
    public List<AddressEntity> getAllSavedAddresses(){

        try {
            return this.entityManager.createNamedQuery("allAddresses", AddressEntity.class).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public String deleteAddress(AddressEntity addressEntity){
        String Uuid=addressEntity.getUuid();
        this.entityManager.remove(addressEntity);
        return Uuid;
    }
}