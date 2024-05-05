package org.springframework.shantaomvc.processor;

public interface RequestProcessor {

    boolean process(RequestProcessorChain requestProcessorChain) throws Exception;

}
