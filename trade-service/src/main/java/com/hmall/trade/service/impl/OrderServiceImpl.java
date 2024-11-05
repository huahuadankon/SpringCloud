package com.hmall.trade.service.impl;

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.client.CartClient;
import com.hmall.api.client.ItemClient;
import com.hmall.api.client.PayClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.common.config.MqConfig;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.UserContext;


import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.dto.OrderFormDTO;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.domain.po.OrderDetail;
import com.hmall.trade.mapper.OrderMapper;
import com.hmall.trade.service.IOrderDetailService;
import com.hmall.trade.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2023-05-05
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    private final ItemClient itemClient;
    private final CartClient cartClient;

    private final PayClient payClient;

    private final RabbitTemplate rabbitTemplate;


    private final IOrderDetailService detailService;
    @Override
    @GlobalTransactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        log.info("用户数据信息，{}",UserContext.getUser());
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        val items = itemClient.queryItemsByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);


        // 3.清理购物车商品
        // cartClient.deleteCartItemByIds(itemIds);
        //改成异步调用

        try {
            // 将 Set<Long> 转为 List<Long>
            List<Long> itemList = new ArrayList<>(itemIds);
            rabbitTemplate.convertAndSend("trade.topic", "order.create", itemList,
                    new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            // 设置消息头中的用户信息
                            message.getMessageProperties().setHeader("user-info", UserContext.getUser());
                            return message;
                        }
                    });
        } catch (Exception e) {
            log.error("消息发送失败: {}", e.getMessage());
        }


        // 4.扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }

        //发送延迟消息，检测订单支付状态
        rabbitTemplate.convertAndSend(MQConstants.DELAY_EXCHANGE_NAME,
                MQConstants.DELAY_ORDER_KEY,
                order.getId(),message -> {
                message.getMessageProperties().setDelay(10000);
                return message;
                });
        return order.getId();


    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    @GlobalTransactional
    public void cancelOrder(Long orderId) {
        //将订单状态修改为未支付

        lambdaUpdate().eq(Order::getId, orderId)
                .ne(Order::getStatus,5)
                .set(Order::getStatus,5)
                .update();
        //减库存回复
        List<OrderDetail> list = detailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();

        List<OrderDetailDTO> orderDetailsDto = BeanUtils.copyList(list, OrderDetailDTO.class);
        try {
            itemClient.recoverStore(orderDetailsDto);
        }catch (Exception e){
            throw new RuntimeException("回复库存失败");
        }


    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
