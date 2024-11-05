package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author liuyichen
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {
    private final IOrderService orderService;
    private final PayClient payClient;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME,delayed = "true"),
            key = MQConstants.DELAY_ORDER_KEY
    ))
    public void listenOrderDelay(Long orderId) {
        //查询订单状态
        Order order = orderService.getById(orderId);
        Integer status = order.getStatus();
        //检查订单状态，判断是否支付
        if(status == null || status !=1){
            //订单不存在或已支付
            return;
        }
        //已支付，查询支付流水状态
        PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);
        //已支付，标记订单状态为已支付
        if(payOrderDTO!=null && payOrderDTO.getStatus()==3){
            orderService.markOrderPaySuccess(orderId);
        }else {
            //未支付，取消订单，恢复库存
            orderService.cancelOrder(orderId);
        }



    }
}
