package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class CustomerAddressDao {

    @PersistenceContext
    private EntityManager entityManager;

    public CustomerAddressEntity getCustomerAddressByAddressId(long AddressId){

        try {
            return this.entityManager.createNamedQuery("customerAddressByAddressId", CustomerAddressEntity.class).setParameter("id", AddressId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<CustomerAddressEntity> getCustomerAddressesListByCustomerId(CustomerEntity customerEntity){

        try {
            return this.entityManager.createNamedQuery("customerAddressesListByCustomerId", CustomerAddressEntity.class).setParameter("customer", customerEntity).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }
}