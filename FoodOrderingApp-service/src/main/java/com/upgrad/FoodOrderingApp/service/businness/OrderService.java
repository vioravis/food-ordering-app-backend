package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.OrderDao;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.PaymentMethodNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private PaymentService paymentService;

    @Transactional
    public CouponEntity getCouponByCouponName(String couponName, final String authorizationToken) throws AuthorizationFailedException {

        // Gets the customerAuthToken details from customerDao
        CustomerAuthEntity customerAuthEntity = authenticationService.getCustomerAuthToken(authorizationToken);

        // Validates the access token retrieved from database
        authenticationService.validateAccessToken(customerAuthEntity);

        return orderDao.getCouponByName(couponName);
    }

    @Transactional
    public List<OrdersEntity> getCustomerOrders(final String authorizationToken) throws AuthorizationFailedException {

        // Gets the customerAuthToken details from customerDao
        CustomerAuthEntity customerAuthEntity = authenticationService.getCustomerAuthToken(authorizationToken);

        // Validates the access token retrieved from database
        authenticationService.validateAccessToken(customerAuthEntity);

        return orderDao.getCustomerOrders(customerAuthEntity.getId().longValue());
    }

    @Transactional
    public OrdersEntity saveOrder(OrdersEntity ordersEntity, final String authorizationToken)
            throws AuthorizationFailedException, CouponNotFoundException, AddressNotFoundException, PaymentMethodNotFoundException {

        //get the customerAuthToken details from customerDao
        CustomerAuthEntity customerAuthEntity = authenticationService.getCustomerAuthToken(authorizationToken);

        // Validates the provided access token
        authenticationService.validateAccessToken(customerAuthEntity);

        CouponEntity couponEntity = couponService.getCouponByUuid(ordersEntity.getCoupon().getUuid());

        if (couponEntity == null) {
            throw new CouponNotFoundException("CPF-002", "No coupon by this id");
        }

        AddressEntity addressEntity = addressService.getAddressById(ordersEntity.getAddress().getId());

        if (addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        PaymentEntity paymentEntity = paymentService.getPaymentByUuid(ordersEntity.getUuid());

        if (paymentEntity ==  null) {
            throw new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id");
        }

        return orderDao.saveOrder(ordersEntity);
    }
}