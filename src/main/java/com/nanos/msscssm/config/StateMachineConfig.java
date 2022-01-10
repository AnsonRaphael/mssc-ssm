package com.nanos.msscssm.config;

import com.nanos.msscssm.domain.PaymentEvent;
import com.nanos.msscssm.domain.PaymentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableStateMachineFactory
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {
    private final Action<PaymentState, PaymentEvent> preAuthAction;
    private final Action<PaymentState, PaymentEvent> authAction;
    private final Guard<PaymentState, PaymentEvent> paymentIdGuard;
    private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;
    private final Action<PaymentState, PaymentEvent> preAuthDeclinedAction;
    private final Action<PaymentState, PaymentEvent> authApprovedAction;
    private final Action<PaymentState, PaymentEvent> authDeclinedAction;
    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.AUTH_ERROR)
                .end(PaymentState.PRE_AUTH_ERROR);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE).action(preAuthAction).guard(paymentIdGuard)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVE)
                .action(preAuthApprovedAction)
                .and()
                .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED)
                .action(preAuthDeclinedAction)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZED)
                .action(authAction)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED)
                .and()
                .withExternal().source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED)
                .action(authDeclinedAction);
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState,PaymentEvent> adapter = new StateMachineListenerAdapter<>(){
            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("--------- State Changed from %s to %s",from,to));
            }
        };
        config.withConfiguration().listener(adapter);
    }
//    public Guard<PaymentState, PaymentEvent> paymentIdGuard(){
//        return context -> {
//            return context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
//        };
//    }
//    public Action<PaymentState,PaymentEvent> preAuthAction(){
//        return stateContext -> {
//            System.out.println("PreAuth was called!!!");
//            if (new Random().nextInt(10) < 8) {
//                System.out.println("Approved");
//                stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVE)
//                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
//                        .build());
//            }else {
//                System.out.println("Declined! No Credit!!!!!!");
//                stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
//                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,stateContext.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
//                        .build());
//            }
//        };
//    }
//
//    public Action<PaymentState, PaymentEvent> authAction(){
//        return context -> {
//            System.out.println("Auth was called!!!");
//
//            if (new Random().nextInt(10) < 8) {
//                System.out.println("Auth Approved");
//                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
//                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
//                        .build());
//
//            } else {
//                System.out.println("Auth Declined! No Credit!!!!!!");
//                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
//                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
//                        .build());
//            }
//        };
//    }
}
