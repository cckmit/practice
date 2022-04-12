package top.hubby.mq.sender.configuration;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import top.hubby.mq.congig.RabbitMQConfiguration;
import top.hubby.mq.constants.enums.EventStatus;
import top.hubby.mq.sender.service.SenderService;
import top.hubby.mq.sender.service.DtxEventService;

import javax.annotation.Resource;

/**
 * @author zack <br>
 * @create 2022-04-11 19:40 <br>
 * @project project-cloud-custom <br>
 */
@Slf4j
@EnableRabbit
@Configuration
@ConditionalOnProperty(name = "common.mq.enable", matchIfMissing = true)
@ImportAutoConfiguration({DefaultQueueConfig.class, SenderService.class, QueueConfiguration.class, DtxEventServiceImpl.class})
@MapperScan("top.hubby.mq.sender.mapper")
public class RabbitMQAutoConfiguration extends RabbitMQConfiguration
        implements SmartInitializingSingleton {
    public static RabbitTemplate mqSender;

    @Resource
    private DtxEventService dtxService;

    @Override
    public void afterSingletonsInstantiated() {
        mqSender = this.getRabbitTemplate();
    }

    @Override
    protected void initRabbitTemplate() {
        /**
         * ConfirmCallback 异步执行的
         *
         * <pre>
         *    1. 对于可路由消息回调时且ack为true:  持久化到磁盘 + 对于镜像队列[所有镜像都已接受该消息]
         *    2. 如果交换机不存在: 此时 ack 为 false
         *    3. 从消息发送到收到确认有延迟, 特别是队列和消息都需要持久化, 因此需要异步发送 + 不保证顺序
         * </pre>
         */
        RabbitTemplate.ConfirmCallback defaultConfirmCallback =
                (correlationData, ack, cause) -> {
                    if (!ack) {
                        log.info(
                                "ConfirmCallback: correlationData: {} ack: {} cause:{}",
                                correlationData,
                                ack,
                                cause);
                        return;
                    }

                    if (ObjectUtil.isNotNull(correlationData) && StrUtil.isNotBlank(correlationData.getId())) {
                        dtxService.updateEventStatus(NumberUtil.parseLong(correlationData.getId()), EventStatus.PULISHED);
                    }
                };

        /**
         * ReturnCallback 异步执行的
         *
         * <pre>
         *    1. 在设置了强制标志后, 只有消息不可路由时才会有这个回调: 根据路由找不到 queue
         *    2. ConfirmCallback 总是在 ReturnCallback 之前调用
         *   </pre>
         */
        RabbitTemplate.ReturnCallback defaultReturnCallback =
                (message, replyCode, replyText, exchange, routingKey) -> {
                    log.info(
                            "ReturnCallback: Fail message: {} replyCode: {} replyText:{} exchange: {} routingKey: {}",
                            message,
                            replyCode,
                            replyText,
                            exchange,
                            routingKey);
                };

        // ack: 这个表示是否成功发送到 broker
        this.getRabbitTemplate().setConfirmCallback(defaultConfirmCallback);
        // 消息没有被发送到 queue 才会调用
        this.getRabbitTemplate().setReturnCallback(defaultReturnCallback);
    }
}
