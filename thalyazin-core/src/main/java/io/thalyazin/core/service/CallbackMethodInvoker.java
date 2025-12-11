package io.thalyazin.core.service;

import io.thalyazin.core.model.WorkflowMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Invokes callback methods (onSuccess, onFailure) using reflection.
 * Handles various method signatures: no args, single arg, or two args.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackMethodInvoker {

    private final PayloadDeserializer payloadDeserializer;

    /**
     * Invoke a callback method with deserialized payload.
     *
     * @param method  the method to invoke
     * @param handler the handler object
     * @param message the workflow message
     * @param error   the error (can be null for success callbacks)
     */
    public void invoke(Method method, Object handler, WorkflowMessage message, Throwable error) throws Exception {
        method.setAccessible(true);
        Class<?>[] paramTypes = method.getParameterTypes();

        if (paramTypes.length == 0) {
            method.invoke(handler);
        } else if (paramTypes.length == 1) {
            invokeWithOneParam(method, handler, message, error, paramTypes[0], true);
        } else if (paramTypes.length == 2) {
            invokeWithTwoParams(method, handler, message, error, paramTypes, true);
        } else {
            log.warn("Callback method {} has unsupported parameter count: {}", method.getName(), paramTypes.length);
        }
    }

    /**
     * Invoke a callback method using raw payload (no deserialization).
     * Useful for failure handlers that don't need type conversion.
     *
     * @param method  the method to invoke
     * @param handler the handler object
     * @param message the workflow message
     * @param error   the error (can be null for success callbacks)
     */
    public void invokeRaw(Method method, Object handler, WorkflowMessage message, Throwable error) throws Exception {
        method.setAccessible(true);
        Class<?>[] paramTypes = method.getParameterTypes();

        if (paramTypes.length == 0) {
            method.invoke(handler);
        } else if (paramTypes.length == 1) {
            invokeWithOneParam(method, handler, message, error, paramTypes[0], false);
        } else if (paramTypes.length == 2) {
            invokeWithTwoParams(method, handler, message, error, paramTypes, false);
        } else {
            log.warn("Callback method {} has unsupported parameter count: {}", method.getName(), paramTypes.length);
        }
    }

    private void invokeWithOneParam(Method method, Object handler, WorkflowMessage message,
                                    Throwable error, Class<?> paramType, boolean deserialize) throws Exception {
        if (WorkflowMessage.class.isAssignableFrom(paramType)) {
            method.invoke(handler, message);
        } else if (Throwable.class.isAssignableFrom(paramType) && error != null) {
            method.invoke(handler, error);
        } else {
            // Assume it's the payload type
            Object payload = deserialize ? payloadDeserializer.deserialize(message) : message.getPayload();
            method.invoke(handler, payload);
        }
    }

    private void invokeWithTwoParams(Method method, Object handler, WorkflowMessage message,
                                     Throwable error, Class<?>[] paramTypes, boolean deserialize) throws Exception {
        Class<?> firstParam = paramTypes[0];
        Class<?> secondParam = paramTypes[1];

        Object firstArg;
        if (WorkflowMessage.class.isAssignableFrom(firstParam)) {
            firstArg = message;
        } else {
            firstArg = deserialize ? payloadDeserializer.deserialize(message) : message.getPayload();
        }

        Object secondArg;
        if (Throwable.class.isAssignableFrom(secondParam)) {
            secondArg = error;
        } else {
            secondArg = null;
        }

        method.invoke(handler, firstArg, secondArg);
    }
}
